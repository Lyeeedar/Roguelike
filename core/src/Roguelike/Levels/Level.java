package Roguelike.Levels;

import java.util.HashSet;
import java.util.Iterator;

import Roguelike.Global.Statistics;
import Roguelike.RoguelikeGame;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Entity.Entity;
import Roguelike.Entity.Tasks.AbstractTask;
import Roguelike.Items.Item;
import Roguelike.Lights.Light;
import Roguelike.Shadows.ShadowCaster;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.SpriteEffect;
import Roguelike.StatusEffect.StatusEffect;
import Roguelike.Tiles.GameTile;
import Roguelike.Tiles.SeenTile;
import Roguelike.Tiles.SeenTile.SeenHistoryItem;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Level
{
	public Entity player;
	
	public Color Ambient = new Color(0.1f, 0.1f, 0.3f, 1.0f);
	
	private SeenTile[][] SeenGrid;
	private GameTile[][] Grid;
	public int width;
	public int height;
			
	public Level(GameTile[][] grid)
	{
		this.Grid = grid;
		this.width = grid.length;
		this.height = grid[0].length;
		
		this.SeenGrid = new SeenTile[width][height];
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				SeenGrid[x][y] = new SeenTile();
				SeenGrid[x][y].GameTile = Grid[x][y];
			}
		}
	}
	
	public GameTile[][] getGrid()
	{
		return Grid;
	}
	
	public GameTile getGameTile(int[] pos)
	{
		return getGameTile(pos[0], pos[1]);
	}
	
	public GameTile getGameTile(int x, int y)
	{
		return Grid[x][y];
	}
	
	public SeenTile getSeenTile(int[] pos)
	{
		return getSeenTile(pos[0], pos[1]);
	}
	
	public SeenTile getSeenTile(int x, int y)
	{
		return SeenGrid[x][y];
	}
	
	public void revealWholeLevel()
	{
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				if (!SeenGrid[x][y].seen)
				{
					SeenTile s = SeenGrid[x][y];
					s.GameTile = Grid[x][y];
					s.seen = true;
					
					s.History.clear();
					s.History.add(new SeenHistoryItem(Grid[x][y].TileData.floorSprite, Grid[x][y].TileData.Description));
				}
			}
		}
	}
	
	public void UpdateSeenGrid()
	{
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				if (Grid[x][y].GetVisible())
				{
					SeenTile s = SeenGrid[x][y];
					s.GameTile = Grid[x][y];
					s.seen = true;
					
					s.History.clear();
					s.History.add(new SeenHistoryItem(Grid[x][y].TileData.floorSprite, Grid[x][y].TileData.Description));
										
					if (Grid[x][y].Entity != null)
					{						
						s.History.add(new SeenHistoryItem(Grid[x][y].Entity.Sprite, "A " + Grid[x][y].Entity.Name));
					}
					
					for (Item i : Grid[x][y].Items)
					{
						s.History.add(new SeenHistoryItem(i.Icon, "A " + i.Name));
					}
				}
			}
		}
	}
	
	public void calculateLight()
	{
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				Grid[x][y].Light = new Color(Ambient);
			}
		}
		
		for (Light l : getAllLights())
		{
			calculateSingleLight(l);
		}
	}
	
	private void calculateSingleLight(Light l)
	{
		if (Math.max(Math.abs(player.Tile.x - l.lx), Math.abs(player.Tile.y - l.ly)) > player.getStatistic(Statistics.RANGE)+(int)Math.ceil(l.Intensity))
		{
			return; // too far away
		}
		
		Array<int[]> output = new Array<int[]>();
		ShadowCaster shadow = new ShadowCaster(Grid, (int)Math.ceil(l.Intensity));
		shadow.ComputeFOV(l.lx, l.ly, output);
				
		for (int[] tilePos : output)
		{
			GameTile tile = getGameTile(tilePos);
			
			float dst = 1 - Vector2.dst(l.lx, l.ly, tile.x, tile.y) / l.Intensity;
			if (dst < 0) {dst = 0;}
			
			tile.Light.add(new Color(l.Colour).mul(dst));
		}
	}
	
	public Array<ActiveAbility> ActiveAbilities = new Array<ActiveAbility>(false, 16);
	Array<ActiveAbility> NewActiveAbilities = new Array<ActiveAbility>(false, 16);
	public void addActiveAbility(ActiveAbility aa)
	{
		NewActiveAbilities.add(aa);
	}
	
	public Array<Entity> visibleList = new Array<Entity>(false, 16);
	public Array<Entity> invisibleList = new Array<Entity>(false, 16);
	
	private float updateDeltaStep = 0.05f;
	private float updateAccumulator;
	public void update(float delta)
	{
		updateAccumulator += delta;
		
		if (!hasActiveEffects())
		{	
			// Do player move and fill lists
			if (visibleList.size == 0 && invisibleList.size == 0 && updateAccumulator >= updateDeltaStep && !hasAbilitiesToUpdate())
			{
				processPlayer();
								
				updateAccumulator = 0;
			}			
			
			if (visibleList.size > 0 || invisibleList.size > 0)
			{
				// process invisible until empty
				processInvisibleList();
				
				// process visible
				processVisibleList();
			}
			
			if (ActiveAbilities.size > 0)
			{
				Iterator<ActiveAbility> itr = ActiveAbilities.iterator();
				while (itr.hasNext())
				{
					ActiveAbility aa = itr.next();
					boolean finished = aa.update();
					
					if (finished)
					{
						itr.remove();
					}
				}
			}
			
			if (NewActiveAbilities.size > 0)
			{
				ActiveAbilities.addAll(NewActiveAbilities, 0, NewActiveAbilities.size);
				NewActiveAbilities.clear();
			}
		}
		
		cleanUpDead();
		
		calculateLight();	
		
		for (Sprite s : getAllSprites())
		{
			s.update(delta);
		}
		
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				Iterator<SpriteEffect> itr = Grid[x][y].SpriteEffects.iterator();
				while (itr.hasNext())
				{
					SpriteEffect e = itr.next();
					boolean finished = e.Sprite.update(delta);
					
					if (finished) { itr.remove(); }
				}
				
				if (Grid[x][y].Entity != null)
				{
					itr = Grid[x][y].Entity.SpriteEffects.iterator();
					while (itr.hasNext())
					{
						SpriteEffect e = itr.next();
						boolean finished = e.Sprite.update(delta);
						
						if ( finished) { itr.remove(); }
					}
				}
			}
		}
	}
	
	private boolean hasAbilitiesToUpdate()
	{
		for (ActiveAbility aa : ActiveAbilities)
		{
			if (aa.needsUpdate())
			{
				return true;
			}
		}
		
		return false;
	}
		
	public void updateVisibleTiles()
	{
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{	
				Grid[x][y].SetVisible(false);
			}
		}
		
		Array<int[]> output = new Array<int[]>();
		ShadowCaster shadow = new ShadowCaster(Grid, player.getStatistic(Statistics.RANGE));
		shadow.ComputeFOV(player.Tile.x, player.Tile.y, output);
		
		for (int[] tilePos : output)
		{
			getGameTile(tilePos).SetVisible(true);
		}
		
		UpdateSeenGrid();
	}
	
	private void processPlayer()
	{
		if (player.Tasks.size == 0)
		{
			player.AI.update(player);
		}
		
		if (player.Tasks.size > 0)
		{
			AbstractTask task = player.Tasks.removeIndex(0);
			float actionCost = task.processTask(player) * player.getActionDelay();
			
			player.updateAccumulators(actionCost);
			
			updateVisibleTiles();
			
			getAllEntitiesToBeProcessed(actionCost);
			
			// check if enemy visible			
			if (enemyVisible())
			{
				// Clear pending moves
				player.AI.setData("Pos", null);
			}
		}
	}
	
	private void processVisibleList()
	{
		Iterator<Entity> itr = visibleList.iterator();
		while (itr.hasNext())
		{
			Entity e = itr.next();
			
			if (e.HP <= 0)
			{
				itr.remove();
				continue;
			}
			
			// If entity can take action
			if (e.actionDelayAccumulator > 0)
			{
				// If no tasks queued, process the ai
				if (e.Tasks.size == 0)
				{
					e.AI.update(e);
				}
				
				// If a task is queued, process it
				if (e.Tasks.size > 0)
				{
					float actionCost = e.Tasks.removeIndex(0).processTask(e);
					e.actionDelayAccumulator -= actionCost * e.getActionDelay();
				}
				else
				{
					e.actionDelayAccumulator -= e.getActionDelay();
				}
			}
			
			if (e.actionDelayAccumulator <= 0)
			{
				itr.remove();
			}
			else if (!e.Tile.GetVisible())
			{
				// If entity is now invisible, submit to the invisible list to be processed
				itr.remove();
				invisibleList.add(e);
			}
		}
	}
	
	private void processInvisibleList()
	{
		while(invisibleList.size > 0)
		{
			Iterator<Entity> itr = invisibleList.iterator();
			
			// Repeat full pass through list
			while (itr.hasNext())
			{
				Entity e = itr.next();
				
				if (e.HP <= 0)
				{
					itr.remove();
					continue;
				}
				
				// If entity can take action
				if (e.actionDelayAccumulator > 0)
				{
					// If no tasks queued, process the ai
					if (e.Tasks.size == 0)
					{
						e.AI.update(e);
					}
					
					// If a task is queued, process it
					if (e.Tasks.size > 0)
					{
						float actionCost = e.Tasks.removeIndex(0).processTask(e);
						e.actionDelayAccumulator -= actionCost * e.getActionDelay();
					}
					else
					{
						e.actionDelayAccumulator -= e.getActionDelay();
					}
				}
				
				if (e.actionDelayAccumulator <= 0)
				{
					itr.remove();
				}
				else if (e.Tile.GetVisible())
				{
					// If entity is now visible, submit to the visible list to be processed
					itr.remove();
					visibleList.add(e);
				}
			}
		}
	}
	
	public boolean enemyVisible()
	{
		boolean enemy = false;
		
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{				
				if (Grid[x][y].GetVisible() && Grid[x][y].Entity != null)
				{					
					if (!Grid[x][y].Entity.isAllies(player))
					{
						enemy = true;
						break;
					}
				}
			}
			if (enemy) { break; }
		}
				
		return enemy;
	}
	
	public boolean hasActiveEffects()
	{
		boolean activeEffects = false;
		
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{			
				if (!Grid[x][y].GetVisible()) { continue; }
				
				if (Grid[x][y].SpriteEffects.size > 0)
				{
					activeEffects = true;
					break;
							
				}
				
				Entity e = Grid[x][y].Entity;
				if (e != null)
				{									
					boolean active = hasActiveEffects(e);
					if (active)
					{
						activeEffects = true;
						break;
					}
				}				
			}
			
			if (activeEffects) { break; }
		}
		
		return activeEffects;
	}
	
	public boolean hasActiveEffects(Entity e)
	{
		boolean activeEffects = false;
		
		if (e.Sprite.SpriteAnimation != null)
		{
			activeEffects = true;
		}
		
		if (e.SpriteEffects.size > 0)
		{
			activeEffects = true;
		}
		
		return activeEffects;
	}
	
	public void getAllEntitiesToBeProcessed(float cost)
	{		
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				boolean visible = Grid[x][y].GetVisible();
				Entity e = Grid[x][y].Entity;
				if (e != null && e != player)
				{
					e.updateAccumulators(cost);
					
					if (e.actionDelayAccumulator > 0)
					{
						if (visible)
						{
							visibleList.add(e);
						}
						else
						{
							invisibleList.add(e);
						}					
					}
				}
			}
		}
		
		for (ActiveAbility aa : ActiveAbilities)
		{
			aa.updateAccumulators(cost);
		}
	}
	
	public void cleanUpDead()
	{
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				Entity e = Grid[x][y].Entity;
				if (e != null && e != player)
				{
					if (e.HP <= 0 && !hasActiveEffects(e))
					{
						e.Tile.Entity = null;
						
						RoguelikeGame.Instance.addConsoleMessage("The " + e.Name + " dies!");
						
						for (Item i : e.getInventory().m_items)
						{
							Grid[x][y].Items.add(i);
						}
					}
				}
			}
		}
	}
	
	public HashSet<Sprite> getAllSprites()
	{
		HashSet<Sprite> sprites = new HashSet<Sprite>();
		
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				sprites.add(Grid[x][y].TileData.floorSprite);
				if (Grid[x][y].TileData.featureSprite != null) { sprites.add(Grid[x][y].TileData.featureSprite); }
				
				if (Grid[x][y].Entity != null)
				{
					sprites.add(Grid[x][y].Entity.Sprite);
					
					for (StatusEffect se : Grid[x][y].Entity.statusEffects)
					{
						if (se.continualEffect != null)
						{
							sprites.add(se.continualEffect);
						}
					}
				}
				
				for (Item i : Grid[x][y].Items)
				{
					sprites.add(i.Icon);
				}
			}
		}
		
		for (ActiveAbility aa : ActiveAbilities)
		{
			sprites.add(aa.getSprite());
		}
		
		return sprites;
	}
		
	public Array<Entity> getAllEntities()
	{
		Array<Entity> list = new Array<Entity>();
		
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				if (Grid[x][y].Entity != null)
				{
					list.add(Grid[x][y].Entity);
				}
			}
		}
		
		return list;
	}
	
	public Array<Light> getAllLights()
	{
		Array<Light> list = new Array<Light>();
		
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				GameTile tile = Grid[x][y];
				
				if (tile.TileData.light != null)
				{
					Light l = tile.TileData.light.copy();
					l.lx = x;
					l.ly = y;
					list.add(l);
				}
				
				for (SpriteEffect se : tile.SpriteEffects)
				{
					if (se.light != null)
					{
						se.light.lx = x;
						se.light.ly = y;
						list.add(se.light);
					}
				}
				
				if (tile.Entity != null)
				{
					if (tile.Entity.Light != null)
					{
						tile.Entity.Light.lx = x;
						tile.Entity.Light.ly = y;
						list.add(tile.Entity.Light);
					}
					
					for (SpriteEffect se : tile.Entity.SpriteEffects)
					{
						if (se.light != null)
						{
							se.light.lx = x;
							se.light.ly = y;
							list.add(se.light);
						}
					}
				}
			}
		}
		
		for (ActiveAbility aa : ActiveAbilities)
		{
			if (aa.light != null)
			{
				if (aa.AffectedTiles.size == 1)
				{
					aa.light.lx = aa.AffectedTiles.peek().x;
					aa.light.ly = aa.AffectedTiles.peek().y;
					list.add(aa.light);
				}
				else
				{
					for (GameTile tile : aa.AffectedTiles)
					{
						Light l = aa.light.copy();
						l.lx = tile.x;
						l.ly = tile.y;
						list.add(l);
					}
				}
			}
		}
		
		return list;
	}
}
