package Roguelike.UI;

import Roguelike.Ability.AbilityTree;
import Roguelike.AssetManager;
import Roguelike.Entity.Tasks.TaskAttack;
import Roguelike.Global;
import Roguelike.Items.Item;
import Roguelike.RoguelikeGame;
import Roguelike.Screens.GameScreen;
import Roguelike.Sprite.Sprite;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

public class EquipmentPanel extends TilePanel
{
	private final GlyphLayout layout = new GlyphLayout();
	private BitmapFont font;
	private TextureRegion white;

	public EquipmentPanel( Skin skin, Stage stage )
	{
		super( skin, stage, AssetManager.loadSprite( "GUI/TileBackground" ), AssetManager.loadSprite( "GUI/TileBorder" ), 1, Item.EquipmentSlot.values().length + 1, 48, false );

		drawHorizontalBackground = false;
		font = skin.getFont( "default" );
		padding = 10;

		this.white = AssetManager.loadTextureRegion( "Sprites/white.png" );
	}

	@Override
	public void populateTileData()
	{
		tileData.clear();

		for ( Item.EquipmentSlot slot : Item.EquipmentSlot.values() )
		{
			Item item = Global.CurrentLevel.player.getInventory().getEquip( slot );

			if ( item == null )
			{
				tileData.add( slot );
			}
			else
			{
				tileData.add( item );
			}
		}

		int upgradeStone = Global.CurrentLevel.player.inventory.upgradeStones;
		tileData.add( upgradeStone );
	}

	@Override
	public Sprite getSpriteForData( Object data )
	{
		if ( !( data instanceof Item ) ) { return null; }

		return ( (Item) data ).getIcon();
	}

	@Override
	public void handleDataClicked( final Object data, InputEvent event, float x, float y )
	{
		if ( data instanceof Item )
		{
			Item item = (Item)data;

			if ( item.getMainSlot() == Item.EquipmentSlot.WEAPON )
			{
				GameScreen.Instance.weaponTiles = TaskAttack.buildAllDirectionHitTiles( Global.CurrentLevel.player );
			}
		}
		else if ( data instanceof Integer )
		{
			int stones = Global.CurrentLevel.player.inventory.upgradeStones;

			Table table = new Table();
			table.defaults().pad( 5 );

			table.add( new Label("You have " + stones + " upgrade stones.", skin) ).colspan( 2 );
			table.row();
			table.add( new Seperator( skin ) ).colspan( 2 ).expandX().fillX();
			table.row();

			for ( Item.EquipmentSlot slot : Item.EquipmentSlot.values() )
			{
				final Item equip = Global.CurrentLevel.player.inventory.getEquip( slot );

				if ( equip != null )
				{
					final int required = 2 * equip.upgradeCount;

					if (required <= stones)
					{
						TextButton button = new TextButton(equip.getName(), skin);
						button.addListener( new InputListener()
						{
							@Override
							public boolean touchDown( InputEvent event, float x, float y, int pointer, int button )
							{
								return true;
							}

							@Override
							public void touchUp( InputEvent event, float x, float y, int pointer, int button )
							{
								GameScreen.Instance.lockContextMenu = false;
								GameScreen.Instance.clearContextMenu();

								Global.CurrentLevel.player.inventory.upgradeStones -= required;
								equip.upgrade();

								Global.CurrentLevel.player.isVariableMapDirty = true;
								for ( int i = 0; i < Global.CurrentLevel.player.slottedAbilities.size; i++ )
								{
									AbilityTree tree = Global.CurrentLevel.player.slottedAbilities.get( i );
									if (tree != null)
									{
										tree.current.current.setCaster( Global.CurrentLevel.player );
									}
								}
							}
						} );
						table.add( button ).expandX().left();
					}
					else
					{
						table.add( new Label(equip.getName(), skin) ).expandX().left();
					}

					table.add( new Label( "Requires " + required + " stones", skin ) ).expandX().left();

					table.row();
				}
			}

			table.add( new Seperator( skin ) ).colspan( 2 ).expandX().fillX();
			table.row();

			TextButton cancel = new TextButton( "Cancel", skin );
			cancel.addListener( new InputListener()
			{
				@Override
				public boolean touchDown( InputEvent event, float x, float y, int pointer, int button )
				{
					return true;
				}

				@Override
				public void touchUp( InputEvent event, float x, float y, int pointer, int button )
				{
					GameScreen.Instance.lockContextMenu = false;
					GameScreen.Instance.clearContextMenu();
				}
			} );

			table.add( cancel ).colspan( 2 );
			table.row();

			GameScreen.Instance.displayContextMenu( table, true );
		}
	}

	@Override
	public Table getToolTipForData( Object data )
	{
		if ( data instanceof Item.EquipmentSlot )
		{
			Table table = new Table();

			table.add( new Label( Global.capitalizeString( data.toString() ), skin ) );

			return table;
		}
		else if ( data instanceof Integer )
		{
			Table table = new Table();

			table.add( new Label( "Upgrade Stones: " + data.toString(), skin ) );

			return table;
		}

		return ( (Item) data ).createTable( skin, Global.CurrentLevel.player );
	}

	@Override
	public Color getColourForData( Object data )
	{
		if (data instanceof Integer)
		{
			int stones = (Integer)data;

			for ( Item.EquipmentSlot slot : Item.EquipmentSlot.values() )
			{
				final Item equip = Global.CurrentLevel.player.inventory.getEquip( slot );

				if ( equip != null )
				{
					final int required = 2 * equip.upgradeCount;

					if (required <= stones)
					{
						return null;
					}
				}
			}
		}

		if ( !( data instanceof Item ) ) { return Color.DARK_GRAY; }

		return null;
	}

	@Override
	public void onDrawItemBackground( Object data, Batch batch, int x, int y, int width, int height )
	{

	}

	@Override
	public void onDrawItem( Object data, Batch batch, int x, int y, int width, int height )
	{
		if ( data instanceof Integer )
		{
			String text = data.toString() + "\n" + "stones";
			layout.setText( font, text, Color.WHITE, 0, Align.center, false );

			font.draw( batch, layout, x + width / 2, y + height / 2 + layout.height / 2 );
		}
	}

	@Override
	public void onDrawItemForeground( Object data, Batch batch, int x, int y, int width, int height )
	{

	}

}
