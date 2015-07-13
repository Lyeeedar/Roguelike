/*******************************************************************************
 * Copyright (c) 2013 Philip Collin.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Philip Collin - initial API and implementation
 ******************************************************************************/
package Roguelike.DungeonGeneration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import PaulChew.Pnt;
import PaulChew.Triangle;
import PaulChew.Triangulation;
import Roguelike.AssetManager;
import Roguelike.Entity.Entity;
import Roguelike.Levels.Level;
import Roguelike.Pathfinding.Pathfinder;
import Roguelike.Pathfinding.PathfindingTile;
import Roguelike.Sprite.Sprite;
import Roguelike.Tiles.GameTile;
import Roguelike.Tiles.TileData;

import com.badlogic.gdx.utils.Array;

public class DungeonRoomGenerator 
{	
	public enum TileType
	{
		FLOOR,
		WALL,
		DOOR
	}
	
	private class GenerationTile implements PathfindingTile
	{
		public boolean isRoom = false;
		public TileType tileType = TileType.WALL;
		
		@Override
		public boolean GetPassable(HashSet<String> factions)
		{
			return true;
		}
	}
	
	private class DungeonRoom
	{
		public int width;
		public int height;
		
		public final int x;
		public final int y;
		
		public DungeonRoom(int x, int y)
		{
			this.x = x;
			this.y = y;
		}

		public DungeonRoom(int x, int y, int width, int height)
		{
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}
	}
	
	final Array<DungeonRoom> rooms = new Array<DungeonRoom>();
	final GenerationTile[][] tiles;
	final Random ran = new Random();
	
	final int width;
	final int height;
	
