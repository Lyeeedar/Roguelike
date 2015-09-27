package Roguelike.Tiles;

import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.Global.Direction;
import Roguelike.Entity.GameEntity;
import Roguelike.Fields.Field;
import Roguelike.Fields.Field.FieldLayer;
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
	public SeenHistoryItem essenceHistory;

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

		if ( tile.tileData.raisedSprite != null )
		{
			if ( nextTile != null && nextTile.tileData.raisedSprite != null && nextTile.tileData.raisedSprite.name.equals( tile.tileData.raisedSprite.name ) )
			{
				overhangHistory = Global.SeenHistoryItemPool.obtain().set( tile.tileData.raisedSprite.topSprite );
			}
			else
			{
				tileHistory.add( Global.SeenHistoryItemPool.obtain().set( tile.tileData.raisedSprite.frontSprite ) );
			}

			if ( tile.tileData.raisedSprite.overhangSprite != null
					&& prevTile != null
					&& prevTile.visible
					&& ( prevTile.tileData.raisedSprite == null || !prevTile.tileData.raisedSprite.name.equals( tile.tileData.raisedSprite.name ) ) )
			{
				prevSeenTile.overhangHistory = Global.SeenHistoryItemPool.obtain().set( tile.tileData.raisedSprite.overhangSprite );
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
				&& ( tile.environmentEntity.sprite != null || tile.environmentEntity.raisedSprite != null ) )
		{
			if ( environmentHistory == null )
			{
				environmentHistory = Global.SeenHistoryItemPool.obtain();
			}

			Sprite sprite = tile.environmentEntity.sprite;

			if ( tile.environmentEntity.raisedSprite != null )
			{
				if ( nextTile != null
						&& nextTile.tileData.raisedSprite != null
						&& nextTile.tileData.raisedSprite.name.equals( tile.environmentEntity.raisedSprite.name ) )
				{
					sprite = tile.environmentEntity.raisedSprite.topSprite;
				}
				else
				{
					sprite = tile.environmentEntity.raisedSprite.frontSprite;
				}
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

		// Store essence history
		if ( tile.essence > 0 )
		{
			Sprite sprite = AssetManager.loadSprite( "orb" );
			float scale = 0.5f + 0.5f * ( MathUtils.clamp( tile.essence, 10.0f, 1000.0f ) / 1000.0f );
			sprite.baseScale[0] = scale;
			sprite.baseScale[1] = scale;

			if ( essenceHistory == null )
			{
				essenceHistory = Global.SeenHistoryItemPool.obtain();
			}

			essenceHistory.set( sprite );
		}
		else if ( essenceHistory != null )
		{
			Global.SeenHistoryItemPool.free( essenceHistory );
			essenceHistory = null;
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
