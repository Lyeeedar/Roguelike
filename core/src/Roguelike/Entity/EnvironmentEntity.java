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
import Roguelike.Levels.Level;
import Roguelike.Save.SaveLevel;
import Roguelike.Screens.LoadingScreen;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.Sprite.AnimationMode;
import Roguelike.StatusEffect.StatusEffect;
import Roguelike.Tiles.GameTile;
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
	private static EnvironmentEntity CreateTransition( final Element data, String levelUID )
	{
		ActivationAction action = new ActivationAction( "Change Level" )
		{
			@Override
			public void activate( EnvironmentEntity entity )
			{
				Level current = entity.tile.level;
				SaveLevel save = Global.getLevel( (SaveLevel) entity.data.get( "Destination" ) );

				LoadingScreen.Instance.set( save, current.player, "Stair: " + save.UID, null );

				RoguelikeGame.Instance.switchScreen( ScreenEnum.LOADING );
			}
		};

		String destination = data.get( "Destination" );
		final SaveLevel destinationLevel = new SaveLevel( destination, 0, null, MathUtils.random( Long.MAX_VALUE ) );

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
						eedata.put( "Stair: " + destinationLevel.UID, new Object() );
						symbol.environmentEntityData = eedata;

						break;
					}
				}
			}
		}

		// Make stairs down and return
		{
			Sprite stairs = null;
			if ( destination.equals( "this" ) )
			{
				stairs = AssetManager.loadSprite( "dc-dngn/gateways/stone_stairs_up" );
			}
			else
			{
				stairs = AssetManager.loadSprite( "dc-dngn/gateways/stone_stairs_down" );
			}

			final EnvironmentEntity entity = new EnvironmentEntity();
			entity.passableBy = Passability.parse( "true" );
			entity.passableBy.setBit( Passability.LIGHT );
			entity.sprite = stairs;
			entity.canTakeDamage = false;
			entity.actions.add( action );
			entity.data.put( "Destination", destinationLevel );
			entity.data.put( "Stair: " + levelUID, new Object() );
			entity.UID = "EnvironmentEntity DownStair: ID " + entity.hashCode();

			return entity;
		}
	}

	// ----------------------------------------------------------------------
	private static EnvironmentEntity CreateDoor()
	{
		final Sprite doorClosed = AssetManager.loadSprite( "Objects/Door0", 1, Color.WHITE, AnimationMode.NONE, null );
		final Sprite doorOpen = AssetManager.loadSprite( "Objects/Door1", 1, Color.WHITE, AnimationMode.NONE, null );

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
				}
				else
				{
					entity.passableBy = Passability.parse( "false" );
					;
					entity.passableBy.clearBit( Passability.LIGHT );
					entity.sprite = doorClosed;
					name = "Open";
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

		entity.statistics.put( Statistic.METAL_DEF, 50 );
		entity.statistics.put( Statistic.METAL_HARDINESS, 50 );

		entity.statistics.put( Statistic.WOOD_DEF, 50 );
		entity.statistics.put( Statistic.WOOD_HARDINESS, 75 );

		entity.statistics.put( Statistic.AIR_DEF, 50 );
		entity.statistics.put( Statistic.AIR_HARDINESS, 50 );

		entity.statistics.put( Statistic.WATER_DEF, 50 );
		entity.statistics.put( Statistic.WATER_HARDINESS, 50 );

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

		final int count = xml.getInt( "Count", 1 );
		final String name = xml.get( "Entity" );
		final int respawn = xml.getInt( "Respawn", 50 );

		entity.data.put( "Accumulator", 0.0f );

		entity.onTurnAction = new OnTurnAction()
		{
			String entityName = name;
			float cooldown = respawn;

			GameEntity[] entities;
			float accumulator;

			@Override
			public void update( EnvironmentEntity entity, float delta )
			{
				// reload data
				if ( entities == null )
				{
					entities = new GameEntity[count];

					for ( int i = 0; i < count; i++ )
					{
						String key = "Entity" + i;
						if ( entity.data.containsKey( key ) )
						{
							String UID = (String) entity.data.get( key );

							GameEntity ge = (GameEntity) entity.tile.level.getEntityWithUID( UID );

							if ( ge == null )
							{
								System.out.println( "Failed to find entity with UID: " + UID );
							}

							entities[i] = ge;
						}
					}

					accumulator = (Float) entity.data.get( "Accumulator" );
				}

				if ( accumulator > 0 )
				{
					accumulator -= delta;

					if ( accumulator <= 0 )
					{
						GameEntity ge = GameEntity.load( entityName );

						GameTile tile = entity.tile;
						int x = tile.x;
						int y = tile.y;

						GameTile spawnTile = null;

						for ( Direction d : Direction.values() )
						{
							int nx = x + d.getX();
							int ny = y + d.getY();

							GameTile ntile = tile.level.getGameTile( nx, ny );

							if ( ntile != null && ntile.getPassable( ge.getTravelType() ) && ntile.entity == null )
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
						}
					}
				}
				else
				{
					boolean needsSpawn = false;
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

					if ( needsSpawn )
					{
						accumulator = cooldown;
					}
				}

				entity.data.put( "Accumulator", accumulator );
			}

		};

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
	public static EnvironmentEntity load( Element xml, String levelUID )
	{
		EnvironmentEntity entity = null;

		String type = xml.get( "Type", "" );

		if ( type.equalsIgnoreCase( "Door" ) )
		{
			entity = CreateDoor();
		}
		else if ( type.equalsIgnoreCase( "Transition" ) )
		{
			entity = CreateTransition( xml, levelUID );
		}
		else if ( type.equalsIgnoreCase( "Spawner" ) )
		{
			entity = CreateSpawner( xml );
		}
		else
		{
			entity = CreateBasic( xml );
		}

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

}
