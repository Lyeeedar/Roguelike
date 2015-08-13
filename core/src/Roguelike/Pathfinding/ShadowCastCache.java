package Roguelike.Pathfinding;

import Roguelike.Global.Passability;
import Roguelike.Tiles.GameTile;
import Roguelike.Tiles.Point;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;

public class ShadowCastCache
{
	private final Array<Passability> LightPassability;
	
	public ShadowCastCache(Array<Passability> LightPassability)
	{
		this.LightPassability = LightPassability;
	}
	
	public ShadowCastCache()
	{
		LightPassability = new Array<Passability>(new Passability[]{Passability.LIGHT});
	}
	
	public ShadowCastCache copy()
	{
		ShadowCastCache cache = new ShadowCastCache(LightPassability);
		cache.lastrange = lastrange;
		cache.lastx = lastx;
		cache.lasty = lasty;
		cache.opaqueTiles.addAll(opaqueTiles);
		cache.shadowCastOutput.addAll(shadowCastOutput);
		
		return cache;
	}
	
	private int lastrange;
	private int lastx;
	private int lasty;
	private Array<Point> opaqueTiles = new Array<Point>();
	private Array<Point> shadowCastOutput = new Array<Point>();
	
	public Array<Point> getShadowCast(GameTile[][] grid, int x, int y, int range)
	{
		boolean recalculate = false;
				
		if (x != lastx || y != lasty)
		{
			recalculate = true;
		}
		else if (range != lastrange)
		{
			recalculate = true;
		}
		else
		{
			for (Point pos : opaqueTiles)
			{
				GameTile tile = grid[pos.x][pos.y];				
				if (tile.getPassable(LightPassability))
				{
					recalculate = true; // something has moved
					break;
				}
			}
		}
		
		if (recalculate)
		{
			Pools.freeAll(shadowCastOutput);
			shadowCastOutput.clear();
			
			ShadowCaster shadow = new ShadowCaster(grid, range);
			shadow.ComputeFOV(x, y, shadowCastOutput);
			
			// build list of opaque
			opaqueTiles.clear();
			for (Point pos : shadowCastOutput)
			{
				GameTile tile = grid[pos.x][pos.y];				
				if (!tile.getPassable(LightPassability))
				{
					opaqueTiles.add(pos);
				}
			}
			lastx = x;
			lasty = y;
			lastrange = range;
		}
		
		return shadowCastOutput;
	}
}
