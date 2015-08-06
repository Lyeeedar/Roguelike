package Roguelike.DungeonGeneration;

import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Random;

import PaulChew.Pnt;
import PaulChew.Triangle;
import PaulChew.Triangulation;
import Roguelike.Global;
import Roguelike.Global.Direction;
import Roguelike.Global.Passability;
import Roguelike.DungeonGeneration.DungeonFileParser.CorridorFeature.PlacementMode;
import Roguelike.DungeonGeneration.DungeonFileParser.CorridorStyle.PathStyle;
import Roguelike.DungeonGeneration.DungeonFileParser.DFPRoom;
import Roguelike.DungeonGeneration.DungeonFileParser.RoomGeneratorType;
import Roguelike.DungeonGeneration.FactionParser.Encounter;
import Roguelike.DungeonGeneration.FactionParser.Feature;
import Roguelike.DungeonGeneration.FactionParser.FeaturePlacementType;
import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Entity.GameEntity;
import Roguelike.Items.Recipe;
import Roguelike.Levels.Level;
import Roguelike.Pathfinding.AStarPathfind;
import Roguelike.Pathfinding.BresenhamLine;
import Roguelike.Pathfinding.PathfindingTile;
import Roguelike.Tiles.GameTile;
import Roguelike.Util.ImageUtils;

import com.badlogic.gdx.utils.Array;

public class RecursiveDockGenerator
{
	//####################################################################//
	//region Constructor

	//----------------------------------------------------------------------
	public RecursiveDockGenerator(String level, int depth, long seed, boolean createDynamics)
	{
		dfp = DungeonFileParser.load(level+"/"+level);
		this.depth = depth;
		this.fileName = level;
		this.seed = seed;
		this.createDynamics = createDynamics;
		
		minPadding = (dfp.corridorStyle.minWidth/2)+1;
		maxPadding += minPadding;
		paddedMinRoom = minRoomSize + minPadding*2;
		
		ran = new Random(seed);
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
		level.bgmName = dfp.BGM;
		level.ambientSounds.addAll(dfp.ambientSounds);
		
		level.depth = depth;
		level.fileName = fileName;
		level.seed = seed;
		level.requiredRooms = additionalRooms;

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
					EnvironmentEntity entity = symbol.getEnvironmentEntity();
					
					if (createDynamics || !entity.canTakeDamage)
					{		
						if (entity.attachToWall)
						{
							Direction location = Direction.CENTER;
							
							if (symbol.attachLocation != null)
							{
								location = symbol.attachLocation;
							}
							else
							{
								// get direction
								HashSet<Direction> validDirections = new HashSet<Direction>();
								for (Direction dir : Direction.values())
								{
									boolean passable = symbolGrid[x+dir.GetX()][y+dir.GetY()] == dfp.sharedSymbolMap.get('#');
									if (!passable) { validDirections.add(dir); }
								}
								
								if (validDirections.size() > 0)
								{
									// look for direction with full surround
									for (Direction dir : Direction.values())
									{
										boolean acwvalid = validDirections.contains(dir.GetAnticlockwise());
										boolean valid = validDirections.contains(dir);
										boolean cwvalid = validDirections.contains(dir.GetClockwise());
										
										if (acwvalid && valid && cwvalid)
										{
											location = dir;
											break;
										}
									}
									
									// else pick random
									if (location == Direction.CENTER)
									{
										location = validDirections.toArray(new Direction[validDirections.size()])[ran.nextInt(validDirections.size())];
									}
								}
							}
							
							entity.location = location;
							entity.sprite.rotation = location.GetAngle();
						}
						
						newTile.addEnvironmentEntity(entity);
					}
				}

				if (createDynamics && symbol.hasGameEntity())
				{
					GameEntity e = symbol.getGameEntity();
					newTile.addGameEntity(e);
					
					for (int i = 0; i < 10; i++)
					{
						e.inventory.addItem(Recipe.generateMaterial(100));
					}
				}

				newTile.metaValue = symbol.metaValue;

				actualTiles[x][y] = newTile;

