package Roguelike.Entity;

import java.util.Iterator;

import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.Global.Direction;
import Roguelike.Global.Statistic;
import Roguelike.RoguelikeGame;
import Roguelike.DungeonGeneration.DungeonFileParser.DFPRoom;
import Roguelike.DungeonGeneration.RecursiveDockGenerator;
import Roguelike.DungeonGeneration.RecursiveDockGenerator.Room;
import Roguelike.GameEvent.GameEventHandler;
import Roguelike.Levels.Level;
import Roguelike.Screens.GameScreen;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.Sprite.AnimationMode;
import Roguelike.StatusEffect.StatusEffect;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public class EnvironmentEntity extends Entity
{
	public Direction location = Direction.CENTER;
	public boolean attachToWall = false;
	public boolean overHead = false;
	
	public boolean passable;
	public boolean opaque;

	public Array<ActivationAction> actions = new Array<ActivationAction>();
	
	public OnTurnAction onTurnAction;
	
	@Override
	public void update(float cost)
	{
		if (onTurnAction != null)
		{
			onTurnAction.update(this, cost);
		}
		
		for (GameEventHandler h : getAllHandlers())
		{
			h.onTurn(this, cost);
		}

		Iterator<StatusEffect> itr = statusEffects.iterator();
		while (itr.hasNext())
		{
			StatusEffect se = itr.next();

			if(se.duration <= 0)
			{
				itr.remove();
			}
		}

		stacks = stackStatusEffects();
	}
	
	@Override
	public int getStatistic(Statistic stat)
	{
		int val = statistics.get(stat);

		for (StatusEffect se : statusEffects)
		{
			val += se.getStatistic(this, stat);
		}
		
		return val;
	}
	
	@Override
	protected void internalLoad(String file)
	{
		
	}

	//----------------------------------------------------------------------
	@Override
	public Array<GameEventHandler> getAllHandlers()
	{
		Array<GameEventHandler> handlers = new Array<GameEventHandler>();

		for (StatusEffect se : statusEffects)
		{
			handlers.add(se);
		}

		return handlers;
	}
	
	private static EnvironmentEntity CreateTransition(final Element data)
	{
		final Sprite stairdown = AssetManager.loadSprite("dc-dngn/gateways/stone_stairs_down");
		final Sprite stairup = AssetManager.loadSprite("dc-dngn/gateways/stone_stairs_up");
		
		final EnvironmentEntity entranceEntity = new EnvironmentEntity();
		entranceEntity.passable = true;
		entranceEntity.opaque = false;
		entranceEntity.sprite = stairdown;
		
		final EnvironmentEntity exitEntity = new EnvironmentEntity();
		exitEntity.passable = true;
		exitEntity.opaque = false;
		exitEntity.sprite = stairup;
		
		ActivationAction entranceAA = new ActivationAction("Change Level")
		{
			Level connectedLevel;
			EnvironmentEntity exit = exitEntity;
			
			public void activate(EnvironmentEntity entity)
			{
				// generate new level if required
				if (connectedLevel == null)
				{
					RecursiveDockGenerator generator = new RecursiveDockGenerator(data.get("Destination"), entranceEntity.tile.level.depth+1);
					
					DFPRoom dfpRoom = DFPRoom.parse(data.getChildByName("ExitRoom"), generator.dfp.sharedSymbolMap);
					final Room room = new Room();
					dfpRoom.fillRoom(room);
					
					for (int x = 0; x < room.width; x++)
					{
						for (int y = 0; y < room.height; y++)
						{
							if (room.roomContents[x][y].environmentData != null)
							{
								if (room.roomContents[x][y].environmentData.get("Destination", "").equals("this"))
								{
									room.roomContents[x][y].environmentEntityObject = exitEntity;
								}
							}
						}
					}
					
					generator.toBePlaced.add(room);
					generator.generate();
					connectedLevel = generator.getLevel();
				}
				
				exit.tile.addObject(entity.tile.level.player);
				connectedLevel.player = entity.tile.level.player;
				
				connectedLevel.updateVisibleTiles();
				
				// switch out levels
				Global.CurrentLevel = connectedLevel;
			}
		};
		entranceEntity.actions.add(entranceAA);
		
		ActivationAction exitAA = new ActivationAction("Change Level")
		{
			EnvironmentEntity exit = entranceEntity;
			
			public void activate(EnvironmentEntity entity)
			{
				Level connectedLevel = entranceEntity.tile.level;
				
				exit.tile.addObject(entity.tile.level.player);
				connectedLevel.player = entity.tile.level.player;
				
				connectedLevel.updateVisibleTiles();
				
				// switch out levels
				Global.CurrentLevel = connectedLevel;
			}
		};
		exitEntity.actions.add(exitAA);
		
		return entranceEntity;
	}
	
	private static EnvironmentEntity CreateDoor()
	{
		final Sprite doorClosed = new Sprite(1, new Texture[]{AssetManager.loadTexture("Sprites/Objects/Door0.png")}, new int[]{16, 16}, new int[]{0, 0}, Color.WHITE, AnimationMode.NONE, null);
		final Sprite doorOpen = new Sprite(1, new Texture[]{AssetManager.loadTexture("Sprites/Objects/Door1.png")}, new int[]{16, 16}, new int[]{0, 0}, Color.WHITE, AnimationMode.NONE, null);
		
		ActivationAction open = new ActivationAction("Open")
		{
			public void activate(EnvironmentEntity entity)
			{
				entity.passable = true;
				entity.opaque = false;				
				entity.sprite = doorOpen;
				
				entity.actions.get(1).visible = true;
				visible = false;
			}
		};
		
		ActivationAction close = new ActivationAction("Close")
		{
			public void activate(EnvironmentEntity entity)
			{
				if (entity.tile.entity != null)
				{
					return;
				}
				
				entity.passable = false;
				entity.opaque = true;				
				entity.sprite = doorClosed;
				
				entity.actions.get(0).visible = true;
				visible = false;
			}
		};
		close.visible = false;
		
		EnvironmentEntity entity = new EnvironmentEntity();
		entity.passable = false;
		entity.opaque = true;
		entity.sprite = doorClosed;
		entity.actions.add(open);
		entity.actions.add(close);
		
		return entity;
	}
	
	private static EnvironmentEntity CreateSpawner(Element xml)
	{
		EnvironmentEntity entity = new EnvironmentEntity();
		entity.passable = xml.getBoolean("Passable", true);
		entity.opaque = xml.getBoolean("Opaque", false);
		
		entity.baseInternalLoad(xml);
		
		final int count = xml.getInt("Count", 1);
		final String name = xml.get("Entity");
		final int respawn = xml.getInt("Respawn", 50);

		
		entity.onTurnAction = new OnTurnAction()
		{
			String entityName = name;
			float cooldown = respawn;
			
			GameEntity[] entities = new GameEntity[count];
			float accumulator;
			
			@Override
			public void update(EnvironmentEntity entity, float delta)
			{
				if (accumulator > 0)
				{
					accumulator -= delta;
					
					if (accumulator <= 0)
					{
						GameEntity ge = GameEntity.load(entityName);
						
						GameTile tile = entity.tile;
						int x = tile.x;
						int y = tile.y;
						
						GameTile spawnTile = null;
						
						for (Direction d : Direction.values())
						{
							int nx = x + d.GetX();
							int ny = y + d.GetY();
							
							GameTile ntile = tile.level.getGameTile(nx, ny);
							
							if (ntile != null && ntile.getPassable(ge.getTravelType()) && ntile.entity == null)
							{
								spawnTile = ntile;
								break;
							}
						}
						
						if (spawnTile != null)
						{														
							for (int i = 0; i < entities.length; i++)
							{
								if (entities[i] == null)
								{
									entities[i] = ge;
									break;
								}
							}
							
							spawnTile.addObject(ge);
						}
					}
				}
				else
				{
					boolean needsSpawn = false;
					for (int i = 0; i < entities.length; i++)
					{
						GameEntity ge = entities[i];
						
						if (ge == null || ge.HP <= 0)
						{
							entities[i] = null;
							needsSpawn = true;
						}
					}
					
					if (needsSpawn)
					{
						accumulator = cooldown;
					}
				}
			}
			
		};
		
		return entity;
	}
	
	private static EnvironmentEntity CreateBasic(Element xml)
	{
		EnvironmentEntity entity = new EnvironmentEntity();
		entity.passable = xml.getBoolean("Passable", true);
		entity.opaque = xml.getBoolean("Opaque", false);
		entity.attachToWall = xml.getBoolean("AttachToWall", false);
		entity.overHead = xml.getBoolean("OverHead", false);
		
		entity.baseInternalLoad(xml);
		
		return entity;
	}
	
	public static EnvironmentEntity load(Element xml)
	{
		String type = xml.get("Type", "");
		if (type.equalsIgnoreCase("Door"))
		{
			return CreateDoor();
		}
		else if (type.equalsIgnoreCase("Transition"))
		{
			return CreateTransition(xml);
		}
		else if (type.equalsIgnoreCase("Spawner"))
		{
			return CreateSpawner(xml);
		}
		else
		{
			return CreateBasic(xml);
		}
	}

	public static abstract class ActivationAction
	{
		public String name;
		public boolean visible = true;

		public ActivationAction(String name)
		{
			this.name = name;
		}
		
		public abstract void activate(EnvironmentEntity entity);
	}

	public static abstract class OnTurnAction
	{
		public abstract void update(EnvironmentEntity entity, float delta);
	}

	

}
