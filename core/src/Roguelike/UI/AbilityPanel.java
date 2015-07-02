package Roguelike.UI;

import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.RoguelikeGame;
import Roguelike.Entity.ActiveAbility;
import Roguelike.Entity.Entity;
import Roguelike.Entity.PassiveAbility;
import Roguelike.Items.Item.ItemType;
import Roguelike.Sprite.Sprite;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;

public class AbilityPanel extends Widget
{
	private final int TileSize = 32;
	
	private Tooltip tooltip;
	private int lastX;
	private int lastY;
	
	private int SelectedAbility;
	
	private BitmapFont font;
	private Texture white;
	
	private final Entity entity;
	private final Sprite equippedTileSprite;
	private final Sprite usedTileSprite;
	private final Sprite unusedTileSprite;
	
	private final Skin skin;
	private final Stage stage;
	
	public AbilityPanel(Entity entity, Skin skin, Stage stage)
	{
		this.equippedTileSprite = AssetManager.loadSprite("GUI/GUI", 0.5f, new int[]{16, 16}, new int[]{8, 7});
		this.usedTileSprite = AssetManager.loadSprite("GUI/GUI", 0.5f, new int[]{16, 16}, new int[]{8, 10});
		this.unusedTileSprite = AssetManager.loadSprite("GUI/GUI", 0.5f, new int[]{16, 16}, new int[]{8, 13});
		this.font = new BitmapFont();
		this.white = AssetManager.loadTexture("Sprites/white.png");
				
		this.entity = entity;
		
		this.skin = skin;
		this.stage = stage;
		
		SelectedAbility = 0;
		
		addListener(new AbilityPanelListener());
	}
	
	@Override
	public void draw (Batch batch, float parentAlpha)
	{
		super.draw(batch, parentAlpha);
		batch.setColor(Color.WHITE);
		
		int xoffset = (int)getX();		
		int top = (int)(getY()+getHeight());
		
		boolean aaDragged = RoguelikeGame.Instance.dragDropPayload != null && RoguelikeGame.Instance.dragDropPayload.obj instanceof ActiveAbility;
		boolean paDragged = RoguelikeGame.Instance.dragDropPayload != null && RoguelikeGame.Instance.dragDropPayload.obj instanceof PassiveAbility;
		
		float x = 0;
		for (int i = 0; i < Global.NUM_ABILITY_SLOTS; i++)
		{
			ActiveAbility aa = entity.getSlottedActiveAbilities()[i];
			PassiveAbility pa = entity.getSlottedPassiveAbilities()[i];
			
			if (aaDragged)
			{
				batch.setColor(Color.CYAN);
			}
			
			if (i == SelectedAbility)
			{
				equippedTileSprite.render(batch, (int)(xoffset + x), top - TileSize, TileSize, TileSize);
			}
			else if (aa != null)
			{
				usedTileSprite.render(batch, (int)(xoffset + x), top - TileSize, TileSize, TileSize);
				
			}
			else
			{
				unusedTileSprite.render(batch, (int)(xoffset + x), top - TileSize, TileSize, TileSize);
			}
			
			if (aaDragged)
			{
				batch.setColor(Color.WHITE);
			}
			
			if (aa != null)
			{
				aa.Icon.render(batch, (int)(xoffset + x), top - TileSize, TileSize, TileSize);
				
				if (aa.cooldownAccumulator > 0)
				{
					batch.setColor(0.5f, 0.5f, 0.5f, 0.5f);
					batch.draw(white, (int)(xoffset + x), top - TileSize, TileSize, TileSize);
					batch.setColor(Color.WHITE);
					
					font.draw(batch, ""+aa.cooldownAccumulator, (int)(xoffset + x) + TileSize/2, (top - TileSize) + TileSize/2);
				}
			}
			
			if (paDragged)
			{
				batch.setColor(Color.CYAN);
			}
			
			if (pa != null)
			{
				usedTileSprite.render(batch, (int)(xoffset + x), top - TileSize*2, TileSize, TileSize);			
			}
			else
			{
				unusedTileSprite.render(batch, (int)(xoffset + x), top - TileSize*2, TileSize, TileSize);
			}
			
			if (paDragged)
			{
				batch.setColor(Color.WHITE);
			}
			
			if (pa != null)
			{
				pa.Icon.render(batch, (int)(xoffset + x), top - TileSize*2, TileSize, TileSize);
			}
			
			x += TileSize;
		}
		
		ActiveAbility selected = entity.getSlottedActiveAbilities()[SelectedAbility];
		if (selected != null)
		{
			usedTileSprite.render(batch, (int)(xoffset + x), top - TileSize*2, TileSize*2, TileSize*2);
			selected.Icon.render(batch, (int)(xoffset + x), top - TileSize*2, TileSize*2, TileSize*2);
			
			if (selected.cooldownAccumulator > 0)
			{
				batch.setColor(0.5f, 0.5f, 0.5f, 0.5f);
				batch.draw(white, (int)(xoffset + x), top - TileSize*2, TileSize*2, TileSize*2);
				batch.setColor(Color.WHITE);
				
				font.draw(batch, ""+selected.cooldownAccumulator, (int)(xoffset + x) + TileSize, top - TileSize);
			}
		}
		else
		{
			unusedTileSprite.render(batch, (int)(xoffset + x), top - TileSize*2, TileSize*2, TileSize*2);
		}
	}
	
