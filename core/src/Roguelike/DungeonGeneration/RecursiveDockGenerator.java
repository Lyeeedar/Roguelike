package Roguelike.DungeonGeneration;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

import PaulChew.Pnt;
import PaulChew.Triangle;
import PaulChew.Triangulation;
import Roguelike.Global;
import Roguelike.Global.Direction;
import Roguelike.Global.Passability;
import Roguelike.DungeonGeneration.DungeonFileParser.CorridorFeature.PlacementMode;
import Roguelike.DungeonGeneration.DungeonFileParser.CorridorStyle.PathStyle;
import Roguelike.DungeonGeneration.DungeonFileParser.DFPRoom.Orientation;
import Roguelike.DungeonGeneration.Room.RoomDoor;
import Roguelike.Pathfinding.AStarPathfind;
import Roguelike.Pathfinding.PathfindingTile;
import Roguelike.Save.SaveLevel;
import Roguelike.Tiles.Point;
import Roguelike.Util.EnumBitflag;
import Roguelike.Util.ImageUtils;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;

public class RecursiveDockGenerator extends AbstractDungeonGenerator
{
	// ####################################################################//
	// region Constructor

	// ----------------------------------------------------------------------
	public RecursiveDockGenerator()
	{

	}

	// endregion Constructor
	// ####################################################################//
	// region Public Methods

