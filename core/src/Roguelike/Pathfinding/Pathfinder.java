package Roguelike.Pathfinding;

import java.util.HashSet;

import Roguelike.Global.Passability;
import Roguelike.Tiles.Point;

import com.badlogic.gdx.utils.Array;

public class Pathfinder
{
	private int startx;
	private int starty;
	private int endx;
	private int endy;
	private PathfindingTile[][] Grid;
	private boolean canMoveDiagonal;
	private HashSet<String> factions;
	
	public Pathfinder(PathfindingTile[][] grid, int startx, int starty, int endx, int endy, boolean canMoveDiagonal)
	{
		this.startx = startx;
		this.starty = starty;
		this.endx = endx;
		this.endy = endy;
		this.Grid = grid;
		this.canMoveDiagonal = canMoveDiagonal;
		this.factions = factions;
	}
	
	public Array<Point> getPath(Array<Passability> travelType)
	{
		AStarPathfind astar = new AStarPathfind(Grid, startx, starty, endx, endy, canMoveDiagonal, false, travelType);
		Array<Point> path = astar.getPath();
		
		if (path == null)
		{
			path = BresenhamLine.line(startx, starty, endx, endy, Grid, true, Integer.MAX_VALUE, travelType);
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
			public boolean getPassable(Array<Passability> travelType)
			{
				return true;
			}

			@Override
			public int getInfluence()
			{
				return passable ? 0 : 1000;
			}
		}
		
		public static void runTests()
		{
			bucketTest();
			straightTest();
			wallTest();
			irregularTest();
			
		}
		
		private static void straightTest()
		{
			String[] sgrid = {
					"..........",
					"..........",
					"..........",
					"..........",
					"..........",
					"..........",
					"..........",
					"..........",
					"..........",
					".........."
			};
			
			runTest(sgrid);
		}
		
		private static void wallTest()
		{
			String[] sgrid = {
					"..........",
					"..........",
					"..........",
					"..........",
					".########.",
					"..........",
					"..........",
					"..........",
					"..........",
					".........."
			};
			
			runTest(sgrid);
		}
		
		private static void irregularTest()
		{
			String[] sgrid = {
					".......###",
					"..........",
					"#####.....",
					"..........",
					".########.",
					"..........",
					"#..##.....",
					".....#####",
					"..........",
					".........."
			};
			
			runTest(sgrid);
		}
		
		private static void bucketTest()
		{
			String[] sgrid = {
					"..........",
					"..........",
					".#......#.",
					".#......#.",
					".########.",
					".#......#.",
					".#......#.",
					"##......#.",
					"..........",
					".........."
			};
			
			runTest(sgrid);
		}
		
		private static void runTest(String[] grid)
		{
			int width = grid[0].length();
			int height = grid.length;
			
			TestTile[][] testgrid = new TestTile[height][width];
			for (int x = 0; x < width; x++)
			{
				for (int y = 0; y < height; y++)
				{
					testgrid[y][x] = new TestTile();
					
					if (grid[y].charAt(x) == '#')
					{
						testgrid[y][x].passable = false;
					}
				}
			}
			
			// diagonal
			path(testgrid, 1, 1, 8, 8);			
			path(testgrid, 1, 8, 8, 1);

			// straight
			path(testgrid, 1, 1, 1, 8);			
			path(testgrid, 1, 8, 8, 8);

			// offset
			path(testgrid, 1, 1, 2, 8);
		}
		
		private static void path(TestTile[][] grid, int startx, int starty, int endx, int endy)
		{
			AStarPathfind astar = new AStarPathfind(grid, startx, starty, endx, endy, false, true, new Array<Passability>());
			Array<Point> path = astar.getPath();
			
			for (Point step : path)
			{
				grid[step.x][step.y].isPath = true;
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