	public DungeonRoomGenerator(int width, int height)
	{
		this.width = width;
		this.height = height;
		
		this.tiles = new GenerationTile[width][height];
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				tiles[x][y] = new GenerationTile();
			}
		}
	}
	
	public Level getLevel()
	{
		DungeonFileParser dfp = DungeonFileParser.load("level1"); 
		
		TileData wallData = dfp.sharedSymbolMap.get('#').tileData;
		TileData floorData = dfp.sharedSymbolMap.get('.').tileData;
		
		GameTile[][] actualTiles = new GameTile[width][height];
		Level level = new Level(actualTiles);
		
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				GenerationTile oldTile = tiles[x][y];

				GameTile newTile = new GameTile
				(
					x, y,
					level,
					oldTile.tileType == TileType.WALL ? wallData : floorData
				);
				
				actualTiles[x][y] = newTile;
			}
		}
		
		for (DungeonRoom room : rooms)
		{
			dfp.placeRoom(room.x, room.y, room.width, room.height, actualTiles);
		}
		
		return level;
	}
	
	public void generate() 
	{	
		partition(0, 0, width, height, rooms);
		
		markRooms();

		connectRooms();	
	}
	
	private int minSize = 5;
	private int maxSize = 20;
	public void partition(int x, int y, int width, int height, Array<DungeonRoom> output)
	{
		if ((width < minSize*3 && height < minSize*3) || (width < maxSize && height < maxSize && ran.nextInt(5) == 0))
		{
			float pw = 0.4f + ran.nextFloat() * 0.3f;
			float ph = 0.4f + ran.nextFloat() * 0.3f;
			
			float ow = ran.nextFloat() * (1.0f - pw);
			float oh = ran.nextFloat() * (1.0f - ph);
			
			int nw = (int)(width*pw);
			int nh = (int)(height*ph);
			
			int now = (int)(width*ow);
			int noh = (int)(height*oh);
			
			output.add(new DungeonRoom(x+now+nw/2, y+noh+nh/2, nw, nh));
		}
		else if (width < minSize*3)
		{
			float split = 0.3f + ran.nextFloat() * 0.4f;
			int splitheight = (int)(height * split);
			
			partition(x, y, width, splitheight, output);
			partition(x, y+splitheight, width, height-splitheight, output);
		}
		else if (height < minSize*3)
		{
			float split = 0.3f + ran.nextFloat() * 0.4f;
			int splitwidth = (int)(width * split);
			
			partition(x, y, splitwidth, height, output);
			partition(x+splitwidth, y, width-splitwidth, height, output);
		}
		else
		{
			boolean vertical = ran.nextBoolean();
			if (vertical)
			{
				float split = 0.3f + ran.nextFloat() * 0.4f;
				int splitwidth = (int)(width * split);
				
				partition(x, y, splitwidth, height, output);
				partition(x+splitwidth, y, width-splitwidth, height, output);
			}
			else
			{
				float split = 0.3f + ran.nextFloat() * 0.4f;
				int splitheight = (int)(height * split);
				
				partition(x, y, width, splitheight, output);
				partition(x, y+splitheight, width, height-splitheight, output);
			}
		}
	}
	
	protected void connectRooms()
	{
		ArrayList<Pnt> roomPnts = new ArrayList<Pnt>();
		
		for (DungeonRoom dr : rooms)
		{
			Pnt p = new Pnt(dr.x, dr.y);
			roomPnts.add(p);
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
		
		ArrayList<Pnt[]> paths = new ArrayList<Pnt[]>();
		
		for (Triangle tri : dt)
		{
			calculatePaths(paths, tri);
		}

		for (Pnt[] p : paths)
		{
			Pathfinder pathFind = new Pathfinder(tiles, (int)p[0].coord(0), (int)p[0].coord(1), (int)p[1].coord(0), (int)p[1].coord(1), false, new HashSet<String>());
			carveCorridor(pathFind.getPath());
		}
	}
	
	protected void carveCorridor(int[][] path)
	{		
		boolean isRoom = tiles[path[0][0]][path[0][1]].isRoom;
		if (!isRoom) 
		{
			System.err.println("Error! Room Linking path did not start in a Room!");
		}
		
		GenerationTile t = null;
		GenerationTile lt = null;
		for (int[] pos : path)
		{
			lt = t;
			t = tiles[pos[0]][pos[1]];
			if (!isRoom && t.isRoom)
			{
				lt.tileType = TileType.DOOR;
				t.tileType = TileType.FLOOR;
			}
			else if (isRoom && !t.isRoom)
			{
				t.tileType = TileType.DOOR;
			}
			else
			{
				t.tileType = TileType.FLOOR;
			}
			isRoom = t.isRoom;
		}
	}
	
	protected void calculatePaths(ArrayList<Pnt[]> paths, Triangle triangle)
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
        
        if (ignore != 0 && checkIgnored(vertices[0], vertices[1]) && !checkAdded(vertices[0], vertices[1]))
        {
        	addPath(vertices[0], vertices[1], paths);
        }
        else
        {
        	ignorePnts.add(new Pnt[]{vertices[0], vertices[1]});
        }
        if (ignore != 1 && checkIgnored(vertices[0], vertices[2]) && !checkAdded(vertices[0], vertices[2]))
        {
        	addPath(vertices[0], vertices[2], paths);
        }
        else
        {
        	ignorePnts.add(new Pnt[]{vertices[0], vertices[2]});
        }
        if (ignore != 2 && checkIgnored(vertices[1], vertices[2]) && !checkAdded(vertices[1], vertices[2]))
        {
        	addPath(vertices[1], vertices[2], paths);
        }
        else
        {
        	ignorePnts.add(new Pnt[]{vertices[1], vertices[2]});
        }
	}
	
    protected void addPath(Pnt p1, Pnt p2, ArrayList<Pnt[]> paths)
    {
    	if (p1.coord(0) < 0 || p2.coord(0) < 0)
    	{
    		ignorePnts.add(new Pnt[]{p1, p2});
    	}
    	else if (p1.coord(1) < 0 || p2.coord(1) < 0)
    	{
    		ignorePnts.add(new Pnt[]{p1, p2});
    	}
    	else if (p1.coord(0) > 1000 || p2.coord(0) > 1000)
    	{
    		ignorePnts.add(new Pnt[]{p1, p2});
    	}
    	else if (p1.coord(1) > 1000 || p2.coord(1) > 1000)
    	{
    		ignorePnts.add(new Pnt[]{p1, p2});
    	}
    	else
    	{
        	addedPnts.add(new Pnt[]{p1, p2});
        	paths.add(new Pnt[]{p1, p2});
    	}
    }
	
	ArrayList<Pnt[]> ignorePnts = new ArrayList<Pnt[]>();
	ArrayList<Pnt[]> addedPnts = new ArrayList<Pnt[]>();
    
    protected boolean checkIgnored(Pnt p1, Pnt p2)
    {
    	for (Pnt[] p : ignorePnts)
    	{
    		if (p[0].equals(p1) && p[1].equals(p2))
    		{
    			return false;
    		}
    		else if (p[0].equals(p2) && p[1].equals(p1))
    		{
    			return false;
    		}
    	}
    	return true;
    }
    
    protected boolean checkAdded(Pnt p1, Pnt p2)
    {
    	for (Pnt[] p : addedPnts)
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

	protected void placeRoomSeed()
	{
		int x = ran.nextInt(width);
		int y = ran.nextInt(height);
		
		boolean collision = false;
		for (DungeonRoom room : rooms)
		{
			if (room.x == x && room.y == y)
			{
				collision = true;
				break;
			}
		}
		
		if (collision)
		{
			placeRoomSeed();
			return;
		}
		
		DungeonRoom room = new DungeonRoom(x, y);
		rooms.add(room);
	}
	
	protected void growRooms()
	{
		for (int i = 0; i < 100; i++)
		{
			for (DungeonRoom room : rooms)
			{
				room.width++;
				if (checkCollision(room)) { room.width--; }
				
				room.height++;
				if (checkCollision(room)) { room.height--; }				
			}
		}
	}
	
	protected void markRooms()
	{
		for (DungeonRoom room : rooms)
		{
			for (int x = -room.width/2; x < room.width/2; x++)
			{
				for (int y = -room.height/2; y < room.height/2; y++)
				{
					tiles[room.x + x][room.y + y].tileType = TileType.FLOOR;
					tiles[room.x + x][room.y + y].isRoom = true;
				}
			}
		}
	}
	
	protected boolean checkCollision(DungeonRoom room)
	{
		if (
				room.x - room.width/2 < 1 ||
				room.x + room.width/2 >= width ||
				room.y - room.height/2 < 1 ||
				room.y + room.height/2 >= height
			)
		{
			return true;
		}
		
		for (DungeonRoom otherRoom : rooms)
		{
			if (otherRoom == room) { continue; }
			
			if (
					Math.abs((room.x-otherRoom.x) + 2) < room.width/2 + otherRoom.width/2 &&
					Math.abs((room.y-otherRoom.y) + 2) < room.height/2 + otherRoom.height/2
					)
			{
				return true;
			}
		}
		
		return false;
	}
}

