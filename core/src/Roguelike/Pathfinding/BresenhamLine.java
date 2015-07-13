package Roguelike.Pathfinding;

import java.util.HashSet;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class BresenhamLine
{
	public static int[][] line (int x, int y, int x2, int y2, PathfindingTile[][] Grid, boolean checkPassable, boolean toInfinity, HashSet<String> factions) 
	{
		x = MathUtils.clamp(x, 0, Grid.length-1);
		x2 = MathUtils.clamp(x2, 0, Grid.length-1);
		y = MathUtils.clamp(y, 0, Grid[0].length-1);
		y2 = MathUtils.clamp(y2, 0, Grid[0].length-1);
		
		if (x == x2 && y == y2) { return null; }

	    int w = x2 - x ;
	    int h = y2 - y ;
	    
	    int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0 ;
	    
	    if (w < 0) dx1 = -1 ; else if (w > 0) dx1 = 1 ;
	    if (h < 0) dy1 = -1 ; else if (h > 0) dy1 = 1 ;
	    if (w < 0) dx2 = -1 ; else if (w > 0) dx2 = 1 ;
	    
	    int longest = Math.abs(w) ;
	    int shortest = Math.abs(h) ;
	    
	    if ( !(longest > shortest) ) 	    	
	    {
	        longest = Math.abs(h) ;
	        shortest = Math.abs(w) ;
	        
	        if (h < 0) dy2 = -1 ; else if (h > 0) dy2 = 1 ;
	        dx2 = 0 ;            
	    }
	    
	    int numerator = longest >> 1 ;
	    
	    int dist = longest;
	    if (toInfinity)
	    {
	    	dist = Integer.MAX_VALUE;
	    }
	    
	    Array<int[]> path = new Array<int[]>();
	    
	    for (int i = 0; i <= dist; i++) 
	    {
	        path.add(new int[]{x, y});
	        
	        numerator += shortest ;
	        if ( !(numerator < longest) ) 
	        {
	            numerator -= longest ;
	            x += dx1 ;
	            y += dy1 ;
	        } 
	        else 
	        {
	            x += dx2 ;
	            y += dy2 ;
	        }
	        
	        if (
	        	x < 0 ||
	        	y < 0 ||
	        	x >= Grid.length-1 ||
	        	y >= Grid[0].length-1 ||
	        	(checkPassable && !Grid[x][y].GetPassable(factions))
	        	) 
	        { 
	        	break; 
	        }
	    }
	    
	    return path.toArray(int[].class);
	}
}
