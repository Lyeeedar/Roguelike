package Roguelike.Tiles;

import com.badlogic.gdx.utils.Pools;

public class Point
{
	public int x;
	public int y;
	
	public Point()
	{
		
	}
	
	public Point set(int x, int y)
	{
		this.x = x;
		this.y = y;
		
		return this;
	}
	
	public Point copy()
	{
		return Pools.obtain(Point.class).set(x, y);
	}
}
