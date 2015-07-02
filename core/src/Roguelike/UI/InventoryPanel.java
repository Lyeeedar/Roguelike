package Roguelike.UI;

import java.util.Iterator;

import Roguelike.AssetManager;
import Roguelike.Entity.Entity;
import Roguelike.Items.Item;
import Roguelike.Items.Item.ItemType;
import Roguelike.Sprite.Sprite;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;

public class InventoryPanel extends Widget
{
	private Tooltip tooltip;
	private int lastX;
	private int lastY;
	
	private int TileSize;
	private boolean sizeInvalid;
	
	private final int NumTilesWidth = ItemType.values().length;
	private int NumTilesHeight;
	
	private float prefWidth;
	private float prefHeight;
	
	private final Skin skin;
	private final Stage stage;
	
	private final Sprite equippedTileSprite;
	private final Sprite usedTileSprite;
	private final Sprite unusedTileSprite;
	private final Sprite floorTileSprite;
	
	private final Entity entity;
	
	private ItemType selectedFilter = ItemType.ALL;
	
	private final BitmapFont font = new BitmapFont();
	
	public InventoryPanel(Entity entity, Skin skin, Stage stage)
	{
		this.entity = entity;
		
		this.TileSize = 32;
		this.skin = skin;
		this.stage = stage;
		
		this.equippedTileSprite = AssetManager.loadSprite("GUI/GUI", 0.5f, new int[]{16, 16}, new int[]{8, 7});
		this.usedTileSprite = AssetManager.loadSprite("GUI/GUI", 0.5f, new int[]{16, 16}, new int[]{8, 10});
		this.unusedTileSprite = AssetManager.loadSprite("GUI/GUI", 0.5f, new int[]{16, 16}, new int[]{8, 13});
		this.floorTileSprite = AssetManager.loadSprite("GUI/GUI", 0.5f, new int[]{16, 16}, new int[]{8, 16});		
				
		this.addListener(new InventoryPanelListener());
	}
	
	private void computeSize()
	{
		sizeInvalid = false;
		
		float height = getHeight();
		
		NumTilesHeight = (int)(height / TileSize) ;
		
		prefWidth = NumTilesWidth * TileSize;		
		prefHeight = NumTilesHeight * TileSize;
	}
	
	@Override
	public void invalidate()
	{
		super.invalidate();
		computeSize();
	}
	
	@Override
	public void draw (Batch batch, float parentAlpha)
	{
		batch.setColor(Color.WHITE);
		
		Iterator<Item> itr = entity.getInventory().iterator(selectedFilter);
		
		int xOffset = (int)getX();
		
		int top = (int)(getY()+getHeight());
		
		int y = 1;
		int x = 0;
		for (ItemType type : ItemType.values())
		{
			equippedTileSprite.render(batch, x*TileSize + xOffset, top - y*TileSize, TileSize, TileSize);
			font.draw(batch, ""+type.toString().charAt(0), x*TileSize + xOffset, top - y*TileSize);		
			x++;
		}
		
		for (y = 2; y < NumTilesHeight; y++)		
		{
			for (x = 0; x < NumTilesWidth; x++)
			{
				if (itr.hasNext())
				{
					Item item = itr.next();
										
					if (entity.getInventory().isEquipped(item))
					{
						equippedTileSprite.render(batch, x*TileSize + xOffset, top - y*TileSize, TileSize, TileSize);
					}
					else
					{
						usedTileSprite.render(batch, x*TileSize + xOffset, top - y*TileSize, TileSize, TileSize);
					}
					
					item.Icon.render(batch, x*TileSize + xOffset, top - y*TileSize, TileSize, TileSize);
				}
				else
				{
					unusedTileSprite.render(batch, x*TileSize + xOffset, top - y*TileSize, TileSize, TileSize);
				}
			}
		}
		
		itr = entity.Tile.Items.iterator();
		y = NumTilesHeight;
		for (x = 0; x < NumTilesWidth; x++)
		{
			floorTileSprite.render(batch, x*TileSize + xOffset, top - y*TileSize, TileSize, TileSize);
			
			if (itr.hasNext())
			{
				Item item = itr.next();				
				item.Icon.render(batch, x*TileSize + xOffset, top - y*TileSize, TileSize, TileSize);
			}
		}
		
		super.draw(batch, parentAlpha);
	}

