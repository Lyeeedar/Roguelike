package Roguelike.DungeonGeneration;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Random;

import PaulChew.Pnt;
import PaulChew.Triangle;
import PaulChew.Triangulation;
import Roguelike.Global.Direction;
import Roguelike.DungeonGeneration.DungeonFileParser.DFPRoom;
import Roguelike.DungeonGeneration.FactionParser.Encounter;
import Roguelike.DungeonGeneration.FactionParser.Feature;
import Roguelike.DungeonGeneration.FactionParser.FeaturePlacementType;
import Roguelike.Entity.GameEntity;
import Roguelike.Levels.Level;
import Roguelike.Pathfinding.AStarPathfind;
import Roguelike.Pathfinding.BresenhamLine;
import Roguelike.Pathfinding.Pathfinder;
import Roguelike.Pathfinding.PathfindingTile;
import Roguelike.Tiles.GameTile;
import Roguelike.Util.ImageUtils;

import com.badlogic.gdx.utils.Array;

public class RecursiveDockGenerator
{
	//####################################################################//
	//region Constructor

	//----------------------------------------------------------------------
	public RecursiveDockGenerator(String level)
	{
		dfp = DungeonFileParser.load(level+"/"+level); 
	}

	//endregion Constructor
	//####################################################################//
	//region Public Methods

