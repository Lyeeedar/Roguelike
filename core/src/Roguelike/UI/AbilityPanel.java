package Roguelike.UI;

import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Ability.PassiveAbility.PassiveAbility;
import Roguelike.Entity.GameEntity;
import Roguelike.Screens.GameScreen;
import Roguelike.Sprite.Sprite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;

public class AbilityPanel extends Widget
{
	private final int TileSize = 32;
	
	private Tooltip tooltip;
		
	private BitmapFont font;
	private Texture white;
		
	private final Sprite tileBackground;
	private final Sprite tileBorder;
	
	private final Skin skin;
	private final Stage stage;
	
	public AbilityPanel(Skin skin, Stage stage)
	{
		this.tileBackground = AssetManager.loadSprite("GUI/TileBackground");	
		this.tileBorder = AssetManager.loadSprite("GUI/TileBorder");
		this.font = new BitmapFont();
		this.white = AssetManager.loadTexture("Sprites/white.png");
						
		this.skin = skin;
		this.stage = stage;
				
		addListener(new AbilityPanelListener());
	}
	
	@Override
	public void draw (Batch batch, float parentAlpha)
	{
		super.draw(batch, parentAlpha);
		batch.setColor(Color.WHITE);
		
		int xoffset = (int)getX();		
		int top = (int)(getY()+getHeight());
		
		boolean aaDragged = GameScreen.Instance.dragDropPayload != null && GameScreen.Instance.dragDropPayload.shouldDraw() && GameScreen.Instance.dragDropPayload.obj instanceof ActiveAbility;
		boolean paDragged = GameScreen.Instance.dragDropPayload != null && GameScreen.Instance.dragDropPayload.shouldDraw() && GameScreen.Instance.dragDropPayload.obj instanceof PassiveAbility;
		
		GameEntity entity = Global.CurrentLevel.player;
		
		float x = 0;
		for (int i = 0; i < Global.NUM_ABILITY_SLOTS; i++)
		{
			ActiveAbility aa = entity.getSlottedActiveAbilities()[i];
			PassiveAbility pa = entity.getSlottedPassiveAbilities()[i];
			
			// Draw active
			
			// background
			tileBackground.render(batch, (int)(xoffset + x), top - TileSize, TileSize, TileSize);
			
			// icon
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
			
			// border
			if (aa != null && aa == GameScreen.Instance.preparedAbility)
			{
				batch.setColor(Color.CYAN);
			}
			else if (aaDragged)
			{				
				batch.setColor(0.5f, 0.6f, 1.0f, 1.0f);
			}
			
			tileBorder.render(batch, (int)(xoffset + x), top - TileSize, TileSize, TileSize);
			
			batch.setColor(Color.WHITE);
			
			// Draw Passive
			
			// background
			tileBackground.render(batch, (int)(xoffset + x), top - TileSize*2, TileSize, TileSize);			
			
			// icon
			if (pa != null)
			{
				pa.Icon.render(batch, (int)(xoffset + x), top - TileSize*2, TileSize, TileSize);
			}
			
			// border
			if (paDragged)
			{
				batch.setColor(0.5f, 0.6f, 1.0f, 1.0f);
			}
			
			tileBorder.render(batch, (int)(xoffset + x), top - TileSize*2, TileSize, TileSize);	
			
			batch.setColor(Color.WHITE);
			
			x += TileSize;
		}
	}
	
	@Override
	public float getPrefWidth()
	{
		return TileSize * Global.NUM_ABILITY_SLOTS;
	}

	@Override
	public float getPrefHeight()
	{
		return TileSize * 2;
	}
	
	public void handleDrop(float x, float y)
	{
		GameEntity entity = Global.CurrentLevel.player;
		boolean aaDragged = GameScreen.Instance.dragDropPayload != null && GameScreen.Instance.dragDropPayload.obj instanceof ActiveAbility;
		boolean paDragged = GameScreen.Instance.dragDropPayload != null && GameScreen.Instance.dragDropPayload.obj instanceof PassiveAbility;
		
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
						entity.slotActiveAbility((ActiveAbility)GameScreen.Instance.dragDropPayload.obj, xIndex);
					}
				}
				else
				{
					if (paDragged)
					{
						entity.slotPassiveAbility((PassiveAbility)GameScreen.Instance.dragDropPayload.obj, xIndex);
					}					
				}
			}
		}
	}
	
	private class AbilityPanelListener extends InputListener
	{
		//----------------------------------------------------------------------
		@Override
		public void touchDragged (InputEvent event, float x, float y, int pointer)
		{
			if (GameScreen.Instance.dragDropPayload != null)
			{
				GameScreen.Instance.dragDropPayload.x = event.getStageX() - 16;
				GameScreen.Instance.dragDropPayload.y = event.getStageY() - 16;				
			}			
		}
		
		@Override
		public boolean touchDown (InputEvent event, float x, float y, int pointer, int button)
		{
			GameEntity entity = Global.CurrentLevel.player;
			GameScreen.Instance.clearContextMenu();
			
			int xIndex = (int) (x / TileSize);
			int yIndex = (int) ((getHeight() - y) / TileSize);
			
			if (tooltip != null) { tooltip.remove(); tooltip = null; }
			
			if (xIndex < Global.NUM_ABILITY_SLOTS)
			{
				if (yIndex == 0)
				{
					ActiveAbility aa = entity.getSlottedActiveAbilities()[xIndex];
					
					if (aa != null && aa.cooldownAccumulator <= 0)
					{
						GameScreen.Instance.dragDropPayload = new DragDropPayload(aa, aa.getIcon(), x-16, getHeight() - y - 16);
					}
				}
				else if (yIndex == 1)
				{
					PassiveAbility pa = entity.getSlottedPassiveAbilities()[xIndex];
					
					if (pa != null)
					{
						GameScreen.Instance.dragDropPayload = new DragDropPayload(pa, pa.getIcon(), x-16, getHeight() - y - 16);
					}
				}
			}	
			
			return true;
		}
		
		@Override
		public void touchUp (InputEvent event, float x, float y, int pointer, int button)
		{
			GameEntity entity = Global.CurrentLevel.player;
			if (GameScreen.Instance.dragDropPayload != null)
			{
				GameScreen.Instance.touchUp((int)event.getStageX(), Gdx.graphics.getHeight() - (int)event.getStageY(), pointer, button);
			}
			
			int xIndex = (int) (x / TileSize);
			int yIndex = (int) ((getHeight() - y) / TileSize);
			
			if (tooltip != null) { tooltip.remove(); tooltip = null; }
			
			if (xIndex < Global.NUM_ABILITY_SLOTS)
			{
				if (yIndex == 0)
				{
					ActiveAbility aa = entity.getSlottedActiveAbilities()[xIndex];
					
					if (aa != null && aa.cooldownAccumulator <= 0)
					{
						GameScreen.Instance.prepareAbility(aa);
					}
				}
			}	
		}
		
		@Override
		public boolean mouseMoved (InputEvent event, float x, float y)
		{	
			GameEntity entity = Global.CurrentLevel.player;
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
