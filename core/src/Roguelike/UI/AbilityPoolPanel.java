package Roguelike.UI;

import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.Ability.AbilityPool;
import Roguelike.Ability.AbilityPool.AbilityLine;
import Roguelike.Ability.AbilityPool.AbilityLine.Ability;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Ability.PassiveAbility.PassiveAbility;
import Roguelike.Entity.Tasks.TaskWait;
import Roguelike.Screens.GameScreen;
import Roguelike.Sprite.Sprite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.Array;

public class AbilityPoolPanel extends Widget
{
	private final int ButtonHeight = 32;
	private final int TileSize = 32;
	private float MaxLineWidth;
	
	private final BitmapFont font;
	private final Texture white;
	
	private final Sprite locked;
	
	private final Sprite tileBackground;
	private final Sprite tileBorder;
	
	private final Sprite buttonUp;
	private final Sprite buttonDown;
	
	private final Skin skin;
	private final Stage stage;
	
	private Tooltip tooltip;
	
	private int selectedAbilityLine = 0;
	
	//----------------------------------------------------------------------
	private BitmapFont contextMenuNormalFont;
	private BitmapFont contextMenuHilightFont;
		
	private final GlyphLayout layout = new GlyphLayout();
	
	public AbilityPoolPanel(Skin skin, Stage stage)
	{
		this.skin = skin;
		this.stage = stage;
		
		{
			FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("Sprites/GUI/stan0755.ttf"));
			FreeTypeFontParameter parameter = new FreeTypeFontParameter();
			parameter.size = 10;
			parameter.borderWidth = 1;
			parameter.borderColor = Color.BLACK;
			font = generator.generateFont(parameter); // font size 12 pixels
			generator.dispose(); // don't forget to dispose to avoid memory leaks!
		}
		
		{
			FreeTypeFontGenerator fgenerator = new FreeTypeFontGenerator(Gdx.files.internal("Sprites/GUI/stan0755.ttf"));
			FreeTypeFontParameter parameter = new FreeTypeFontParameter();
			parameter.size = 12;
			parameter.borderWidth = 1;
			parameter.kerning = true;
			parameter.borderColor = Color.BLACK;
			contextMenuNormalFont = fgenerator.generateFont(parameter);
			contextMenuNormalFont.getData().markupEnabled = true;
			fgenerator.dispose(); // don't forget to dispose to avoid memory leaks!
		}
		
		{
			FreeTypeFontGenerator fgenerator = new FreeTypeFontGenerator(Gdx.files.internal("Sprites/GUI/stan0755.ttf"));
			FreeTypeFontParameter parameter = new FreeTypeFontParameter();
			parameter.size = 14;
			parameter.borderWidth = 1;
			parameter.kerning = true;
			parameter.borderColor = Color.BLACK;
			contextMenuHilightFont = fgenerator.generateFont(parameter);
			contextMenuHilightFont.getData().markupEnabled = true;
			fgenerator.dispose(); // don't forget to dispose to avoid memory leaks!
		}
		
		this.white = AssetManager.loadTexture("Sprites/white.png");
		this.locked = AssetManager.loadSprite("GUI/locked");
				
		this.tileBackground = AssetManager.loadSprite("GUI/TileBackground");	
		this.tileBorder = AssetManager.loadSprite("GUI/TileBorder");
		
		this.buttonUp = AssetManager.loadSprite("GUI/ButtonUp");
		this.buttonDown = AssetManager.loadSprite("GUI/ButtonDown");
		
