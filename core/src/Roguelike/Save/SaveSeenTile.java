package Roguelike.Save;

import Roguelike.Tiles.SeenTile;
import Roguelike.Tiles.SeenTile.SeenHistoryItem;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;

public final class SaveSeenTile extends SaveableObject<SeenTile>
{
	public boolean seen = false;

	public Array<SeenHistoryItem> tileHistory = new Array<SeenHistoryItem>();
	public Array<SeenHistoryItem> fieldHistory = new Array<SeenHistoryItem>();
	public SeenHistoryItem environmentHistory;
	public SeenHistoryItem entityHistory;
	public SeenHistoryItem itemHistory;
	public SeenHistoryItem essenceHistory;

	public Color light = new Color( 1 );

	@Override
	public void store( SeenTile obj )
	{
		seen = obj.seen;

		light.set( obj.light );

		// tile history
		Pools.freeAll( tileHistory );
		tileHistory.clear();
		for ( SeenHistoryItem item : obj.tileHistory )
		{
			tileHistory.add( item.copy() );
		}

		// field history
		Pools.freeAll( fieldHistory );
		fieldHistory.clear();
		for ( SeenHistoryItem item : obj.fieldHistory )
		{
			fieldHistory.add( item.copy() );
		}

		// environment history
		if ( environmentHistory != null )
		{
			Pools.free( environmentHistory );
			environmentHistory = null;
		}
		if ( obj.environmentHistory != null )
		{
			environmentHistory = obj.environmentHistory.copy();
		}

		// entity history
		if ( entityHistory != null )
		{
			Pools.free( entityHistory );
			entityHistory = null;
		}
		if ( obj.entityHistory != null )
		{
			entityHistory = obj.entityHistory.copy();
		}

		// item history
		if ( itemHistory != null )
		{
			Pools.free( itemHistory );
			itemHistory = null;
		}
		if ( obj.itemHistory != null )
		{
			itemHistory = obj.itemHistory.copy();
		}

		// essence history
		if ( essenceHistory != null )
		{
			Pools.free( essenceHistory );
			essenceHistory = null;
		}
		if ( obj.essenceHistory != null )
		{
			essenceHistory = obj.essenceHistory.copy();
		}
	}

	@Override
	public SeenTile create()
	{
		SeenTile tile = new SeenTile();
		tile.seen = seen;
		tile.light.set( light );

		// tile history
		Pools.freeAll( tile.tileHistory );
		tile.tileHistory.clear();
		for ( SeenHistoryItem item : tileHistory )
		{
			tile.tileHistory.add( item.copy() );
		}

		// field history
		Pools.freeAll( tile.fieldHistory );
		tile.fieldHistory.clear();
		for ( SeenHistoryItem item : fieldHistory )
		{
			tile.fieldHistory.add( item.copy() );
		}

		// environment history
		if ( tile.environmentHistory != null )
		{
			Pools.free( tile.environmentHistory );
			tile.environmentHistory = null;
		}
		if ( environmentHistory != null )
		{
			tile.environmentHistory = environmentHistory.copy();
		}

		// entity history
		if ( tile.entityHistory != null )
		{
			Pools.free( tile.entityHistory );
			tile.entityHistory = null;
		}
		if ( entityHistory != null )
		{
			tile.entityHistory = entityHistory.copy();
		}

		// item history
		if ( tile.itemHistory != null )
		{
			Pools.free( tile.itemHistory );
			tile.itemHistory = null;
		}
		if ( itemHistory != null )
		{
			tile.itemHistory = itemHistory.copy();
		}

		// essence history
		if ( tile.essenceHistory != null )
		{
			Pools.free( tile.essenceHistory );
			tile.essenceHistory = null;
		}
		if ( essenceHistory != null )
		{
			tile.essenceHistory = essenceHistory.copy();
		}

		return tile;
	}
}
