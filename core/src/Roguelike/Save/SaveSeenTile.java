package Roguelike.Save;

import Roguelike.Global;
import Roguelike.Tiles.SeenTile;
import Roguelike.Tiles.SeenTile.SeenHistoryItem;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;

public final class SaveSeenTile extends SaveableObject<SeenTile>
{
	public boolean seen = false;

	public Array<SeenHistoryItem> tileHistory = new Array<SeenHistoryItem>();
	public Array<SeenHistoryItem> fieldHistory = new Array<SeenHistoryItem>();
	public SeenHistoryItem environmentHistory;
	public SeenHistoryItem entityHistory;
	public SeenHistoryItem itemHistory;
	public Array<SeenHistoryItem> orbHistory = new Array<SeenHistoryItem>(  );

	public Color light = new Color( 1 );

	@Override
	public void store( SeenTile obj )
	{
		seen = obj.seen;

		light.set( obj.light );

		// tile history
		Global.SeenHistoryItemPool.freeAll( tileHistory );
		tileHistory.clear();
		for ( SeenHistoryItem item : obj.tileHistory )
		{
			tileHistory.add( item.copy() );
		}

		// field history
		Global.SeenHistoryItemPool.freeAll( fieldHistory );
		fieldHistory.clear();
		for ( SeenHistoryItem item : obj.fieldHistory )
		{
			fieldHistory.add( item.copy() );
		}

		// environment history
		if ( environmentHistory != null )
		{
			Global.SeenHistoryItemPool.free( environmentHistory );
			environmentHistory = null;
		}
		if ( obj.environmentHistory != null )
		{
			environmentHistory = obj.environmentHistory.copy();
		}

		// entity history
		if ( entityHistory != null )
		{
			Global.SeenHistoryItemPool.free( entityHistory );
			entityHistory = null;
		}
		if ( obj.entityHistory != null )
		{
			entityHistory = obj.entityHistory.copy();
		}

		// item history
		if ( itemHistory != null )
		{
			Global.SeenHistoryItemPool.free( itemHistory );
			itemHistory = null;
		}
		if ( obj.itemHistory != null )
		{
			itemHistory = obj.itemHistory.copy();
		}

		// essence history
		Global.SeenHistoryItemPool.freeAll( orbHistory );
		orbHistory.clear();

		for (SeenHistoryItem hist : obj.orbHistory)
		{
			orbHistory.add( hist.copy() );
		}
	}

	@Override
	public SeenTile create()
	{
		SeenTile tile = new SeenTile();
		tile.seen = seen;
		tile.light.set( light );

		// tile history
		Global.SeenHistoryItemPool.freeAll( tile.tileHistory );
		tile.tileHistory.clear();
		for ( SeenHistoryItem item : tileHistory )
		{
			tile.tileHistory.add( item.copy() );
		}

		// field history
		Global.SeenHistoryItemPool.freeAll( tile.fieldHistory );
		tile.fieldHistory.clear();
		for ( SeenHistoryItem item : fieldHistory )
		{
			tile.fieldHistory.add( item.copy() );
		}

		// environment history
		if ( tile.environmentHistory != null )
		{
			Global.SeenHistoryItemPool.free( tile.environmentHistory );
			tile.environmentHistory = null;
		}
		if ( environmentHistory != null )
		{
			tile.environmentHistory = environmentHistory.copy();
		}

		// entity history
		if ( tile.entityHistory != null )
		{
			Global.SeenHistoryItemPool.free( tile.entityHistory );
			tile.entityHistory = null;
		}
		if ( entityHistory != null )
		{
			tile.entityHistory = entityHistory.copy();
		}

		// item history
		if ( tile.itemHistory != null )
		{
			Global.SeenHistoryItemPool.free( tile.itemHistory );
			tile.itemHistory = null;
		}
		if ( itemHistory != null )
		{
			tile.itemHistory = itemHistory.copy();
		}

		// essence history
		Global.SeenHistoryItemPool.freeAll( tile.orbHistory );
		tile.orbHistory.clear();

		for ( SeenHistoryItem hist : orbHistory )
		{
			tile.orbHistory.add( hist.copy() );
		}

		return tile;
	}
}
