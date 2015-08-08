package Roguelike.UI;

import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.Ability.IAbility;
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
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;

public class AbilityPanel extends TilePanel
{			
	private BitmapFont font;
	private Texture white;

	private final GlyphLayout layout = new GlyphLayout();
	
	public AbilityPanel(Skin skin, Stage stage)
	{
		super(skin, stage, AssetManager.loadSprite("GUI/TileBackground"), AssetManager.loadSprite("GUI/TileBorder"), Global.NUM_ABILITY_SLOTS, 2, 32, false);
		
		this.font = new BitmapFont();
		this.white = AssetManager.loadTexture("Sprites/white.png");				
	}

	@Override
	public void populateTileData()
	{
		tileData.clear();
		
		for (ActiveAbility aa : Global.abilityPool.slottedActiveAbilities)
		{
			tileData.add(aa);
		}
		
		for (PassiveAbility pa : Global.abilityPool.slottedPassiveAbilities)
		{
			tileData.add(pa);
		}
	}

	@Override
	public Sprite getSpriteForData(Object data)
	{
		if (data == null) { return null; }
		
		return ((IAbility)data).getIcon();
	}

	@Override
	public void handleDataClicked(Object data, InputEvent event, float x, float y)
	{
		if (data instanceof ActiveAbility)
		{
			ActiveAbility aa = (ActiveAbility)data;
			
			if (aa.isAvailable())
			{
				GameScreen.Instance.prepareAbility(aa);
			}
		}
	}

	@Override
	public Table getToolTipForData(Object data)
	{
		if (data == null) { return null; }
		
		return ((IAbility)data).createTable(skin);
	}

	@Override
	public Color getColourForData(Object data)
	{
		if (data == null) { return Color.DARK_GRAY; }
		
		if (data == GameScreen.Instance.preparedAbility) { return Color.CYAN; }
		
		return null;
	}

	@Override
	public void onDrawItemBackground(Object data, Batch batch, int x, int y, int width, int height)
	{
	}

	@Override
	public void onDrawItem(Object data, Batch batch, int x, int y, int width, int height)
	{
		if (data instanceof ActiveAbility)
		{
			ActiveAbility aa = (ActiveAbility)data;
			
			if (!aa.isAvailable())
			{
				String text = "" + (int)Math.ceil(aa.cooldownAccumulator);
				layout.setText(font, text);
				
				batch.setColor(0.4f, 0.4f, 0.4f, 0.4f);
				batch.draw(white, x, y, width, height);
				batch.setColor(Color.WHITE);
				
				font.draw(batch, text, x + width/2 - layout.width/2, y + height/2 + layout.height/2);
			}
		}	
	}

	@Override
	public void onDrawItemForeground(Object data, Batch batch, int x, int y, int width, int height)
	{
	}

}
