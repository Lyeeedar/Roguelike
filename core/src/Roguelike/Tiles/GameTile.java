package Roguelike.Tiles;

import java.util.HashSet;

import Roguelike.Entity.Entity;
import Roguelike.Items.Item;
import Roguelike.Levels.Level;
import Roguelike.Pathfinding.PathfindingTile;
import Roguelike.Shadows.ShadowCastTile;
import Roguelike.Sprite.SpriteEffect;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;

public class GameTile implements ShadowCastTile, PathfindingTile
{
	public int x;
	public int y;
	
	public TileData TileData;
	
	public Color Light;
	
	public Entity Entity;
	public Level Level;
	
	public Array<SpriteEffect> SpriteEffects = new Array<SpriteEffect>();
	
	public Array<Item> Items = new Array<Item>(false, 16);
		
	boolean visible;
	
	public GameTile(int x, int y, Level level, TileData tileData)
	{
		this.x = x;
		this.y = y;
		this.Level = level;
		
		this.TileData = tileData;
		
		Light = new Color(Color.WHITE);
	}
		
	public void addObject(Entity obj)
	{
		if (obj.Tile != null)
		{
			obj.Tile.Entity = null;
		}
		
		Entity = obj;
		obj.Tile = this;
	}
	
	@Override
	public boolean GetOpaque()
	{
		return TileData.Opaque;
	}

	@Override
	public boolean GetVisible()
	{
		return visible;
	}
	
	@Override
	public void SetVisible(boolean visible)
	{
		this.visible = visible;
	}

	@Override
	public boolean GetPassable(HashSet<String> factions)
	{
		if (!TileData.Passable) { return false; }
		
		return Entity != null ? !Entity.isAllies(factions) : true ;
	}
}
