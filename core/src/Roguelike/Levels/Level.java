package Roguelike.Levels;

import java.util.Iterator;

import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.Global.Passability;
import Roguelike.Global.Statistic;
import Roguelike.RoguelikeGame;
import Roguelike.RoguelikeGame.ScreenEnum;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Ability.PassiveAbility.PassiveAbility;
import Roguelike.DungeonGeneration.DungeonFileParser.DFPRoom;
import Roguelike.Entity.Entity;
import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Entity.GameEntity;
import Roguelike.Entity.Tasks.AbstractTask;
import Roguelike.Entity.Tasks.TaskWait;
import Roguelike.Fields.Field;
import Roguelike.Fields.Field.FieldLayer;
import Roguelike.GameEvent.GameEventHandler;
import Roguelike.Items.Inventory;
import Roguelike.Items.Item;
import Roguelike.Items.Item.ItemCategory;
import Roguelike.Items.Recipe;
import Roguelike.Lights.Light;
import Roguelike.Pathfinding.ShadowCastCache;
import Roguelike.Pathfinding.ShadowCaster;
import Roguelike.Screens.GameScreen;
import Roguelike.Sound.RepeatingSoundEffect;
import Roguelike.Sprite.BumpAnimation;
import Roguelike.Sprite.MoveAnimation;
import Roguelike.Sprite.MoveAnimation.MoveEquation;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.SpriteEffect;
import Roguelike.Tiles.GameTile;
import Roguelike.Tiles.Point;
import Roguelike.Tiles.SeenTile;
import Roguelike.Tiles.SeenTile.SeenHistoryItem;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;

