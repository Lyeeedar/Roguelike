package Roguelike.UI;

import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Ability.IAbility;
import Roguelike.Ability.PassiveAbility.PassiveAbility;
import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.Items.Item;
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
	private final GlyphLayout layout = new GlyphLayout();
	private BitmapFont font;
	private TextureRegion white;

	public AbilityPanel( Skin skin, Stage stage )
	{
		super( skin, stage, AssetManager.loadSprite( "GUI/TileBackground" ), AssetManager.loadSprite( "GUI/TileBorder" ), Global.NUM_ABILITY_SLOTS, 1, 32, false );

		font = AssetManager.loadFont( "Sprites/GUI/stan0755.ttf", 12 );
		padding = 3;

		this.white = AssetManager.loadTextureRegion( "Sprites/white.png" );
	}

	@Override
	public void populateTileData()
	{
		tileData.clear();

		for ( IAbility a : Global.CurrentLevel.player.slottedAbilities )
		{
			if ( a != null )
			{
				tileData.add( a );
			}
			else
			{
				tileData.add( tileData.size );
			}
		}

		while ( tileData.size < Global.NUM_ABILITY_SLOTS )
		{
			tileData.add( tileData.size );
		}
	}

	@Override
	public Sprite getSpriteForData( Object data )
	{
		if ( data == null || data instanceof Integer ) { return null; }

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
							// Global.CurrentLevel.player.clearActiveAbility( aa
							// );
						}
						else
						{
							PassiveAbility pa = (PassiveAbility) data;
							// Global.CurrentLevel.player.clearPassiveAbility(
							// pa );
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
			if ( GameScreen.Instance.abilityToEquip != null )
			{
				int index = 0;
				if ( data instanceof Integer )
				{
					index = (Integer) data;
				}
				else
				{
					index = Global.CurrentLevel.player.slottedAbilities.indexOf( (IAbility) data, true );
				}

				while ( Global.CurrentLevel.player.slottedAbilities.size <= index )
				{
					Global.CurrentLevel.player.slottedAbilities.add( null );
				}

				Global.CurrentLevel.player.slottedAbilities.removeIndex( index );
				Global.CurrentLevel.player.slottedAbilities.insert( index, GameScreen.Instance.abilityToEquip );
				GameScreen.Instance.abilityToEquip = null;

				if ( data instanceof IAbility )
				{
					Item item = new Item();
					item.ability = (IAbility) data;

					Global.CurrentLevel.player.tile[ 0 ][ 0 ].items.add( item );
				}

				GameScreen.Instance.lockContextMenu = false;
				GameScreen.Instance.clearContextMenu();
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
	}

	@Override
	public Table getToolTipForData( Object data )
	{
		if ( data == null || data instanceof Integer ) { return null; }

		return ( (IAbility) data ).createTable( skin, Global.CurrentLevel.player );
	}

	@Override
	public Color getColourForData( Object data )
	{
		if ( data == null || data instanceof Integer ) { return Color.DARK_GRAY; }

		if ( data == GameScreen.Instance.preparedAbility ) { return Color.CYAN; }

		return null;
	}

	@Override
	public void onDrawItemBackground( Object data, Batch batch, int x, int y, int width, int height )
	{
		if ( GameScreen.Instance.abilityToEquip != null )
		{
			batch.setColor( Color.GOLD );
			tileBackground.render( batch, x, y, width, height );
		}
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