		addListener(new AbilityPoolPanelListener());
	}
	
	private void calculateWidth()
	{		
		float maxLineWidth = 0;
		
		for (AbilityLine line : Global.abilityPool.abilityLines)
		{
			layout.setText(font, line.name);
			float temp = layout.width+30;
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
		return Global.abilityPool.abilityLines.size * ButtonHeight;
	}
	
	@Override
	public void draw (Batch batch, float parentAlpha)
	{
		calculateWidth();
		
		AbilityLine selectedLine = Global.abilityPool.abilityLines.get(selectedAbilityLine);
		
		batch.setColor(0.6f, 0.6f, 0.6f, 0.5f);
		batch.draw(white, getX(), getY(), MaxLineWidth, getHeight());
		batch.setColor(Color.WHITE);
		
		float y = getY() + getHeight();
		
		for (AbilityLine line : Global.abilityPool.abilityLines)
		{
			if (line == selectedLine)
			{
				buttonDown.render(batch, (int)getX(), (int)y-ButtonHeight, (int)(MaxLineWidth), ButtonHeight);
			}
			else
			{
				buttonUp.render(batch, (int)getX(), (int)y-ButtonHeight, (int)(MaxLineWidth), ButtonHeight);
			}
			
			font.draw(batch, line.name, getX()+15, y-ButtonHeight/4);			
			y -= ButtonHeight;
		}
		
		batch.setColor(0.3f, 0.3f, 0.3f, 0.5f);
		batch.draw(white, getX() + MaxLineWidth, getY(), getPrefWidth() - MaxLineWidth, getHeight());
		batch.setColor(Color.WHITE);
		
		Array<Ability[]> tiers = selectedLine.abilityTiers;
		
		float xoff = getX() + MaxLineWidth + TileSize;
		y = getY() + getHeight();
		for (int i = 0; i < tiers.size; i++)
		{
			font.draw(batch, "Tier "+(i+1), xoff, y);
			y -= TileSize/2;
			
			for (int ii = 0; ii < 5; ii++)
			{
				Ability ab = tiers.get(i)[ii];
				
				tileBackground.render(batch, (int)(xoff + TileSize*ii), (int)y-TileSize, TileSize, TileSize);
				ab.ability.getIcon().render(batch, (int)(xoff + TileSize*ii), (int)y-TileSize, TileSize, TileSize);
				tileBorder.render(batch, (int)(xoff + TileSize*ii), (int)y-TileSize, TileSize, TileSize);
				
				if (!ab.unlocked)
				{
					batch.setColor(0.5f, 0.5f, 0.5f, 0.5f);
					batch.draw(white, (int)(xoff + TileSize*ii), (int)y-TileSize, TileSize, TileSize);
					batch.setColor(Color.WHITE);
					
					locked.render(batch, (int)(xoff + TileSize*ii), (int)y-TileSize, TileSize, TileSize);
				}
				else
				{
					int index = -1;
					if (ab.ability instanceof ActiveAbility)
					{
						index = Global.CurrentLevel.player.getActiveAbilityIndex((ActiveAbility)ab.ability);
					}
					else
					{
						index = Global.CurrentLevel.player.getPassiveAbilityIndex((PassiveAbility)ab.ability);
					}
					
					if (index >= 0)
					{
						int cx = (int)(xoff + TileSize*ii);
						int cy = (int)y-TileSize;
						
						cx += TileSize / 2;
						cy += TileSize / 2;
						
						font.draw(batch, ""+index, cx, cy);
					}

				}
			}
			
			y -= TileSize*1.5f;
		}
	}
	
	private class AbilityPoolPanelListener extends InputListener
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
		
		//----------------------------------------------------------------------
		@Override
		public void touchUp (InputEvent event, float x, float y, int pointer, int mousebutton)
		{
			GameScreen.Instance.clearContextMenu();
			
			if (x < getPrefWidth())
			{
				int ypos = 0;
				float ycursor = getHeight() - y;
				
				Array<Ability[]> abilityLine = Global.abilityPool.abilityLines.get(selectedAbilityLine).abilityTiers;
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
						if (xindex < 5 && xindex >= 0)
						{
							final Ability a = abilityLine.get(i)[xindex];
							
							if (a.unlocked)
							{
								if (a.ability instanceof ActiveAbility)
								{
									Table table = new Table();
									
									for (int ii = 0; ii < Global.NUM_ABILITY_SLOTS; ii++)
									{
										ActiveAbility equipped = Global.CurrentLevel.player.getSlottedActiveAbilities()[ii];
										final int index = ii;
										
										Table row = new Table();
										
										String text = ii + ".";
										if (equipped != null)
										{
											text += "  " + equipped.getName();
										}
															
										HoverTextButton button = new HoverTextButton(text, contextMenuNormalFont, contextMenuHilightFont);
										button.changePadding(5, 5);					
										row.add(button).expand().fill();
										
										row.addListener(new InputListener()
										{
											@Override
											public boolean touchDown (InputEvent event, float x, float y, int pointer, int button)
											{	
												Global.CurrentLevel.player.slotActiveAbility((ActiveAbility)a.ability, index);
												GameScreen.Instance.clearContextMenu();
												
												return true;
											}
										});

										table.add(row).width(Value.percentWidth(1, table)).pad(2);
										table.row();
									}
									
									table.pack();
									
									GameScreen.Instance.contextMenu = new Tooltip(table, skin, stage);
									GameScreen.Instance.contextMenu.show(event, x, y-GameScreen.Instance.contextMenu.getHeight()/2);
								}
								else if (a.ability instanceof PassiveAbility)
								{
									Table table = new Table();
									
									for (int ii = 0; ii < Global.NUM_ABILITY_SLOTS; ii++)
									{
										PassiveAbility equipped = Global.CurrentLevel.player.getSlottedPassiveAbilities()[ii];
										final int index = ii;
										
										Table row = new Table();
										
										String text = ii + ".";
										if (equipped != null)
										{
											text += "  " + equipped.getName();
										}
																			
										HoverTextButton button = new HoverTextButton(text, contextMenuNormalFont, contextMenuHilightFont);
										button.changePadding(5, 5);					
										row.add(button).expand().fill();
										
										row.addListener(new InputListener()
										{
											@Override
											public boolean touchDown (InputEvent event, float x, float y, int pointer, int button)
											{	
												Global.CurrentLevel.player.slotPassiveAbility((PassiveAbility)a.ability, index);
												GameScreen.Instance.clearContextMenu();
												
												return true;
											}
										});

										table.add(row).width(Value.percentWidth(1, table)).pad(2);
										table.row();
									}
									
									table.pack();
									
									GameScreen.Instance.contextMenu = new Tooltip(table, skin, stage);
									GameScreen.Instance.contextMenu.show(event, x, y-GameScreen.Instance.contextMenu.getHeight()/2);
								}
							}
							else
							{
								Table table = new Table();
								
								if (Global.CurrentLevel.player.essence >= a.cost)
								{
									HoverTextButton button = new HoverTextButton("Unlock for " + a.cost + " essence?\nYou have " + Global.CurrentLevel.player.essence + " essence.", contextMenuNormalFont, contextMenuHilightFont);
									button.changePadding(5, 5);					
									table.add(button).width(Value.percentWidth(1, table)).pad(2);
									
									table.addListener(new InputListener()
									{
										@Override
										public boolean touchDown (InputEvent event, float x, float y, int pointer, int button)
										{	
											Global.CurrentLevel.player.essence -= a.cost;
											
											a.unlocked = true;
											GameScreen.Instance.clearContextMenu();
											
											return true;
										}
									});							
								}
								else
								{
									table.add(new Label("Not enough essence " + Global.CurrentLevel.player.essence + " / " + a.cost + ".\nGo kill more things", skin)).width(Value.percentWidth(1, table)).pad(2);
									
									table.addListener(new InputListener()
									{
										@Override
										public boolean touchDown (InputEvent event, float x, float y, int pointer, int button)
										{	
											GameScreen.Instance.clearContextMenu();
											
											return true;
										}
									});												
								}
								
								table.pack();
								
								GameScreen.Instance.contextMenu = new Tooltip(table, skin, stage);
								GameScreen.Instance.contextMenu.show(event, x, y);
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
			
			if (GameScreen.Instance.dragDropPayload != null)
			{
				GameScreen.Instance.touchUp((int)event.getStageX(), Global.Resolution[1] - (int)event.getStageY(), pointer, mousebutton);
			}
		}
		
		@Override
		public boolean mouseMoved (InputEvent event, float x, float y)
		{
			if (tooltip != null) { tooltip.remove(); tooltip = null; }
			
			if (x < MaxLineWidth)
			{
				int yindex = (int)((getHeight() - y) / ButtonHeight);
				
				if (yindex >= 0 && yindex < Global.abilityPool.abilityLines.size)
				{
					Table t = new Table();
					t.add(new Label(Global.abilityPool.abilityLines.get(yindex).name, skin));
					tooltip = new Tooltip(t, skin, stage);
					tooltip.show(event, x, y);
				}				
			}
			else if (x < getPrefWidth())
			{
				int ypos = 0;
				float ycursor = getHeight() - y;
				
				Array<Ability[]> abilityLine = Global.abilityPool.abilityLines.get(selectedAbilityLine).abilityTiers;
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
						if (xindex >= 0 && xindex < 5)
						{
							Ability a = abilityLine.get(i)[xindex];
							
							Table table = a.ability.createTable(skin);
							
							if (!a.unlocked)
							{
								table.row();
								table.add(new Label("Cost: "+a.cost, skin));
							}
							
							tooltip = new Tooltip(table, skin, stage);
							tooltip.show(event, x, y);
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
			else
			{
				return false;
			}
			
			return true;
		}
		
		@Override
		public boolean touchDown (InputEvent event, float x, float y, int pointer, int button)
		{
			GameScreen.Instance.clearContextMenu();
			
			if (x < MaxLineWidth)
			{
				int yindex = (int)((getHeight() - y) / ButtonHeight);
				
				if (yindex >= 0 && yindex < Global.abilityPool.abilityLines.size)
				{
					selectedAbilityLine = yindex;
				}
			}
			else if (x < getPrefWidth())
			{
				int ypos = 0;
				float ycursor = getHeight() - y;
				
				Array<Ability[]> abilityLine = Global.abilityPool.abilityLines.get(selectedAbilityLine).abilityTiers;
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
							final Ability a = abilityLine.get(i)[xindex];
							
							if (a.unlocked)
							{
								GameScreen.Instance.dragDropPayload = new DragDropPayload(a.ability, a.ability.getIcon(), x-16, getHeight() - y - 16);
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
			else
			{
				return false;
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
