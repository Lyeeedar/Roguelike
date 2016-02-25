package Roguelike.DungeonGeneration;

import Roguelike.Global;
import Roguelike.Save.SaveLevel;
import Roguelike.Tiles.Point;
import Roguelike.Util.EnumBitflag;
import Roguelike.Util.ImageUtils;
import com.badlogic.gdx.utils.Array;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;

/**
 * Created by Philip on 24-Feb-16.
 */
public class StaticLevelGenerator extends AbstractDungeonGenerator
{
	// ----------------------------------------------------------------------
	public StaticLevelGenerator()
	{}

	// ----------------------------------------------------------------------
	@Override
	public void setup( SaveLevel level, DungeonFileParser dfp )
	{
		this.saveLevel = level;
		this.dfp = dfp;

		width = dfp.minWidth;
		height = dfp.minHeight;

		ran = new Random( level.seed );
	}

	// ----------------------------------------------------------------------
	@Override
	public boolean generate()
	{
		if ( generationIndex == 0 )
		{
			selectRooms();

			generationIndex++;
			generationText = "Filling Grid";
		}
		else if ( generationIndex == 1 )
		{
			toBePlaced.addAll( requiredRooms );

			fillGridBase();

			generationIndex++;
			generationText = "Finding Rooms";
		}
		else if ( generationIndex == 2 )
		{
			findRoomLocations();

			generationIndex++;
			generationText = "Placing Rooms";
		}
		else if ( generationIndex == 3 )
		{
			placeRooms();

			generationIndex++;
			generationText = "Placing factions";
		}
		else if ( generationIndex == 4 )
		{
			placeFactions();

			generationIndex++;
			generationText = "Marking Rooms";
		}
		else if ( generationIndex == 5 )
		{
			markRooms();

			generationIndex++;
			generationText = "Flattening Level";
		}
		else if ( generationIndex == 6 )
		{
			level = createLevel( grid, dfp.getSymbol( '#' ) );

			generationIndex++;
			generationText = "Completed";
		}

		percent = (int) ( ( 100.0f / 7.0f ) * generationIndex );

		if ( generationIndex < 7 )
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	// ----------------------------------------------------------------------
	private void fillGridBase()
	{
		width = dfp.roomDef.length;
		height = dfp.roomDef[0].length;

		grid = new Symbol[width][height];

		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				grid[x][y] = dfp.getSymbol( dfp.roomDef[x][y] );
				grid[x][y].resolveExtends( dfp.sharedSymbolMap );
			}
		}
	}

	// ----------------------------------------------------------------------
	private void findRoomLocations()
	{
		for (int x = 0; x < width; x++)
		{
			for ( int y = 0; y < height; y++ )
			{
				if (grid[x][y].metaValue != null && grid[x][y].metaValue.equalsIgnoreCase( "roomseed" ))
				{
					boolean skip = false;
					for (RoomLocationData data : roomLocations)
					{
						if (x >= data.x && x <= data.x+data.width && y >= data.y && y <= data.y+data.height)
						{
							skip = true;
							break;
						}
					}

					if (skip)
					{
						continue;
					}

					// find the room
					roomLocations.add( findRoom( x, y ) );
				}
			}
		}
	}

	// ----------------------------------------------------------------------
	private RoomLocationData findRoom(int sx, int sy)
	{
		Point min = new Point(sx, sy);
		Point max = new Point(sx, sy);

		boolean minxCollided = false;
		boolean minyCollided = false;
		boolean maxxCollided = false;
		boolean maxyCollided = false;

		while (true)
		{
			if (!minxCollided)
			{
				min.x--;
				//walking along (minx,miny) to (minx,maxy) is there a collison
				for ( int i = 0; i < (max.y - min.y)+1; i++ )
				{
					int y = min.y + i;

					Symbol symbol = grid[ min.x ][ y ];

					if ( symbol.metaValue == null || !symbol.metaValue.equalsIgnoreCase( "roomseed" ) )
					{
						min.x++;
						minxCollided = true;
						break;
					}
				}
			}

			if (!minyCollided)
			{
				min.y--;
				//walking along (minx,miny) to (maxx,miny) is there a collison
				for ( int i = 0; i < (max.x - min.x)+1; i++ )
				{
					int x = min.x + i;

					Symbol symbol = grid[ x ][ min.y ];

					if ( symbol.metaValue == null || !symbol.metaValue.equalsIgnoreCase( "roomseed" ) )
					{
						min.y++;
						minyCollided = true;
						break;
					}
				}
			}

			if (!maxxCollided)
			{
				max.x++;
				//walking along (maxx,miny) to (maxx,maxy) is there a collison
				for ( int i = 0; i < (max.y - min.y)+1; i++ )
				{
					int y = min.y + i;

					Symbol symbol = grid[ max.x ][ y ];

					if ( symbol.metaValue == null || !symbol.metaValue.equalsIgnoreCase( "roomseed" ) )
					{
						max.x--;
						maxxCollided = true;
						break;
					}
				}
			}

			if (!maxyCollided)
			{
				max.y++;
				//walking along (minx,maxy) to (maxx,maxy) is there a collison
				for ( int i = 0; i < (max.x - min.x)+1; i++ )
				{
					int x = min.x + i;

					Symbol symbol = grid[ x ][ max.y ];

					if ( symbol.metaValue == null || !symbol.metaValue.equalsIgnoreCase( "roomseed" ) )
					{
						max.y--;
						maxyCollided = true;
						break;
					}
				}
			}

			if (minxCollided && maxxCollided && minyCollided && maxyCollided)
			{
				break;
			}
		}

		RoomLocationData data = new RoomLocationData();
		data.x = min.x;
		data.y = min.y;
		data.width = (max.x - min.x)+1;
		data.height = (max.y - min.y)+1;

		return data;
	}

	// ----------------------------------------------------------------------
	private void placeRooms()
	{
		Array<RoomPlacementData> placementDatas = new Array<RoomPlacementData>(  );

		for (Room room : requiredRooms)
		{
			RoomPlacementData data = new RoomPlacementData();
			data.room = room;

			for (RoomLocationData locationData : roomLocations)
			{
				if (room.width <= locationData.width && room.height <= locationData.height)
				{
					data.validLocations.add( locationData );
				}
				else if (room.height <= locationData.width && room.width <= locationData.height)
				{
					data.validLocations.add( locationData );
				}
			}

			placementDatas.add( data );
		}

		while (placementDatas.size > 0)
		{
			// sort
			placementDatas.sort( new Comparator<RoomPlacementData>() {
				@Override
				public int compare( RoomPlacementData o1, RoomPlacementData o2 )
				{
					return o1.validLocations.size - o2.validLocations.size;
				}
			} );

			// remove first
			RoomPlacementData data = placementDatas.removeIndex( 0 );

			if (data.validLocations.size == 0)
			{
				throw new RuntimeException( "Could not find space for room!" );
			}

			// place
			RoomLocationData location = data.validLocations.get( ran.nextInt( data.validLocations.size ) );

			// rotate / flip if neccesary
			boolean fits = false;
			boolean rotate = false;
			boolean flipVert = false;
			boolean flipHori = false;

			boolean fitsVertical = location.width <= width && location.height <= height;
			boolean fitsHorizontal = location.height <= width && location.width <= height;

			if ( data.room.roomData.lockRotation )
			{
				if ( fitsVertical )
				{
					fits = true;
					flipVert = true;

					if ( ran.nextBoolean() )
					{
						flipHori = true;
					}
				}
			}
			else
			{
				if ( fitsVertical || fitsHorizontal )
				{
					fits = true;

					// randomly flip
					if ( ran.nextBoolean() )
					{
						flipVert = true;
					}

					if ( ran.nextBoolean() )
					{
						flipHori = true;
					}

					// if it fits on both directions, randomly pick one
					if ( fitsVertical && fitsHorizontal )
					{
						if ( ran.nextBoolean() )
						{
							rotate = true;
						}
					}
					else if ( fitsHorizontal )
					{
						rotate = true;
					}
				}
			}

			// If it fits then place the room and rotate/flip as neccesary
			if ( fits )
			{
				if ( flipVert )
				{
					data.room.flipVertical();
				}

				if ( flipHori )
				{
					data.room.flipHorizontal();
				}

				if ( rotate )
				{
					data.room.rotate();
				}

				if ( flipVert && rotate )
				{
					data.room.orientation = Global.Direction.WEST;
				}
				else if ( flipVert )
				{
					data.room.orientation = Global.Direction.SOUTH;
				}
				else if ( rotate )
				{
					data.room.orientation = Global.Direction.EAST;
				}
				else
				{
					data.room.orientation = Global.Direction.NORTH;
				}
			}

			// setup pos
			data.room.x = location.x;
			data.room.y = location.y;

			//remove this room from all children
			for (RoomPlacementData odata : placementDatas)
			{
				odata.validLocations.removeValue( location, true );
			}
			roomLocations.removeValue( location, true );

			placedRooms.add( data.room );
		}

		// Fill rest with faction stuff
		for (RoomLocationData location : roomLocations)
		{
			Room room = new Room();
			room.x = location.x;
			room.y = location.y;
			room.width = location.width;
			room.height = location.height;

			room.roomContents = new Symbol[room.width][room.height];
			for ( int rx = 0; rx < room.width; rx++ )
			{
				for ( int ry = 0; ry < room.height; ry++ )
				{
					room.roomContents[rx][ry] = grid[room.x+rx][room.y+ry];
				}
			}

			placedRooms.add( room );
		}
	}

	// ----------------------------------------------------------------------
	protected void markRooms()
	{
		for ( Room room : placedRooms )
		{
			for ( int x = 0; x < room.width; x++ )
			{
				for ( int y = 0; y < room.height; y++ )
				{
					Symbol symbol = room.roomContents[x][y];
					symbol.containingRoom = room;

					grid[room.x+x][room.y+y] = symbol;
				}
			}
		}
	}

	// ----------------------------------------------------------------------
	public static final EnumBitflag<Global.Passability> GeneratorPassability = new EnumBitflag<Global.Passability>( Global.Passability.WALK );

	// ----------------------------------------------------------------------
	Array<RoomLocationData> roomLocations = new Array<RoomLocationData>(  );

	// ----------------------------------------------------------------------
	private Symbol[][] grid;

	// ----------------------------------------------------------------------
	private class RoomLocationData
	{
		public int x;
		public int y;
		public int width;
		public int height;
	}

	// ----------------------------------------------------------------------
	private class RoomPlacementData
	{
		public Room room;
		public Array<RoomLocationData> validLocations = new Array<RoomLocationData>(  );
	}
}
