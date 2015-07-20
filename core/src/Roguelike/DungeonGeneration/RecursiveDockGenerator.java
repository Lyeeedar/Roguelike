package Roguelike.DungeonGeneration;

import java.util.HashSet;
import java.util.Random;

import PaulChew.Pnt;
import PaulChew.Triangle;
import PaulChew.Triangulation;
import Roguelike.AssetManager;
import Roguelike.DungeonGeneration.DungeonFileParser.DFPRoom;
import Roguelike.DungeonGeneration.DungeonFileParser.Symbol;
import Roguelike.DungeonGeneration.EncounterParser.Encounter;
import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Entity.GameEntity;
import Roguelike.Levels.Level;
import Roguelike.Pathfinding.AStarPathfind;
import Roguelike.Pathfinding.BresenhamLine;
import Roguelike.Pathfinding.Pathfinder;
import Roguelike.Pathfinding.PathfindingTile;
import Roguelike.Sprite.Sprite;
import Roguelike.Tiles.GameTile;
import Roguelike.Tiles.TileData;

import com.badlogic.gdx.graphics.Color;
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
		GameTile[][] actualTiles = new GameTile[width][height];
		Level level = new Level(actualTiles);
		level.Ambient = dfp.ambient;
		
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				GenerationTile oldTile = tiles[x][y];

				GameTile newTile = new GameTile
				(
					x, y,
					level,
					oldTile.symbol.getAsTileData()
				);
				
				if (oldTile.symbol.isDoor())
				{
					newTile.addEnvironmentEntity(EnvironmentEntity.CreateDoor());
				}
				else if (oldTile.symbol.isTransition())
				{
					newTile.addEnvironmentEntity(oldTile.symbol.getAsTransition(dfp.sharedSymbolMap));
				}
				
				if (oldTile.symbol.entityData != null)
				{
					newTile.addObject(GameEntity.load(oldTile.symbol.entityData.getText()));
				}
				
				newTile.metaValue = oldTile.symbol.metaValue;
				
				actualTiles[x][y] = newTile;
				
				System.out.print(oldTile.symbol.character);
			}
			System.out.print("\n");
		}
		
		// place enemies
		EncounterParser encounter = new EncounterParser("Fungi");
		Color col = new Color(ran.nextFloat()*0.3f + 0.7f, ran.nextFloat()*0.3f + 0.7f, ran.nextFloat()*0.3f + 0.7f, 1.0f);
		
		// pick centerpoint
		Room center = placedRooms.get(ran.nextInt(placedRooms.size));
		
		int maxdist = 0;
		
		{ int dist = height - (center.y+center.height/2); if (dist > maxdist) { maxdist = dist; } }
		{ int dist = width - (center.x+center.width/2); if (dist > maxdist) { maxdist = dist; } }
		{ int dist = center.y+center.height/2; if (dist > maxdist) { maxdist = dist; } }
		{ int dist = center.x+center.width/2; if (dist > maxdist) { maxdist = dist; } }
		
		for (Room room : placedRooms)
		{
			int dist = Math.max(Math.abs(room.x-center.x), Math.abs(room.y-center.y));
			
			float alpha = (float)dist / (float)maxdist;
			int index = (int)(encounter.encounters.length * alpha);
			
			Encounter enc = encounter.encounters[index];
			
			Array<GameTile> freeTiles = new Array<GameTile>();
			
			for (int x = 0; x < room.width; x++)
			{
				for (int y = 0; y < room.height; y++)
				{
					if (actualTiles[room.x+x][room.y+y].getPassable(new HashSet<String>()))
					{
						freeTiles.add(actualTiles[room.x+x][room.y+y]);
					}
				}
			}
			
			for (String enemy : enc.mobs)
			{
				GameTile tile = freeTiles.removeIndex(ran.nextInt(freeTiles.size));
				GameEntity entity = GameEntity.load(enemy);
				entity.Sprite.colour = col;
				
				tile.addObject(entity);
			}
			
			room.fill(ran, dfp, AssetManager.loadSprite("Objects/Ground0", 16), alpha, actualTiles);
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
					
					tile.pathfindType = symbol.isDoor() || symbol.isPassable() ? PathfindType.CORRIDOR : PathfindType.WALL ;
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
	
	private DungeonFileParser dfp;
		
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
			
			// minimize room size
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
					if (roomContents[x][y] == floor)
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
					if (roomContents[x][y] == floor)
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
					if (roomContents[x][y] == floor)
					{
						maxx = x + 1;
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
					if (roomContents[x][y] == floor)
					{
						maxy = y + 1;
						break loop;
					}
				}
			}
			
			// minimise room
			int newwidth = maxx - minx;
			int newheight = maxy - miny;
			
			Symbol[][] newgrid = new Symbol[newwidth][newheight];
			
			for (x = 0; x < newwidth; x++)
			{
				for (y = 0; y < newheight; y++)
				{
					newgrid[x][y] = roomContents[minx+x][miny+y];
				}
			}
			
			width = newwidth;
			height = newheight;
			roomContents = newgrid;
			
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
		public void fill(Random ran, DungeonFileParser dfp, Sprite sprite, float alpha, GameTile[][] grid)
		{
			Symbol floor = dfp.sharedSymbolMap.get('.');
			
			Array<int[]> clear = new Array<int[]>();
						
			for (int x = 0; x < width; x++)
			{
				for (int y = 0; y < height; y++)
				{
					if (roomContents[x][y] == floor) { clear.add(new int[]{x+this.x, y+this.y}); }
				}
			}
			
			int coverage = (int)((float)clear.size * alpha);
			
			while (coverage > 0)
			{
				int[] pos = clear.removeIndex(ran.nextInt(clear.size));
				grid[pos[0]][pos[1]].detail = sprite;
				
				coverage--;
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
