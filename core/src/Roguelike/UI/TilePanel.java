package Roguelike.UI;

import java.util.Iterator;

import Roguelike.Screens.GameScreen;
import Roguelike.Sprite.Sprite;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
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
	protected int tileSize;
	
	protected int viewWidth;
	protected int viewHeight;
	
	protected final Skin skin;
	protected final Stage stage;
	protected Tooltip tooltip;
	
	protected Sprite tileBackground;
	protected Sprite tileBorder;
	
	protected Array<Object> tileData = new Array<Object>();
	protected Object mouseOver;
	
	protected boolean expandVertically;
	
	public TilePanel(Skin skin, Stage stage, Sprite tileBackground, Sprite tileBorder, int viewWidth, int viewHeight, int tileSize, boolean expandVertically)
	{
		this.skin = skin;
		this.stage = stage;
		this.tileBackground = tileBackground;
		this.tileBorder = tileBorder;
		this.viewWidth = viewWidth;
		this.viewHeight = viewHeight;
		this.tileSize = tileSize;
		this.expandVertically = expandVertically;
		
		this.addListener(new TilePanelListener());
		this.setWidth(getPrefWidth());
	}
	
	public abstract void populateTileData();
	public abstract Sprite getSpriteForData(Object data);
	public abstract void handleDataClicked(Object data, InputEvent event, float x, float y);
	public abstract Table getToolTipForData(Object data);
	public abstract Color getColourForData(Object data);
	
	public abstract void onDrawItemBackground(Object data, Batch batch, int x, int y, int width, int height);
	public abstract void onDrawItem(Object data, Batch batch, int x, int y, int width, int height);
	public abstract void onDrawItemForeground(Object data, Batch batch, int x, int y, int width, int height);
	
	
	@Override
	public void invalidate () 
	{
		super.invalidate();
		
		if (expandVertically)
		{
			viewHeight = (int) (getHeight() / tileSize);
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
	
	@Override
	public void draw (Batch batch, float parentAlpha)
	{
		populateTileData();
		
		int xOffset = (int)getX();	
		int top = (int)(getY()+getHeight()) - tileSize;
		
		int x = 0;
		int y = 0;
		
		batch.setColor(Color.DARK_GRAY);	
		for (y = 0; y < viewHeight; y++)		
		{
			for (x = 0; x < viewWidth; x++)
			{
				if (tileBackground != null) { tileBackground.render(batch, x*tileSize + xOffset, top - y*tileSize, tileSize, tileSize); }
				if (tileBorder != null) { tileBorder.render(batch, x*tileSize + xOffset, top - y*tileSize, tileSize, tileSize); }
			}
		}
		
		x = 0;
		y = 0;
		
		Iterator<Object> itr = tileData.iterator();
		while (itr.hasNext())
		{
			Object item = itr.next();
			
			Color baseColour = item != null && item == mouseOver ? Color.WHITE : Color.LIGHT_GRAY;
			Color itemColour = getColourForData(item);
			if (itemColour != null)
			{
				itemColour = new Color(baseColour).mul(itemColour);
			}
			else
			{
				itemColour = baseColour;
			}
			
			batch.setColor(itemColour);
			if (tileBackground != null) { tileBackground.render(batch, x*tileSize + xOffset, top - y*tileSize, tileSize, tileSize); }
			onDrawItemBackground(item, batch, x*tileSize + xOffset, top - y*tileSize, tileSize, tileSize);
			
			batch.setColor(Color.WHITE);
			Sprite sprite = getSpriteForData(item);
			if (sprite != null) { sprite.render(batch, x*tileSize + xOffset, top - y*tileSize, tileSize, tileSize);	 }
			onDrawItem(item, batch, x*tileSize + xOffset, top - y*tileSize, tileSize, tileSize);
			
			batch.setColor(itemColour);
			if (tileBorder != null) { tileBorder.render(batch, x*tileSize + xOffset, top - y*tileSize, tileSize, tileSize); }
			onDrawItemForeground(item, batch, x*tileSize + xOffset, top - y*tileSize, tileSize, tileSize);
			
			x++;
			if (x == viewWidth)
			{
				x = 0;
				y++;
				if (y == viewHeight)
				{
					break;
				}
			}
		}
	}

	public class TilePanelListener extends InputListener
	{
		private Object pointToItem(float x, float y)
		{
			if (x < 0 || y < 0 || x > getWidth() || y > getHeight()) { return null; }
			
			y = getHeight() - y;
			
			int xIndex = (int) (x / tileSize);
			int yIndex = (int) (y / tileSize);
			
			int index = yIndex * viewWidth + xIndex;			
			if (index >= tileData.size) { return null; }
			return tileData.get(index);
		}
		
		private void setMouseOverUI(float x, float y)
		{
			if (x < 0 || y < 0 || x > getWidth() || y > getHeight())
			{
				GameScreen.Instance.mouseOverUI = false;
			}
			else
			{
				y = getHeight() - y;
				
				int xIndex = (int) (x / tileSize);
				int yIndex = (int) (y / tileSize);
				
				if (xIndex < viewWidth && yIndex < viewHeight)
				{
					GameScreen.Instance.mouseOverUI = true;
				}
				else
				{
					GameScreen.Instance.mouseOverUI = false;
				}
			}	
		}
		
		@Override
		public boolean mouseMoved (InputEvent event, float x, float y)
		{
			if (tooltip != null)
			{
				tooltip.setVisible(false);
				tooltip.remove();
				tooltip = null;
			}
			
			Object item = pointToItem(x, y);
			
			if (item != null)
			{
				Table table = getToolTipForData(item);
				
				if (table != null)
				{
					tooltip = new Tooltip(table, skin, stage);
					tooltip.show(event, x, y);
				}
			}
			
			mouseOver = item;
			
			setMouseOverUI(x, y);
			
			return GameScreen.Instance.mouseOverUI;
		}
		
		@Override
		public boolean touchDown (InputEvent event, float x, float y, int pointer, int button)
		{
			if (tooltip != null)
			{
				tooltip.setVisible(false);
				tooltip.remove();
				tooltip = null;
			}
			GameScreen.Instance.clearContextMenu();
			
			Object item = pointToItem(x, y);
			
			if (item != null)
			{
				handleDataClicked(item, event, x, y);
			}
			
			setMouseOverUI(x, y);
			
			return GameScreen.Instance.mouseOverUI;
		}
		
		@Override
		public void enter(InputEvent event, float x, float y, int pointer, Actor toActor) 
		{
			setMouseOverUI(x, y);
		}
		
		@Override
		public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) 
		{
			mouseOver = null;
			
			if (tooltip != null)
			{
				tooltip.setVisible(false);
				tooltip.remove();
				tooltip = null;
			}
			
			GameScreen.Instance.mouseOverUI = false;
		}
	}
}
