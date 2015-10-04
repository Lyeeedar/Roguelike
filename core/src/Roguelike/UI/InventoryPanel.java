package Roguelike.UI;

import java.util.HashMap;
import java.util.Iterator;

import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.Items.Item;
import Roguelike.Items.Item.ItemCategory;
import Roguelike.Sprite.Sprite;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;

public class InventoryPanel extends Table
{
	private int TileSize;

	private final Skin skin;
	private final Stage stage;

	private final Sprite tileBackground;
	private Sprite tileBorder;

	private final Sprite buttonUp;
	private final Sprite buttonDown;
	private final Sprite buttonBorder;

	private ItemCategory selectedFilter = ItemCategory.ALL;

	private HeaderLine header;
	private InventoryBody body;

	public InventoryPanel( Skin skin, Stage stage )
	{
		this.TileSize = 32;
		this.skin = skin;
		this.stage = stage;

		this.tileBackground = AssetManager.loadSprite( "GUI/TileBackground" );
		this.tileBorder = AssetManager.loadSprite( "GUI/TileBorder" );

		this.buttonUp = AssetManager.loadSprite( "GUI/Button" );
		this.buttonDown = AssetManager.loadSprite( "GUI/ButtonDown" );
		this.buttonBorder = AssetManager.loadSprite( "GUI/ButtonBorder" );

		header = new HeaderLine( skin, stage, buttonUp, tileBorder, TileSize );
		body = new InventoryBody( skin, stage, tileBackground, tileBorder, 32 );

		add( header ).width( Value.percentWidth( 1, this ) );
		row();
		add( body ).expand().fill().width( Value.percentWidth( 1, this ) );

		setTouchable( Touchable.childrenOnly );
	}

	public class HeaderLine extends TilePanel
	{
		private final HashMap<ItemCategory, Sprite> headers = new HashMap<ItemCategory, Sprite>();

		public HeaderLine( Skin skin, Stage stage, Sprite tileBackground, Sprite tileBorder, int tileSize )
		{
			super( skin, stage, tileBackground, tileBorder, ItemCategory.values().length, 1, tileSize, false );

			padding = 5;

			headers.put( ItemCategory.WEAPON, AssetManager.loadSprite( "GUI/Weapon" ) );
			headers.put( ItemCategory.ARMOUR, AssetManager.loadSprite( "GUI/Armour" ) );
			headers.put( ItemCategory.JEWELRY, AssetManager.loadSprite( "GUI/Jewelry" ) );
			headers.put( ItemCategory.TREASURE, AssetManager.loadSprite( "GUI/Treasure" ) );
			headers.put( ItemCategory.MATERIAL, AssetManager.loadSprite( "GUI/Ingot" ) );
			headers.put( ItemCategory.MISC, AssetManager.loadSprite( "GUI/Misc" ) );
			headers.put( ItemCategory.ALL, AssetManager.loadSprite( "GUI/All" ) );

			tileData.clear();
			for ( ItemCategory type : ItemCategory.values() )
			{
				tileData.add( type );
			}
		}

		@Override
		public void populateTileData()
		{
		}

		@Override
		public Sprite getSpriteForData( Object data )
		{
			return headers.get( data );
		}

		@Override
		public void handleDataClicked( Object data, InputEvent event, float x, float y )
		{
			selectedFilter = (ItemCategory) data;
		}

		@Override
		public Table getToolTipForData( Object data )
		{
			Table table = new Table();
			table.add( new Label( data.toString(), skin, "title" ) ).width( Value.percentWidth( 1, table ) );

			return table;
		}

		@Override
		public Color getColourForData( Object data )
		{
			if ( data == selectedFilter ) { return Color.DARK_GRAY; }

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
			buttonBorder.render( batch, x, y, width, height );
		}

	}

	public class InventoryBody extends TilePanel
	{
		private BitmapFont font;
		private final GlyphLayout layout = new GlyphLayout();

		public InventoryBody( Skin skin, Stage stage, Sprite tileBackground, Sprite tileBorder, int tileSize )
		{
			super( skin, stage, tileBackground, tileBorder, ItemCategory.values().length, 1, tileSize, true );

			font = AssetManager.loadFont( "Sprites/GUI/stan0755.ttf", 12 );

			padding = 5;
		}

		@Override
		public void populateTileData()
		{
			tileData.clear();

			Iterator<Item> itr = Global.CurrentLevel.player.getInventory().iterator( selectedFilter );
			while ( itr.hasNext() )
			{
				tileData.add( itr.next() );
			}

			dataHeight = tileData.size / viewWidth;
		}

		@Override
		public Sprite getSpriteForData( Object data )
		{
			return ( (Item) data ).getIcon();
		}

		@Override
		public void handleDataClicked( Object data, InputEvent event, float x, float y )
		{
			Item item = (Item) data;

			if ( event.getButton() == Buttons.RIGHT )
			{
				// show context menu:
				// Unequip, use?, destory
			}
			else
			{
				if ( item.getMainSlot() != null )
				{
					Global.CurrentLevel.player.getInventory().toggleEquip( item );
				}
			}
		}

		@Override
		public Table getToolTipForData( Object data )
		{
			return ( (Item) data ).createTable( skin, Global.CurrentLevel.player );
		}

		@Override
		public Color getColourForData( Object data )
		{
			Item item = (Item) data;

			if ( item.getMainSlot() != null )
			{
				if ( Global.CurrentLevel.player.getInventory().isEquipped( item ) ) { return Color.GREEN; }
			}

			return null;
		}

		@Override
		public void onDrawItemBackground( Object data, Batch batch, int x, int y, int width, int height )
		{
		}

		@Override
		public void onDrawItem( Object data, Batch batch, int x, int y, int width, int height )
		{
			Item item = (Item) data;
			if ( item.count > 1 )
			{
				String text = "" + item.count;
				layout.setText( font, text );
				font.draw( batch, text, x + width / 2 - layout.width / 2, y + height / 2 + layout.height / 2 );
			}
		}

		@Override
		public void onDrawItemForeground( Object data, Batch batch, int x, int y, int width, int height )
		{
		}

	}
}