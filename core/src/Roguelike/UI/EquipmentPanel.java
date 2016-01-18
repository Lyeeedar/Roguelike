package Roguelike.UI;

import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.Items.Item;
import Roguelike.Sprite.Sprite;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class EquipmentPanel extends TilePanel
{
	private final GlyphLayout layout = new GlyphLayout();
	private BitmapFont font;
	private TextureRegion white;

	public EquipmentPanel( Skin skin, Stage stage )
	{
		super( skin, stage, AssetManager.loadSprite( "GUI/TileBackground" ), AssetManager.loadSprite( "GUI/TileBorder" ), 1, Item.EquipmentSlot.values().length, 48, false );

		drawHorizontalBackground = false;
		font = AssetManager.loadFont( "Sprites/GUI/stan0755.ttf", 12 );
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
	}

	@Override
	public Sprite getSpriteForData( Object data )
	{
		if ( data == null || data instanceof Item.EquipmentSlot ) { return null; }

		return ( (Item) data ).getIcon();
	}

	@Override
	public void handleDataClicked( final Object data, InputEvent event, float x, float y )
	{
		// show toolip + drop button
	}

	@Override
	public Table getToolTipForData( Object data )
	{
		if ( data == null || data instanceof Item.EquipmentSlot )
		{
			Table table = new Table();

			table.add( new Label( Global.capitalizeString( data.toString() ), skin ) );

			return table;
		}

		return ( (Item) data ).createTable( skin, Global.CurrentLevel.player );
	}

	@Override
	public Color getColourForData( Object data )
	{
		if ( data == null || data instanceof Item.EquipmentSlot ) { return Color.DARK_GRAY; }

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
