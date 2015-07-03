package Roguelike.UI;

import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.RoguelikeGame;
import Roguelike.Entity.AbilityPool;
import Roguelike.Entity.AbilityPool.AbilityLine;
import Roguelike.Entity.AbilityPool.AbilityLine.Ability;
import Roguelike.Sprite.Sprite;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.Array;

public class AbilityPoolPanel extends Widget
{
	private final int ButtonHeight = 32;
	private final int TileSize = 32;
	private float MaxLineWidth;
	
	private final AbilityPool abilityPool;
	private final BitmapFont font;
	private final Texture white;
	
	private final Sprite locked;
	private final Sprite usedTileSprite;
	private final Skin skin;
	private final Stage stage;
	
	private Tooltip tooltip;
	
	private int selectedAbilityLine = 0;
	
	private Ability unlocking;
	
	public AbilityPoolPanel(AbilityPool abilityPool, Skin skin, Stage stage)
	{
		this.abilityPool = abilityPool;
		this.skin = skin;
		this.stage = stage;
		
		this.font = new BitmapFont();
		this.white = AssetManager.loadTexture("Sprites/white.png");
		this.locked = AssetManager.loadSprite("GUI/locked");
				
		this.usedTileSprite = AssetManager.loadSprite("GUI/GUI", 0.5f, new int[]{16, 16}, new int[]{8, 10});
		
		calculateWidth();
		
		addListener(new AbilityPoolPanelListener());
	}
	
	private void calculateWidth()
	{
		float spaceSize = 10;//font.getSpaceWidth();
		
		float maxLineWidth = 0;
		
		for (AbilityLine line : abilityPool.abilityLines)
		{
			float temp = line.name.length() * spaceSize;
			if (temp > maxLineWidth)
			{
				maxLineWidth = temp;
			}
		}
		
		MaxLineWidth = maxLineWidth;
	}
	
	@Override
	public float getPrefWidth()
	{
		return MaxLineWidth + 7 * TileSize;
	}

	@Override
	public float getPrefHeight()
	{
		return abilityPool.abilityLines.size * ButtonHeight;
	}
	
	@Override
	public void draw (Batch batch, float parentAlpha)
	{
		batch.setColor(Color.BLUE);
		batch.draw(white, getX(), getY(), MaxLineWidth, getHeight());
		batch.setColor(Color.WHITE);
		
		float y = getY() + getHeight();
		for (AbilityLine line : abilityPool.abilityLines)
		{
			usedTileSprite.render(batch, (int)getX(), (int)y-ButtonHeight, (int)(MaxLineWidth), ButtonHeight);
			
			font.draw(batch, line.name, getX()+15, y-ButtonHeight/4);			
			y -= ButtonHeight;
		}
		
		batch.setColor(Color.CYAN);
		batch.draw(white, getX() + MaxLineWidth, getY(), getWidth() - MaxLineWidth, getHeight());
		batch.setColor(Color.WHITE);
		
		Array<Ability[]> abilityLine = abilityPool.abilityLines.get(selectedAbilityLine).abilityTiers;
		
		float xoff = getX() + MaxLineWidth + TileSize;
		y = getY() + getHeight();
		for (int i = 0; i < abilityLine.size; i++)
		{
			font.draw(batch, "Tier "+(i+1), xoff, y);
			y -= TileSize/2;
			
			for (int ii = 0; ii < 5; ii++)
			{
				Ability ab = abilityLine.get(i)[ii];
				
				usedTileSprite.render(batch, (int)(xoff + TileSize*ii), (int)y-TileSize, TileSize, TileSize);
				ab.ability.getIcon().render(batch, (int)(xoff + TileSize*ii), (int)y-TileSize, TileSize, TileSize);
				
				if (!ab.unlocked)
				{
					batch.setColor(0.5f, 0.5f, 0.5f, 0.5f);
					batch.draw(white, (int)(xoff + TileSize*ii), (int)y-TileSize, TileSize, TileSize);
					batch.setColor(Color.WHITE);
					
					if (ab == unlocking)
					{
						
					}
					else
					{
						locked.render(batch, (int)(xoff + TileSize*ii), (int)y-TileSize, TileSize, TileSize);
					}
				}
			}
			
			y -= TileSize*1.5f;
		}
	}
	
