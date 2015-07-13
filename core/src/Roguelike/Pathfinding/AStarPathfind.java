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
			addNodeToOpenList(current.x + offset[0], current.y + offset[1], current.distance);
		}
		
		if (canMoveDiagonal)
		{
			for (int[] offset : DiagonalOffsets)
			{
				addNodeToOpenList(current.x + offset[0], current.y + offset[1], current.distance);
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
	
	private void addNodeToOpenList(int x, int y, int distance)
	{
		if (
			x < 0 ||
			y < 0 ||
			x >= width-1 ||
			y >= height-1
			)
		{
			return;
		}
		
		if (!isStart(x, y) && !isEnd(x, y) && !grid[x][y].GetPassable(factions)) { return; }
		
		if (nodes[x][y] == null)
		{
			nodes[x][y] = new Node(x, y);
		}
		else
		{
			if (nodes[x][y].distance <= distance+1)
			{
				return;
			}
		}

		int heuristic = (int) 
				Math.sqrt(
				Math.pow( Math.abs(x-endx), 2) +
				Math.pow( Math.abs(y-endy), 2));
		
		Node node = nodes[x][y];
		node.setup(heuristic, distance+1);
		
		if (!openList.contains(node))
		{
			openList.add(node);
		}
		else
		{
			openList.remove(node);
			openList.add(node);
		}
	}
	
	public int[][] getPath()
	{
		nodes = new Node[width][height];
		
		addNodeToOpenList(startx, starty, -1);
				
		while(!isEnd(currentx, currenty) && openList.size() > 0)
		{
			path();
		}
		
		if (nodes[endx][endy] == null)
		{			
			return null;
		}
		else
		{
			int length = nodes[endx][endy].distance+1;
			int[][] path = new int[length][2];
			
			path[length-1][0] = endx;
			path[length-1][1] = endy;

			int cx = endx;
			int cy = endy;
			
			for (int i = length-1; i > 0; i--)
			{
				Node n = null;
				
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
							nodes[nx][ny].distance == i-1
							)
						{
							n = nodes[nx][ny];
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
						nodes[nx][ny].distance == i-1
						)
					{
						n = nodes[nx][ny];
					}
				}
				
				path[i-1][0] = n.x;
				path[i-1][1] = n.y;
				
				cx = n.x;
				cy = n.y;
			}
			
			return path;
		}
	}
		
	private class Node implements Comparable<Node>
	{
		int x;
		int y;
		int cost;
		int heuristic;
		int distance;

		public Node(int x, int y)
		{
			this.x = x;
			this.y = y;
		}
		
		public void setup(int heuristic, int distance)
		{
			this.heuristic = heuristic;
			this.distance = distance;
			this.cost = (heuristic*2) + distance;
		}

		@Override
		public int compareTo(Node arg0)
		{
			return Integer.compare(cost, arg0.cost);
		}
	}

}
