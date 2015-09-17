package Roguelike.DungeonGeneration;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import Roguelike.Global.Direction;
import Roguelike.DungeonGeneration.DungeonFileParser.DFPRoom;
import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Entity.GameEntity;
import Roguelike.Levels.Dungeon;
import Roguelike.Levels.Level;
import Roguelike.Save.SaveLevel;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public abstract class AbstractDungeonGenerator
{
	public int percent;
	public String generationText = "Selecting Rooms";
	protected int generationIndex = 0;
	protected SaveLevel saveLevel;

	protected DungeonFileParser dfp;
	protected Array<DFPRoom> additionalRooms = new Array<DFPRoom>();
	protected Array<Room> requiredRooms = new Array<Room>();
	protected Array<Room> toBePlaced = new Array<Room>();

	protected Array<Room> placedRooms = new Array<Room>();

	protected Dungeon dungeon;
	protected Level level;
	protected Random ran;
	protected int width;
	protected int height;

	public abstract void setup( SaveLevel level, DungeonFileParser dfp );

	public abstract boolean generate();

	protected boolean DEBUG_OUTPUT = false;

	public Level getLevel()
	{
		return level;
	}

	// ----------------------------------------------------------------------
	protected void selectRooms()
	{
		for ( DFPRoom r : dfp.getRequiredRooms( saveLevel.depth, ran ) )
		{
			if ( r.isTransition )
			{
				if ( !saveLevel.created )
				{
					additionalRooms.add( r );
				}
			}
			else
			{
				Room room = new Room();
				r.fillRoom( room, ran, dfp );
				requiredRooms.add( room );
			}
		}

		for ( DFPRoom r : dfp.getOptionalRooms( saveLevel.depth, ran ) )
		{
			if ( r.isTransition )
			{
				if ( !saveLevel.created )
				{
					additionalRooms.add( r );
				}
			}
			else
			{
				Room room = new Room();
				r.fillRoom( room, ran, dfp );
				requiredRooms.add( room );
			}
		}

		additionalRooms.addAll( saveLevel.requiredRooms );

		for ( DFPRoom r : additionalRooms )
		{
			Room room = new Room();
			r.fillRoom( room, ran, dfp );
			requiredRooms.add( room );
		}

		requiredRooms.sort( new Comparator<Room>()
				{
			@Override
			public int compare( Room arg0, Room arg1 )
			{
				return arg0.comparisonString().compareTo( arg1.comparisonString() );
			}
				} );
	}

	// ----------------------------------------------------------------------
	public static Symbol[][] minimiseGrid( Symbol[][] grid, Symbol wall )
	{
		int width = grid.length;
		int height = grid[0].length;

		int minx = -1;
		int miny = -1;
		int maxx = -1;
		int maxy = -1;

		boolean complete = false;

		// find min x
		for ( int x = 0; x < width; x++ )
		{
			for ( int y = 0; y < height; y++ )
			{
				Symbol s = grid[x][y];
				if ( s.character != wall.character )
				{
					minx = x - 1;

					complete = true;
					break;
				}
			}
			if ( complete )
			{
				break;
			}
		}
		if ( minx == -1 ) { return grid; }

		// find min y
		complete = false;
		for ( int y = 0; y < height; y++ )
		{
			for ( int x = minx; x < width; x++ )
			{
				Symbol s = grid[x][y];
				if ( s.character != wall.character )
				{
					miny = y - 1;

					complete = true;
					break;
				}
			}
			if ( complete )
			{
				break;
			}
		}
		if ( miny == -1 ) { return grid; }

		// find max x
		complete = false;
		for ( int x = width - 1; x >= minx; x-- )
		{
			for ( int y = miny; y < height; y++ )
			{
				Symbol s = grid[x][y];
				if ( s.character != wall.character )
				{
					maxx = x + 2;

					complete = true;
					break;
				}
			}
			if ( complete )
			{
				break;
			}
		}
		if ( maxx == -1 ) { return grid; }

		// find max y
		complete = false;
		for ( int y = height - 1; y >= miny; y-- )
		{
			for ( int x = minx; x < maxx; x++ )
			{
				Symbol s = grid[x][y];
				if ( s.character != wall.character )
				{
					maxy = y + 2;

					complete = true;
					break;
				}
			}
			if ( complete )
			{
				break;
			}
		}
		if ( maxy == -1 ) { return grid; }

		// minimise room
		int newwidth = maxx - minx;
		int newheight = maxy - miny;

		Symbol[][] newgrid = new Symbol[newwidth][newheight];

		for ( int x = 0; x < newwidth; x++ )
		{
			for ( int y = 0; y < newheight; y++ )
			{
				newgrid[x][y] = grid[minx + x][miny + y];
			}
		}

		return newgrid;
	}

	// ----------------------------------------------------------------------
	protected Level createLevel( Symbol[][] symbolGrid, Symbol outerWall )
	{
		if ( DEBUG_OUTPUT )
		{
			for ( int x = 0; x < width; x++ )
			{
				for ( int y = 0; y < height; y++ )
				{
					System.out.print( symbolGrid[x][y].character );
				}
				System.out.print( "\n" );
			}
			System.out.println( "\n" );
		}

		// minimise
		symbolGrid = minimiseGrid( symbolGrid, outerWall );
		width = symbolGrid.length;
		height = symbolGrid[0].length;

		if ( DEBUG_OUTPUT )
		{
			for ( int x = 0; x < width; x++ )
			{
				for ( int y = 0; y < height; y++ )
				{
					System.out.print( symbolGrid[x][y].character );
				}
				System.out.print( "\n" );
			}
			System.out.println( "\n" );
		}

		GameTile[][] actualTiles = new GameTile[width][height];
		Level level = new Level( actualTiles );
		level.Ambient = dfp.ambient;
		level.affectedByDayNight = dfp.affectedByDayNight;
		level.bgmName = dfp.BGM;
		level.ambientSounds.addAll( dfp.ambientSounds );

		level.depth = saveLevel.depth;
		level.fileName = saveLevel.fileName;
		level.seed = saveLevel.seed;
		level.requiredRooms = additionalRooms;

		level.background = dfp.background;

		for ( int x = 0; x < width; x++ )
		{
			for ( int y = 0; y < height; y++ )
			{
				Symbol symbol = symbolGrid[x][y];

				GameTile newTile = new GameTile( x, y, level, symbol.getTileData() );
				newTile.metaValue = symbol.metaValue;

				newTile.metaValue = symbol.metaValue;

				actualTiles[x][y] = newTile;

				// System.out.print(symbol.character);
			}
			// System.out.print("\n");
		}

		for ( int x = 0; x < width; x++ )
		{
			for ( int y = 0; y < height; y++ )
			{
				Symbol symbol = symbolGrid[x][y];
				GameTile newTile = actualTiles[x][y];

				if ( symbol.hasEnvironmentEntity() )
				{
					EnvironmentEntity entity = symbol.getEnvironmentEntity( dungeon.UID, saveLevel.UID );

					if ( !saveLevel.created || !entity.canTakeDamage )
					{
						if ( entity.attachToWall )
						{
							Direction location = Direction.CENTER;

							if ( symbol.attachLocation != null )
							{
								location = symbol.attachLocation;
							}
							else
							{
								// get direction
								HashSet<Direction> validDirections = new HashSet<Direction>();
								for ( Direction dir : Direction.values() )
								{
									boolean passable = symbolGrid[x + dir.getX()][y + dir.getY()].getTileData().passableBy.getBitFlag() != 0;
									if ( !passable )
									{
										validDirections.add( dir );
									}
								}

								if ( validDirections.size() > 0 )
								{
									// look for direction with full surround
									for ( Direction dir : Direction.values() )
									{
										boolean acwvalid = validDirections.contains( dir.getAnticlockwise() );
										boolean valid = validDirections.contains( dir );
										boolean cwvalid = validDirections.contains( dir.getClockwise() );

										if ( acwvalid && valid && cwvalid )
										{
											location = dir;
											break;
										}
									}

									// If that failed then just try the cardinal
									// directions
									if ( location == Direction.CENTER )
									{
										for ( Direction dir : Direction.values() )
										{
											if ( dir.isCardinal() )
											{
												if ( validDirections.contains( dir ) )
												{
													location = dir;
													break;
												}
											}
										}
									}

									// else pick random
									if ( location == Direction.CENTER )
									{
										location = validDirections.toArray( new Direction[validDirections.size()] )[ran.nextInt( validDirections.size() )];
									}

									location = location.getOpposite();
								}
							}

							entity.location = location;
							entity.sprite.rotation = location.getAngle();
						}
						else
						{
							if ( symbol.containingRoom != null
									&& symbol.containingRoom.orientation != Direction.CENTER
									&& symbol.environmentData.getBoolean( "MatchRoomRotation", false ) )
							{
								entity.sprite.rotation = symbol.containingRoom.orientation.getAngle();
							}
						}

						newTile.addEnvironmentEntity( entity );
					}
				}

				if ( !saveLevel.created && symbol.hasGameEntity() )
				{
					GameEntity e = symbol.getGameEntity();
					newTile.addGameEntity( e );
				}
			}
		}

		saveLevel.addSavedLevelContents( level );

		level.depth = saveLevel.depth;
		level.UID = saveLevel.UID;

		level.dungeon = dungeon;
		dungeon.addLevel( saveLevel );
		dungeon.addLevel( level );

		level.calculateAmbient();

		return level;
	}

	// ----------------------------------------------------------------------
	public static AbstractDungeonGenerator load( Dungeon dungeon, SaveLevel level )
	{
		DungeonFileParser dfp = DungeonFileParser.load( level.fileName + "/" + level.fileName );

		Class<AbstractDungeonGenerator> c = ClassMap.get( dfp.generator.toUpperCase() );
		AbstractDungeonGenerator type = null;

		try
		{
			type = ClassReflection.newInstance( c );
		}
		catch ( ReflectionException e )
		{
			e.printStackTrace();
		}

		type.dungeon = dungeon;
		type.setup( level, dfp );

		return type;
	}

	// ----------------------------------------------------------------------
	protected static HashMap<String, Class> ClassMap = new HashMap<String, Class>();

	// ----------------------------------------------------------------------
	static
	{
		ClassMap.put( "RECURSIVEDOCK", RecursiveDockGenerator.class );
		ClassMap.put( "WORLDMAP", WorldMapGenerator.class );
	}
}
