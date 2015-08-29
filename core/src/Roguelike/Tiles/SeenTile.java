package Roguelike.Tiles;

import Roguelike.AssetManager;
import Roguelike.Global.Direction;
import Roguelike.Entity.GameEntity;
import Roguelike.Fields.Field;
import Roguelike.Fields.Field.FieldLayer;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.Sprite.AnimationState;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;

public class SeenTile
{
	public boolean seen = false;

	public Array<SeenHistoryItem> tileHistory = new Array<SeenHistoryItem>( false, 1, SeenHistoryItem.class );
	public Array<SeenHistoryItem> fieldHistory = new Array<SeenHistoryItem>( false, 1, SeenHistoryItem.class );
	public SeenHistoryItem environmentHistory;
	public SeenHistoryItem entityHistory;
	public SeenHistoryItem itemHistory;
	public SeenHistoryItem essenceHistory;

	public GameTile gameTile;
	public Color light = new Color( 1 );

	public void set( GameTile tile, GameEntity player )
	{
		light.set( tile.light );
		gameTile = tile;

		// Update tile history list size
		if ( tileHistory.size != tile.tileData.sprites.length )
		{
			Pools.freeAll( tileHistory );
			tileHistory.clear();

			for ( int i = 0; i < tile.tileData.sprites.length; i++ )
			{
				tileHistory.add( Pools.obtain( SeenHistoryItem.class ) );
			}
		}

		// Store tile history
		for ( int i = 0; i < tile.tileData.sprites.length; i++ )
		{
			tileHistory.get( i ).set( tile.tileData.sprites[i] );
		}

		if ( tile.hasFields )
		{
			// Update field history list size
			if ( fieldHistory.size != tile.fields.size() )
			{
				Pools.freeAll( fieldHistory );
				fieldHistory.clear();

				for ( int i = 0; i < tile.fields.size(); i++ )
				{
					fieldHistory.add( Pools.obtain( SeenHistoryItem.class ) );
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
			Pools.freeAll( fieldHistory );
			fieldHistory.clear();
		}

		// Store environment history
		if ( tile.environmentEntity != null )
		{
			if ( environmentHistory == null )
			{
				environmentHistory = Pools.obtain( SeenHistoryItem.class );
			}

			environmentHistory.set( tile.environmentEntity.sprite );
			environmentHistory.location = tile.environmentEntity.location;
		}
		else if ( environmentHistory != null )
		{
			Pools.free( environmentHistory );
			environmentHistory = null;
		}

		// Store entity history
		if ( tile.entity != null && tile.entity != player )
		{
			if ( entityHistory == null )
			{
				entityHistory = Pools.obtain( SeenHistoryItem.class );
			}

			entityHistory.set( tile.entity.sprite );
		}
		else if ( entityHistory != null )
		{
			Pools.free( entityHistory );
			entityHistory = null;
		}

		// Store item history
		if ( tile.items.size == 0 )
		{
			if ( itemHistory != null )
			{
				Pools.free( itemHistory );
				itemHistory = null;
			}
		}
		else if ( tile.items.size == 1 )
		{
			if ( itemHistory == null )
			{
				itemHistory = Pools.obtain( SeenHistoryItem.class );
			}

			itemHistory.set( tile.items.get( 0 ).getIcon() );
		}
		else
		{
			if ( itemHistory == null )
			{
				itemHistory = Pools.obtain( SeenHistoryItem.class );
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
				essenceHistory = Pools.obtain( SeenHistoryItem.class );
			}

			essenceHistory.set( sprite );
		}
		else if ( essenceHistory != null )
		{
			Pools.free( essenceHistory );
			essenceHistory = null;
		}
	}

	public static class SeenHistoryItem
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
			SeenHistoryItem item = Pools.obtain( SeenHistoryItem.class );

			item.sprite = sprite;
			item.animationState = animationState;
			item.location = location;

			return item;
		}
	}
}