				//System.out.print(symbol.character);
			}
			//System.out.print("\n");
		}
		
		level.depth = depth;
		
		return level;
	}

	//----------------------------------------------------------------------
	public void generate() 
	{
		for (DFPRoom r : additionalRooms)
		{
			Room room = new Room();
			r.fillRoom(room, ran, dfp);
			toBePlaced.add(room);
		}
		
		for (DFPRoom r : dfp.getRequiredRooms(depth))
		{
			Room room = new Room();
			r.fillRoom(room, ran, dfp);
			toBePlaced.add(room);
		}

		if (dfp.optionalRooms.size > 0)
		{
			DFPRoom r = dfp.optionalRooms.get(ran.nextInt(dfp.optionalRooms.size));
			Room room = new Room();
			r.fillRoom(room, ran, dfp);
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
					
					if (dfp.corridorStyle.pathStyle == PathStyle.STRAIGHT)
					{
						tiles[x][y].influence = width+height;
					}
					else if (dfp.corridorStyle.pathStyle == PathStyle.WANDERING)
					{
						tiles[x][y].influence = ran.nextInt((width+height)/2)+(width+height)/2;
					}

					if (x == 0 || y == 0 || x == width-1 || y == height-1)
					{						
						tiles[x][y].passable = false;
					}
					else
					{
						tiles[x][y].passable = true;
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
		
		for (Room room : placedRooms)
		{
			room.findDoors(ran, dfp);
		}

		markRooms();

		connectRooms();

		if (DEBUG_OUTPUT)
		{
			Symbol[][] symbolGrid = new Symbol[width][height];
			for (int x = 0; x < width; x++)
			{
				for (int y = 0; y < height; y++)
				{
					symbolGrid[x][y] = tiles[x][y].symbol;
				}
			}
			ImageUtils.writeSymbolGridToFile(symbolGrid, "beforeFeatures.png", DEBUG_SIZE);
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
					room.addFeatures(ran, dfp, fp, influence, createDynamics);
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

				pair.room.addFeatures(ran, dfp, majorFaction, influence, createDynamics);
			}
			else
			{
				int influence = ran.nextInt(80) + 10;

				pair.room.addFeatures(ran, dfp, FactionParser.load(dfp.getMinorFaction(ran)), influence, createDynamics);
			}
		}

		markRooms();

		//	connectRooms();

		if (DEBUG_OUTPUT)
		{
			Symbol[][] symbolGrid = new Symbol[width][height];
			for (int x = 0; x < width; x++)
			{
				for (int y = 0; y < height; y++)
				{
					symbolGrid[x][y] = tiles[x][y].symbol;
				}
			}
			ImageUtils.writeSymbolGridToFile(symbolGrid, "afterFeatures.png", DEBUG_SIZE);
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
			for (RoomDoor door : room.doors)
			{
				int x = door.pos[0]+room.x;
				int y = door.pos[1]+room.y;
				
				if (door.side == Direction.WEST)
				{
					x -= dfp.corridorStyle.minWidth - 1;
				}
				else if (door.side == Direction.NORTH)
				{
					y -= dfp.corridorStyle.minWidth - 1;
				}
				
				Pnt p = new Pnt(x, y);
				roomPnts.add(p);
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
		
		Array<Triangle> tris = new Array<Triangle>();
		for (Triangle tri : dt)
		{
			tris.add(tri);
		}
		tris.sort(new Comparator<Triangle>()
		{

			@Override
			public int compare(Triangle arg0, Triangle arg1)
			{
				return arg0.compareTo(arg1);
		}});
		
		for (Triangle tri : tris)
		{
			calculatePaths(paths, tri, ignoredPaths, addedPaths);
		}
		
		for (Pnt[] p : paths)
		{
			int x1 = (int)p[0].coord(0);
			int y1 = (int)p[0].coord(1);
			int x2 = (int)p[1].coord(0);
			int y2 = (int)p[1].coord(1);
						
			AStarPathfind pathFind = new AStarPathfind(tiles, x1, y1, x2, y2, false, true, dfp.corridorStyle.minWidth, GeneratorPassability);
			int[][] path = pathFind.getPath();

			carveCorridor(path);
		}
	}

	//----------------------------------------------------------------------
	protected void carveCorridor(int[][] path)
	{	
		int centralCount = 0;
		int sideCount = 0;
		boolean placementAlternator = true;
		
		int width = dfp.corridorStyle.minWidth;
		
		for (int i = 0; i < path.length; i++)
		{
			int[] pos = path[i];
			
			GenerationTile t = tiles[pos[0]][pos[1]];	
			t.isCorridor = true;
			
			for (int x = 0; x < width; x++)
			{
				for (int y = 0; y < width; y++)
				{
					t = tiles[pos[0]+x][pos[1]+y];	

					if (t.symbol == dfp.sharedSymbolMap.get('#'))
					{
						t.symbol = dfp.sharedSymbolMap.get('.');
					}
					
					// Wipe out all features not placed by this path
					if (t.placerHashCode != path.hashCode())
					{
						t.symbol.environmentData = null;
						t.symbol.environmentEntityObject = null;
					}
					
					// Wipe out all features in the central square
					if (x > 0 && x < width-1 && y > 0 && y < width-1)
					{
						t.symbol.environmentData = null;
						t.symbol.environmentEntityObject = null;
					}
				}
			}
			
			if (dfp.corridorStyle.centralConstant != null)
			{
				t = tiles[pos[0]+width/2][pos[1]+width/2];
				t.symbol = dfp.corridorStyle.centralConstant.getAsSymbol(t.symbol);
				t.placerHashCode = path.hashCode();
			}
			
			if (dfp.corridorStyle.centralRecurring != null)
			{
				centralCount++;
				
				if (centralCount == dfp.corridorStyle.centralRecurring.interval)
				{
					t = tiles[pos[0]+width/2][pos[1]+width/2];
					t.symbol = dfp.corridorStyle.centralRecurring.getAsSymbol(t.symbol);
					t.placerHashCode = path.hashCode();
					
					centralCount = 0;
				}
			}
			
			if (dfp.corridorStyle.sideRecurring != null)
			{
				sideCount++;
				
				if (sideCount == dfp.corridorStyle.sideRecurring.interval && i > 0)
				{
					boolean placeTop = dfp.corridorStyle.sideRecurring.placementMode == PlacementMode.BOTH ||
							dfp.corridorStyle.sideRecurring.placementMode == PlacementMode.TOP ||
							(dfp.corridorStyle.sideRecurring.placementMode == PlacementMode.ALTERNATE && placementAlternator);
					
					boolean placeBottom = dfp.corridorStyle.sideRecurring.placementMode == PlacementMode.BOTH ||
							dfp.corridorStyle.sideRecurring.placementMode == PlacementMode.BOTTOM ||
							(dfp.corridorStyle.sideRecurring.placementMode == PlacementMode.ALTERNATE && !placementAlternator);
					
					Symbol wall = dfp.sharedSymbolMap.get('#');
					if (path[i-1][0] != pos[0])
					{
						if (placeTop && tiles[pos[0]+width/2][pos[1]-1].symbol == wall)
						{
							t = tiles[pos[0]+width/2][pos[1]];
							t.symbol = dfp.corridorStyle.sideRecurring.getAsSymbol(t.symbol);
							t.symbol.attachLocation = Direction.NORTH;
							t.placerHashCode = path.hashCode();
						}
						
						if (placeBottom && tiles[pos[0]+width/2][pos[1]+width].symbol == wall)
						{
							t = tiles[pos[0]+width/2][pos[1]+width-1];
							t.symbol = dfp.corridorStyle.sideRecurring.getAsSymbol(t.symbol);
							t.symbol.attachLocation = Direction.SOUTH;
							t.placerHashCode = path.hashCode();
						}
					}
					else
					{
						if (placeTop && tiles[pos[0]-1][pos[1]+width/2].symbol == wall)
						{
							t = tiles[pos[0]][pos[1]+width/2];
							t.symbol = dfp.corridorStyle.sideRecurring.getAsSymbol(t.symbol);
							t.symbol.attachLocation = Direction.EAST;
							t.placerHashCode = path.hashCode();
						}
						
						if (placeBottom && tiles[pos[0]+width][pos[1]+width/2].symbol == wall)
						{
							t = tiles[pos[0]+width-1][pos[1]+width/2];
							t.symbol = dfp.corridorStyle.sideRecurring.getAsSymbol(t.symbol);
							t.symbol.attachLocation = Direction.WEST;
							t.placerHashCode = path.hashCode();
						}
					}
					
					sideCount = 0;
					placementAlternator = !placementAlternator;
				}
			}
			
			if (dfp.corridorStyle.maxWidth != dfp.corridorStyle.minWidth)
			{
				int targetWidth = i > path.length-3 ? dfp.corridorStyle.minWidth : dfp.corridorStyle.minWidth + ran.nextInt(dfp.corridorStyle.maxWidth - dfp.corridorStyle.minWidth);
				
				int total = width + targetWidth;
				
				width = total / 2;
				
				System.out.println(width);
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

					tile.passable = symbol.isPassable(GeneratorPassability);
					tile.symbol = symbol;
					tile.isRoom = true;
				}
			}
		}
	}

	//endregion Private Methods
	//####################################################################//
	//region Data
	
	//----------------------------------------------------------------------
	public static final Array<Passability> GeneratorPassability = new Array<Passability>(new Passability[]{Passability.WALK});
	
	public int depth;
	public long seed;
	public String fileName;
	public boolean createDynamics;
	
	private static final boolean DEBUG_OUTPUT = false;
	private static final int DEBUG_SIZE = 16;

	private GenerationTile[][] tiles;
	public Random ran;

	private int width = 50;
	private int height = 50;

	private int minPadding = 1;
	private int maxPadding = 3;

	private int minRoomSize = 7;
	private int maxRoomSize = 25;

	private int paddedMinRoom;

	public Array<DFPRoom> additionalRooms = new Array<DFPRoom>();
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
		public Array<RoomDoor> doors = new Array<RoomDoor>();

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
		public void generateRoomContents(Random ran, DungeonFileParser dfp, Symbol floor, Symbol wall, RoomGeneratorType type)
		{
			roomContents = new Symbol[width][height];

			for (int i = 0; i < 20; i++)
			{
				if (type == RoomGeneratorType.OVERLAPPINGRECTS)
				{
					OverlappingRects.process(roomContents, floor, wall, ran);
				}
				else if (type == RoomGeneratorType.CELLULARAUTOMATA)
				{
					CellularAutomata.process(roomContents, floor, wall, ran);
				}
				else if (type == RoomGeneratorType.STARBURST)
				{
					Starburst.process(roomContents, floor, wall, ran);
				}
				else
				{
					for (int x = 0; x < width; x++)
					{
						for (int y = 0; y < height; y++)
						{
							roomContents[x][y] = floor;
						}
					}
				}

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
					//System.out.println("Not enough room tiles filled ("+count+" / " + (width*height) + ").");
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

			int numDoors = (int)(Math.max(0, ran.nextGaussian())*3) + 1;
			for (int i = 0; i < numDoors; i++)
			{
				int doorSide = ran.nextInt(4);

				int x = 0;
				int y = 0;
				
				if (doorSide == 0)
				{
					x = 0;
					y = 1 + ran.nextInt(height-(1+dfp.corridorStyle.minWidth));
					
					for (int c = 0; c < dfp.corridorStyle.minWidth; c++)
					{
						roomContents[x][y+c] = floor;
					}
				}
				else if (doorSide == 1)
				{
					x = 1 + ran.nextInt(width-(1+dfp.corridorStyle.minWidth));
					y = 0;
					
					for (int c = 0; c < dfp.corridorStyle.minWidth; c++)
					{
						roomContents[x+c][y] = floor;
					}
				}
				else if (doorSide == 2)
				{
					x = width-1;
					y = 1 + ran.nextInt(height-(1+dfp.corridorStyle.minWidth));	
					
					for (int c = 0; c < dfp.corridorStyle.minWidth; c++)
					{
						roomContents[x][y+c] = floor;
					}
				}
				else if (doorSide == 3)
				{
					x = 1 + ran.nextInt(width-(1+dfp.corridorStyle.minWidth));
					y = height-1;
					
					for (int c = 0; c < dfp.corridorStyle.minWidth; c++)
					{
						roomContents[x+c][y] = floor;
					}
				}

				int[][] path = BresenhamLine.lineNoDiag(width/2, height/2, x, y);
				for (int[] pos : path)
				{
					for (int ix = 0; ix < dfp.corridorStyle.minWidth; ix++)
					{
						for (int iy = 0; iy < dfp.corridorStyle.minWidth; iy++)
						{
							int nx = pos[0]+ix;
							int ny = pos[1]+iy;
							
							if (nx < width && ny < height)
							{
								roomContents[nx][ny] = floor;
							}
						}
					}
				}
				
			}
		}
		
		//----------------------------------------------------------------------
		public void generateRoomContents(Random ran, DungeonFileParser dfp)
		{
			Symbol floor = dfp.sharedSymbolMap.get('.');
			Symbol wall = dfp.sharedSymbolMap.get('#');
			
			RoomGeneratorType type = dfp.getRoomGenerator(ran);
			
			generateRoomContents(ran, dfp, floor, wall, type);
		}

		//----------------------------------------------------------------------
		public void addFeatures(Random ran, DungeonFileParser dfp, FactionParser faction, int influence, boolean createDynamics)
		{
			Symbol[][] roomCopy = new Symbol[width][height];
			for (int x  = 0; x < width; x++)
			{
				for (int y = 0; y < height; y++)
				{
					roomCopy[x][y] = roomContents[x][y];
				}
			}

			// build the any list
			Array<int[]> validList = new Array<int[]>();
			for (int x = 1; x < width-1; x++)
			{
				for (int y = 1; y < height-1; y++)
				{
					if (roomContents[x][y].isPassable(GeneratorPassability))
					{
						int[] pos = {x, y};
						validList.add(pos);
					}
				}
			}

			// start placing features

			// Do furthest
			{
				Array<Feature> features = faction.features.get(FeaturePlacementType.FURTHEST);

				for (Feature f : features)
				{
					// Skip if out of range
					if (influence < f.minRange || influence > f.maxRange)
					{
						continue;
					}
					
					// Build list
					PriorityQueue<FeatureTile> furthestList = new PriorityQueue<FeatureTile>();
					for (int[] tile : validList)
					{
						int x = tile[0];
						int y = tile[1];
						
						if (roomContents[x][y].getTileData().canFeature && roomContents[x][y].isPassable(GeneratorPassability) && (f.environmentData == null || !roomContents[x][y].hasEnvironmentEntity()))
						{
							furthestList.add(new FeatureTile(tile, doors));
						}
					}

					// Skip if no valid tiles
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

						roomContents[pos[0]][pos[1]] = f.getAsSymbol(roomContents[pos[0]][pos[1]]);

						if (furthestList.size() == 0) { break; }
					}
				}
			}

			// Do wall		
			{
				Array<Feature> features = faction.features.get(FeaturePlacementType.WALL);

				for (Feature f : features)
				{
					if (influence < f.minRange || influence > f.maxRange)
					{
						continue;
					}

					// Build list
					Array<int[]> wallList = new Array<int[]>();
					for (int[] tile : validList)
					{
						int x = tile[0];
						int y = tile[1];
						
						if (roomContents[x][y].getTileData().canFeature && roomContents[x][y].isPassable(GeneratorPassability) && (f.environmentData == null || !roomContents[x][y].hasEnvironmentEntity()))
						{
							boolean isWall = false;
							for (Direction d : Direction.values())
							{
								int nx = x + d.GetX();
								int ny = y + d.GetY();

								if (nx >= 0 && nx < width && ny >= 0 && ny < height)
								{
									if (!roomContents[nx][ny].isPassable(GeneratorPassability))
									{
										isWall = true;
										break;
									}
								}
							}

							if (isWall)
							{
								wallList.add(tile);
							}
						}
					}
					
					if (wallList.size == 0)
					{
						break;
					}

					int numTilesToPlace = f.getNumTilesToPlace(influence, wallList.size);

					for (int i = 0; i < numTilesToPlace; i++)
					{
						int[] pos = wallList.removeIndex(ran.nextInt(wallList.size));

						roomContents[pos[0]][pos[1]] = f.getAsSymbol(roomContents[pos[0]][pos[1]]);

						if (wallList.size == 0) { break; }
					}
				}
			}

			// Do centre
			{
				Array<Feature> features = faction.features.get(FeaturePlacementType.CENTRE);

				for (Feature f : features)
				{
					if (influence < f.minRange || influence > f.maxRange)
					{
						continue;
					}
					
					// Build list
					Array<int[]> centreList = new Array<int[]>();
					for (int[] tile : validList)
					{
						int x = tile[0];
						int y = tile[1];
						
						if (roomContents[x][y].getTileData().canFeature && roomContents[x][y].isPassable(GeneratorPassability) && (f.environmentData == null || !roomContents[x][y].hasEnvironmentEntity()))
						{
							boolean isWall = false;
							for (Direction d : Direction.values())
							{
								int nx = x + d.GetX();
								int ny = y + d.GetY();

								if (nx >= 0 && nx < width && ny >= 0 && ny < height)
								{
									if (!roomContents[nx][ny].isPassable(GeneratorPassability))
									{
										isWall = true;
										break;
									}
								}
							}

							if (!isWall)
							{
								centreList.add(tile);
							}
						}
					}

					if (centreList.size == 0)
					{
						break;
					}

					int numTilesToPlace = f.getNumTilesToPlace(influence, centreList.size);

					for (int i = 0; i < numTilesToPlace; i++)
					{
						int[] pos = centreList.removeIndex(ran.nextInt(centreList.size));

						roomContents[pos[0]][pos[1]] = f.getAsSymbol(roomContents[pos[0]][pos[1]]);

						if (centreList.size == 0) { break; }
					}
				}
			}

			// Do any
			{
				Array<Feature> features = faction.features.get(FeaturePlacementType.ANY);

				for (Feature f : features)
				{
					if (influence < f.minRange || influence > f.maxRange)
					{
						continue;
					}
					
					Array<int[]> anyList = new Array<int[]>();
					for (int[] tile : validList)
					{
						int x = tile[0];
						int y = tile[1];
						
						if (roomContents[x][y].getTileData().canFeature && roomContents[x][y].isPassable(GeneratorPassability) && (f.environmentData == null || !roomContents[x][y].hasEnvironmentEntity()))
						{
							anyList.add(tile);
						}
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
			for (int i = 0; i < doors.size; i++)
			{
				RoomDoor door = doors.get(i);
				for (int ii = 0; ii < doors.size; ii++)
				{
					RoomDoor otherDoor = doors.get(ii);
					if (door == otherDoor) { continue; }

					AStarPathfind pathfind = new AStarPathfind(roomContents, door.pos[0], door.pos[1], otherDoor.pos[0], otherDoor.pos[1], true, false, GeneratorPassability);
					int[][] path = pathfind.getPath();

					for (int[] point : path)
					{
						Symbol s = roomContents[point[0]][point[1]];
						
						if (!s.isPassable(GeneratorPassability))
						{
							roomContents[point[0]][point[1]].tileData = roomCopy[point[0]][point[1]].tileData;
						}
						
						if (s.hasEnvironmentEntity() && !s.getEnvironmentEntityPassable(GeneratorPassability))
						{
							roomContents[point[0]][point[1]].environmentData = roomCopy[point[0]][point[1]].environmentData;
							roomContents[point[0]][point[1]].environmentEntityObject = null;
						}
					}
				}
			}
			
			// Do spawn
			{
				// get valid spawn tiles
				Array<int[]> spawnList = new Array<int[]>();
				for (int[] tile : validList)
				{
					int x = tile[0];
					int y = tile[1];
					
					if (roomContents[x][y].getTileData().canSpawn && roomContents[x][y].isPassable(GeneratorPassability) && !roomContents[x][y].hasGameEntity())
					{
						spawnList.add(tile);
					}
				}

				// place mobs
				Encounter enc = faction.getEncounter(ran, influence);

				for (String mob : enc.getMobsToPlace(influence, spawnList.size))
				{
					if (spawnList.size == 0) { break; }

					int[] pos = spawnList.removeIndex(ran.nextInt(spawnList.size));

					roomContents[pos[0]][pos[1]] = roomContents[pos[0]][pos[1]].copy();
					roomContents[pos[0]][pos[1]].entityData = mob;
				}
			}			
		}
		
		//----------------------------------------------------------------------
		private void addDoor(int pos, int space, Direction dir, Random ran, DungeonFileParser dfp)
		{			
			if (space >= dfp.corridorStyle.minWidth)
			{
				int offset = space > dfp.corridorStyle.minWidth ? ran.nextInt(space - dfp.corridorStyle.minWidth) : 0;
				
				if (dir == Direction.WEST)
				{
					doors.add(new RoomDoor(Direction.WEST, new int[]{0, pos+offset}));
				}
				else if (dir == Direction.EAST)
				{
					doors.add(new RoomDoor(Direction.EAST, new int[]{width-1, pos+offset}));
				}
				else if (dir == Direction.NORTH)
				{
					doors.add(new RoomDoor(Direction.NORTH, new int[]{pos+offset, 0}));
				}
				else if (dir == Direction.SOUTH)
				{
					doors.add(new RoomDoor(Direction.SOUTH, new int[]{pos+offset, height-1}));
				}
			}
		}
		
		//----------------------------------------------------------------------
		private void processSide(Direction dir, Random ran, DungeonFileParser dfp)
		{
			int range = (dir == Direction.WEST || dir == Direction.EAST) ? height : width;
			
			int blockStart = -1;
			for (int pos = 0; pos < range; pos++)
			{
				int x = 0;
				int y = 0;
				
				if (dir == Direction.WEST)
				{
					x = 0;
					y = pos;
				}
				else if (dir == Direction.EAST)
				{
					x = width-1;
					y = pos;
				}
				else if (dir == Direction.NORTH)
				{
					x = pos;
					y = 0;
				}
				else
				{
					x = pos;
					y = height-1;
				}
				
				if (blockStart >= 0)
				{
					if (!roomContents[x][y].isPassable(GeneratorPassability))
					{						
						addDoor(blockStart, pos - blockStart, dir, ran, dfp);						
						blockStart = -1;
					}
				}
				else
				{
					if (roomContents[x][y].isPassable(GeneratorPassability)) { blockStart = pos; }
				}
			}
			
			if (blockStart >= 0)
			{
				int pos = range - 1;
				addDoor(blockStart, pos - blockStart, dir, ran, dfp);
			}
		}

		//----------------------------------------------------------------------
		public void findDoors(Random ran, DungeonFileParser dfp)
		{			
			// Sides
			//  1
			// 0 2
			//  3
			
			// Side 0
			processSide(Direction.WEST, ran, dfp);

			// Side 2
			processSide(Direction.EAST, ran, dfp);
			
			// Side 1
			processSide(Direction.NORTH, ran, dfp);
			
			// Side 3
			processSide(Direction.SOUTH, ran, dfp);
		}
		
		//----------------------------------------------------------------------
		private class FeatureTile implements Comparable<FeatureTile>
		{
			public int[] pos;
			public int dist;

			public FeatureTile(int[] pos, Array<RoomDoor> doors)
			{
				this.pos = pos;

				int d = 0;
				for (RoomDoor door : doors)
				{
					d += Math.abs(pos[0] - door.pos[0]) + Math.abs(pos[1] - door.pos[1]);
				}

				d /= doors.size;
				
				dist = d;
			}

			@Override
			public int compareTo(FeatureTile o)
			{
				return ((Integer)dist).compareTo(o.dist);
			}

		}
	}

	//----------------------------------------------------------------------
	public static class RoomDoor
	{
		public final Direction side;
		public final int[] pos;
		
		public RoomDoor(Direction side, int[] pos)
		{
			this.side = side;
			this.pos = pos;
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
		public int influence;
		public long placerHashCode;
		
		public boolean passable;
		public boolean isCorridor = false;
		public boolean isRoom = false;
		
		//----------------------------------------------------------------------
		@Override
		public boolean getPassable(Array<Passability> travelType)
		{
			return passable;
		}

		//----------------------------------------------------------------------
		@Override
		public int getInfluence()
		{
			if (isCorridor)
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
			return ""+symbol.character;
		}
	}

	//endregion Classes
	//####################################################################//
}