	//----------------------------------------------------------------------
	public Level getLevel()
	{
		// flatten to symbol grid
		Symbol[][] symbolGrid = new Symbol[width][height];
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				symbolGrid[x][y] = tiles[x][y].symbol;
			}
		}

		// minimise
		symbolGrid = RecursiveDockGenerator.minimiseGrid(symbolGrid, dfp.sharedSymbolMap.get('#'));

		width = symbolGrid.length;
		height = symbolGrid[0].length;

		GameTile[][] actualTiles = new GameTile[width][height];
		Level level = new Level(actualTiles);
		level.Ambient = dfp.ambient;

		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				Symbol symbol = symbolGrid[x][y];

				GameTile newTile = new GameTile
						(
								x, y,
								level,
								symbol.getTileData()
								);

				if (symbol.hasEnvironmentEntity())
				{
					newTile.addEnvironmentEntity(symbol.getEnvironmentEntity());
				}

				if (symbol.hasGameEntity())
				{
					newTile.addObject(symbol.getGameEntity());
				}

				newTile.metaValue = symbol.metaValue;

				actualTiles[x][y] = newTile;

				System.out.print(symbol.character);
			}
			System.out.print("\n");
		}

		return level;
	}

	//----------------------------------------------------------------------
	public void generate() 
	{
		for (DFPRoom r : dfp.requiredRooms)
		{
			Room room = new Room();
			r.fillRoom(room);
			toBePlaced.add(room);
		}

		if (dfp.optionalRooms.size > 0)
		{
			DFPRoom r = dfp.optionalRooms.get(ran.nextInt(dfp.optionalRooms.size));
			Room room = new Room();
			r.fillRoom(room);
			toBePlaced.add(room);
		}

		Array<Room> requiredRooms = new Array<Room>();
		requiredRooms.addAll(toBePlaced);

		while (true)
		{
			this.tiles = new GenerationTile[width][height];
			for (int x = 0; x < width; x++)
			{
				for (int y = 0; y < height; y++)
				{
					tiles[x][y] = new GenerationTile();

					if (x == 0 || y == 0 || x == width-1 || y == height-1)
					{						
						tiles[x][y].pathfindType = PathfindType.WALL;
					}

					tiles[x][y].symbol = dfp.sharedSymbolMap.get('#');
				}
			}

			partition(minPadding, minPadding, width-minPadding*2, height-minPadding*2);

			if (toBePlaced.size == 0)
			{
				break;
			}
			else
			{
				toBePlaced.clear();
				placedRooms.clear();

				toBePlaced.addAll(requiredRooms);

				width += 10;
				height += 10;

				System.out.println("Failed to place all rooms. Retrying");
			}
		}

		markRooms();

		connectRooms();

		{
			Symbol[][] symbolGrid = new Symbol[width][height];
			for (int x = 0; x < width; x++)
			{
				for (int y = 0; y < height; y++)
				{
					symbolGrid[x][y] = tiles[x][y].symbol;
				}
			}
			ImageUtils.writeSymbolGridToFile(symbolGrid, "beforeFeatures.png", 16);
		}


		// place factions

		// get largest room
		int max = 0;
		Room largest = null;

		for (Room room : placedRooms)
		{
			int size = room.width * room.height;

			if (size > max)
			{
				max = size;
				largest = room;
			}
		}

		String majorFactionName = dfp.getMajorFaction(ran);

		FactionParser majorFaction = FactionParser.load(majorFactionName);

		class Pair implements Comparable<Pair>
		{
			int dist;
			Room room;

			public Pair(int dist, Room room)
			{
				this.dist = dist;
				this.room = room;
			}

			@Override
			public int compareTo(Pair arg0)
			{
				return ((Integer)dist).compareTo(arg0.dist);
			}
		}

		Array<Pair> sortedRooms = new Array<Pair>();
		for (Room room : placedRooms)
		{
			if (room.faction == null)
			{
				int dist = Math.abs(room.x - largest.x) + Math.abs(room.y - largest.y);
				sortedRooms.add(new Pair(dist, room));
			}
			else
			{
				int influence = ran.nextInt(80) + 10;
				
				FactionParser fp = FactionParser.load(room.faction);
				
				if (fp != null)
				{
					room.addFeatures(ran, dfp, fp, influence);
				}	
			}
		}
		sortedRooms.sort();

		int numMinor = sortedRooms.size / 4;

		// Add features
		for (int i = 0; i < sortedRooms.size; i++)
		{
			Pair pair = sortedRooms.get(i);

			if (i < sortedRooms.size - numMinor)
			{
				int influence = pair.dist;
				if (influence > 0) 
				{ 
					float fract = (float)influence / (float)(width+height);
					influence = (int)(fract * 100);
				}

				influence = 100 - influence;

				pair.room.addFeatures(ran, dfp, majorFaction, influence);
			}
			else
			{
				int influence = ran.nextInt(80) + 10;

				pair.room.addFeatures(ran, dfp, FactionParser.load(dfp.getMinorFaction(ran)), influence);
			}
		}

		markRooms();

		//	connectRooms();

		{
			Symbol[][] symbolGrid = new Symbol[width][height];
			for (int x = 0; x < width; x++)
			{
				for (int y = 0; y < height; y++)
				{
					symbolGrid[x][y] = tiles[x][y].symbol;
				}
			}
			ImageUtils.writeSymbolGridToFile(symbolGrid, "afterFeatures.png", 16);
		}
	}

	//----------------------------------------------------------------------
	public static Symbol[][] minimiseGrid(Symbol[][] grid, Symbol wall)
	{
		int width = grid.length;
		int height = grid[0].length;

		int minx = -1;
		int miny = -1;
		int maxx = -1;
		int maxy = -1;

		// find min x
		loop:
			for (int x = 0; x < width; x++)
			{
				for (int y = 0; y < height; y++)
				{
					if (grid[x][y] != wall)
					{
						minx = x-1;
						break loop;
					}
				}
			}

		// find min y
		loop:
			for (int y = 0; y < height; y++)
			{
				for (int x = minx; x < width; x++)
				{
					if (grid[x][y] != wall)
					{
						miny = y-1;
						break loop;
					}
				}
			}

			// find max x
			loop:
				for (int x = width-1; x >= minx; x--)
				{
					for (int y = miny; y < height; y++)
					{
						if (grid[x][y] != wall)
						{
							maxx = x + 2;
							break loop;
						}
					}
				}

			// find max y
			loop:
				for (int y = height-1; y >= miny; y--)
				{
					for (int x = minx; x < maxx; x++)
					{
						if (grid[x][y] != wall)
						{
							maxy = y + 2;
							break loop;
						}
					}
				}

				// minimise room
				int newwidth = maxx - minx;
				int newheight = maxy - miny;

				Symbol[][] newgrid = new Symbol[newwidth][newheight];

				for (int x = 0; x < newwidth; x++)
				{
					for (int y = 0; y < newheight; y++)
					{
						newgrid[x][y] = grid[minx+x][miny+y];
					}
				}

				return newgrid;
	}

	//endregion Public Methods
	//####################################################################//
	//region Private Methods

	//----------------------------------------------------------------------
	protected void partition(int x, int y, int width, int height)
	{		
		int padX = Math.min(ran.nextInt(maxPadding-minPadding)+minPadding, (width-minRoomSize)/2);
		int padY = Math.min(ran.nextInt(maxPadding-minPadding)+minPadding, (height-minRoomSize)/2);

		int padX2 = padX * 2;
		int padY2 = padY * 2;

		// get the room to be placed
		Room room = null;

		// if the predefined rooms array has items, then try to pick one from it
		if (toBePlaced.size > 0)
		{
			// Array of indexes to be tried, stops duplicate work
			Array<Integer> indexes = new Array<Integer>();
			for (int i = 0; i < toBePlaced.size; i++) { indexes.add(i); }

			while (room == null && indexes.size > 0)
			{
				int index = indexes.removeIndex(ran.nextInt(indexes.size));

				Room testRoom = toBePlaced.get(index);

				// Check if the room fits, either at the default rotation or at 90 degrees
				boolean fitsVertical = testRoom.width + padX2 <= width && testRoom.height + padY2 <= height;
				boolean fitsHorizontal = testRoom.height + padX2 <= width && testRoom.width + padY2 <= height;

				if (fitsVertical || fitsHorizontal)
				{
					room = testRoom;
					toBePlaced.removeIndex(index);

					// randomly flip
					if (ran.nextBoolean())
					{
						room.flipVertical();
					}

					if (ran.nextBoolean())
					{
						room.flipHorizontal();
					}

					// if it fits on both directions, randomly pick one
					if (fitsVertical && fitsHorizontal)
					{
						if (ran.nextBoolean())
						{
							room.rotate();
						}
					}
					else if (fitsHorizontal)
					{
						room.rotate();
					}
				}
			}
		}

		// failed to find a suitable predefined room, so create a new one
		if (room == null)
		{
			int roomWidth = Math.min(ran.nextInt(maxRoomSize-minRoomSize)+minRoomSize, width - padX2);
			int roomHeight = Math.min(ran.nextInt(maxRoomSize-minRoomSize)+minRoomSize, height - padY2);

			room = new Room();
			room.width = roomWidth;
			room.height = roomHeight;

			room.generateRoomContents(ran, dfp);
		}

		placedRooms.add(room);

		// pick corner

		// possible sides:
		// 0 1
		// 2 3
		int side = ran.nextInt(4);

		// Position room at side
		if (side == 0)
		{
			room.x = x + padX;
			room.y = y + padY;
		}
		else if (side == 1)
		{
			room.x = ( x + width ) - ( room.width + padX );
			room.y = y + padY;
		}
		else if (side == 2)
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
		if (side == 0)
		{
			// r1
			// 22
			{
				int nx = room.x + room.width + padX;
				int ny = y;
				int nwidth = x + width - nx;
				int nheight = room.height + padY2;

				if (nwidth >= paddedMinRoom && nheight >= paddedMinRoom)
				{
					partition(nx, ny, nwidth, nheight);
				}
			}

			{
				int nx = x;
				int ny = room.y + room.height + padY;
				int nwidth = width;
				int nheight = y + height - ny;

				if (nwidth >= paddedMinRoom && nheight >= paddedMinRoom)
				{
					partition(nx, ny, nwidth, nheight);
				}
			}
		}
		else if (side == 1)
		{
			// 1r
			// 12
			{
				int nx = x;
				int ny = y;
				int nwidth = width - (room.width + padX2);
				int nheight = height;

				if (nwidth >= paddedMinRoom && nheight >= paddedMinRoom)
				{
					partition(nx, ny, nwidth, nheight);
				}
			}

			{
				int nx = room.x - padX;
				int ny = room.y + room.height + padY;
				int nwidth = room.width + padX2;
				int nheight = (y + height) - ny;

				if (nwidth >= paddedMinRoom && nheight >= paddedMinRoom)
				{
					partition(nx, ny, nwidth, nheight);
				}
			}
		}
		else if (side == 2)
		{
			// 12
			// r2
			{
				int nx = x;
				int ny = y;
				int nwidth = room.width + padX2;
				int nheight = height - (room.height + padY2);

				if (nwidth >= paddedMinRoom && nheight >= paddedMinRoom)
				{
					partition(nx, ny, nwidth, nheight);
				}
			}

			{
				int nx = x + room.width + padX2;
				int ny = y;
				int nwidth = (x + width) - nx;
				int nheight = height;

				if (nwidth >= paddedMinRoom && nheight >= paddedMinRoom)
				{
					partition(nx, ny, nwidth, nheight);
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
				int nwidth = width - (room.width + padX2);
				int nheight = (y + height) - ny;

				if (nwidth >= paddedMinRoom && nheight >= paddedMinRoom)
				{
					partition(nx, ny, nwidth, nheight);
				}
			}

			{
				int nx = x ;
				int ny = y;
				int nwidth = width;
				int nheight = height - (room.height + padY2);

				if (nwidth >= paddedMinRoom && nheight >= paddedMinRoom)
				{
					partition(nx, ny, nwidth, nheight);
				}
			}
		}
	}

	//----------------------------------------------------------------------
	protected void connectRooms()
	{
		Array<Pnt> roomPnts = new Array<Pnt>();

		for (Room room : placedRooms)
		{
			for (int x = 0; x < room.width; x++)
			{
				for (int y = 0; y < room.height; y++)
				{
					if (x == 0 || x == room.width-1 || y == 0 || y == room.height-1)
					{
						if (room.roomContents[x][y] == dfp.sharedSymbolMap.get('.'))
						{
							Pnt p = new Pnt(x+room.x, y+room.y);
							roomPnts.add(p);							
						}
					}
				}
			}
		}

		Triangle initialTriangle = new Triangle(
				new Pnt(-10000, -10000),
				new Pnt(10000, -10000),
				new Pnt(0, 10000));
		Triangulation dt = new Triangulation(initialTriangle);

		for (Pnt p : roomPnts)
		{
			dt.delaunayPlace(p);
		}

		Array<Pnt[]> ignoredPaths = new Array<Pnt[]>();
		Array<Pnt[]> addedPaths = new Array<Pnt[]>();
		Array<Pnt[]> paths = new Array<Pnt[]>();

		for (Triangle tri : dt)
		{
			calculatePaths(paths, tri, ignoredPaths, addedPaths);
		}

		for (Pnt[] p : paths)
		{
			int x1 = (int)p[0].coord(0);
			int y1 = (int)p[0].coord(1);
			int x2 = (int)p[1].coord(0);
			int y2 = (int)p[1].coord(1);
			Pathfinder pathFind = new Pathfinder(tiles, x1, y1, x2, y2, false, new HashSet<String>());

			int[][] path = pathFind.getPath();
			if (path[0][0] != x1 || path[0][1] != y1 || path[path.length-1][0] != x2 || path[path.length-1][1] != y2)
			{
				System.out.println("Path failed to find route!!!!");
			}

			carveCorridor(path);
		}
	}

	//----------------------------------------------------------------------
	protected void carveCorridor(int[][] path)
	{		
		for (int[] pos : path)
		{
			GenerationTile t = tiles[pos[0]][pos[1]];	

			if (t.pathfindType == PathfindType.NONE)
			{
				t.pathfindType = PathfindType.CORRIDOR;
				t.symbol = dfp.sharedSymbolMap.get('.');
			}
		}		
	}

	//----------------------------------------------------------------------
	protected void calculatePaths(Array<Pnt[]> paths, Triangle triangle, Array<Pnt[]> ignoredPaths, Array<Pnt[]> addedPaths)
	{
		Pnt[] vertices = triangle.toArray(new Pnt[0]);

		int ignore = 0;
		double dist = 0;

		dist = Math.pow(2, vertices[0].coord(0)-vertices[1].coord(0))+Math.pow(2, vertices[0].coord(1)-vertices[1].coord(1));

		double temp = Math.pow(2, vertices[0].coord(0)-vertices[2].coord(0))+Math.pow(2, vertices[0].coord(1)-vertices[2].coord(1));
		if (dist < temp)
		{
			dist = temp;
			ignore = 1;		
		}

		temp = Math.pow(2, vertices[1].coord(0)-vertices[2].coord(0))+Math.pow(2, vertices[1].coord(1)-vertices[2].coord(1));
		if (dist < temp)
		{
			dist = temp;
			ignore = 2;		
		}

		if (ignore != 0 && !checkIgnored(vertices[0], vertices[1], ignoredPaths) && !checkAdded(vertices[0], vertices[1], addedPaths))
		{
			addPath(vertices[0], vertices[1], paths, ignoredPaths, addedPaths);
		}
		else
		{
			ignoredPaths.add(new Pnt[]{vertices[0], vertices[1]});
		}

		if (ignore != 1 && !checkIgnored(vertices[0], vertices[2], ignoredPaths) && !checkAdded(vertices[0], vertices[2], addedPaths))
		{
			addPath(vertices[0], vertices[2], paths, ignoredPaths, addedPaths);
		}
		else
		{
			ignoredPaths.add(new Pnt[]{vertices[0], vertices[2]});
		}

		if (ignore != 2 && !checkIgnored(vertices[1], vertices[2], ignoredPaths) && !checkAdded(vertices[1], vertices[2], addedPaths))
		{
			addPath(vertices[1], vertices[2], paths, ignoredPaths, addedPaths);
		}
		else
		{
			ignoredPaths.add(new Pnt[]{vertices[1], vertices[2]});
		}
	}

	//----------------------------------------------------------------------
	protected void addPath(Pnt p1, Pnt p2, Array<Pnt[]> paths, Array<Pnt[]> ignoredPaths, Array<Pnt[]> addedPaths)
	{
		if (
				p1.coord(0) < 0 || p1.coord(1) < 0 ||
				p1.coord(0) >= width-1 || p1.coord(1) >= height-1 ||
				p2.coord(0) < 0 || p2.coord(1) < 0 ||
				p2.coord(0) >= width-1 || p2.coord(1) >= height-1
				)
		{
			ignoredPaths.add(new Pnt[]{p1, p2});
		}
		else
		{
			addedPaths.add(new Pnt[]{p1, p2});
			paths.add(new Pnt[]{p1, p2});
		}
	}

	//----------------------------------------------------------------------
	protected boolean checkIgnored(Pnt p1, Pnt p2, Array<Pnt[]> ignoredPaths)
	{
		for (Pnt[] p : ignoredPaths)
		{
			if (p[0].equals(p1) && p[1].equals(p2))
			{
				return true;
			}
			else if (p[0].equals(p2) && p[1].equals(p1))
			{
				return true;
			}
		}
		return false;
	}

	//----------------------------------------------------------------------
	protected boolean checkAdded(Pnt p1, Pnt p2, Array<Pnt[]> addedPaths)
	{
		for (Pnt[] p : addedPaths)
		{
			if (p[0].equals(p1) && p[1].equals(p2))
			{
				return true;
			}
			else if (p[0].equals(p2) && p[1].equals(p1))
			{
				return true;
			}
		}
		return false;
	}

	//----------------------------------------------------------------------
	protected void markRooms()
	{
		for (Room room : placedRooms)
		{			
			for (int x = 0; x < room.width; x++)
			{
				for (int y = 0; y < room.height; y++)
				{
					GenerationTile tile = tiles[room.x+x][room.y+y];
					Symbol symbol = room.roomContents[x][y];

					tile.pathfindType = symbol.isPassable() ? PathfindType.CORRIDOR : PathfindType.WALL ;
					tile.symbol = symbol;
				}
			}
		}
	}

	//endregion Private Methods
	//####################################################################//
	//region Data

	private GenerationTile[][] tiles;
	private Random ran = new Random();

	private int width = 75;
	private int height = 75;

	private int minPadding = 1;
	private int maxPadding = 4;

	private int minRoomSize = 7;
	private int maxRoomSize = 25;

	private int paddedMinRoom = minRoomSize + minPadding*2;

	public Array<Room> toBePlaced = new Array<Room>();

	private Array<Room> placedRooms = new Array<Room>();

	public DungeonFileParser dfp;

	//endregion Data
	//####################################################################//
	//region Classes

	//----------------------------------------------------------------------
	public static class Room
	{
		//----------------------------------------------------------------------
		public int width;
		public int height;

		//----------------------------------------------------------------------
		public int x;
		public int y;

		//----------------------------------------------------------------------
		public Symbol[][] roomContents;
		
		//----------------------------------------------------------------------
		public String faction;

		//----------------------------------------------------------------------
		public void rotate()
		{
			Symbol[][] newContents = new Symbol[height][width];

			for (int x = 0; x < width; x++)
			{
				for (int y = 0; y < height; y++)
				{
					newContents[y][x] = roomContents[x][y];
				}
			}

			roomContents = newContents;

			int temp = height;
			height = width;
			width = temp;
		}

		//----------------------------------------------------------------------
		public void flipVertical()
		{
			for (int x = 0; x < width; x++)
			{
				for (int y = 0; y < height/2; y++)
				{
					Symbol temp = roomContents[x][y];
					roomContents[x][y] = roomContents[x][height-y-1];
					roomContents[x][height-y-1] = temp;
				}
			}
		}

		//----------------------------------------------------------------------
		public void flipHorizontal()
		{
			for (int x = 0; x < width/2; x++)
			{
				for (int y = 0; y < height; y++)
				{
					Symbol temp = roomContents[x][y];
					roomContents[x][y] = roomContents[width-x-1][y];
					roomContents[width-x-1][y] = temp;
				}
			}
		}

		//----------------------------------------------------------------------
		public void generateRoomContents(Random ran, DungeonFileParser dfp)
		{
			roomContents = new Symbol[width][height];

			Symbol floor = dfp.sharedSymbolMap.get('.');
			Symbol wall = dfp.sharedSymbolMap.get('#');

			while (true)
			{
				CellularAutomata.process(roomContents, floor, wall, ran);

				int count = 0;
				// Ensure solid outer wall and count floor
				for (int x = 0; x < width; x++)
				{
					for (int y = 0; y < height; y++)
					{
						if (x == 0 || x == width-1 || y == 0 || y == height-1)
						{
							roomContents[x][y] = wall;
						}

						if (roomContents[x][y] == floor) { count++; }
					}
				}

				if (count > (width*height)/3)
				{
					break;
				}
				else
				{
					System.out.println("Not enough room tiles filled ("+count+" / " + (width*height) + ").");
				}
			}						

			// minimise room size
			roomContents = RecursiveDockGenerator.minimiseGrid(roomContents, wall);			
			width = roomContents.length;
			height = roomContents[0].length;

			// Place corridor connections
			// Sides
			//  1
			// 0 2
			//  3

			int numDoors = (int)(Math.max(0, ran.nextGaussian())*2) + 1;
			for (int i = 0; i < numDoors; i++)
			{
				int doorSide = ran.nextInt(4);

				int x = 0;
				int y = 0;

				if (doorSide == 0)
				{
					x = 0;
					y = 1 + ran.nextInt(height-2);
				}
				else if (doorSide == 1)
				{
					x = 1 + ran.nextInt(width-2);
					y = 0;					
				}
				else if (doorSide == 2)
				{
					x = width-1;
					y = 1 + ran.nextInt(height-2);					
				}
				else if (doorSide == 3)
				{
					x = 1 + ran.nextInt(width-2);
					y = height-1;					
				}

				roomContents[x][y] = floor;

				int[][] path = BresenhamLine.lineNoDiag(width/2, height/2, x, y);
				for (int[] pos : path)
				{
					roomContents[pos[0]][pos[1]] = floor;
				}
			}


		}

		//----------------------------------------------------------------------
		public void addFeatures(Random ran, DungeonFileParser dfp, FactionParser faction, int influence)
		{
			// find all the entrances
			Array<int[]> doorArr = new Array<int[]>();
			for (int x  = 0; x < width; x++)
			{
				for (int y = 0; y < height; y++)
				{
					if (x == 0 || y == 0 || x == width-1 || y == height-1)
					{
						if (roomContents[x][y].isPassable())
						{
							doorArr.add(new int[]{x, y});
						}
					}
				}
			}
			int[][] doors = doorArr.toArray(int[].class);

			// separate floor tiles into lists
			Array<int[]> anyList = new Array<int[]>();
			PriorityQueue<FeatureTile> furthestList = new PriorityQueue<FeatureTile>();
			Array<int[]> wallList = new Array<int[]>();
			Array<int[]> centreList = new Array<int[]>();

			for (int x = 1; x < width-1; x++)
			{
				for (int y = 1; y < height-1; y++)
				{
					if (roomContents[x][y].isPassable() && !roomContents[x][y].hasEnvironmentEntity())
					{
						int[] pos = {x, y};

						anyList.add(pos);

						boolean isWall = false;
						for (Direction d : Direction.values())
						{
							int nx = x + d.GetX();
							int ny = y + d.GetY();

							if (nx >= 0 && nx < width && ny >= 0 && ny < height)
							{
								if (!roomContents[nx][ny].isPassable())
								{
									isWall = true;
									break;
								}
							}
						}

						if (isWall)
						{
							wallList.add(pos);
						}
						else
						{
							centreList.add(pos);
						}

						furthestList.add(new FeatureTile(pos, doors));
					}
				}
			}

			// start placing features

			// Do furthest
			if (furthestList.size() > 0)
			{
				Array<Feature> features = faction.features.get(FeaturePlacementType.FURTHEST);

				for (Feature f : features)
				{
					// Skip if out of range
					if (influence < f.minRange || influence > f.maxRange)
					{
						continue;
					}

					// Skip if no valid tiles left
					if (furthestList.size() == 0)
					{
						break;
					}

					// calculate num features to place
					int numTilesToPlace = f.getNumTilesToPlace(influence, furthestList.size());

					// Place the features
					for (int i = 0; i < numTilesToPlace; i++)
					{
						int[] pos = furthestList.poll().pos;
						anyList.removeValue(pos, true);
						wallList.removeValue(pos, true);
						centreList.removeValue(pos, true);

						roomContents[pos[0]][pos[1]] = f.getAsSymbol(roomContents[pos[0]][pos[1]]);

						if (furthestList.size() == 0) { break; }
					}
				}
			}

			// Do wall		
			if (wallList.size > 0)
			{
				Array<Feature> features = faction.features.get(FeaturePlacementType.WALL);

				for (Feature f : features)
				{
					if (influence < f.minRange || influence > f.maxRange)
					{
						continue;
					}

					if (wallList.size == 0)
					{
						break;
					}

					int numTilesToPlace = f.getNumTilesToPlace(influence, wallList.size);

					for (int i = 0; i < numTilesToPlace; i++)
					{
						int[] pos = wallList.removeIndex(ran.nextInt(wallList.size));
						anyList.removeValue(pos, true);
						centreList.removeValue(pos, true);

						roomContents[pos[0]][pos[1]] = f.getAsSymbol(roomContents[pos[0]][pos[1]]);

						if (wallList.size == 0) { break; }
					}
				}
			}

			// Do centre
			if (centreList.size > 0)
			{
				Array<Feature> features = faction.features.get(FeaturePlacementType.CENTRE);

				for (Feature f : features)
				{
					if (influence < f.minRange || influence > f.maxRange)
					{
						continue;
					}

					if (centreList.size == 0)
					{
						break;
					}

					int numTilesToPlace = f.getNumTilesToPlace(influence, centreList.size);

					for (int i = 0; i < numTilesToPlace; i++)
					{
						int[] pos = centreList.removeIndex(ran.nextInt(centreList.size));
						anyList.removeValue(pos, true);

						roomContents[pos[0]][pos[1]] = f.getAsSymbol(roomContents[pos[0]][pos[1]]);

						if (centreList.size == 0) { break; }
					}
				}
			}

			// Do any
			if (anyList.size > 0)
			{
				Array<Feature> features = faction.features.get(FeaturePlacementType.ANY);

				for (Feature f : features)
				{
					if (influence < f.minRange || influence > f.maxRange)
					{
						continue;
					}

					if (anyList.size == 0)
					{
						break;
					}

					int numTilesToPlace = f.getNumTilesToPlace(influence, anyList.size);


					for (int i = 0; i < numTilesToPlace; i++)
					{
						int[] pos = anyList.removeIndex(ran.nextInt(anyList.size));

						roomContents[pos[0]][pos[1]] = f.getAsSymbol(roomContents[pos[0]][pos[1]]);

						if (anyList.size == 0) { break; }
					}
				}
			}

			// Ensure connectivity
			for (int[] door : doors)
			{
				for (int[] otherDoor : doors)
				{
					if (door == otherDoor) { continue; }

					AStarPathfind pathfind = new AStarPathfind(roomContents, door[0], door[1], otherDoor[0], otherDoor[1], true, new HashSet<String>());
					int[][] path = pathfind.getPath();

					for (int[] point : path)
					{
						roomContents[point[0]][point[1]] = dfp.sharedSymbolMap.get('.');
					}
				}
			}

			// place mobs
			Encounter enc = faction.getEncounter(ran, influence);

			for (String mob : enc.mobs)
			{
				if (anyList.size == 0) { break; }

				int[] pos = anyList.removeIndex(ran.nextInt(anyList.size));

				roomContents[pos[0]][pos[1]] = roomContents[pos[0]][pos[1]].copy();
				roomContents[pos[0]][pos[1]].entityData = mob;
			}
		}

		//----------------------------------------------------------------------
		private class FeatureTile implements Comparable<FeatureTile>
		{
			public int[] pos;
			public int dist;

			public FeatureTile(int[] pos, int[][] doors)
			{
				this.pos = pos;

				int d = 0;
				for (int[] door : doors)
				{
					d += Math.abs(pos[0] - door[0]) + Math.abs(pos[1] - door[1]);
				}

				d /= doors.length;
			}

			@Override
			public int compareTo(FeatureTile o)
			{
				return ((Integer)dist).compareTo(o.dist);
			}

		}
	}

	//----------------------------------------------------------------------
	public enum PathfindType
	{
		NONE,
		WALL,
		CORRIDOR
	}

	//----------------------------------------------------------------------
	public static class GenerationTile implements PathfindingTile
	{
		public Symbol symbol;

		public PathfindType pathfindType = PathfindType.NONE;

		//----------------------------------------------------------------------
		@Override
		public boolean getPassable(HashSet<String> factions)
		{
			return pathfindType != PathfindType.WALL;
		}

		//----------------------------------------------------------------------
		@Override
		public int getInfluence()
		{
			if (pathfindType == PathfindType.CORRIDOR)
			{
				return 0;
			}
			else
			{
				return 100;
			}
		}

		@Override
		public String toString()
		{
			return ""+symbol.character;
		}
	}

	//endregion Classes
	//####################################################################//
}
