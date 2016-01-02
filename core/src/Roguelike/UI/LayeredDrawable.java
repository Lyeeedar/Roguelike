package Roguelike.UI;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Philip on 02-Jan-16.
 */
public class LayeredDrawable implements Drawable
{
	public Array<Drawable> layers = new Array<Drawable>(  );

	public LayeredDrawable( Drawable... drawables )
	{
		layers.addAll( drawables );
	}

	@Override
	public void draw( Batch batch, float x, float y, float width, float height )
	{
		for ( Drawable drawable : layers )
		{
			drawable.draw( batch, x, y, width, height );
		}
	}

	@Override
	public float getLeftWidth()
	{
		return layers.get( 0 ).getLeftWidth();
	}

	@Override
	public void setLeftWidth( float leftWidth )
	{
		for ( Drawable drawable : layers )
		{
			drawable.setLeftWidth( leftWidth );
		}
	}

	@Override
	public float getRightWidth()
	{
		return layers.get( 0 ).getRightWidth();
	}

	@Override
	public void setRightWidth( float rightWidth )
	{
		for ( Drawable drawable : layers )
		{
			drawable.setRightWidth( rightWidth );
		}
	}

	@Override
	public float getTopHeight()
	{
		return layers.get( 0 ).getTopHeight();
	}

	@Override
	public void setTopHeight( float topHeight )
	{
		for ( Drawable drawable : layers )
		{
			drawable.setTopHeight( topHeight );
		}
	}

	@Override
	public float getBottomHeight()
	{
		return layers.get( 0 ).getBottomHeight();
	}

	@Override
	public void setBottomHeight( float bottomHeight )
	{
		for ( Drawable drawable : layers )
		{
			drawable.setBottomHeight( bottomHeight );
		}
	}

	@Override
	public float getMinWidth()
	{
		return layers.get( 0 ).getMinWidth();
	}

	@Override
	public void setMinWidth( float minWidth )
	{
		for ( Drawable drawable : layers )
		{
			drawable.setMinWidth( minWidth );
		}
	}

	@Override
	public float getMinHeight()
	{
		return layers.get( 0 ).getMinHeight();
	}

	@Override
	public void setMinHeight( float minHeight )
	{
		for ( Drawable drawable : layers )
		{
			drawable.setMinHeight( minHeight );
		}
	}
}
