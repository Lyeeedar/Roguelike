package Roguelike.UI;

import Roguelike.Ability.AbilityTree;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Ability.IAbility;
import Roguelike.Ability.PassiveAbility.PassiveAbility;
import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.Items.Item;
import Roguelike.Screens.GameScreen;
import Roguelike.Sound.SoundInstance;
import Roguelike.Sprite.Sprite;
import com.badlogic.gdx.Game;
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
import com.badlogic.gdx.utils.Align;

public class AbilityPanel extends TilePanel
{
	private final GlyphLayout layout = new GlyphLayout();
	private BitmapFont font;
	private TextureRegion white;
	private TextureRegion passiveTileBorder;

	public AbilityPanel( Skin skin, Stage stage )
	{
		super( skin, stage, AssetManager.loadSprite( "GUI/TileBackground" ), AssetManager.loadSprite( "GUI/TileBorder" ), 1, Global.NUM_ABILITY_SLOTS, 48, false );

		drawHorizontalBackground = false;
		font = skin.getFont( "default" );
		padding = 10;

		this.white = AssetManager.loadTextureRegion( "Sprites/white.png" );
		passiveTileBorder = AssetManager.loadTextureRegion( "Sprites/GUI/PassiveTileBorder.png" );
	}

	@Override
	public void populateTileData()
	{
		tileData.clear();

		for ( AbilityTree a : Global.CurrentLevel.player.slottedAbilities )
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

		return ( (AbilityTree) data ).current.current.getIcon();
	}

	@Override
	public void handleDataClicked( final Object data, InputEvent event, float x, float y )
	{
		if ( data instanceof AbilityTree && ((AbilityTree)data).current.current instanceof ActiveAbility )
		{
			ActiveAbility aa = (ActiveAbility)((AbilityTree) data).current.current;

			if (GameScreen.Instance.preparedAbility == aa)
			{
				GameScreen.Instance.prepareAbility( null );
			}
			else if ( aa.isAvailable() )
			{
				GameScreen.Instance.prepareAbility( aa );
			}
		}
	}

	@Override
	public Table getToolTipForData( Object data )
	{
		if ( data == null || data instanceof Integer ) { return null; }

		return ( (AbilityTree) data ).current.current.createTable( skin, Global.CurrentLevel.player );
	}

	@Override
	public Color getColourForData( Object data )
	{
		if ( data == null || data instanceof Integer ) { return Color.DARK_GRAY; }

		if ( ((AbilityTree)data).current.current == GameScreen.Instance.preparedAbility ) { return Color.CYAN; }

		return null;
	}

	@Override
	public void onDrawItemBackground( Object data, Batch batch, int x, int y, int width, int height )
	{

	}

	@Override
	public void onDrawItem( Object data, Batch batch, int x, int y, int width, int height )
	{
		if ( data instanceof AbilityTree && ((AbilityTree)data).current.current instanceof ActiveAbility )
		{
			ActiveAbility aa = (ActiveAbility)((AbilityTree) data).current.current;

			if ( !aa.isAvailable() )
			{
				batch.setColor( 0.1f, 0.1f, 0.1f, 0.75f );
				batch.draw( white, x, y, width, height );
				batch.setColor( Color.WHITE );

				int cooldown = (int) Math.ceil( aa.cooldownAccumulator );

				if (cooldown > 0)
				{
					String text = aa.cooldownType.text + "\n" + cooldown;
					layout.setText( font, text, Color.WHITE, 0, Align.center, false );

					font.draw( batch, layout, x + width / 2, y + height / 2 + layout.height / 2 );
				}
			}
		}
	}

	@Override
	public void onDrawItemForeground( Object data, Batch batch, int x, int y, int width, int height )
	{
		if (data instanceof AbilityTree)
		{
			AbilityTree tree = (AbilityTree) data;

			if (( (AbilityTree) data ).current.current instanceof PassiveAbility)
			{
				batch.draw( passiveTileBorder, x, y, width, height );
			}

			if (tree.current.needsLevelAnim)
			{
				Sprite sprite = AssetManager.loadSprite( "EffectSprites/Heal/Heal", 0.1f );

				GameScreen.Instance.addSpriteAction( sprite, x+width/2, y+height/2, width, height );
				Global.CurrentLevel.player.pendingMessages.add( new Message( tree.current.current.getName() + " levelled up! (Level " + tree.current.current.getLevel() + ")", Color.GOLD ) );

				levelUpSound.play( Global.CurrentLevel.player.tile[0][0] );

				tree.current.needsLevelAnim = false;
			}
		}
	}

	public static final SoundInstance levelUpSound = SoundInstance.getSound( "LevelUp" );

}