	@Override
	public float getPrefWidth()
	{
		return TileSize * (Global.NUM_ABILITY_SLOTS+2);
	}

	@Override
	public float getPrefHeight()
	{
		return TileSize * 2;
	}
	
	public ActiveAbility getSelectedAbility()
	{
		return entity.getSlottedActiveAbilities()[SelectedAbility];
	}
	
	public void handleDrop(float x, float y)
	{
		boolean aaDragged = RoguelikeGame.Instance.dragDropPayload != null && RoguelikeGame.Instance.dragDropPayload.obj instanceof ActiveAbility;
		boolean paDragged = RoguelikeGame.Instance.dragDropPayload != null && RoguelikeGame.Instance.dragDropPayload.obj instanceof PassiveAbility;
		
		if (aaDragged || paDragged)
		{
			int xIndex = (int) (x / TileSize);
			int yIndex = (int) ((getHeight() - y) / TileSize);
			
			if (tooltip != null) { tooltip.remove(); tooltip = null; }
			
			if (xIndex < Global.NUM_ABILITY_SLOTS)
			{
				if (yIndex == 0)
				{
					if (aaDragged)
					{
						entity.slotActiveAbility((ActiveAbility)RoguelikeGame.Instance.dragDropPayload.obj, xIndex);
					}
				}
				else
				{
					if (paDragged)
					{
						entity.slotPassiveAbility((PassiveAbility)RoguelikeGame.Instance.dragDropPayload.obj, xIndex);
					}					
				}
			}
		}
	}
	
	private class AbilityPanelListener extends InputListener
	{
		@Override
		public boolean touchDown (InputEvent event, float x, float y, int pointer, int button)
		{
			int xIndex = (int) (x / TileSize);
			int yIndex = (int) ((getHeight() - y) / TileSize);
			
			if (tooltip != null) { tooltip.remove(); tooltip = null; }
			
			if (xIndex < Global.NUM_ABILITY_SLOTS)
			{
				if (yIndex == 0)
				{
					ActiveAbility aa = entity.getSlottedActiveAbilities()[xIndex];
					
					if (aa != null)
					{
						SelectedAbility = xIndex;
					}
				}
			}
			
			return true;
		}
		
		@Override
		public boolean mouseMoved (InputEvent event, float x, float y)
		{	
			int xIndex = (int) (x / TileSize);
			int yIndex = (int) ((getHeight() - y) / TileSize);
			
			if (xIndex != lastX || yIndex != lastY || tooltip == null)
			{
				if (tooltip != null) { tooltip.remove(); tooltip = null; }
				
				if (xIndex < Global.NUM_ABILITY_SLOTS)
				{
					if (yIndex == 0)
					{
						ActiveAbility aa = entity.getSlottedActiveAbilities()[xIndex];
						
						if (aa != null)
						{
							tooltip = new Tooltip(aa.createTable(skin), skin, stage);
							tooltip.show(event, x, y);
						}
					}
					else
					{
						PassiveAbility aa = entity.getSlottedPassiveAbilities()[xIndex];
						
						if (aa != null)
						{
							tooltip = new Tooltip(aa.createTable(skin), skin, stage);
							tooltip.show(event, x, y);
						}
					}
				}
				else
				{
					ActiveAbility aa = entity.getSlottedActiveAbilities()[SelectedAbility];
					
					if (aa != null)
					{
						tooltip = new Tooltip(aa.createTable(skin), skin, stage);
						tooltip.show(event, x, y);
					}
				}
				
				lastX = xIndex;
				lastY = yIndex;
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
