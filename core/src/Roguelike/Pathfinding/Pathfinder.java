package Roguelike.Pathfinding;

import java.util.HashSet;

public class Pathfinder
{
	private int startx;
	private int starty;
	private int endx;
	private int endy;
	private PathfindingTile[][] Grid;
	private boolean canMoveDiagonal;
	private HashSet<String> factions;
	
	public Pathfinder(PathfindingTile[][] grid, int startx, int starty, int endx, int endy, boolean canMoveDiagonal, HashSet<String> factions)
	{
		this.startx = startx;
		this.starty = starty;
		this.endx = endx;
		this.endy = endy;
		this.Grid = grid;
		this.canMoveDiagonal = canMoveDiagonal;
		this.factions = factions;
	}
	
	public int[][] getPath()
	{
		AStarPathfind astar = new AStarPathfind(Grid, startx, starty, endx, endy, canMoveDiagonal, factions);
		int[][] path = astar.getPath();
		
		if (path == null)
		{
			path = BresenhamLine.line(startx, starty, endx, endy, Grid, true, false, factions);
		}
		
		return path;
	}
	
	
	public static class PathfinderTest
	{
		public static class TestTile implements PathfindingTile
		{
			public boolean isPath = false;
			public boolean passable = true;
			
			@Override
			public boolean getPassable(HashSet<String> factions)
			{
				return true;
			}

			@Override
			public int getInfluence()
			{
				return passable ? 0 : 200;
			}
		}
		
		public static void straightTest()
		{
			TestTile[][] grid = new TestTile[10][10];
			for (int x = 0; x < 10; x++)
			{
				for (int y = 0; y < 10; y++)
				{
					grid[x][y] = new TestTile();
				}
			}
			
			// diagonal
			path(grid, 1, 1, 8, 8);			
			path(grid, 1, 8, 8, 1);
			
			// straight
			path(grid, 1, 1, 1, 8);			
			path(grid, 1, 8, 8, 8);
			
			// offset
			path(grid, 1, 1, 2, 8);
		}
		
		public static void wallTest()
		{
			TestTile[][] grid = new TestTile[10][10];
			for (int x = 0; x < 10; x++)
			{
				for (int y = 0; y < 10; y++)
				{
					grid[x][y] = new TestTile();
					
					if (x == 5 && y > 0 && y < 9)
					{
						grid[x][y].passable = false;
					}
				}
			}
			
			// diagonal
			path(grid, 1, 1, 8, 8);			
			path(grid, 1, 8, 8, 1);
			
			// straight
			path(grid, 1, 1, 1, 8);			
			path(grid, 1, 8, 8, 8);
			
			// offset
			path(grid, 1, 1, 2, 8);
		}
		
		private static void path(TestTile[][] grid, int startx, int starty, int endx, int endy)
		{
			AStarPathfind astar = new AStarPathfind(grid, startx, starty, endx, endy, true, new HashSet<String>());
			int[][] path = astar.getPath();
			
			for (int[] step : path)
			{
				grid[step[0]][step[1]].isPath = true;
			}
			
			for (int x = 0; x < 10; x++)
			{
				for (int y = 0; y < 10; y++)
				{
					char c = '.';
					if (grid[x][y].isPath)
					{
						c = 'p';
					}
					else if (!grid[x][y].passable)
					{
						c = '#';
					}
					
					System.out.print(""+c);
				}
				
				System.out.print("\n");
			}
			
			System.out.print("\n");
			
			for (int x = 0; x < 10; x++)
			{
				for (int y = 0; y < 10; y++)
				{
					grid[x][y].isPath = false;
				}
			}
		}
	}
}
