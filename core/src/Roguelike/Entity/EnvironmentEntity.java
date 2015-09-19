package Roguelike.Entity;

import java.util.HashMap;
import java.util.Iterator;

import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.Global.Direction;
import Roguelike.Global.Passability;
import Roguelike.Global.Statistic;
import Roguelike.RoguelikeGame;
import Roguelike.RoguelikeGame.ScreenEnum;
import Roguelike.DungeonGeneration.DungeonFileParser;
import Roguelike.DungeonGeneration.DungeonFileParser.DFPRoom;
import Roguelike.DungeonGeneration.Symbol;
import Roguelike.GameEvent.GameEventHandler;
import Roguelike.Levels.Dungeon;
import Roguelike.Levels.Level;
import Roguelike.Save.SaveLevel;
import Roguelike.Screens.GameScreen;
import Roguelike.Screens.LoadingScreen;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.Sprite.AnimationMode;
import Roguelike.StatusEffect.StatusEffect;
import Roguelike.Tiles.GameTile;
import Roguelike.Tiles.Point;
import Roguelike.Util.EnumBitflag;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public class EnvironmentEntity extends Entity
{
	public Direction location = Direction.CENTER;
	public boolean attachToWall = false;
	public boolean overHead = false;

	public EnumBitflag<Passability> passableBy;

	public Array<ActivationAction> actions = new Array<ActivationAction>();

	public OnTurnAction onTurnAction;
	public OnHearAction onHearAction;
	public OnDeathAction onDeathAction;

	public Element creationData;
	public HashMap<String, Object> data = new HashMap<String, Object>();

	// ----------------------------------------------------------------------
	@Override
	public void update( float cost )
	{
		if ( onTurnAction != null )
		{
			onTurnAction.update( this, cost );
		}

		for ( GameEventHandler h : getAllHandlers() )
		{
			h.onTurn( this, cost );
		}

		Iterator<StatusEffect> itr = statusEffects.iterator();
		while ( itr.hasNext() )
		{
			StatusEffect se = itr.next();

			if ( se.duration <= 0 )
			{
				itr.remove();
			}
		}

		stacks = stackStatusEffects();

		if ( popupDuration > 0 )
		{
			popupDuration -= cost;
		}
	}

	// ----------------------------------------------------------------------
	@Override
	public int getStatistic( Statistic stat )
	{
		int val = statistics.get( stat );

		HashMap<String, Integer> variableMap = getBaseVariableMap();

		for ( StatusEffect se : statusEffects )
		{
			val += se.getStatistic( variableMap, stat );
		}

		return val;
	}

	// ----------------------------------------------------------------------
	@Override
	protected void internalLoad( String file )
	{

	}

	// ----------------------------------------------------------------------
	@Override
	public void removeFromTile()
	{
		for ( int x = 0; x < size; x++ )
		{
			for ( int y = 0; y < size; y++ )
			{
				if ( tile[x][y] != null )
				{
					tile[x][y].environmentEntity = null;
					tile[x][y] = null;
				}
			}
		}
	}

	// ----------------------------------------------------------------------
	@Override
	public Array<GameEventHandler> getAllHandlers()
	{
		Array<GameEventHandler> handlers = new Array<GameEventHandler>();

		for ( StatusEffect se : statusEffects )
		{
			handlers.add( se );
		}

		return handlers;
	}

	// ----------------------------------------------------------------------
	private static EnvironmentEntity CreateTransition( final Element data, String levelUID, int depth )
	{
		ActivationAction action = new ActivationAction( "Change Level" )
		{
			@Override
			public void activate( EnvironmentEntity entity )
			{
				Level current = entity.tile[0][0].level;
				SaveLevel save = current.dungeon.getSaveLevel( (SaveLevel) entity.data.get( "Destination" ) );

				boolean needsLoad = LoadingScreen.Instance.set( current.dungeon, save, current.player, "Stair: " + current.UID, null );

				if ( needsLoad )
				{
					RoguelikeGame.Instance.switchScreen( ScreenEnum.LOADING );
				}
			}
		};

		String destination = data.get( "Destination" );
		final SaveLevel destinationLevel = new SaveLevel( destination, depth + 1, null, MathUtils.random( Long.MAX_VALUE ) );

		if ( !destination.equals( "this" ) )
		{
			DungeonFileParser dfp = DungeonFileParser.load( destination + "/" + destination );
			DFPRoom exitRoom = DFPRoom.parse( data.getChildByName( "ExitRoom" ), dfp.sharedSymbolMap );
			destinationLevel.requiredRooms.add( exitRoom );

			// Place in symbol
			for ( Symbol symbol : exitRoom.localSymbolMap.values() )
			{
				if ( symbol.environmentData != null )
				{
					if ( symbol.environmentData.get( "Type" ).equals( "Transition" ) && symbol.environmentData.get( "Destination" ).equals( "this" ) )
					{
						HashMap<String, Object> eedata = new HashMap<String, Object>();
						eedata.put( "Destination", new SaveLevel( levelUID ) );
						eedata.put( "Stair: " + levelUID, new Object() );
						symbol.environmentEntityData = eedata;

						break;
					}
				}
			}
		}

		// Make stairs down and return
		{
			Sprite stairs = null;

			Element spriteElement = data.getChildByName( "Sprite" );
			if ( spriteElement != null )
			{
				stairs = AssetManager.loadSprite( spriteElement );
			}
			else
			{
				if ( destination.equals( "this" ) )
				{
					stairs = AssetManager.loadSprite( "dc-dngn/gateways/stone_stairs_up" );
				}
				else
				{
					stairs = AssetManager.loadSprite( "dc-dngn/gateways/stone_stairs_down" );
				}
			}

			final EnvironmentEntity entity = new EnvironmentEntity();
			entity.size = data.getInt( "Size", 1 );
			entity.passableBy = Passability.parse( "true" );
			entity.passableBy.setBit( Passability.LIGHT );
			entity.sprite = stairs;
			entity.canTakeDamage = false;
			entity.actions.add( action );
			entity.data.put( "Destination", destinationLevel );
			entity.data.put( "Stair: " + destinationLevel.UID, new Object() );
			entity.UID = "EnvironmentEntity Stair: ID " + entity.hashCode();

			entity.tile = new GameTile[entity.size][entity.size];
			stairs.size = entity.size;

			return entity;
		}
	}

	// ----------------------------------------------------------------------
	private static EnvironmentEntity CreateDungeonTransition( final Element data, String dungeonUID, String levelUID )
	{
		ActivationAction action = new ActivationAction( "Leave Dungeon" )
		{
			@Override
			public void activate( EnvironmentEntity entity )
			{
				Level current = entity.tile[0][0].level;

				String dungeonUID = (String) entity.data.get( "DestinationDungeon" );
				Dungeon dungeon = Global.Dungeons.get( dungeonUID );

				if ( dungeonUID == null )
				{
					dungeon = new Dungeon( current.dungeon.getSaveLevel( current.UID ), new Point().set( entity ), (String) entity.data.get( "Faction" ), (Integer) entity.data.get( "MaxDepth" ) );
					entity.data.put( "DestinationDungeon", dungeon.UID );

					Global.Dungeons.put( dungeon.UID, dungeon );
				}

				SaveLevel save = dungeon.getSaveLevel( (SaveLevel) entity.data.get( "DestinationLevel" ) );

				boolean needsLoad = LoadingScreen.Instance.set( dungeon, save, current.player, "Dungeon: " + current.UID, null );

				GameScreen.Instance.addFullScreenMessage( save.fileName );

				if ( needsLoad )
				{
					RoguelikeGame.Instance.switchScreen( ScreenEnum.LOADING );
				}
			}
		};

		String destination = data.get( "Destination" );
		final SaveLevel destinationLevel = new SaveLevel( destination, 1, null, MathUtils.random( Long.MAX_VALUE ) );

		if ( !destination.equals( "this" ) )
		{
			action.name = "Enter Dungeon";

			DungeonFileParser dfp = DungeonFileParser.load( destination + "/" + destination );
			DFPRoom exitRoom = DFPRoom.parse( data.getChildByName( "ExitRoom" ), dfp.sharedSymbolMap );
			destinationLevel.requiredRooms.add( exitRoom );

			// Place in symbol
			for ( Symbol symbol : exitRoom.localSymbolMap.values() )
			{
				if ( symbol.environmentData != null )
				{
					if ( symbol.environmentData.get( "Type" ).equals( "Dungeon" ) && symbol.environmentData.get( "Destination" ).equals( "this" ) )
					{
						HashMap<String, Object> eedata = new HashMap<String, Object>();
						eedata.put( "DestinationLevel", new SaveLevel( levelUID ) );
						eedata.put( "DestinationDungeon", dungeonUID );
						eedata.put( "Dungeon: " + levelUID, new Object() );
						symbol.environmentEntityData = eedata;

						break;
					}
				}
			}
		}

		// Make stairs down and return
		{
			Sprite stairs = null;

			Element spriteElement = data.getChildByName( "Sprite" );
			if ( spriteElement != null )
			{
				stairs = AssetManager.loadSprite( spriteElement );
			}
			else
			{
				if ( destination.equals( "this" ) )
				{
					stairs = AssetManager.loadSprite( "dc-dngn/gateways/stone_stairs_up" );
				}
				else
				{
					stairs = AssetManager.loadSprite( "dc-dngn/gateways/stone_stairs_down" );
				}
			}

			final EnvironmentEntity entity = new EnvironmentEntity();
			entity.size = data.getInt( "Size", 1 );
			entity.passableBy = Passability.parse( "true" );
			entity.passableBy.setBit( Passability.LIGHT );
			entity.sprite = stairs;
			entity.canTakeDamage = false;
			entity.actions.add( action );
			entity.data.put( "DestinationLevel", destinationLevel );
			entity.data.put( "Dungeon: " + destinationLevel.UID, new Object() );
			entity.data.put( "Faction", data.get( "Faction", "" ) );
			entity.data.put( "MaxDepth", data.getInt( "MaxDepth", 1 ) );
			entity.UID = "EnvironmentEntity Dungeon: ID " + entity.hashCode();

			entity.onTurnAction = new OnTurnAction()
			{

				@Override
				public void update( EnvironmentEntity entity, float delta )
				{
					String dungeonUID = (String) entity.data.get( "DestinationDungeon" );
					Dungeon dungeon = Global.Dungeons.get( dungeonUID );

					if ( dungeon != null && dungeon.isCleared )
					{
						entity.removeFromTile();
						Global.Dungeons.remove( dungeon.UID );
					}
				}

			};

			entity.tile = new GameTile[entity.size][entity.size];
			stairs.size = entity.size;

			return entity;
		}
	}

	// ----------------------------------------------------------------------
	public static EnvironmentEntity CreateDungeonExitPortal( Dungeon dungeon )
	{
		ActivationAction action = new ActivationAction( "Leave Dungeon" )
		{
			@Override
			public void activate( EnvironmentEntity entity )
			{
				Level current = entity.tile[0][0].level;

				String dungeonUID = (String) entity.data.get( "DestinationDungeon" );
				Dungeon dungeon = Global.Dungeons.get( dungeonUID );

				SaveLevel save = dungeon.outsideLevel;

				boolean needsLoad = LoadingScreen.Instance.set( dungeon, save, current.player, dungeon.outsidePoint, null );

				if ( needsLoad )
				{
					RoguelikeGame.Instance.switchScreen( ScreenEnum.LOADING );
				}
			}
		};

		final EnvironmentEntity entity = new EnvironmentEntity();
		entity.passableBy = Passability.parse( "true" );
		entity.sprite = AssetManager.loadSprite( "dc-dngn/zot_pillar" );
		entity.canTakeDamage = false;
		entity.actions.add( action );
		entity.data.put( "DestinationDungeon", dungeon.UID );
		entity.UID = "EnvironmentEntity DungeonPortal: ID " + entity.hashCode();

		return entity;
	}

	// ----------------------------------------------------------------------
	private static EnvironmentEntity CreateDoor()
	{
		final Sprite doorClosed = AssetManager.loadSprite( "Objects/DoorClosed", 1, Color.WHITE, AnimationMode.NONE, null, false );
		final Sprite doorOpen = AssetManager.loadSprite( "Objects/DoorOpen", 1, Color.WHITE, AnimationMode.NONE, null, false );

		ActivationAction action = new ActivationAction( "Open" )
		{
			@Override
			public void activate( EnvironmentEntity entity )
			{
				boolean closed = entity.data.get( "State" ).equals( "Closed" );
				if ( closed )
				{
					entity.passableBy = Passability.parse( "true" );
					entity.passableBy.setBit( Passability.LIGHT );
					entity.sprite = doorOpen;
					name = "Close";

					entity.data.put( "State", "Open" );
				}
				else
				{
					entity.passableBy = Passability.parse( "false" );
					entity.passableBy.clearBit( Passability.LIGHT );
					entity.sprite = doorClosed;
					name = "Open";

					entity.data.put( "State", "Closed" );
				}
			}
		};

		EnvironmentEntity entity = new EnvironmentEntity();
		entity.data.put( "State", "Closed" );
		entity.passableBy = Passability.parse( "false" );
		entity.passableBy.clearBit( Passability.LIGHT );
		entity.sprite = doorClosed;
		entity.actions.add( action );
		entity.UID = "EnvironmentEntity Door: ID " + entity.hashCode();
		entity.canTakeDamage = false;

		return entity;
	}

	// ----------------------------------------------------------------------
	private static EnvironmentEntity CreateSpawner( Element xml )
	{
		EnvironmentEntity entity = new EnvironmentEntity();
		entity.passableBy = Passability.parse( xml.get( "Passable" ) );

		if ( xml.get( "Opaque", null ) != null )
		{
			boolean opaque = xml.getBoolean( "Opaque", false );

			if ( opaque )
			{
				entity.passableBy.clearBit( Passability.LIGHT );
			}
			else
			{
				entity.passableBy.setBit( Passability.LIGHT );
			}
		}

		entity.baseInternalLoad( xml );

		final boolean spawnOnHear = xml.get( "Spawn", "OnTurn" ).toLowerCase().contains( "onhear" );
		final boolean spawnOnDeath = xml.get( "Spawn", "OnTurn" ).toLowerCase().contains( "ondeath" );

		entity.data.put( "MaxAlive", xml.getInt( "MaxAlive", 1 ) );
		entity.data.put( "NumToSpawn", xml.getInt( "NumToSpawn", Integer.MAX_VALUE ) );
		entity.data.put( "EntityName", xml.get( "Entity" ) );
		entity.data.put( "Delay", xml.getFloat( "Delay", 10.0f ) );
		entity.data.put( "SpawnOnTurn", xml.get( "Spawn", "OnTurn" ).toLowerCase().contains( "onturn" ) );

		entity.data.put( "Accumulator", 0.0f );
		entity.data.put( "NumSpawned", 0 );
		entity.data.put( "IsSpawning", false );
		entity.data.put( "RequestSpawn", false );

		entity.onTurnAction = new OnTurnAction()
		{
			GameEntity[] entities;

			@Override
			public void update( EnvironmentEntity entity, float delta )
			{
				float accumulator = (Float) entity.data.get( "Accumulator" );

				// reload data
				if ( entities == null )
				{
					int maxAlive = (Integer) entity.data.get( "MaxAlive" );

					entities = new GameEntity[maxAlive];

					for ( int i = 0; i < maxAlive; i++ )
					{
						String key = "Entity" + i;
						if ( entity.data.containsKey( key ) )
						{
							String UID = (String) entity.data.get( key );

							GameEntity ge = (GameEntity) entity.tile[0][0].level.getEntityWithUID( UID );

							if ( ge == null )
							{
								System.out.println( "Failed to find entity with UID: " + UID );
							}

							entities[i] = ge;
						}
					}
				}

				boolean isSpawning = (Boolean) entity.data.get( "IsSpawning" );

				if ( isSpawning )
				{
					if ( accumulator > 0 )
					{
						accumulator -= delta;
					}

					if ( accumulator <= 0 )
					{
						GameEntity ge = GameEntity.load( (String) entity.data.get( "EntityName" ) );

						GameTile tile = entity.tile[0][0];
						int x = tile.x;
						int y = tile.y;

						GameTile spawnTile = null;

						for ( Direction d : Direction.values() )
						{
							int nx = x + d.getX();
							int ny = y + d.getY();

							GameTile ntile = tile.level.getGameTile( nx, ny );

							if ( ntile != null && ntile.getPassable( ge.getTravelType(), null ) && ntile.entity == null )
							{
								spawnTile = ntile;
								break;
							}
						}

						if ( spawnTile != null )
						{
							for ( int i = 0; i < entities.length; i++ )
							{
								if ( entities[i] == null )
								{
									entities[i] = ge;

									entity.data.put( "Entity" + i, ge.UID );
									break;
								}
							}

							spawnTile.addGameEntity( ge );

							entity.data.put( "IsSpawning", false );
							entity.data.put( "NumSpawned", ( (Integer) entity.data.get( "NumSpawned" ) ) + 1 );
						}
					}
				}

				boolean requestSpawn = (Boolean) entity.data.get( "RequestSpawn" );
				boolean spawnOnTurn = (Boolean) entity.data.get( "SpawnOnTurn" );
				int numToSpawn = (Integer) entity.data.get( "NumToSpawn" );

				if ( !isSpawning && ( requestSpawn || spawnOnTurn ) )
				{
					boolean needsSpawn = false;

					if ( (Integer) entity.data.get( "NumSpawned" ) < numToSpawn )
					{
						for ( int i = 0; i < entities.length; i++ )
						{
							GameEntity ge = entities[i];

							if ( ge == null || ge.HP <= 0 )
							{
								entities[i] = null;
								entity.data.remove( "Entity" + i );

								needsSpawn = true;
							}
						}
					}

					if ( needsSpawn )
					{
						accumulator = (Float) entity.data.get( "Delay" );
						entity.data.put( "IsSpawning", true );
						entity.data.put( "RequestSpawn", false );
					}
				}

				entity.data.put( "Accumulator", accumulator );
			}
		};

		if ( spawnOnHear )
		{
			entity.onHearAction = new OnHearAction()
			{
				@Override
				public void process( EnvironmentEntity entity, Point source, String key, Object value )
				{
					entity.data.put( "RequestSpawn", true );
				}
			};
		}

		if ( spawnOnDeath )
		{
			entity.onDeathAction = new OnDeathAction()
			{
				@Override
				public void process( EnvironmentEntity entity )
				{
					GameEntity ge = GameEntity.load( (String) entity.data.get( "EntityName" ) );

					GameTile tile = entity.tile[0][0];
					int x = tile.x;
					int y = tile.y;

					GameTile spawnTile = null;

					for ( Direction d : Direction.values() )
					{
						int nx = x + d.getX();
						int ny = y + d.getY();

						GameTile ntile = tile.level.getGameTile( nx, ny );

						if ( ntile != null && ntile.getPassable( ge.getTravelType(), null ) && ntile.entity == null )
						{
							spawnTile = ntile;
							break;
						}
					}

					if ( spawnTile != null )
					{
						spawnTile.addGameEntity( ge );
					}
				}
			};
		}

		return entity;
	}

	// ----------------------------------------------------------------------
	private static EnvironmentEntity CreateBasic( Element xml )
	{
		EnvironmentEntity entity = new EnvironmentEntity();

		entity.passableBy = Passability.parse( xml.get( "Passable", "false" ) );

		if ( xml.get( "Opaque", null ) != null )
		{
			boolean opaque = xml.getBoolean( "Opaque", false );

			if ( opaque )
			{
				entity.passableBy.clearBit( Passability.LIGHT );
			}
			else
			{
				entity.passableBy.setBit( Passability.LIGHT );
			}
		}

		entity.attachToWall = xml.getBoolean( "AttachToWall", false );
		entity.overHead = xml.getBoolean( "OverHead", false );
		entity.canTakeDamage = xml.getChildByName( "Statistics" ) != null;

		entity.baseInternalLoad( xml );

		return entity;
	}

	// ----------------------------------------------------------------------
	public static EnvironmentEntity load( Element xml, String dungeonUID, String levelUID, int depth )
	{
		EnvironmentEntity entity = null;

		String type = xml.get( "Type", "" );

		if ( type.equalsIgnoreCase( "Door" ) )
		{
			entity = CreateDoor();
		}
		else if ( type.equalsIgnoreCase( "Transition" ) )
		{
			entity = CreateTransition( xml, levelUID, depth );
		}
		else if ( type.equalsIgnoreCase( "Dungeon" ) )
		{
			entity = CreateDungeonTransition( xml, dungeonUID, levelUID );
		}
		else if ( type.equalsIgnoreCase( "Spawner" ) )
		{
			entity = CreateSpawner( xml );
		}
		else
		{
			entity = CreateBasic( xml );
		}

		entity.tile = new GameTile[entity.size][entity.size];
		entity.creationData = xml;
		return entity;
	}

	// ----------------------------------------------------------------------
	public static abstract class ActivationAction
	{
		public String name;
		public boolean visible = true;

		public ActivationAction( String name )
		{
			this.name = name;
		}

		public abstract void activate( EnvironmentEntity entity );
	}

	// ----------------------------------------------------------------------
	public static abstract class OnTurnAction
	{
		public abstract void update( EnvironmentEntity entity, float delta );
	}

	// ----------------------------------------------------------------------
	public static abstract class OnHearAction
	{
		public abstract void process( EnvironmentEntity entity, Point source, String key, Object value );
	}

	// ----------------------------------------------------------------------
	public static abstract class OnDeathAction
	{
		public abstract void process( EnvironmentEntity entity );
	}
}