public class Level
{
	//####################################################################//
	//region Constructor
	
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
				SeenGrid[x][y].gameTile = Grid[x][y];
			}
		}
	}
	
	//endregion Constructor
	//####################################################################//
	//region Public Methods
	
	public void addActiveAbility(ActiveAbility aa)
	{
		NewActiveAbilities.add(aa);
	}
	
	public void update(float delta)
	{
		updateAccumulator += delta;
				
		// setup player abilities
		if (Global.abilityPool.isVariableMapDirty)
		{
			player.slottedActiveAbilities.clear();
			player.slottedPassiveAbilities.clear();
			
			for (ActiveAbility aa : Global.abilityPool.slottedActiveAbilities)
			{
				if (aa != null)
				{
					aa.caster = player;
					player.slottedActiveAbilities.add(aa);
				}
				
			}
			
			for (PassiveAbility pa : Global.abilityPool.slottedPassiveAbilities)
			{
				if (pa != null)
				{
					player.slottedPassiveAbilities.add(pa);
				}
			}
			
			player.isVariableMapDirty = true;
			Global.abilityPool.isVariableMapDirty = false;
			
			for (int i = 0; i < 3; i++)
			{
				player.tasks.add(new TaskWait());
			}
		}
		
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
		
		updateVisibleTiles();
		lightList.clear();	
		
		Color acol = new Color(Ambient);
		acol.mul(acol.a);
		acol.a = 1;
		
		int playerViewRange = player.getStatistic(Statistic.RANGE);
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				GameTile tile = Grid[x][y];
				
				getLightsForTile(tile, lightList, playerViewRange);
				
				if (tile.visible) 
				{ 
					tile.light = new Color(acol);
					
					updateSpritesForTile(tile, delta);
					updateSpriteEffectsForTile(tile, delta);
				}
				else
				{
					clearEffectsForTile(tile);
				}
				
				cleanUpDeadForTile(tile);
			}
		}

		for (ActiveAbility aa : ActiveAbilities)
		{
			aa.getSprite().update(delta);
			
			if (aa.light != null)
			{
				for (GameTile t : aa.AffectedTiles)
				{
					int lx = t.x;
					int ly = t.y;
					if (checkLightCloseEnough(lx, ly, (int)aa.light.baseIntensity, player.tile.x, player.tile.y, playerViewRange))
					{
						Light light = aa.light.copy();
						light.lx = lx;
						light.ly = ly;
						lightList.add(light);
					}
				}
			}
		}

		calculateLight(delta, lightList);
		
		for (RepeatingSoundEffect sound : ambientSounds)
		{
			sound.update(delta);
		}
	}
	
	//endregion Public Methods
	//####################################################################//
	//region Visibility
	
	public void updateVisibleTiles()
	{
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{	
				Grid[x][y].visible = false;
			}
		}
				
		Array<Point> output = visibilityData.getShadowCast(Grid, player.tile.x, player.tile.y, player.getVariable(Statistic.RANGE));
		
		for (Point tilePos : output)
		{
			getGameTile(tilePos).visible = true;
		}
		
		updateSeenGrid();
	}
	
	private void updateSeenGrid()
	{
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				GameTile tile = Grid[x][y];
				if (tile.visible)
				{
					SeenTile s = SeenGrid[x][y];
					s.gameTile = tile;
					s.seen = true;
					
					Pools.freeAll(s.history);
					s.history.clear();
					
					for (Sprite sprite : tile.tileData.sprites)
					{
						s.history.add(Pools.obtain(SeenHistoryItem.class).set(sprite, tile.tileData.description));
					}
					
					if (tile.fields.size() > 0)
					{
						for (FieldLayer layer : FieldLayer.values())
						{
							Field field = tile.fields.get(layer);
							if (field != null)
							{						
								s.history.add(Pools.obtain(SeenHistoryItem.class).set(field.sprite, ""));
							}
						}
					}
					
					if (tile.environmentEntity != null)
					{
						SeenHistoryItem shi = Pools.obtain(SeenHistoryItem.class).set(tile.environmentEntity.sprite, "");
						shi.location = tile.environmentEntity.location;
						s.history.add(shi);
					}
										
					if (tile.entity != null && tile.entity != player)
					{						
						s.history.add(Pools.obtain(SeenHistoryItem.class).set(tile.entity.sprite, ""));
					}
					
					if (tile.items.size == 0)
					{
						
					}
					else if (tile.items.size == 1)
					{
						s.history.add(Pools.obtain(SeenHistoryItem.class).set(tile.items.get(0).getIcon(), ""));
					}
					else
					{
						s.history.add(Pools.obtain(SeenHistoryItem.class).set(AssetManager.loadSprite("bag"), ""));
					}
				}
			}
		}
	}
	
	//endregion Visibility
	//####################################################################//
	//region Lights
	
	private void calculateLight(float delta, Array<Light> lights)
	{
		for (Light l : lights)
		{
			l.update(delta);
			calculateSingleLight(l);
			if (l.copied) 
			{ 
				Pools.free(l); 
			}
		}
	}
	
	private void calculateSingleLight(Light l)
	{
		Array<Point> output = l.shadowCastCache.getShadowCast(Grid, l.lx, l.ly, (int)l.baseIntensity);
				
		for (Point tilePos : output)
		{
			GameTile tile = getGameTile(tilePos);
			
			float dst = 1 - Vector2.dst2(l.lx, l.ly, tile.x, tile.y) / (l.actualIntensity * l.actualIntensity);
			if (dst < 0) { dst = 0; }
			
			Color lcol = tempColor.set(l.colour).mul(dst);
			lcol.mul(lcol.a);
			lcol.a = 1;
			
			tile.light.add(lcol);
		}
	}
	
	private void getLightsForTile(GameTile tile, Array<Light> output, int viewRange)
	{		
		int lx = tile.x;
		int ly = tile.y;
		
		int px = player.tile.x;
		int py = player.tile.y;
		
		if (tile.tileData.light != null)
		{
			if (checkLightCloseEnough(lx, ly, (int)tile.tileData.light.baseIntensity, px, py, viewRange))
			{
				Light l = tile.tileData.light.copy();
				l.lx = lx;
				l.ly = ly;
				output.add(l);
			}		
		}
		
		if (tile.fields.size() > 0)
		{
			for (FieldLayer layer : FieldLayer.values())
			{
				Field field = tile.fields.get(layer);
				if (field != null && field.light != null)
				{
					if (checkLightCloseEnough(lx, ly, (int)field.light.baseIntensity, px, py, viewRange))
					{
						field.light.lx = lx;
						field.light.ly = ly;
						output.add(field.light);
					}
				}
			}
		}
		
		if (tile.environmentEntity != null && tile.environmentEntity.light != null)
		{
			if (checkLightCloseEnough(lx, ly, (int)tile.environmentEntity.light.baseIntensity, px, py, viewRange))
			{
				tile.environmentEntity.light.lx = lx;
				tile.environmentEntity.light.ly = ly;
				output.add(tile.environmentEntity.light);
			}
		}
		
		for (SpriteEffect se : tile.spriteEffects)
		{
			if (se.light != null)
			{
				if (checkLightCloseEnough(lx, ly, (int)se.light.baseIntensity, px, py, viewRange))
				{
					se.light.lx = lx;
					se.light.ly = ly;
					output.add(se.light);
				}
			}
		}
		
		if (tile.entity != null)
		{
			tempLightList.clear();
			tile.entity.getLight(tempLightList);
			for (Light l : tempLightList)
			{
				if (checkLightCloseEnough(lx, ly, (int)l.baseIntensity, px, py, viewRange))
				{
					l.lx = lx;
					l.ly = ly;
					output.add(l);
				}
			}
			
			for (SpriteEffect se : tile.entity.spriteEffects)
			{
				if (se.light != null)
				{
					if (checkLightCloseEnough(lx, ly, (int)se.light.baseIntensity, px, py, viewRange))
					{
						se.light.lx = lx;
						se.light.ly = ly;
						output.add(se.light);
					}
				}
			}
		}
	}
	
	private boolean checkLightCloseEnough(int lx, int ly, int intensity, int px, int py, int viewRange)
	{
		if (Math.max(Math.abs(px - lx), Math.abs(py - ly)) > viewRange+intensity)
		{
			return false;
		}
		
		return true;
	}
	
	//endregion Lights
	//####################################################################//
	//region Cleanup
	
	private void cleanUpDeadForTile(GameTile tile)
	{
		{
			GameEntity e = tile.entity;
			if (e != null)
			{
				if (tile.visible)
				{
					if (e.hasDamage)
					{
						GameScreen.Instance.addActorDamageAction(e);
						e.hasDamage = false;
					}
					
					if (e.healingAccumulator > 0)
					{
						GameScreen.Instance.addActorHealingAction(e);
					}
					
					if (e.popup != null)
					{
						GameScreen.Instance.addPopupBubble(e);
					}
				}
				else
				{
					e.damageAccumulator = 0;
					e.healingAccumulator = 0;
				}

				if (e != player && e.HP <= 0 && !hasActiveEffects(e))
				{
					e.tile.entity = null;

					dropItems(e.getInventory(), e.tile);						
				}
				else if (e == player && e.HP <= 0 && !hasActiveEffects(e))
				{
					RoguelikeGame.Instance.switchScreen(ScreenEnum.GAMEOVER);
				}
			}
		}

		{
			Entity e = tile.environmentEntity;
			if (e != null)
			{
				if (e.damageAccumulator > 0 && tile.visible)
				{
					GameScreen.Instance.addActorDamageAction(e);
				}

				if (e != player && e.HP <= 0 && !hasActiveEffects(e))
				{
					e.tile.environmentEntity = null;
					
					dropItems(e.getInventory(), e.tile);
				}
			}
		}
	}
	
	private void dropItems(Inventory inventory, GameTile source)
	{
		Array<Point> possibleTiles = new Array<Point>();
		ShadowCaster sc = new ShadowCaster(Grid, 5, ItemDropPassability);
		sc.ComputeFOV(source.x, source.y, possibleTiles);
		
		// remove nonpassable tiles
		Iterator<Point> itr = possibleTiles.iterator();
		while (itr.hasNext())
		{
			Point pos = itr.next();
			
			GameTile tile = getGameTile(pos);
			
			if (!tile.getPassable(ItemDropPassability))
			{
				itr.remove();
				Pools.free(pos);
			}
		}
		
		float delay = 0;
		for (Item i : inventory.m_items)
		{
			if (i.canDrop && i.shouldDrop())
			{
				if (i.category == ItemCategory.MATERIAL)
				{
					i = Recipe.generateItemForMaterial(i);
				}

				Point target = possibleTiles.random();
				GameTile tile = getGameTile(target);
				
				tile.items.add(i);
				
				int[] diff = tile.getPosDiff(source);
				
				MoveAnimation anim = new MoveAnimation(0.4f, diff, MoveEquation.LEAP);
				anim.leapHeight = 6;
				i.icon.spriteAnimation = anim;				
				i.icon.renderDelay = delay;
				delay += 0.02f;
			}
		}
		
		Pools.freeAll(possibleTiles);
	}
	
	private void clearEffectsForTile(GameTile tile)
	{
		tile.spriteEffects.clear();
		
		if (tile.environmentEntity != null)
		{
			tile.environmentEntity.spriteEffects.clear();
			tile.environmentEntity.sprite.spriteAnimation = null;
		}
		
		if (tile.entity != null)
		{
			tile.entity.spriteEffects.clear();
			tile.entity.sprite.spriteAnimation = null;
		}
		
		for (Item i : tile.items)
		{
			i.getIcon().spriteAnimation = null;
		}
	}
	
	//endregion Cleanup
	//####################################################################//
	//region Update
	
	private void updateSpriteEffectsForTile(GameTile tile, float delta)
	{
		Iterator<SpriteEffect> itr = tile.spriteEffects.iterator();
		while (itr.hasNext())
		{
			SpriteEffect e = itr.next();
			boolean finished = e.Sprite.update(delta);
			
			if (finished) { itr.remove(); }
		}
		
		if (tile.entity != null)
		{
			itr = tile.entity.spriteEffects.iterator();
			while (itr.hasNext())
			{
				SpriteEffect e = itr.next();
				boolean finished = e.Sprite.update(delta);
				
				if ( finished) { itr.remove(); }
			}
		}
		
		if (tile.environmentEntity != null)
		{
			itr = tile.environmentEntity.spriteEffects.iterator();
			while (itr.hasNext())
			{
				SpriteEffect e = itr.next();
				boolean finished = e.Sprite.update(delta);
				
				if ( finished) { itr.remove(); }
			}
		}
	}
		
	private void updateSpritesForTile(GameTile tile, float delta)
	{
		for (Sprite sprite : tile.tileData.sprites)
		{
			sprite.update(delta);
		}
		
		if (tile.fields.size() > 0)
		{
			for (FieldLayer layer : FieldLayer.values())
			{
				Field field = tile.fields.get(layer);
				if (field != null)
				{
					field.sprite.update(delta);
				}
			}
		}		
		
		if (tile.environmentEntity != null)
		{
			tile.environmentEntity.sprite.update(delta);
		}
		
		if (tile.entity != null)
		{
			tile.entity.sprite.update(delta);
		}
		
		for (Item i : tile.items)
		{
			i.getIcon().update(delta);
		}
	}
	
	//endregion Update
	//####################################################################//
	//region Process
	
	private void processPlayer()
	{
		if (player.tasks.size == 0)
		{
			player.AI.update(player);
		}
		
		if (player.tasks.size > 0)
		{
			AbstractTask task = player.tasks.removeIndex(0);
			for (GameEventHandler handler : player.getAllHandlers())
			{
				handler.onTask(player, task);
			}
			
			if (!task.cancel) { task.processTask(player); }
			
			float actionCost = task.cost * player.getActionDelay();
			
			Global.AUT += actionCost;
			
			Global.abilityPool.update(actionCost);
			
			player.update(actionCost);
			
			getAllEntitiesToBeProcessed(actionCost);
			
			tempEnvironmentEntityList.clear();
			getAllEnvironmentEntities(tempEnvironmentEntityList);
			for (EnvironmentEntity ee : tempEnvironmentEntityList)
			{
				ee.update(actionCost);
			}
			
			tempFieldList.clear();
			getAllFields(tempFieldList);
			for (Field f : tempFieldList)
			{
				if (f.tile == null) { continue; }
				
				f.update(actionCost);
				if (f.stacks < 1)
				{
					f.tile.fields.put(f.layer, null);
					f.tile = null;
				}
			}
			
			// check if enemy visible			
			if (enemyVisible())
			{
				// Clear pending moves
				player.AI.setData("Pos", null);
				player.AI.setData("Rest", null);
			}
			
			if (player.sprite.spriteAnimation instanceof BumpAnimation)
			{
				player.AI.setData("Pos", null);
				player.AI.setData("Rest", null);
			}
		}
	}
	
	private void processVisibleList()
	{
		Iterator<GameEntity> itr = visibleList.iterator();
		while (itr.hasNext())
		{
			GameEntity e = itr.next();
			
			if (e.HP <= 0)
			{
				itr.remove();
				continue;
			}
			
			// If entity can take action
			if (e.actionDelayAccumulator > 0)
			{
				// If no tasks queued, process the ai
				if (e.tasks.size == 0)
				{
					e.AI.update(e);
				}
				
				// If a task is queued, process it
				if (e.tasks.size > 0)
				{
					AbstractTask task = e.tasks.removeIndex(0);
					for (GameEventHandler handler : e.getAllHandlers())
					{
						handler.onTask(e, task);
					}
					
					if (!task.cancel) { task.processTask(e); }
					
					float actionCost = task.cost * e.getActionDelay();					
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
			else if (!e.tile.visible)
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
			Iterator<GameEntity> itr = invisibleList.iterator();
			
			// Repeat full pass through list
			while (itr.hasNext())
			{
				GameEntity e = itr.next();
				
				if (e.HP <= 0)
				{
					itr.remove();
					continue;
				}
				
				// If entity can take action
				if (e.actionDelayAccumulator > 0)
				{
					// If no tasks queued, process the ai
					if (e.tasks.size == 0)
					{
						e.AI.update(e);
					}
					
					// If a task is queued, process it
					if (e.tasks.size > 0)
					{
						AbstractTask task = e.tasks.removeIndex(0);
						for (GameEventHandler handler : e.getAllHandlers())
						{
							handler.onTask(e, task);
						}
						
						if (!task.cancel) { task.processTask(e); }
						
						float actionCost = task.cost * e.getActionDelay();					
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
				else if (e.tile.visible)
				{
					// If entity is now visible, submit to the visible list to be processed
					itr.remove();
					visibleList.add(e);
				}
			}
		}
	}
	
	private void getAllEntitiesToBeProcessed(float cost)
	{		
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				boolean visible = Grid[x][y].visible;
				GameEntity e = Grid[x][y].entity;
				if (e != null && e != player)
				{
					e.update(cost);
					
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
	
	//endregion Process
	//####################################################################//
	//region Getters
	
	public final GameTile[][] getGrid()
	{
		return Grid;
	}
	
	public final GameTile getGameTile(Point pos)
	{
		return getGameTile(pos.x, pos.y);
	}
	
	public final GameTile getGameTile(int x, int y)
	{
		if (x < 0 || x >= width || y < 0 || y >= height) { return null; }
		
		return Grid[x][y];
	}
	
	public final SeenTile getSeenTile(Point pos)
	{
		return getSeenTile(pos.x, pos.y);
	}
	
	public final SeenTile getSeenTile(int x, int y)
	{
		if (x < 0 || x >= width || y < 0 || y >= height) { return null; }
		
		return SeenGrid[x][y];
	}
	
	public final Entity getEntityWithUID(String UID)
	{
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				GameTile tile = getGameTile(x, y);
				
				if (tile.environmentEntity != null)
				{
					if (tile.environmentEntity.UID.equals(UID)) { return tile.environmentEntity; }
				}
				
				if (tile.entity != null)
				{
					if (tile.entity.UID.equals(UID)) { return tile.entity; }
				}
			}
		}
		
		return null;
	}
	
	public final void getAllFields(Array<Field> list)
	{		
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				if (Grid[x][y].fields.size() > 0)
				{
					for (FieldLayer layer : FieldLayer.values())
					{
						Field field = Grid[x][y].fields.get(layer);
						if (field != null)
						{
							list.add(field);
						}
					}
				}
			}
		}
	}
	
	public final void getAllEntities(Array<GameEntity> list)
	{		
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				if (Grid[x][y].entity != null)
				{
					list.add(Grid[x][y].entity);
				}
			}
		}
	}
	
	public final void getAllEnvironmentEntities(Array<EnvironmentEntity> list)
	{		
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				if (Grid[x][y].environmentEntity != null)
				{
					list.add(Grid[x][y].environmentEntity);
				}
			}
		}
	}
	
	//endregion Getters	
	//####################################################################//
	//region Misc
	
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
	
	private boolean enemyVisible()
	{
		boolean enemy = false;
		
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{				
				if (Grid[x][y].visible && Grid[x][y].entity != null)
				{					
					if (!Grid[x][y].entity.isAllies(player))
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
	
	private boolean hasActiveEffects()
	{
		boolean activeEffects = false;
		
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{			
				if (!Grid[x][y].visible) { continue; }
				
				if (Grid[x][y].spriteEffects.size > 0)
				{
					activeEffects = true;
					break;
							
				}
				
				{
					Entity e = Grid[x][y].entity;
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
				
				{
					Entity e = Grid[x][y].environmentEntity;
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
			}
			
			if (activeEffects) { break; }
		}
		
		return activeEffects;
	}
	
	private boolean hasActiveEffects(Entity e)
	{
		if (e.tile.spriteEffects.size > 0)
		{
			return true;
		}
		
		boolean activeEffects = false;
		
		if (e.sprite.spriteAnimation != null)
		{
			activeEffects = true;
		}
		
		if (e.spriteEffects.size > 0)
		{
			activeEffects = true;
		}
		
		return activeEffects;
	}
	
	//endregion Misc
	//####################################################################//
	//region Data
	
	private static final Array<Passability> ItemDropPassability = new Array<Passability>(new Passability[]{Passability.WALK, Passability.ENTITY});
	
	private static final Color tempColor = new Color();
	
	private Array<GameEntity> visibleList = new Array<GameEntity>(false, 16);
	private Array<GameEntity> invisibleList = new Array<GameEntity>(false, 16);
	
	private Array<Light> lightList = new Array<Light>(false, 16);
	
	private Array<GameEntity> tempEntityList = new Array<GameEntity>(false, 16);
	private Array<EnvironmentEntity> tempEnvironmentEntityList = new Array<EnvironmentEntity>(false, 16);
	private Array<Field> tempFieldList = new Array<Field>(false, 16);
 	private Array<Light> tempLightList = new Array<Light>(false, 16);
	
	private float updateDeltaStep = 0.05f;
	private float updateAccumulator;
	
	public Array<ActiveAbility> ActiveAbilities = new Array<ActiveAbility>(false, 16);
	private Array<ActiveAbility> NewActiveAbilities = new Array<ActiveAbility>(false, 16);
	
	public Array<RepeatingSoundEffect> ambientSounds = new Array<RepeatingSoundEffect>();
	public String bgmName;
	
	public String fileName;
	public int depth;
	public long seed;
	public Array<DFPRoom> requiredRooms;
	public String UID;
	
	public GameEntity player;
	
	public Color Ambient = new Color(0.1f, 0.1f, 0.3f, 1.0f);
	
	public SeenTile[][] SeenGrid;
	private GameTile[][] Grid;
	public int width;
	public int height;
	
	private final ShadowCastCache visibilityData = new ShadowCastCache();
	
	//endregion Data
	//####################################################################//
}
