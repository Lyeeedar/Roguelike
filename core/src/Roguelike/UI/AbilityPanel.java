package Roguelike.UI;

import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.Ability.IAbility;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Ability.PassiveAbility.PassiveAbility;
import Roguelike.Screens.GameScreen;
import Roguelike.Sprite.Sprite;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;

public class AbilityPanel extends TilePanel
{
	private BitmapFont font;
	private TextureRegion white;

	private final GlyphLayout layout = new GlyphLayout();

	public AbilityPanel( Skin skin, Stage stage )
	{
		super( skin, stage, AssetManager.loadSprite( "GUI/TileBackground" ), AssetManager.loadSprite( "GUI/TileBorder" ), Global.NUM_ABILITY_SLOTS, 2, 32, false );

		font = AssetManager.loadFont( "Sprites/GUI/stan0755.ttf", 12 );

		this.white = AssetManager.loadTextureRegion( "Sprites/white.png" );
	}

	@Override
	public void populateTileData()
	{
		tileData.clear();

		for ( ActiveAbility aa : Global.abilityPool.slottedActiveAbilities )
		{
			tileData.add( aa );
		}

		for ( PassiveAbility pa : Global.abilityPool.slottedPassiveAbilities )
		{
			tileData.add( pa );
		}
	}

	@Override
	public Sprite getSpriteForData( Object data )
	{
		if ( data == null ) { return null; }

		return ( (IAbility) data ).getIcon();
	}

	@Override
	public void handleDataClicked( final Object data, InputEvent event, float x, float y )
	{
		if ( event.getButton() == Buttons.RIGHT )
		{
			if ( data != null )
			{
				Table table = new Table();

				TextButton button = new TextButton( "Clear ability slot?", skin );
				table.add( button ).width( Value.percentWidth( 1, table ) ).pad( 2 );

				table.addListener( new InputListener()
				{
					@Override
					public boolean touchDown( InputEvent event, float x, float y, int pointer, int button )
					{
						return true;
					}

					@Override
					public void touchUp( InputEvent event, float x, float y, int pointer, int button )
					{
						if ( data instanceof ActiveAbility )
						{
							ActiveAbility aa = (ActiveAbility) data;
							Global.abilityPool.clearActiveAbility( aa );
						}
						else
						{
							PassiveAbility pa = (PassiveAbility) data;
							Global.abilityPool.clearPassiveAbility( pa );
						}

						GameScreen.Instance.clearContextMenu();
					}
				} );

				table.pack();

				Tooltip tooltip = new Tooltip( table, skin, stage );
				tooltip.show( event, x - tooltip.getWidth() / 2, y - tooltip.getHeight() / 2 );

				GameScreen.Instance.contextMenu = tooltip;
			}
		}
		else
		{
			if ( data instanceof ActiveAbility )
			{
				ActiveAbility aa = (ActiveAbility) data;

				if ( aa.isAvailable() )
				{
					GameScreen.Instance.prepareAbility( aa );
				}
			}
		}
	}

	@Override
	public Table getToolTipForData( Object data )
	{
		if ( data == null ) { return null; }

		return ( (IAbility) data ).createTable( skin, Global.CurrentLevel.player );
	}

	@Override
	public Color getColourForData( Object data )
	{
		if ( data == null ) { return Color.DARK_GRAY; }

		if ( data == GameScreen.Instance.preparedAbility ) { return Color.CYAN; }

		return null;
	}

	@Override
	public void onDrawItemBackground( Object data, Batch batch, int x, int y, int width, int height )
	{
	}

	@Override
	public void onDrawItem( Object data, Batch batch, int x, int y, int width, int height )
	{
		if ( data instanceof ActiveAbility )
		{
			ActiveAbility aa = (ActiveAbility) data;

			if ( !aa.isAvailable() )
			{
				String text = "" + (int) Math.ceil( aa.cooldownAccumulator );
				layout.setText( font, text );

				batch.setColor( 0.4f, 0.4f, 0.4f, 0.4f );
				batch.draw( white, x, y, width, height );
				batch.setColor( Color.WHITE );

				font.draw( batch, text, x + width / 2 - layout.width / 2, y + height / 2 + layout.height / 2 );
			}
		}
	}

	@Override
	public void onDrawItemForeground( Object data, Batch batch, int x, int y, int width, int height )
	{
	}

}
