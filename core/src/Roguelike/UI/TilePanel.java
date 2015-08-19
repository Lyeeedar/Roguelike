package Roguelike.UI;

import Roguelike.Screens.GameScreen;
import Roguelike.Sprite.Sprite;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.Array;

public abstract class TilePanel extends Widget
{
	private final TilePanel thisRef = this;

	protected int tileSize;

	protected int viewWidth;
	protected int viewHeight;

	protected int dataWidth;
	protected int dataHeight;

	protected int scrollX;
	protected int scrollY;

	protected final Skin skin;
	protected final Stage stage;
	protected Tooltip tooltip;

	protected Sprite tileBackground;
	protected Sprite tileBorder;

	protected Array<Object> tileData = new Array<Object>();
	protected Object mouseOver;

	protected boolean expandVertically;

	public TilePanel( Skin skin, Stage stage, Sprite tileBackground, Sprite tileBorder, int viewWidth, int viewHeight, int tileSize, boolean expandVertically )
	{
		this.skin = skin;
		this.stage = stage;
		this.tileBackground = tileBackground;
		this.tileBorder = tileBorder;
		this.viewWidth = viewWidth;
		this.viewHeight = viewHeight;
		this.tileSize = tileSize;
		this.expandVertically = expandVertically;

		TilePanelListener listener = new TilePanelListener();

		if ( GameScreen.Instance.inputMultiplexer != null )
		{
			GameScreen.Instance.inputMultiplexer.addProcessor( new GestureDetector( listener ) );
		}

		this.addListener( listener );
		this.setWidth( getPrefWidth() );
	}

	public abstract void populateTileData();

	public abstract Sprite getSpriteForData( Object data );

	public abstract void handleDataClicked( Object data, InputEvent event, float x, float y );

	public abstract Table getToolTipForData( Object data );

	public abstract Color getColourForData( Object data );

	public abstract void onDrawItemBackground( Object data, Batch batch, int x, int y, int width, int height );

	public abstract void onDrawItem( Object data, Batch batch, int x, int y, int width, int height );

	public abstract void onDrawItemForeground( Object data, Batch batch, int x, int y, int width, int height );

	@Override
	public void invalidate()
	{
		super.invalidate();

		if ( expandVertically )
		{
			viewHeight = (int) ( getHeight() / tileSize );
		}
	}

	@Override
	public float getPrefWidth()
	{
		return tileSize * viewWidth;
	}

	@Override
	public float getPrefHeight()
	{
		return tileSize * viewHeight;
	}

	private void validateScroll()
	{
		int scrollableX = Math.max( 0, dataWidth - viewWidth );
		int scrollableY = Math.max( 0, dataHeight - viewHeight );

		scrollX = MathUtils.clamp( scrollX, 0, scrollableX );
		scrollY = MathUtils.clamp( scrollY, 0, scrollableY );
	}

	@Override
	public void draw( Batch batch, float parentAlpha )
	{
		populateTileData();
		validateScroll();

		int xOffset = (int) getX();
		int top = (int) ( getY() + getHeight() ) - tileSize;

		int x = 0;
		int y = 0;

		batch.setColor( Color.DARK_GRAY );
		for ( y = 0; y < viewHeight; y++ )
		{
			for ( x = 0; x < viewWidth; x++ )
			{
				if ( tileBackground != null )
				{
					tileBackground.render( batch, x * tileSize + xOffset, top - y * tileSize, tileSize, tileSize );
				}
				if ( tileBorder != null )
				{
					tileBorder.render( batch, x * tileSize + xOffset, top - y * tileSize, tileSize, tileSize );
				}
			}
		}

		x = 0;
		y = 0;

		int scrollOffset = scrollY * viewWidth + scrollX;

		for ( int i = scrollOffset; i < tileData.size; i++ )
		{
			Object item = tileData.get( i );

			Color baseColour = item != null && item == mouseOver ? Color.WHITE : Color.LIGHT_GRAY;
			Color itemColour = getColourForData( item );
			if ( itemColour != null )
			{
				itemColour = new Color( baseColour ).mul( itemColour );
			}
			else
			{
				itemColour = baseColour;
			}

			batch.setColor( itemColour );
			if ( tileBackground != null )
			{
				tileBackground.render( batch, x * tileSize + xOffset, top - y * tileSize, tileSize, tileSize );
			}
			onDrawItemBackground( item, batch, x * tileSize + xOffset, top - y * tileSize, tileSize, tileSize );

			batch.setColor( Color.WHITE );
			Sprite sprite = getSpriteForData( item );
			if ( sprite != null )
			{
				sprite.render( batch, x * tileSize + xOffset, top - y * tileSize, tileSize, tileSize );
			}
			onDrawItem( item, batch, x * tileSize + xOffset, top - y * tileSize, tileSize, tileSize );

			batch.setColor( itemColour );
			if ( tileBorder != null )
			{
				tileBorder.render( batch, x * tileSize + xOffset, top - y * tileSize, tileSize, tileSize );
			}
			onDrawItemForeground( item, batch, x * tileSize + xOffset, top - y * tileSize, tileSize, tileSize );

			x++;
			if ( x == viewWidth )
			{
				x = 0;
				y++;
				if ( y == viewHeight )
				{
					break;
				}
			}
		}
	}

	public class TilePanelListener extends InputListener implements GestureListener
	{
		boolean longPressed = false;

		boolean dragged = false;
		float dragX;
		float dragY;

		float lastX;
		float lastY;

