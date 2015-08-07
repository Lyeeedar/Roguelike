package Roguelike.Save;

import Roguelike.Tiles.SeenTile;
import Roguelike.Tiles.SeenTile.SeenHistoryItem;

import com.badlogic.gdx.utils.Array;

public class SaveSeenTile extends SaveableObject<SeenTile>
{
	public boolean seen = false;
	public Array<SeenHistoryItem> History = new Array<SeenHistoryItem>();
	
	@Override
	public void store(SeenTile obj)
	{
		seen = obj.seen;
		History = obj.History;
	}
	
	@Override
	public SeenTile create()
	{
		SeenTile tile = new SeenTile();
		tile.seen = seen;
		tile.History = History;
		
		return tile;
	}
}