	@Override
	public float getPrefWidth()
	{
		if (sizeInvalid) computeSize();
		return prefWidth;
	}

	@Override
	public float getPrefHeight()
	{
		if (sizeInvalid) computeSize();
		return prefHeight;
	}

	private class InventoryPanelListener extends InputListener
	{
		@Override
		public boolean mouseMoved (InputEvent event, float x, float y)
		{			
			int tileX = (int)(x / TileSize);
			int tileY = (int)((getHeight() - y) / TileSize);
			
			if (tileX != lastX || tileY != lastY || tooltip == null)
			{
				if (tooltip != null) { tooltip.remove(); tooltip = null; }
				
				if (tileY == NumTilesHeight-1 && tileX < entity.Tile.Items.size)
				{
					if (tileX < entity.Tile.Items.size)
					{
						Table t = entity.Tile.Items.get(tileX).createTable(skin, entity.getInventory());
						
						tooltip = new Tooltip(t, skin, stage);
						tooltip.show(event, x, y);
					}
				}
				else if (tileY == 0 && tileX < NumTilesWidth)
				{
					ItemType type = ItemType.values()[tileX];
					
					Table t = new Table();
					t.add(new Label(type.toString(), skin));
					
					tooltip = new Tooltip(t, skin, stage);
					tooltip.show(event, x, y);
				}
				else
				{					
					int index = (tileY-1)*NumTilesWidth + tileX;
					
					Item item = null;
					Iterator<Item> itr = entity.getInventory().iterator(selectedFilter);
					int count = 0;
					while (itr.hasNext())
					{
						Item i = itr.next();
						if (count == index)
						{
							item = i;
							break;
						}
						count++;
					}
					
					if (item != null)
					{
						Table t = item.createTable(skin, entity.getInventory());
						
						tooltip = new Tooltip(t, skin, stage);
						tooltip.show(event, x, y);
					}
				}
				
				lastX = tileX;
				lastY = tileY;
			}
			
			return true;
		}
		
		@Override
		public boolean touchDown (InputEvent event, float x, float y, int pointer, int button)
		{
			int tileX = (int)(x / TileSize);
			int tileY = (int)((getHeight() - y) / TileSize);
						
			if (tooltip != null) { tooltip.remove(); }
			
			if (tileY == NumTilesHeight-1 && tileX < entity.Tile.Items.size)
			{
				Item i = entity.Tile.Items.get(tileX);
				entity.getInventory().addItem(i);
				entity.Tile.Items.removeValue(i, true);
			}
			else if (tileY == 0 && tileX < NumTilesWidth)
			{
				selectedFilter = ItemType.values()[tileX];
			}
			else
			{
				int index = (tileY-1)*NumTilesWidth + tileX;
				
				Item item = null;
				Iterator<Item> itr = entity.getInventory().iterator(selectedFilter);
				int count = 0;
				while (itr.hasNext())
				{
					Item i = itr.next();
					if (count == index)
					{
						item = i;
						break;
					}
					count++;
				}
				
				if (item != null)
				{
					if (button == Buttons.LEFT)
					{
						entity.getInventory().toggleEquip(item);
					}
					else if (button == Buttons.RIGHT)
					{
						entity.getInventory().removeItem(item);
						entity.Tile.Items.add(item);
					}
				}				
			}
			
			return true;
		}

		@Override
		public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) 
		{
			if (tooltip != null)
			{
				tooltip.setVisible(false);
				tooltip.remove();
				tooltip = null;
			}
		}
	}
}