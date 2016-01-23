package Roguelike.Tiles;

import Roguelike.Global;
import Roguelike.Entity.Entity;

import com.badlogic.gdx.utils.Pool.Poolable;

public final class Point implements Poolable
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

	public Point set( GameTile tile )
	{
		return set( tile.x, tile.y );
	}

	public Point set( Entity entity )
	{
		return set( entity.tile[0][0].x, entity.tile[0][0].y );
	}

	public Point copy()
	{
		return Global.PointPool.obtain().set( x, y );
	}

	public boolean obtained = false;

	@Override
	public void reset()
	{
		if ( !obtained ) { throw new RuntimeException(); }
		obtained = false;
	}
}
