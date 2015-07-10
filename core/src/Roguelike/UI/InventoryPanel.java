package Roguelike.UI;

import java.util.HashMap;
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
	
	private int TileSize;
	private boolean sizeInvalid;
	
	private final int NumTilesWidth = ItemType.values().length;
	private int NumTilesHeight;
	
	private float prefWidth;
	private float prefHeight;
	
	private final Skin skin;
	private final Stage stage;
	
	private final Sprite tileBackground;
	private final Sprite tileBorder;
	
	private final Sprite buttonUp;
	private final Sprite buttonDown;
	
	private final HashMap<ItemType, Sprite> headers = new HashMap<ItemType, Sprite>();
	
	private final Entity entity;
	
	private ItemType selectedFilter = ItemType.ALL;
		
	public InventoryPanel(Entity entity, Skin skin, Stage stage)
	{
		this.entity = entity;
		
		this.TileSize = 32;
		this.skin = skin;
		this.stage = stage;
		
		this.tileBackground = AssetManager.loadSprite("GUI/TileBackground");	
		this.tileBorder = AssetManager.loadSprite("GUI/TileBorder");
		
		this.buttonUp = AssetManager.loadSprite("GUI/ButtonUp");
		this.buttonDown = AssetManager.loadSprite("GUI/ButtonDown");
		
		headers.put(ItemType.WEAPON, AssetManager.loadSprite("GUI/Weapon"));
		headers.put(ItemType.ARMOUR, AssetManager.loadSprite("GUI/Armour"));
		headers.put(ItemType.JEWELRY, AssetManager.loadSprite("GUI/Jewelry"));
		headers.put(ItemType.TREASURE, AssetManager.loadSprite("GUI/Treasure"));
		headers.put(ItemType.MISC, AssetManager.loadSprite("GUI/Misc"));
		headers.put(ItemType.ALL, AssetManager.loadSprite("GUI/All"));
				
		this.addListener(new InventoryPanelListener());
		this.setWidth(getPrefWidth());
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
			if (type == selectedFilter)
			{
				buttonDown.render(batch, x*TileSize + xOffset, top - y*TileSize, TileSize, TileSize);
			}
			else
			{
				buttonUp.render(batch, x*TileSize + xOffset, top - y*TileSize, TileSize, TileSize);
			}
			
			int headerSize = 24;
			headers.get(type).render(batch, x*TileSize + xOffset + 4, top - y*TileSize + 4, headerSize, headerSize);
			x++;
		}
		
		for (y = 2; y < NumTilesHeight; y++)		
		{
			for (x = 0; x < NumTilesWidth; x++)
			{
				batch.setColor(Color.WHITE);
				
				if (itr.hasNext())
				{
					Item item = itr.next();
										
					tileBackground.render(batch, x*TileSize + xOffset, top - y*TileSize, TileSize, TileSize);
					
					item.Icon.render(batch, x*TileSize + xOffset, top - y*TileSize, TileSize, TileSize);
					
					if (entity.getInventory().isEquipped(item))
					{
						batch.setColor(Color.GREEN);
					}
					else
					{
						batch.setColor(Color.DARK_GRAY);
					}
				}
				else
				{
					tileBackground.render(batch, x*TileSize + xOffset, top - y*TileSize, TileSize, TileSize);
					batch.setColor(Color.DARK_GRAY);
				}
				
				tileBorder.render(batch, x*TileSize + xOffset, top - y*TileSize, TileSize, TileSize);
			}
		}
				
		itr = entity.Tile.Items.iterator();
		y = NumTilesHeight;
		for (x = 0; x < NumTilesWidth; x++)
		{
			batch.setColor(Color.LIGHT_GRAY);
			
			tileBackground.render(batch, x*TileSize + xOffset, top - y*TileSize, TileSize, TileSize);
			
			batch.setColor(Color.WHITE);
			
			if (itr.hasNext())
			{
				Item item = itr.next();				
				item.Icon.render(batch, x*TileSize + xOffset, top - y*TileSize, TileSize, TileSize);
			}
		}
		
		super.draw(batch, parentAlpha);
	}
	
	@Override
	public float getWidth()
	{
		return getPrefWidth();
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
			
			if (tooltip != null) { tooltip.remove(); tooltip = null; }
			
			if (tileY == NumTilesHeight-1)
			{
				if (tileX < entity.Tile.Items.size)
				{
					Table t = entity.Tile.Items.get(tileX).createTable(skin, entity.getInventory());
					
					tooltip = new Tooltip(t, skin, stage);
					tooltip.show(event, x, y);
				}
			}
			else if (tileY == 0)
			{
				if (tileX < NumTilesWidth)
				{
					ItemType type = ItemType.values()[tileX];
					
					Table t = new Table();
					t.add(new Label(type.toString(), skin));
					
					tooltip = new Tooltip(t, skin, stage);
					tooltip.show(event, x, y);
				}				
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
			
			return true;
		}
		
		@Override
		public boolean touchDown (InputEvent event, float x, float y, int pointer, int button)
		{
			int tileX = (int)(x / TileSize);
			int tileY = (int)((getHeight() - y) / TileSize);
						
			if (tooltip != null) { tooltip.remove(); }
			
			if (tileY == NumTilesHeight-1)
			{
				if (tileX < entity.Tile.Items.size)
				{
					Item i = entity.Tile.Items.get(tileX);
					entity.getInventory().addItem(i);
					entity.Tile.Items.removeValue(i, true);
				}				
			}
			else if (tileY == 0)
			{
				if (tileX < NumTilesWidth)
				{
					selectedFilter = ItemType.values()[tileX];
				}
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
						if (entity.getInventory().isEquipped(item))
						{
							entity.getInventory().unequip(item);
						}
						
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