	// ----------------------------------------------------------------------
	// Designed to be called until returns true
	@Override
	public boolean generate()
	{
		if ( generationIndex == 0 )
		{
			selectRooms();

			generationIndex++;
			generationText = "Partitioning Grid";
		}
		else if ( generationIndex == 1 )
		{
			toBePlaced.clear();
			placedRooms.clear();

			toBePlaced.addAll( requiredRooms );

			fillGridBase();
			partition( minPadding, minPadding, width - minPadding * 2, height - minPadding * 2 );

			if ( toBePlaced.size == 0 )
			{
				generationIndex++;
				generationText = "Finding Doors";
			}
			else
			{
				width += 10;
				height += 10;

				System.out.println( "Failed to place all rooms. Increasing size to " + width + "," + height + "and retrying" );
			}
		}
		else if ( generationIndex == 2 )
		{
			for ( Room room : placedRooms )
			{
				room.findDoors( ran, dfp );
			}

			generationIndex++;
			generationText = "Marking Rooms";
		}
		else if ( generationIndex == 3 )
		{
			markRooms();

			generationIndex++;
			generationText = "Connecting Rooms";
		}
		else if ( generationIndex == 4 )
		{
			connectRooms();

			generationIndex++;
			generationText = "Placing Factions";

			if ( DEBUG_OUTPUT )
			{
				Symbol[][] symbolGrid = new Symbol[width][height];
				for ( int x = 0; x < width; x++ )
				{
					for ( int y = 0; y < height; y++ )
					{
						symbolGrid[x][y] = tiles[x][y].symbol;
					}
				}
				if ( !Global.ANDROID )
				{
					ImageUtils.writeSymbolGridToFile( symbolGrid, "beforeFeatures.png", DEBUG_SIZE );
				}
			}
		}
		else if ( generationIndex == 5 )
		{
			placeFactions();

			generationIndex++;
			generationText = "Marking Rooms Again?";
		}
		else if ( generationIndex == 6 )
		{
			markRooms();

			generationIndex++;
			generationText = "Filling empty spaces";
		}
		else if ( generationIndex == 7 )
		{
			identifyAndFillEmptySpaces();

			generationIndex++;
			generationText = "Creating Level";

			if ( DEBUG_OUTPUT )
			{
				Symbol[][] symbolGrid = new Symbol[width][height];
				for ( int x = 0; x < width; x++ )
				{
					for ( int y = 0; y < height; y++ )
					{
						symbolGrid[x][y] = tiles[x][y].symbol;
					}
				}
				if ( !Global.ANDROID )
				{
					ImageUtils.writeSymbolGridToFile( symbolGrid, "afterFeatures.png", DEBUG_SIZE );
				}
			}
		}
		else if ( generationIndex == 8 )
		{
			// flatten to symbol grid
			Symbol[][] symbolGrid = new Symbol[width][height];
			for ( int x = 0; x < width; x++ )
			{
				for ( int y = 0; y < height; y++ )
				{
					symbolGrid[x][y] = tiles[x][y].symbol;
				}
			}
			level = createLevel( symbolGrid, dfp.getSymbol( '#' ) );

			generationIndex++;
			generationText = "Completed";
		}

		percent = (int) ( ( 100.0f / 8.0f ) * generationIndex );

		if ( generationIndex < 9 )
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	// ----------------------------------------------------------------------
	@Override
	public void setup( SaveLevel level, DungeonFileParser dfp )
	{
		this.saveLevel = level;
		this.dfp = dfp;

		if ( Global.ANDROID )
		{
			width = 50;
			height = 50;
		}
		else
		{
			width = 75;
			height = 75;
		}

		minPadding = ( dfp.corridorStyle.width / 2 ) + 1;
		maxPadding += minPadding;
		paddedMinRoom = minRoomSize + minPadding * 2;

		ran = new Random( level.seed );
	}

	// endregion Public Methods
	// ####################################################################//
	// region Private Methods

	// ----------------------------------------------------------------------
	protected void fillGridBase()
	{
		this.tiles = new GenerationTile[width][height];
		for ( int x = 0; x < width; x++ )
		{
			for ( int y = 0; y < height; y++ )
			{
				tiles[x][y] = new GenerationTile( x, y );

				if ( dfp.corridorStyle.pathStyle == PathStyle.STRAIGHT )
				{
					tiles[x][y].influence = width + height;
				}
				else if ( dfp.corridorStyle.pathStyle == PathStyle.WANDERING )
				{
					tiles[x][y].influence = ran.nextInt( ( width + height ) / 2 ) + ( width + height ) / 2;
				}

				if ( x == 0 || y == 0 || x == width - 1 || y == height - 1 )
				{
					tiles[x][y].passable = false;
				}
				else
				{
					tiles[x][y].passable = true;
				}

				tiles[x][y].symbol = dfp.sharedSymbolMap.get( '#' );
			}
		}

		if ( dfp.preprocessor != null )
		{
			Symbol[][] symbolGrid = new Symbol[width][height];
			for ( int x = 0; x < width; x++ )
			{
				for ( int y = 0; y < height; y++ )
				{
					symbolGrid[x][y] = tiles[x][y].symbol;
				}
			}

			Symbol floor = dfp.sharedSymbolMap.get( '.' );
			Symbol wall = dfp.sharedSymbolMap.get( '#' );

			dfp.preprocessor.generator.process( symbolGrid, floor, wall, ran, dfp );

			for ( int x = 0; x < width; x++ )
			{
				for ( int y = 0; y < height; y++ )
				{
					if ( x == 0 || x == width - 1 || y == 0 || y == height - 1 )
					{
						symbolGrid[x][y] = wall;
					}
				}
			}

			for ( int x = 0; x < width; x++ )
			{
				for ( int y = 0; y < height; y++ )
				{
					tiles[x][y].symbol = symbolGrid[x][y];
				}
			}
		}
	}

	// ----------------------------------------------------------------------
	protected void partition( int x, int y, int width, int height )
	{
		int padX = Math.min( ran.nextInt( maxPadding - minPadding ) + minPadding, ( width - minRoomSize ) / 2 );
		int padY = Math.min( ran.nextInt( maxPadding - minPadding ) + minPadding, ( height - minRoomSize ) / 2 );

		int padX2 = padX * 2;
		int padY2 = padY * 2;

		// get the room to be placed
		Room room = null;

		// if the predefined rooms array has items, then try to pick one from it
		if ( toBePlaced.size > 0 )
		{
			// Array of indexes to be tried, stops duplicate work
			Array<Integer> indexes = new Array<Integer>();
			for ( int i = 0; i < toBePlaced.size; i++ )
			{
				indexes.add( i );
			}

			while ( room == null && indexes.size > 0 )
			{
				int index = indexes.removeIndex( ran.nextInt( indexes.size ) );

				Room testRoom = toBePlaced.get( index );

				boolean fits = false;
				boolean rotate = false;
				boolean flipVert = false;
				boolean flipHori = false;

				if ( testRoom.roomData.orientation == Orientation.RANDOM )
				{
					boolean fitsVertical = testRoom.width + padX2 <= width && testRoom.height + padY2 <= height;
					boolean fitsHorizontal = testRoom.height + padX2 <= width && testRoom.width + padY2 <= height;

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
				else if ( testRoom.roomData.orientation == Orientation.CENTRE )
				{
					int l = x + padX;
					int r = this.width - ( x + testRoom.width + padX );
					int b = y + padY;
					int t = this.height - ( y + testRoom.height + padY );

					if ( b <= l && b <= r && b <= t )
					{
						// Closest to bottom
						// This is the default
					}
					else if ( t <= l && t <= r && t <= b )
					{
						// Closest to top
						flipVert = true;
					}
					else if ( l <= r && l <= b && l <= t )
					{
						// Closest to Left
						rotate = true;
					}
					else if ( r <= l && r <= b && r <= t )
					{
						// Closest to right
						rotate = true;
						flipVert = true;
					}

					if ( rotate )
					{
						fits = testRoom.height + padX2 <= width && testRoom.width + padY2 <= height;
					}
					else
					{
						fits = testRoom.width + padX2 <= width && testRoom.height + padY2 <= height;
					}
				}
				else if ( testRoom.roomData.orientation == Orientation.EDGE )
				{
					int l = x + padX;
					int r = this.width - ( x + testRoom.width + padX );
					int b = y + padY;
					int t = this.height - ( y + testRoom.height + padY );

					if ( t <= l && t <= r && t <= b )
					{
						// Closest to top
						// This is the default orientation
					}
					else if ( l <= r && l <= b && l <= t )
					{
						// Closest to Left
						rotate = true;
						flipVert = true;
					}
					else if ( r <= l && r <= b && r <= t )
					{
						// Closest to right
						rotate = true;
					}
					else if ( b <= l && b <= r && b <= t )
					{
						// Closest to bottom
						flipVert = true;
					}

					if ( rotate )
					{
						fits = testRoom.height + padX2 <= width && testRoom.width + padY2 <= height;
					}
					else
					{
						fits = testRoom.width + padX2 <= width && testRoom.height + padY2 <= height;
					}
				}
				else if ( testRoom.roomData.orientation == Orientation.ROTATED )
				{
					fits = testRoom.height + padX2 <= width && testRoom.width + padY2 <= height;

					if ( fits )
					{
						rotate = true;
						flipVert = true;
					}
				}
				else
				// if ( testRoom.roomData.orientation == Orientation.FIXED )
				{
					fits = testRoom.width + padX2 <= width && testRoom.height + padY2 <= height;
				}

				// If it fits then place the room and rotate/flip as neccesary
				if ( fits )
				{
					room = testRoom;
					toBePlaced.removeIndex( index );

					if ( flipVert )
					{
						room.flipVertical();
					}

					if ( flipHori )
					{
						room.flipHorizontal();
					}

					if ( rotate )
					{
						room.rotate();
					}

					if ( flipVert && rotate )
					{
						room.orientation = Direction.WEST;
					}
					else if ( flipVert )
					{
						room.orientation = Direction.SOUTH;
					}
					else if ( rotate )
					{
						room.orientation = Direction.EAST;
					}
					else
					{
						room.orientation = Direction.NORTH;
					}
				}
			}
		}

		// failed to find a suitable predefined room, so create a new one
		if ( room == null && dfp.roomGenerators.size > 0 )
		{
			int roomWidth = Math.min( ran.nextInt( maxRoomSize - minRoomSize ) + minRoomSize, width - padX2 );
			int roomHeight = Math.min( ran.nextInt( maxRoomSize - minRoomSize ) + minRoomSize, height - padY2 );

			room = new Room();
			room.width = roomWidth;
			room.height = roomHeight;

			room.generateRoomContents( ran, dfp );
		}

		if ( room == null ) { return; }

		placedRooms.add( room );

		// pick corner

		// possible sides:
		// 0 1
		// 2 3
		int side = ran.nextInt( 4 );

		// Position room at side
		if ( side == 0 )
		{
			room.x = x + padX;
			room.y = y + padY;
		}
		else if ( side == 1 )
		{
			room.x = ( x + width ) - ( room.width + padX );
			room.y = y + padY;
		}
		else if ( side == 2 )
		{
			room.x = x + padX;
			room.y = ( y + height ) - ( room.height + padY );
		}
		else
		{
			room.x = ( x + width ) - ( room.width + padX );
			room.y = ( y + height ) - ( room.height + padY );
		}

		// split into 2 remaining rectangles and recurse
		if ( side == 0 )
		{
			// r1
			// 22
			{
				int nx = room.x + room.width + padX;
				int ny = y;
				int nwidth = x + width - nx;
				int nheight = room.height + padY2;

				if ( nwidth >= paddedMinRoom && nheight >= paddedMinRoom )
				{
					partition( nx, ny, nwidth, nheight );
				}
			}

			{
				int nx = x;
				int ny = room.y + room.height + padY;
				int nwidth = width;
				int nheight = y + height - ny;

				if ( nwidth >= paddedMinRoom && nheight >= paddedMinRoom )
				{
					partition( nx, ny, nwidth, nheight );
				}
			}
		}
		else if ( side == 1 )
		{
			// 1r
			// 12
			{
				int nx = x;
				int ny = y;
				int nwidth = width - ( room.width + padX2 );
				int nheight = height;

				if ( nwidth >= paddedMinRoom && nheight >= paddedMinRoom )
				{
					partition( nx, ny, nwidth, nheight );
				}
			}

			{
				int nx = room.x - padX;
				int ny = room.y + room.height + padY;
				int nwidth = room.width + padX2;
				int nheight = ( y + height ) - ny;

				if ( nwidth >= paddedMinRoom && nheight >= paddedMinRoom )
				{
					partition( nx, ny, nwidth, nheight );
				}
			}
		}
		else if ( side == 2 )
		{
			// 12
			// r2
			{
				int nx = x;
				int ny = y;
				int nwidth = room.width + padX2;
				int nheight = height - ( room.height + padY2 );

				if ( nwidth >= paddedMinRoom && nheight >= paddedMinRoom )
				{
					partition( nx, ny, nwidth, nheight );
				}
			}

			{
				int nx = x + room.width + padX2;
				int ny = y;
				int nwidth = ( x + width ) - nx;
				int nheight = height;

				if ( nwidth >= paddedMinRoom && nheight >= paddedMinRoom )
				{
					partition( nx, ny, nwidth, nheight );
				}
			}
		}
		else
		{
			// 22
			// 1r
			{
				int nx = x;
				int ny = room.y - padY;
				int nwidth = width - ( room.width + padX2 );
				int nheight = ( y + height ) - ny;

				if ( nwidth >= paddedMinRoom && nheight >= paddedMinRoom )
				{
					partition( nx, ny, nwidth, nheight );
				}
			}

			{
				int nx = x;
				int ny = y;
				int nwidth = width;
				int nheight = height - ( room.height + padY2 );

				if ( nwidth >= paddedMinRoom && nheight >= paddedMinRoom )
				{
					partition( nx, ny, nwidth, nheight );
				}
			}
		}
	}

	// ----------------------------------------------------------------------
	protected void connectRooms()
	{
		Array<Pnt> roomPnts = new Array<Pnt>();

		for ( Room room : placedRooms )
		{
			for ( RoomDoor door : room.doors )
			{
				int x = door.pos[0] + room.x;
				int y = door.pos[1] + room.y;

				if ( door.side == Direction.WEST )
				{
					x -= dfp.corridorStyle.width - 1;
				}
				else if ( door.side == Direction.NORTH )
				{
					y -= dfp.corridorStyle.width - 1;
				}

				Pnt p = new Pnt( x, y );
				roomPnts.add( p );
			}
		}

		Triangle initialTriangle = new Triangle( new Pnt( -10000, -10000 ), new Pnt( 10000, -10000 ), new Pnt( 0, 10000 ) );
		Triangulation dt = new Triangulation( initialTriangle );

		for ( Pnt p : roomPnts )
		{
			dt.delaunayPlace( p );
		}

		Array<Pnt[]> ignoredPaths = new Array<Pnt[]>();
		Array<Pnt[]> addedPaths = new Array<Pnt[]>();
		Array<Pnt[]> paths = new Array<Pnt[]>();

		Array<Triangle> tris = new Array<Triangle>();
		for ( Triangle tri : dt )
		{
			tris.add( tri );
		}
		tris.sort( new Comparator<Triangle>()
				{

			@Override
			public int compare( Triangle arg0, Triangle arg1 )
			{
				return arg0.compareTo( arg1 );
			}
				} );

		for ( Triangle tri : tris )
		{
			calculatePaths( paths, tri, ignoredPaths, addedPaths );
		}

		for ( Pnt room : roomPnts )
		{
			int rx = (int) room.coord( 0 );
			int ry = (int) room.coord( 1 );

			Pnt closest = null;
			int closestDist = Integer.MAX_VALUE;
			boolean found = false;
			outer:
				for ( Pnt[] path : paths )
				{
					for ( Pnt p : path )
					{
						int px = (int) p.coord( 0 );
						int py = (int) p.coord( 1 );

						if ( rx == px && ry == py )
						{
							found = true;
							break outer;
						}

						int tempDist = Math.max( Math.abs( px - rx ), Math.abs( py - ry ) );
						if ( tempDist < closestDist )
						{
							closestDist = tempDist;
							closest = p;
						}
					}
				}

			if ( !found )
			{
				paths.add( new Pnt[] { room, closest } );
			}
		}

		for ( Pnt[] p : paths )
		{
			int x1 = (int) p[0].coord( 0 );
			int y1 = (int) p[0].coord( 1 );
			int x2 = (int) p[1].coord( 0 );
			int y2 = (int) p[1].coord( 1 );

			AStarPathfind pathFind = new AStarPathfind( tiles, x1, y1, x2, y2, false, true, dfp.corridorStyle.width, GeneratorPassability, null );
			Array<Point> path = pathFind.getPath();

			carveCorridor( path );

			Pools.freeAll( path );
		}
	}

	// ----------------------------------------------------------------------
	protected void carveCorridor( Array<Point> path )
	{
		int centralCount = 0;
		int sideCount = 0;
		boolean placementAlternator = true;

		int width = dfp.corridorStyle.width;

		for ( int i = 0; i < path.size; i++ )
		{
			Point pos = path.get( i );

			GenerationTile t = tiles[pos.x][pos.y];
			t.isCorridor = true;

			for ( int x = 0; x < width; x++ )
			{
				for ( int y = 0; y < width; y++ )
				{
					t = tiles[pos.x + x][pos.y + y];

					if ( t.symbol.character == '#' )
					{
						t.symbol = dfp.sharedSymbolMap.get( '.' );
					}

					// Wipe out all features not placed by this path
					if ( !t.isRoom && t.placerHashCode != path.hashCode() )
					{
						t.symbol = t.symbol.copy();
						t.symbol.environmentData = null;
						t.symbol.environmentEntityData = null;
					}

					// Wipe out all features in the central square
					if ( !t.isRoom && x > 0 && x < width - 1 && y > 0 && y < width - 1 )
					{
						t.symbol = t.symbol.copy();
						t.symbol.environmentData = null;
						t.symbol.environmentEntityData = null;
					}
				}
			}

			if ( dfp.corridorStyle.centralConstant != null )
			{
				t = tiles[pos.x + width / 2][pos.y + width / 2];
				t.symbol = dfp.corridorStyle.centralConstant.getAsSymbol( t.symbol );
				t.placerHashCode = path.hashCode();
			}

			if ( dfp.corridorStyle.centralRecurring != null )
			{
				centralCount++;

				if ( centralCount == dfp.corridorStyle.centralRecurring.interval )
				{
					t = tiles[pos.x + width / 2][pos.y + width / 2];
					t.symbol = dfp.corridorStyle.centralRecurring.getAsSymbol( t.symbol );
					t.placerHashCode = path.hashCode();

					centralCount = 0;
				}
			}

			if ( dfp.corridorStyle.sideRecurring != null )
			{
				sideCount++;

				if ( sideCount == dfp.corridorStyle.sideRecurring.interval && i > 0 )
				{
					boolean placeTop = dfp.corridorStyle.sideRecurring.placementMode == PlacementMode.BOTH
							|| dfp.corridorStyle.sideRecurring.placementMode == PlacementMode.TOP
							|| ( dfp.corridorStyle.sideRecurring.placementMode == PlacementMode.ALTERNATE && placementAlternator );

					boolean placeBottom = dfp.corridorStyle.sideRecurring.placementMode == PlacementMode.BOTH
							|| dfp.corridorStyle.sideRecurring.placementMode == PlacementMode.BOTTOM
							|| ( dfp.corridorStyle.sideRecurring.placementMode == PlacementMode.ALTERNATE && !placementAlternator );

					if ( path.get( i - 1 ).x != pos.x )
					{
						if ( dfp.corridorStyle.width == 1 )
						{
							if ( placeTop && isEmpty( tiles[pos.x + width / 2][pos.y - 1] ) )
							{
								t = tiles[pos.x + width / 2][pos.y - 1];
								t.symbol = dfp.corridorStyle.sideRecurring.getAsSymbol( t.symbol );
								t.symbol.attachLocation = Direction.NORTH;
								t.placerHashCode = path.hashCode();
							}

							if ( placeBottom && isEmpty( tiles[pos.x + width / 2][pos.y + width] ) )
							{
								t = tiles[pos.x + width / 2][pos.y + width];
								t.symbol = dfp.corridorStyle.sideRecurring.getAsSymbol( t.symbol );
								t.symbol.attachLocation = Direction.SOUTH;
								t.placerHashCode = path.hashCode();
							}
						}
						else
						{
							if ( placeTop && tiles[pos.x + width / 2][pos.y - 1].symbol.character == '#' )
							{
								t = tiles[pos.x + width / 2][pos.y];
								t.symbol = dfp.corridorStyle.sideRecurring.getAsSymbol( t.symbol );
								t.symbol.attachLocation = Direction.NORTH;
								t.placerHashCode = path.hashCode();
							}

							if ( placeBottom && tiles[pos.x + width / 2][pos.y + width].symbol.character == '#' )
							{
								t = tiles[pos.x + width / 2][pos.y + width - 1];
								t.symbol = dfp.corridorStyle.sideRecurring.getAsSymbol( t.symbol );
								t.symbol.attachLocation = Direction.SOUTH;
								t.placerHashCode = path.hashCode();
							}
						}
					}
					else
					{
						if ( dfp.corridorStyle.width == 1 )
						{
							if ( placeTop && isEmpty( tiles[pos.x - 1][pos.y + width / 2] ) )
							{
								t = tiles[pos.x - 1][pos.y + width / 2];
								t.symbol = dfp.corridorStyle.sideRecurring.getAsSymbol( t.symbol );
								t.symbol.attachLocation = Direction.EAST;
								t.placerHashCode = path.hashCode();
							}

							if ( placeBottom && isEmpty( tiles[pos.x + width][pos.y + width / 2] ) )
							{
								t = tiles[pos.x + width][pos.y + width / 2];
								t.symbol = dfp.corridorStyle.sideRecurring.getAsSymbol( t.symbol );
								t.symbol.attachLocation = Direction.WEST;
								t.placerHashCode = path.hashCode();
							}
						}
						else
						{
							if ( placeTop && tiles[pos.x - 1][pos.y + width / 2].symbol.character == '#' )
							{
								t = tiles[pos.x][pos.y + width / 2];
								t.symbol = dfp.corridorStyle.sideRecurring.getAsSymbol( t.symbol );
								t.symbol.attachLocation = Direction.EAST;
								t.placerHashCode = path.hashCode();
							}

							if ( placeBottom && tiles[pos.x + width][pos.y + width / 2].symbol.character == '#' )
							{
								t = tiles[pos.x + width - 1][pos.y + width / 2];
								t.symbol = dfp.corridorStyle.sideRecurring.getAsSymbol( t.symbol );
								t.symbol.attachLocation = Direction.WEST;
								t.placerHashCode = path.hashCode();
							}
						}
					}

					sideCount = 0;
					placementAlternator = !placementAlternator;
				}
			}
		}
	}

	// ----------------------------------------------------------------------
	protected void calculatePaths( Array<Pnt[]> paths, Triangle triangle, Array<Pnt[]> ignoredPaths, Array<Pnt[]> addedPaths )
	{
		Pnt[] vertices = triangle.toArray( new Pnt[0] );

		int ignore = 0;
		double dist = 0;

		dist = Math.pow( 2, vertices[0].coord( 0 ) - vertices[1].coord( 0 ) ) + Math.pow( 2, vertices[0].coord( 1 ) - vertices[1].coord( 1 ) );

		double temp = Math.pow( 2, vertices[0].coord( 0 ) - vertices[2].coord( 0 ) ) + Math.pow( 2, vertices[0].coord( 1 ) - vertices[2].coord( 1 ) );
		if ( dist < temp )
		{
			dist = temp;
			ignore = 1;
		}

		temp = Math.pow( 2, vertices[1].coord( 0 ) - vertices[2].coord( 0 ) ) + Math.pow( 2, vertices[1].coord( 1 ) - vertices[2].coord( 1 ) );
		if ( dist < temp )
		{
			dist = temp;
			ignore = 2;
		}

		if ( ignore != 0 && !checkIgnored( vertices[0], vertices[1], ignoredPaths ) && !checkAdded( vertices[0], vertices[1], addedPaths ) )
		{
			addPath( vertices[0], vertices[1], paths, ignoredPaths, addedPaths );
		}
		else
		{
			ignoredPaths.add( new Pnt[] { vertices[0], vertices[1] } );
		}

		if ( ignore != 1 && !checkIgnored( vertices[0], vertices[2], ignoredPaths ) && !checkAdded( vertices[0], vertices[2], addedPaths ) )
		{
			addPath( vertices[0], vertices[2], paths, ignoredPaths, addedPaths );
		}
		else
		{
			ignoredPaths.add( new Pnt[] { vertices[0], vertices[2] } );
		}

		if ( ignore != 2 && !checkIgnored( vertices[1], vertices[2], ignoredPaths ) && !checkAdded( vertices[1], vertices[2], addedPaths ) )
		{
			addPath( vertices[1], vertices[2], paths, ignoredPaths, addedPaths );
		}
		else
		{
			ignoredPaths.add( new Pnt[] { vertices[1], vertices[2] } );
		}
	}

	// ----------------------------------------------------------------------
	protected void addPath( Pnt p1, Pnt p2, Array<Pnt[]> paths, Array<Pnt[]> ignoredPaths, Array<Pnt[]> addedPaths )
	{
		if ( p1.coord( 0 ) < 0
				|| p1.coord( 1 ) < 0
				|| p1.coord( 0 ) >= width - 1
				|| p1.coord( 1 ) >= height - 1
				|| p2.coord( 0 ) < 0
				|| p2.coord( 1 ) < 0
				|| p2.coord( 0 ) >= width - 1
				|| p2.coord( 1 ) >= height - 1 )
		{
			ignoredPaths.add( new Pnt[] { p1, p2 } );
		}
		else
		{
			addedPaths.add( new Pnt[] { p1, p2 } );
			paths.add( new Pnt[] { p1, p2 } );
		}
	}

	// ----------------------------------------------------------------------
	protected boolean checkIgnored( Pnt p1, Pnt p2, Array<Pnt[]> ignoredPaths )
	{
		for ( Pnt[] p : ignoredPaths )
		{
			if ( p[0].equals( p1 ) && p[1].equals( p2 ) )
			{
				return true;
			}
			else if ( p[0].equals( p2 ) && p[1].equals( p1 ) ) { return true; }
		}
		return false;
	}

	// ----------------------------------------------------------------------
	protected boolean checkAdded( Pnt p1, Pnt p2, Array<Pnt[]> addedPaths )
	{
		for ( Pnt[] p : addedPaths )
		{
			if ( p[0].equals( p1 ) && p[1].equals( p2 ) )
			{
				return true;
			}
			else if ( p[0].equals( p2 ) && p[1].equals( p1 ) ) { return true; }
		}
		return false;
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
					GenerationTile tile = tiles[room.x + x][room.y + y];
					Symbol symbol = room.roomContents[x][y];
					symbol.containingRoom = room;

					tile.passable = symbol.isPassable( GeneratorPassability );
					tile.symbol = symbol;
					tile.isRoom = true;
				}
			}
		}
	}

	// ----------------------------------------------------------------------
	protected void identifyAndFillEmptySpaces()
	{
		Symbol wall = dfp.sharedSymbolMap.get( '#' );

		for ( int x = 0; x < width; x++ )
		{
			for ( int y = 0; y < height; y++ )
			{
				if ( isEmpty( tiles[x][y] ) )
				{
					HashSet<GenerationTile> output = new HashSet<GenerationTile>();
					floodFillEmptySpace( x, y, output );

					if ( output.size() > 4 )
					{
						// convert into a room
						Room room = new Room();

						// find min/max
						int minx = Integer.MAX_VALUE;
						int miny = Integer.MAX_VALUE;
						int maxx = 0;
						int maxy = 0;

						for ( GenerationTile tile : output )
						{
							if ( tile.x < minx )
							{
								minx = tile.x;
							}
							if ( tile.y < miny )
							{
								miny = tile.y;
							}
							if ( tile.x > maxx )
							{
								maxx = tile.x;
							}
							if ( tile.y > maxy )
							{
								maxy = tile.y;
							}
						}

						room.width = ( maxx - minx ) + 1;
						room.height = ( maxy - miny ) + 1;

						// Copy contents into room
						room.roomContents = new Symbol[room.width][room.height];
						for ( int rx = 0; rx < room.width; rx++ )
						{
							for ( int ry = 0; ry < room.height; ry++ )
							{
								if ( output.contains( tiles[minx + rx][miny + ry] ) )
								{
									room.roomContents[rx][ry] = tiles[minx + rx][miny + ry].symbol;
								}
								else
								{
									room.roomContents[rx][ry] = wall;
								}
							}
						}

						// Identify doors
						room.findDoors( ran, dfp );

						// Find closest faction
						FactionParser closestFaction = null;
						int dist = Integer.MAX_VALUE;

						for ( Map.Entry<FactionParser, Point> faction : factions.entrySet() )
						{
							int tempdist = Math.abs( room.x - faction.getValue().x ) + Math.abs( room.y - faction.getValue().y );
							if ( tempdist < dist )
							{
								dist = tempdist;
								closestFaction = faction.getKey();
							}
						}

						int influence = dist;
						if ( influence > 0 )
						{
							float fract = influence / (float) ( width + height );
							influence = (int) ( fract * 100 );
						}

						influence = 100 - influence;

						room.addFeatures( ran, dfp, closestFaction, influence );

						// Copy placed features back into map
						for ( int rx = 0; rx < room.width; rx++ )
						{
							for ( int ry = 0; ry < room.height; ry++ )
							{
								if ( output.contains( tiles[minx + rx][miny + ry] ) )
								{
									if ( tiles[minx + rx][miny + ry].symbol != room.roomContents[rx][ry] )
									{
										room.roomContents[rx][ry].character = 'E';
									}

									tiles[minx + rx][miny + ry].symbol = room.roomContents[rx][ry];
								}
							}
						}
					}

					// Mark the area as empty space to indicate we already
					// filled this
					for ( GenerationTile tile : output )
					{
						tile.isEmptySpace = true;
					}
				}
			}
		}
	}

	// ----------------------------------------------------------------------
	protected void floodFillEmptySpace( int x, int y, HashSet<GenerationTile> output )
	{
		Array<int[]> toBeProcessed = new Array<int[]>();
		toBeProcessed.add( new int[] { x, y } );

		while ( toBeProcessed.size > 0 )
		{
			int[] point = toBeProcessed.pop();
			x = point[0];
			y = point[1];

			if ( output.contains( tiles[x][y] ) )
			{
				continue;
			}

			output.add( tiles[x][y] );

			for ( Direction dir : Direction.values() )
			{
				if ( dir.isCardinal() )
				{
					int nx = x + dir.getX();
					int ny = y + dir.getY();

					if ( isEmpty( tiles[nx][ny] ) )
					{
						// check neighbours

						if ( isEmpty( nx, ny, dir.getClockwise() ) || isEmpty( nx, ny, dir.getAnticlockwise() ) )
						{
							toBeProcessed.add( new int[] { nx, ny } );
						}
					}
				}
			}
		}
	}

	// ----------------------------------------------------------------------
	protected boolean isEmpty( int x, int y, Direction dir )
	{
		GenerationTile tile = tiles[x + dir.getX()][y + dir.getY()];
		return !tile.isCorridor && !tile.isRoom && !tile.isEmptySpace && tile.symbol.character != '#';
	}

	// ----------------------------------------------------------------------
	protected boolean isEmpty( GenerationTile tile )
	{
		return !tile.isCorridor && !tile.isRoom && !tile.isEmptySpace && tile.symbol.character != '#';
	}

	// ----------------------------------------------------------------------
	protected void placeFactions()
	{
		// place factions

		// get largest room
		int max = 0;
		Room largest = null;

		for ( Room room : placedRooms )
		{
			int size = room.width * room.height;

			if ( size > max )
			{
				max = size;
				largest = room;
			}
		}

		String majorFactionName = dfp.getMajorFaction( ran );

		FactionParser majorFaction = FactionParser.load( majorFactionName );
		if ( majorFaction == null ) { return; }

		factions.put( majorFaction, new Point( largest.x + largest.width / 2, largest.y + largest.height / 2 ) );

		class Pair implements Comparable<Pair>
		{
			int dist;
			Room room;

			public Pair( int dist, Room room )
			{
				this.dist = dist;
				this.room = room;
			}

			@Override
			public int compareTo( Pair arg0 )
			{
				return ( (Integer) dist ).compareTo( arg0.dist );
			}
		}

		Array<Pair> sortedRooms = new Array<Pair>();
		for ( Room room : placedRooms )
		{
			if ( room.faction == null )
			{
				int dist = Math.abs( room.x - largest.x ) + Math.abs( room.y - largest.y );
				sortedRooms.add( new Pair( dist, room ) );
			}
			else if ( !room.faction.equalsIgnoreCase( "none" ) )
			{
				int influence = ran.nextInt( 80 ) + 10;

				FactionParser fp = FactionParser.load( room.faction );

				if ( fp != null )
				{
					room.addFeatures( ran, dfp, fp, influence );

					factions.put( fp, new Point( room.x + room.width / 2, room.y + room.height / 2 ) );
				}
				else
				{
					int dist = Math.abs( room.x - largest.x ) + Math.abs( room.y - largest.y );
					sortedRooms.add( new Pair( dist, room ) );
				}
			}
		}
		sortedRooms.sort();

		int numMinor = sortedRooms.size / 4;

		// Add features
		for ( int i = 0; i < sortedRooms.size; i++ )
		{
			Pair pair = sortedRooms.get( i );

			if ( i < sortedRooms.size - numMinor )
			{
				int influence = pair.dist;
				if ( influence > 0 )
				{
					float fract = influence / (float) ( width + height );
					influence = (int) ( fract * 100 );
				}

				influence = 100 - influence;

				pair.room.addFeatures( ran, dfp, majorFaction, influence );
			}
			else
			{
				int influence = ran.nextInt( 80 ) + 10;

				FactionParser fp = FactionParser.load( dfp.getMinorFaction( ran ) );

				pair.room.addFeatures( ran, dfp, fp, influence );

				factions.put( fp, new Point( pair.room.x + pair.room.width / 2, pair.room.y + pair.room.height / 2 ) );
			}
		}
	}

	// endregion Private Methods
	// ####################################################################//
	// region Data

	// ----------------------------------------------------------------------
	public static final EnumBitflag<Passability> GeneratorPassability = new EnumBitflag<Passability>( Passability.WALK );

	private static final int DEBUG_SIZE = 16;

	private GenerationTile[][] tiles;

	private int minPadding = 1;
	private int maxPadding = 3;

	private int minRoomSize = 7;
	private int maxRoomSize = 25;

	private int paddedMinRoom;

	private HashMap<FactionParser, Point> factions = new HashMap<FactionParser, Point>();

	// endregion Data
	// ####################################################################//
	// region Classes

	// ----------------------------------------------------------------------
	public static class GenerationTile implements PathfindingTile
	{
		public Symbol symbol;
		public int influence;
		public long placerHashCode;

		public boolean passable;
		public boolean isCorridor = false;
		public boolean isRoom = false;
		public boolean isEmptySpace = false;

		public int x;
		public int y;

		public GenerationTile( int x, int y )
		{
			this.x = x;
			this.y = y;
		}

		// ----------------------------------------------------------------------
		@Override
		public boolean getPassable( EnumBitflag<Passability> travelType, Object self )
		{
			return passable;
		}

		// ----------------------------------------------------------------------
		@Override
		public int getInfluence()
		{
			if ( isCorridor )
			{
				return 0;
			}
			else
			{
				return influence;
			}
		}

		@Override
		public String toString()
		{
			return "" + symbol.character;
		}
	}

	// endregion Classes
	// ####################################################################//
}
