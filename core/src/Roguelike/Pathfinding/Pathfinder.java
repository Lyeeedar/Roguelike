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
}
