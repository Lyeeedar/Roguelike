package Roguelike.UI;

import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.Screens.GameScreen;
import Roguelike.Sprite.Sprite;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

/**
 * Created by Philip on 18-Jan-16.
 */
public class ButtonsPanel extends TilePanel
{
	private Sprite optionsSprite;
	private Sprite lookOffSprite;
	private Sprite lookOnSprite;
	private Sprite autoAttackOffSprite;
	private Sprite autoAttackOnSprite;

	public ButtonsPanel( Skin skin, Stage stage )
	{
		super( skin, stage, AssetManager.loadSprite( "GUI/TileBackground" ), AssetManager.loadSprite( "GUI/TileBorder" ), 3, 1, 32, false );

		padding = 10;

		canBeExamined = false;

		optionsSprite = AssetManager.loadSprite( "GUI/All" );
		lookOffSprite = AssetManager.loadSprite( "GUI/QuestionMark" );
		lookOnSprite = AssetManager.loadSprite( "GUI/Eye" );
		autoAttackOffSprite = AssetManager.loadSprite( "GUI/Sheathed" );
		autoAttackOnSprite = AssetManager.loadSprite( "GUI/Unsheathed" );
	}

	@Override
	public void populateTileData()
	{
		tileData.clear();

		tileData.add( 0 );
		tileData.add( 1 );
		tileData.add( 2 );
	}

	@Override
	public Sprite getSpriteForData( Object data )
	{
		Integer val = (Integer)data;
		if (val == 0)
		{
			return optionsSprite;
		}
		else if (val == 1)
		{
			if ( GameScreen.Instance.examineMode )
			{
				return lookOnSprite;
			}
			else
			{
				return lookOffSprite;
			}
		}
		else if (val == 2)
		{
			if (Global.CurrentLevel.player.weaponSheathed)
			{
				return autoAttackOffSprite;
			}
			else
			{
				return autoAttackOnSprite;
			}
		}

		return null;
	}

	@Override
	public void handleDataClicked( Object data, InputEvent event, float x, float y )
	{
		Integer val = (Integer)data;
		if (val == 0)
		{
			GameScreen.Instance.displayGameMenu();
		}
		else if (val == 1)
		{
			GameScreen.Instance.examineMode = !GameScreen.Instance.examineMode;
		}
		else if (val == 2)
		{
			Global.CurrentLevel.player.weaponSheathed = !Global.CurrentLevel.player.weaponSheathed;
		}
	}

	@Override
	public Table getToolTipForData( Object data )
	{
		return null;
	}

	@Override
	public Color getColourForData( Object data )
	{
		return null;
	}

	@Override
	public void onDrawItemBackground( Object data, Batch batch, int x, int y, int width, int height )
	{

	}

	@Override
	public void onDrawItem( Object data, Batch batch, int x, int y, int width, int height )
	{

	}

	@Override
	public void onDrawItemForeground( Object data, Batch batch, int x, int y, int width, int height )
	{

	}
}
