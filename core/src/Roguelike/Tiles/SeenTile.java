package Roguelike.Tiles;

import Roguelike.AssetManager;
import Roguelike.Entity.GameEntity;
import Roguelike.Fields.Field;
import Roguelike.Fields.Field.FieldLayer;
import Roguelike.Global;
import Roguelike.Global.Direction;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.Sprite.AnimationState;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class SeenTile
{
	public boolean seen = false;

	public Array<SeenHistoryItem> tileHistory = new Array<SeenHistoryItem>( false, 1, SeenHistoryItem.class );
	public SeenHistoryItem overhangHistory;
	public Array<SeenHistoryItem> fieldHistory = new Array<SeenHistoryItem>( false, 1, SeenHistoryItem.class );
	public SeenHistoryItem environmentHistory;
	public SeenHistoryItem entityHistory;
	public SeenHistoryItem itemHistory;

	public Array<SeenHistoryItem> orbHistory = new Array<SeenHistoryItem>( false, 1, SeenHistoryItem.class );

	public GameTile gameTile;
	public Color light = new Color( 1 );

	public void set( GameTile tile, GameEntity player, GameTile prevTile, GameTile nextTile, SeenTile prevSeenTile )
	{
		light.set( tile.light );
		gameTile = tile;

		// Update tile history list size

		Global.SeenHistoryItemPool.freeAll( tileHistory );
		tileHistory.clear();

		// Store tile history
		for ( int i = 0; i < tile.tileData.sprites.length; i++ )
		{
			tileHistory.add( Global.SeenHistoryItemPool.obtain().set( tile.tileData.sprites[i] ) );
		}

		if ( tile.tileData.tilingSprite != null )
		{
//			if ( nextTile != null && nextTile.tileData.tilingSprite != null && nextTile.tileData.tilingSprite.name.equals( tile.tileData.tilingSprite.name ) )
//			{
//				overhangHistory = Global.SeenHistoryItemPool.obtain().set( tile.tileData.tilingSprite.topSprite );
//			}
//			else
//			{
//				tileHistory.add( Global.SeenHistoryItemPool.obtain().set( tile.tileData.tilingSprite.frontSprite ) );
//			}

			if ( tile.tileData.tilingSprite.overhangSprite != null
					&& prevTile != null
					&& prevTile.visible
					&& ( prevTile.tileData.tilingSprite == null || !prevTile.tileData.tilingSprite.name.equals( tile.tileData.tilingSprite.name ) ) )
			{
				prevSeenTile.overhangHistory = Global.SeenHistoryItemPool.obtain().set( tile.tileData.tilingSprite.overhangSprite );
			}
		}

		if ( tile.hasFields )
		{
			// Update field history list size
			if ( fieldHistory.size != tile.fields.size )
			{
				Global.SeenHistoryItemPool.freeAll( fieldHistory );
				fieldHistory.clear();

				for ( int i = 0; i < tile.fields.size; i++ )
				{
					fieldHistory.add( Global.SeenHistoryItemPool.obtain() );
				}
			}

			// Store field history
			int i = 0;
			for ( FieldLayer layer : FieldLayer.values() )
			{
				Field field = tile.fields.get( layer );
				if ( field != null )
				{
					fieldHistory.get( i++ ).set( field.sprite );
				}
			}
		}
		else if ( fieldHistory.size > 0 )
		{
			// Clear field history if it should be empty
			Global.SeenHistoryItemPool.freeAll( fieldHistory );
			fieldHistory.clear();
		}

		// Store environment history
		if ( tile.environmentEntity != null
				&& tile.environmentEntity.tile[0][0] == tile
				&& ( tile.environmentEntity.sprite != null || tile.environmentEntity.tilingSprite != null ) )
		{
			if ( environmentHistory == null )
			{
				environmentHistory = Global.SeenHistoryItemPool.obtain();
			}

			Sprite sprite = tile.environmentEntity.sprite;

			if ( tile.environmentEntity.tilingSprite != null )
			{
//				if ( nextTile != null
//						&& nextTile.tileData.tilingSprite != null
//						&& nextTile.tileData.tilingSprite.name.equals( tile.environmentEntity.tilingSprite.name ) )
//				{
//					sprite = tile.environmentEntity.tilingSprite.topSprite;
//				}
//				else
//				{
//					sprite = tile.environmentEntity.tilingSprite.frontSprite;
//				}
			}

			environmentHistory.set( sprite );
			environmentHistory.location = tile.environmentEntity.location;
		}
		else if ( environmentHistory != null )
		{
			Global.SeenHistoryItemPool.free( environmentHistory );
			environmentHistory = null;
		}

		// Store entity history
		if ( tile.entity != null && tile.entity != player && tile.entity.tile[0][0] == tile && tile.entity.sprite != null )
		{
			if ( entityHistory == null )
			{
				entityHistory = Global.SeenHistoryItemPool.obtain();
			}

			entityHistory.set( tile.entity.sprite );
		}
		else if ( entityHistory != null )
		{
			Global.SeenHistoryItemPool.free( entityHistory );
			entityHistory = null;
		}

		// Store item history
		if ( tile.items.size == 0 )
		{
			if ( itemHistory != null )
			{
				Global.SeenHistoryItemPool.free( itemHistory );
				itemHistory = null;
			}
		}
		else if ( tile.items.size == 1 )
		{
			if ( itemHistory == null )
			{
				itemHistory = Global.SeenHistoryItemPool.obtain();
			}

			itemHistory.set( tile.items.get( 0 ).getIcon() );
		}
		else
		{
			if ( itemHistory == null )
			{
				itemHistory = Global.SeenHistoryItemPool.obtain();
			}

			itemHistory.set( AssetManager.loadSprite( "bag" ) );
		}

		Global.SeenHistoryItemPool.freeAll( orbHistory );
		orbHistory.clear();

		if ( tile.orbs.size > 0 )
		{
			for ( GameTile.OrbType type : GameTile.OrbType.values() )
			{
				if ( tile.orbs.containsKey( type ) )
				{
					int val = tile.orbs.get( type );

					Sprite sprite = AssetManager.loadSprite( "orb" );
					float scale = 0.5f + 0.5f * ( MathUtils.clamp( val, 10.0f, 1000.0f ) / 1000.0f );
					sprite.baseScale[ 0 ] = scale;
					sprite.baseScale[ 1 ] = scale;

					SeenHistoryItem history = Global.SeenHistoryItemPool.obtain();

					history.set( sprite );

					orbHistory.add( history );
				}
			}
		}
	}

	public static final class SeenHistoryItem
	{
		public Sprite sprite;
		public AnimationState animationState = new AnimationState();
		public Direction location = Direction.CENTER;

		public SeenHistoryItem()
		{

		}

		public SeenHistoryItem( Sprite sprite )
		{
			set( sprite );
		}

		public SeenHistoryItem set( Sprite sprite )
		{
			this.sprite = sprite;
			this.animationState.set( sprite.animationState );
			this.location = Direction.CENTER;

			return this;
		}

		public SeenHistoryItem copy()
		{
			SeenHistoryItem item = Global.SeenHistoryItemPool.obtain();

			item.sprite = sprite;
			item.animationState = animationState;
			item.location = location;

			return item;
		}
	}
}
