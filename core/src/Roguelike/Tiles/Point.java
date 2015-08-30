package Roguelike.Tiles;

import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.Pools;

public class Point implements Poolable
{
	public int x;
	public int y;

	public Point()
	{

	}

	public Point( int x, int y )
	{
		this.x = x;
		this.y = y;
	}

	public Point set( int x, int y )
	{
		if ( obtained ) { throw new RuntimeException(); }

		obtained = true;

		this.x = x;
		this.y = y;

		return this;
	}

	public Point copy()
	{
		return Pools.obtain( Point.class ).set( x, y );
	}

	private boolean obtained = false;

	@Override
	public void reset()
	{
		if ( !obtained ) { throw new RuntimeException(); }
		obtained = false;
	}
}