		private Object pointToItem( float x, float y )
		{
			if ( x < 0 || y < 0 || x > getPrefWidth() || y > getPrefHeight() ) { return null; }

			y = getHeight() - y;

			int xIndex = (int) ( x / tileSize );
			int yIndex = (int) ( y / tileSize );

			if ( xIndex >= viewWidth || yIndex >= viewHeight ) { return null; }

			xIndex += scrollX;
			yIndex += scrollY;

			int index = yIndex * viewWidth + xIndex;
			if ( index >= tileData.size ) { return null; }
			return tileData.get( index );
		}

		private boolean getMouseOverUI( float x, float y )
		{
			if ( x < 0 || y < 0 || x > getWidth() || y > getHeight() )
			{
				return false;
			}
			else
			{
				y = getHeight() - y;

				int xIndex = (int) ( x / tileSize );
				int yIndex = (int) ( y / tileSize );

				if ( xIndex < viewWidth && yIndex < viewHeight )
				{
					return true;
				}
				else
				{
					return false;
				}
			}
		}

		@Override
		public void touchDragged( InputEvent event, float x, float y, int pointer )
		{
			if ( !dragged && ( Math.abs( x - dragX ) > 10 || Math.abs( y - dragY ) > 10 ) )
			{
				dragged = true;

				lastX = x;
				lastY = y;
			}

			if ( dragged )
			{
				int xdiff = (int) ( ( x - lastX ) / tileSize );
				int ydiff = (int) ( ( y - lastY ) / tileSize );

				if ( xdiff != 0 )
				{
					scrollX -= xdiff;
					lastX = x;
				}

				if ( ydiff != 0 )
				{
					scrollY += ydiff;
					lastY = y;
				}
			}
		}

		@Override
		public boolean scrolled( InputEvent event, float x, float y, int amount )
		{
			boolean mouseOverUI = getMouseOverUI( x, y );

			if ( mouseOverUI )
			{
				if ( dataWidth > viewWidth )
				{
					scrollX += amount;
				}
				else
				{
					scrollY += amount;
				}
			}

			GameScreen.Instance.mouseOverUI = mouseOverUI;
			return mouseOverUI;
		}

		@Override
		public boolean mouseMoved( InputEvent event, float x, float y )
		{
			if ( tooltip != null )
			{
				tooltip.setVisible( false );
				tooltip.remove();
				tooltip = null;
			}

			Object item = pointToItem( x, y );

			if ( item != null )
			{
				Table table = getToolTipForData( item );

				if ( table != null )
				{
					tooltip = new Tooltip( table, skin, stage );
					tooltip.show( event, x, y );
				}
			}

			mouseOver = item;

			boolean mouseOverUI = getMouseOverUI( x, y );
			GameScreen.Instance.mouseOverUI = mouseOverUI;

			if ( mouseOverUI )
			{
				stage.setScrollFocus( thisRef );
			}

			return mouseOverUI;
		}

		@Override
		public boolean touchDown( InputEvent event, float x, float y, int pointer, int button )
		{
			if ( tooltip != null )
			{
				tooltip.setVisible( false );
				tooltip.remove();
				tooltip = null;
			}
			GameScreen.Instance.clearContextMenu();

			boolean mouseOverUI = getMouseOverUI( x, y );
			GameScreen.Instance.mouseOverUI = mouseOverUI;

			dragged = false;
			dragX = x;
			dragY = y;

			return mouseOverUI;
		}

		@Override
		public void touchUp( InputEvent event, float x, float y, int pointer, int button )
		{
			if ( tooltip != null )
			{
				tooltip.setVisible( false );
				tooltip.remove();
				tooltip = null;
			}
			GameScreen.Instance.clearContextMenu();

			Object item = pointToItem( x, y );

			if ( !longPressed && !dragged && item != null )
			{
				handleDataClicked( item, event, x, y );
			}

			boolean mouseOverUI = getMouseOverUI( x, y );
			GameScreen.Instance.mouseOverUI = mouseOverUI;

			// return mouseOverUI;

			dragged = false;
		}

		@Override
		public void enter( InputEvent event, float x, float y, int pointer, Actor toActor )
		{
			boolean mouseOverUI = getMouseOverUI( x, y );
			GameScreen.Instance.mouseOverUI = mouseOverUI;

			if ( mouseOverUI )
			{
				stage.setScrollFocus( thisRef );
			}
		}

		@Override
		public void exit( InputEvent event, float x, float y, int pointer, Actor toActor )
		{
			mouseOver = null;

			if ( tooltip != null )
			{
				tooltip.setVisible( false );
				tooltip.remove();
				tooltip = null;
			}

			GameScreen.Instance.mouseOverUI = false;
		}

		@Override
		public boolean touchDown( float x, float y, int pointer, int button )
		{
			longPressed = false;
			return false;
		}

		@Override
		public boolean tap( float x, float y, int count, int button )
		{
			return false;
		}

		@Override
		public boolean longPress( float x, float y )
		{
			longPressed = true;

			if ( tooltip != null )
			{
				tooltip.setVisible( false );
				tooltip.remove();
				tooltip = null;
			}

			Object item = pointToItem( x, y );

			if ( item != null )
			{
				Table table = getToolTipForData( item );

				if ( table != null )
				{
					tooltip = new Tooltip( table, skin, stage );
					tooltip.show( x, y );
				}
			}

			return true;
		}

		@Override
		public boolean fling( float velocityX, float velocityY, int button )
		{
			return false;
		}

		@Override
		public boolean pan( float x, float y, float deltaX, float deltaY )
		{
			return false;
		}

		@Override
		public boolean panStop( float x, float y, int pointer, int button )
		{
			return false;
		}

		@Override
		public boolean zoom( float initialDistance, float distance )
		{
			return false;
		}

		@Override
		public boolean pinch( Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2 )
		{
			return false;
		}
	}
}
