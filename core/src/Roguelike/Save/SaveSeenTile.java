package Roguelike.Save;

import Roguelike.Tiles.SeenTile;
import Roguelike.Tiles.SeenTile.SeenHistoryItem;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;

public class SaveSeenTile extends SaveableObject<SeenTile>
{
	public boolean seen = false;
	public Array<SeenHistoryItem> history = new Array<SeenHistoryItem>();
	
	@Override
	public void store(SeenTile obj)
	{
		seen = obj.seen;
		
		Pools.freeAll(history);		
		history.clear();
		
		for (SeenHistoryItem item : obj.history)
		{
			history.add(item.copy());
		}
	}
	
	@Override
	public SeenTile create()
	{
		SeenTile tile = new SeenTile();
		tile.seen = seen;
		
		Pools.freeAll(tile.history);
		tile.history.clear();
		
		for (SeenHistoryItem item : history)
		{
			tile.history.add(item.copy());
		}
		
		return tile;
	}
}
