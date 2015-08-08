package Roguelike.UI;

import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.Ability.AbilityPool.Ability;
import Roguelike.Ability.AbilityPool.AbilityLine;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Ability.PassiveAbility.PassiveAbility;
import Roguelike.Screens.GameScreen;
import Roguelike.Sprite.Sprite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.Array;

public class AbilityPoolPanel extends Table
{
	private final int ButtonHeight = 32;
	private final int TileSize = 32;
	private float MaxLineWidth;
	
	private final Texture white;
	
	private final Sprite locked;
	
	private final Sprite tileBackground;
	private final Sprite tileBorder;
	
	private final Sprite buttonUp;
	private final Sprite buttonDown;
	
	private final Skin skin;
	private final Stage stage;
		
	private int selectedAbilityLine = 0;
	
	//----------------------------------------------------------------------
	private BitmapFont contextMenuNormalFont;
	private BitmapFont contextMenuHilightFont;
	
	private AbilityLineList abilityLine;
	private AbilityList abilityList;
			
	public AbilityPoolPanel(Skin skin, Stage stage)
	{
		this.skin = skin;
		this.stage = stage;

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
		
		abilityLine = new AbilityLineList(skin, stage, buttonUp, tileBorder, 32);
		abilityList = new AbilityList(skin, stage, tileBackground, tileBorder, 32);
		
		add(abilityLine).expandY().fillY();
		add(abilityList).expand().fill();
	}
	
	public class AbilityLineList extends TilePanel
	{

		public AbilityLineList(Skin skin, Stage stage, Sprite tileBackground, Sprite tileBorder, int tileSize)
		{
			super(skin, stage, tileBackground, tileBorder, 1, 1, tileSize, true);
		}

		@Override
		public void populateTileData()
		{
			tileData.clear();
			
			for (AbilityLine line : Global.abilityPool.abilityLines)
			{
				tileData.add(line);
			}
		}

		@Override
		public Sprite getSpriteForData(Object data)
		{
			return ((AbilityLine)data).icon;
		}

		@Override
		public void handleDataClicked(Object data, InputEvent event, float x, float y)
		{
			selectedAbilityLine = Global.abilityPool.abilityLines.indexOf((AbilityLine)data, true);
		}

		@Override
		public Table getToolTipForData(Object data)
		{
			Table table = new Table();
			table.add(new Label(((AbilityLine)data).name, skin)).width(Value.percentWidth(1, table));
			
			return table;
		}

		@Override
		public Color getColourForData(Object data)
		{
			AbilityLine line = (AbilityLine)data;
			int index = Global.abilityPool.abilityLines.indexOf(line, true);
			
			return selectedAbilityLine == index ? Color.LIGHT_GRAY : null;
		}

		@Override
		public void onDrawItemBackground(Object data, Batch batch, int x, int y, int width, int height)
		{
		}

		@Override
		public void onDrawItem(Object data, Batch batch, int x, int y, int width, int height)
		{
		}

		@Override
		public void onDrawItemForeground(Object data, Batch batch, int x, int y, int width, int height)
		{
		}
		
	}

	public class AbilityList extends TilePanel
	{

		public AbilityList(Skin skin, Stage stage, Sprite tileBackground, Sprite tileBorder, int tileSize)
		{
			super(skin, stage, tileBackground, tileBorder, 5, 1, tileSize, true);
		}

		@Override
		public void populateTileData()
		{
			tileData.clear();
			
			AbilityLine selected = Global.abilityPool.abilityLines.get(selectedAbilityLine);
			
			for (Ability[] tier : selected.abilityTiers)
			{
				for (Ability a : tier)
				{
					tileData.add(a);
				}
			}
		}

		@Override
		public Sprite getSpriteForData(Object data)
		{
			return ((Ability)data).ability.getIcon();
		}

		@Override
		public void handleDataClicked(Object data, InputEvent event, float x, float y)
		{
			final Ability a = (Ability)data;
			
			Table table = new Table();
			if (!a.unlocked)
			{
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
			}
			else
			{
				if (a.ability instanceof ActiveAbility)
				{					
					for (int ii = 0; ii < Global.NUM_ABILITY_SLOTS; ii++)
					{
						ActiveAbility equipped = Global.abilityPool.slottedActiveAbilities[ii];
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
								Global.abilityPool.slotActiveAbility((ActiveAbility)a.ability, index);
								GameScreen.Instance.clearContextMenu();
								
								return true;
							}
						});

						table.add(row).width(Value.percentWidth(1, table)).pad(2);
						table.row();
					}					
				}
				else if (a.ability instanceof PassiveAbility)
				{					
					for (int ii = 0; ii < Global.NUM_ABILITY_SLOTS; ii++)
					{
						PassiveAbility equipped = Global.abilityPool.slottedPassiveAbilities[ii];
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
								Global.abilityPool.slotPassiveAbility((PassiveAbility)a.ability, index);
								GameScreen.Instance.clearContextMenu();
								
								return true;
							}
						});

						table.add(row).width(Value.percentWidth(1, table)).pad(2);
						table.row();
					}
				}
			}
			
			table.pack();
			
			Tooltip tooltip = new Tooltip(table, skin, stage);			
			tooltip.show(event, x-tooltip.getWidth()/2, y-tooltip.getHeight()/2);
			
			GameScreen.Instance.contextMenu = tooltip;			
		}

		@Override
		public Table getToolTipForData(Object data)
		{
			Ability a = (Ability)data;
			Table table = a.ability.createTable(skin);
			
			if (!a.unlocked)
			{
				table.row();
				table.add(new Label("Cost: "+a.cost, skin));
			}
			
			return table;
		}

		@Override
		public Color getColourForData(Object data)
		{
			return null;
		}

		@Override
		public void onDrawItemBackground(Object data, Batch batch, int x, int y, int width, int height)
		{
		}

		@Override
		public void onDrawItem(Object data, Batch batch, int x, int y, int width, int height)
		{
		}

		@Override
		public void onDrawItemForeground(Object data, Batch batch, int x, int y, int width, int height)
		{
			Ability a = (Ability)data;
			
			if (!a.unlocked)
			{
				batch.setColor(0.5f, 0.5f, 0.5f, 0.5f);
				batch.draw(white, x, y, width, height);
				
				batch.setColor(Color.WHITE);
				locked.render(batch, x, y, width, height);
			}
		}
		
	}
}
