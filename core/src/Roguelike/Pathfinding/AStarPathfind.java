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
package Roguelike.Pathfinding;

import java.util.HashSet;
import java.util.PriorityQueue;

import com.badlogic.gdx.utils.Array;

public class AStarPathfind
{
	private static final int[][] NormalOffsets =
	{
		{ -1, 0 },
		{ 0, -1 },
		{ +1, 0 },
		{ 0, +1 }
	};
	
	private static final int[][] DiagonalOffsets =
	{
		{ -1, -1 },
		{ -1, +1 },
		{ +1, -1 },
		{ +1, +1 }
	};
	
	private PathfindingTile[][] grid;
	private int width;
	private int height;
	private boolean canMoveDiagonal;
	
	private int startx;
	private int starty;
	private int endx;
	private int endy;
	private int currentx;
	private int currenty;
	private Node[][] nodes;
	
	public boolean debug = false;
	
	private PriorityQueue<Node> openList = new PriorityQueue<Node>();
	
	private HashSet<String> factions;

	public AStarPathfind(PathfindingTile[][] grid, int startx, int starty, int endx, int endy, boolean canMoveDiagonal, HashSet<String> factions)
	{
		this.grid = grid;
		this.width = grid.length;
		this.height = grid[0].length;
		this.canMoveDiagonal = canMoveDiagonal;
		
		this.startx = startx;
		this.starty = starty;
		this.currentx = startx;
		this.currenty = starty;
		this.endx = endx;
		this.endy = endy;
		
		this.factions = factions;
	}
	
	private void path()
	{
		Node current = openList.poll();
		
		currentx = current.x;
		currenty = current.y;
		
		if (isEnd(currentx, currenty)) { return; }
		
		for (int[] offset : NormalOffsets)
		{
			addNodeToOpenList(current.x + offset[0], current.y + offset[1], current.distance, current.cost);
		}
		
		if (canMoveDiagonal)
		{
			for (int[] offset : DiagonalOffsets)
			{
				addNodeToOpenList(current.x + offset[0], current.y + offset[1], current.distance, current.cost);
			}
		}
	}
	
	private boolean isStart(int x, int y)
	{
		return x == startx && y == starty;
	}
	
	private boolean isEnd(int x, int y)
	{
		return x == endx && y == endy;
	}
	
	private void addNodeToOpenList(int x, int y, int distance, int prevCost)
	{
		if (
			x < 0 ||
			y < 0 ||
			x >= width ||
			y >= height
			)
		{
			return;
		}
		
		if (!isStart(x, y) && !isEnd(x, y) && !grid[x][y].getPassable(factions)) { return; }
		
		int heuristic = 0;
		
//		if (canMoveDiagonal)
//		{
//			heuristic = (int) Math.max(Math.abs(x-endx), Math.abs(y-endy));
//		}
//		else
//		{
//			heuristic = Math.abs(x-endx) + Math.abs(y-endy);
//		}
		
		heuristic = Math.abs(x-endx) + Math.abs(y-endy);
				
		int cost = heuristic + (distance+1) + grid[x][y].getInfluence() + prevCost;
		
		if (nodes[x][y] == null)
		{
			nodes[x][y] = new Node(x, y);
		}
		else
		{
			if (nodes[x][y].cost <= cost)
			{
				return;
			}
		}
		
		nodes[x][y].cost = cost;
		nodes[x][y].distance = distance+1;
		
		openList.remove(nodes[x][y]);
		openList.add(nodes[x][y]);
	}
	
	public int[][] getPath()
	{
		nodes = new Node[width][height];
		
		addNodeToOpenList(startx, starty, -1, 0);
				
		while(!isEnd(currentx, currenty) && openList.size() > 0)
		{
			path();
			
			if (debug)
			{
				for (int x = 0; x < width; x++)
				{
					for (int y = 0; y < height; y++)
					{
						if (x == startx && y == starty)
						{
							System.out.print("S,");
						}
						else if (x == endx && y == endy)
						{
							System.out.print("E,");
						}
						else if (nodes[x][y] == null)
						{
							System.out.print(grid[x][y].getPassable(factions) ? ".," : "#,");
						}
						else
						{
							System.out.print(nodes[x][y].distance+",");
						}
						
					}
					System.out.print("\n");
				}
				
				System.out.print("\n");
				System.out.print("\n");
			}
		}
		
		if (nodes[endx][endy] == null)
		{			
			return null;
		}
		else
		{
			Array<int[]> path = new Array<int[]>(nodes[endx][endy].distance+1);

			path.add(new int[]{endx, endy});
			
			int cx = endx;
			int cy = endy;
			
			while (cx != startx || cy != starty)
			{
				Node n = null;
				int minCost = Integer.MAX_VALUE;
				
				if (canMoveDiagonal)
				{
					for (int[] offset : DiagonalOffsets)
					{
						int nx = cx + offset[0];
						int ny = cy + offset[1];
						
						if (
							nx >= 0 &&
							nx <= width-1 &&
							ny >= 0 &&
							ny <= height-1 &&
							nodes[nx][ny] != null &&
							nodes[nx][ny].cost < minCost
							)
						{
							n = nodes[nx][ny];
							minCost = n.cost;
						}
					}
				}
				
				for (int[] offset : NormalOffsets)
				{
					int nx = cx + offset[0];
					int ny = cy + offset[1];
					
					if (
						nx >= 0 &&
						nx <= width-1 &&
						ny >= 0 &&
						ny <= height-1 &&
						nodes[nx][ny] != null &&
						nodes[nx][ny].cost < minCost
						)
					{
						n = nodes[nx][ny];
						minCost = n.cost;
					}
				}
				
				path.add(new int[]{n.x, n.y});
				
				cx = n.x;
				cy = n.y;
			}
			
			path.reverse();
			
			return path.toArray(int[].class);
		}
	}
		
	private class Node implements Comparable<Node>
	{
		public int x;
		public int y;
		public int cost;
		public int distance;

		public Node(int x, int y)
		{
			this.x = x;
			this.y = y;
		}

		@Override
		public int compareTo(Node arg0)
		{
			return Integer.compare(cost, arg0.cost);
		}
		
		@Override
		public String toString()
		{
			return ""+cost;
		}
	}

}
