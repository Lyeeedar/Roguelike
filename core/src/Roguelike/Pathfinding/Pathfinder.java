package Roguelike.Pathfinding;

public class Pathfinder
{
	int startx;
	int starty;
	int endx;
	int endy;
	PathfindingTile[][] Grid;
	boolean canMoveDiagonal;
	
	public Pathfinder(PathfindingTile[][] grid, int startx, int starty, int endx, int endy, boolean canMoveDiagonal)
	{
		this.startx = startx;
		this.starty = starty;
		this.endx = endx;
		this.endy = endy;
		this.Grid = grid;
		this.canMoveDiagonal = canMoveDiagonal;
	}
	
	public int[][] getPath()
	{
		AStarPathfind astar = new AStarPathfind(Grid, startx, starty, endx, endy, canMoveDiagonal);
		int[][] path = astar.getPath();
		
		if (path == null)
		{
			path = BresenhamLine.line(startx, starty, endx, endy, Grid, true, false);
		}
		
		return path;
	}
}