	private class AbilityPoolPanelListener extends InputListener
	{
		@Override
		public boolean mouseMoved (InputEvent event, float x, float y)
		{
			if (tooltip != null) { tooltip.remove(); tooltip = null; }
			
			if (x < MaxLineWidth)
			{
				int yindex = (int)((getHeight() - y) / ButtonHeight);
				
				if (yindex >= 0 && yindex < abilityPool.abilityLines.size)
				{
					Table t = new Table();
					t.add(new Label(abilityPool.abilityLines.get(yindex).name, skin));
					tooltip = new Tooltip(t, skin, stage);
					tooltip.show(event, x, y);
				}
				
				unlocking = null;
			}
			else
			{
				int ypos = 0;
				float ycursor = getHeight() - y;
				
				Array<Ability[]> abilityLine = abilityPool.abilityLines.get(selectedAbilityLine).abilityTiers;
				for (int i = 0; i < abilityLine.size; i++)
				{
					if (ycursor < ypos + TileSize/2)
					{
						// over tier
						unlocking = null;
						break;
					}
					ypos += TileSize * 0.5f;
					
					if (ycursor < ypos + TileSize)
					{
						// over tiles
						
						int xindex = (int)((x - MaxLineWidth - TileSize) / TileSize);
						if (xindex >= 0 && xindex < 5)
						{
							Ability a = abilityLine.get(i)[xindex];
							
							if (a != unlocking)
							{
								unlocking = null;
							}
							
							tooltip = new Tooltip(a.ability.createTable(skin), skin, stage);
							tooltip.show(event, x, y);
						}
						
						break;
					}
					ypos += TileSize;
					
					if (ycursor < ypos + TileSize*1.5f)
					{
						// over blank space
						unlocking = null;
						break;
					}
					ypos += TileSize * 0.5f;
				}
			}
			
			return true;
		}
		
		@Override
		public boolean touchDown (InputEvent event, float x, float y, int pointer, int button)
		{			
			if (x < MaxLineWidth)
			{
				int yindex = (int)((getHeight() - y) / ButtonHeight);
				
				if (yindex >= 0 && yindex < abilityPool.abilityLines.size)
				{
					selectedAbilityLine = yindex;
				}
			}
			else
			{
				int ypos = 0;
				float ycursor = getHeight() - y;
				
				Array<Ability[]> abilityLine = abilityPool.abilityLines.get(selectedAbilityLine).abilityTiers;
				for (int i = 0; i < abilityLine.size; i++)
				{
					if (ycursor < ypos + TileSize/2)
					{
						// over tier
						break;
					}
					ypos += TileSize * 0.5f;
					
					if (ycursor < ypos + TileSize)
					{
						// over tiles
						
						int xindex = (int)((x - MaxLineWidth - TileSize) / TileSize);
						if (xindex < 5)
						{
							Ability a = abilityLine.get(i)[xindex];
							
							if (a.unlocked)
							{
								RoguelikeGame.Instance.dragDropPayload = new DragDropPayload(a.ability, a.ability.getIcon(), x-16, getHeight() - y - 16);
							}
							else
							{
								if (unlocking == a)
								{
									a.unlocked = true;
									unlocking = null;
								}
								else
								{
									unlocking = a;
								}
							}
						}
						
						break;
					}
					ypos += TileSize;
					
					if (ycursor < ypos + TileSize*1.5f)
					{
						// over blank space
						break;
					}
					ypos += TileSize * 0.5f;
				}
			}
			
			return true;
		}
		
		@Override
		public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) 
		{
			//unlocking = null;
			
			if (tooltip != null)
			{
				tooltip.setVisible(false);
				tooltip.remove();
				tooltip = null;
			}
		}
	}
}
