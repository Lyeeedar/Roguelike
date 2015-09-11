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

import java.util.PriorityQueue;

import Roguelike.Global.Passability;
import Roguelike.Tiles.Point;
import Roguelike.Util.EnumBitflag;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;

public class AStarPathfind
{
	private static final int[][] NormalOffsets = { { -1, 0 }, { 0, -1 }, { +1, 0 }, { 0, +1 } };

	private static final int[][] DiagonalOffsets = { { -1, -1 }, { -1, +1 }, { +1, -1 }, { +1, +1 } };

	private final PathfindingTile[][] grid;
	private final int width;
	private final int height;
	private final boolean canMoveDiagonal;
	private final int actorSize;
	private final boolean findOptimal;
	private final EnumBitflag<Passability> travelType;
	private final Object self;

	private final int startx;
	private final int starty;
	private final int endx;
	private final int endy;
	private int currentx;
	private int currenty;
	private Node[][] nodes;

	public boolean debug = false;

	private PriorityQueue<Node> openList = new PriorityQueue<Node>();

	public AStarPathfind( PathfindingTile[][] grid, int startx, int starty, int endx, int endy, boolean canMoveDiagonal, boolean findOptimal, int actorSize, EnumBitflag<Passability> travelType, Object self )
	{
		this.grid = grid;
		this.width = grid.length;
		this.height = grid[0].length;
		this.canMoveDiagonal = canMoveDiagonal;
		this.actorSize = actorSize;
		this.findOptimal = findOptimal;
		this.travelType = travelType;
		this.self = self;

		this.startx = startx;
		this.starty = starty;
		this.currentx = startx;
		this.currenty = starty;
		this.endx = endx;
		this.endy = endy;
	}

	private void path()
	{
		Node current = openList.poll();

		currentx = current.x;
		currenty = current.y;

		if ( isEnd( currentx, currenty ) ) { return; }

		for ( int[] offset : NormalOffsets )
		{
			addNodeToOpenList( current.x + offset[0], current.y + offset[1], current );
		}

		if ( canMoveDiagonal )
		{
			for ( int[] offset : DiagonalOffsets )
			{
				addNodeToOpenList( current.x + offset[0], current.y + offset[1], current );
			}
		}

		current.processed = true;
	}

	private boolean isStart( int x, int y )
	{
		return x == startx && y == starty;
	}

	private boolean isEnd( int x, int y )
	{
		return x == endx && y == endy;
	}

	private void addNodeToOpenList( int x, int y, Node parent )
	{
		if ( !isStart( x, y ) && !isEnd( x, y ) )
		{
			for ( int ix = 0; ix < actorSize; ix++ )
			{
				for ( int iy = 0; iy < actorSize; iy++ )
				{
					if ( isColliding( x + ix, y + iy ) ) { return; }
				}
			}
		}

		int heuristic = Math.abs( x - endx ) + Math.abs( y - endy );
		int cost = heuristic + ( parent != null ? parent.cost : 0 );

		cost += grid[x][y].getInfluence();

		// 3 possible conditions

		Node node = nodes[x][y];

		// not added to open list yet, so add it
		if ( node == null )
		{
			node = new Node( x, y );
			node.cost = cost;
			node.parent = parent;
			openList.add( node );

			nodes[x][y] = node;
		}

		// not yet processed, if lower cost update the values and reposition in
		// list
		else if ( !node.processed )
		{
			if ( cost < node.cost )
			{
				node.cost = cost;
				node.parent = parent;

				openList.remove( node );
				openList.add( node );
			}
		}

		// processed, if lower cost then update parent and cost
		else
		{
			if ( cost < node.cost )
			{
				node.cost = cost;
				node.parent = parent;
			}
		}
	}

	public boolean isColliding( int x, int y )
	{
		if ( x < 0 || y < 0 || x >= width || y >= height || grid[x][y] == null || !grid[x][y].getPassable( travelType, self ) ) { return true; }
		return false;
	}

	public Array<Point> getPath()
	{
		nodes = new Node[width][height];

		addNodeToOpenList( startx, starty, null );

		while ( ( findOptimal || !isEnd( currentx, currenty ) ) && openList.size() > 0 )
		{
			path();
		}

		if ( nodes[endx][endy] == null )
		{
			return null;
		}
		else
		{
			Array<Point> path = new Array<Point>();

			path.add( Pools.obtain( Point.class ).set( endx, endy ) );

			Node node = nodes[endx][endy];

			while ( node != null )
			{
				path.add( Pools.obtain( Point.class ).set( node.x, node.y ) );

				node = node.parent;
			}

			path.reverse();

			return path;
		}
	}

	private class Node implements Comparable<Node>
	{
		public int x;
		public int y;
		public int cost;
		public Node parent;

		public boolean processed = false;

		public Node( int x, int y )
		{
			this.x = x;
			this.y = y;
		}

		@Override
		public int compareTo( Node arg0 )
		{
			return Integer.compare( cost, arg0.cost );
		}

		@Override
		public String toString()
		{
			return "" + cost;
		}
	}

}
