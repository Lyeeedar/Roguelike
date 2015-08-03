package Roguelike.Tiles;

import Roguelike.Global;
import Roguelike.Global.Passability;
import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Entity.GameEntity;
import Roguelike.Items.Item;
import Roguelike.Levels.Level;
import Roguelike.Pathfinding.PathfindingTile;
import Roguelike.Shadows.ShadowCastTile;
import Roguelike.Sprite.SpriteEffect;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class GameTile implements ShadowCastTile, PathfindingTile
{
	public int x;
	public int y;
	
	public TileData tileData;
	
	public Color light;
	
	public GameEntity entity;
	public EnvironmentEntity environmentEntity;
		
	public Level level;
	
	public Array<SpriteEffect> spriteEffects = new Array<SpriteEffect>();
	
	public Array<Item> items = new Array<Item>(false, 16);
	
	public String metaValue;
		
	boolean visible;
	
	public GameTile(int x, int y, Level level, TileData tileData)
	{
		this.x = x;
		this.y = y;
		this.level = level;
		
		this.tileData = tileData;
		
		light = new Color(Color.WHITE);
	}
	
	public void addEnvironmentEntity(EnvironmentEntity entity)
	{
		if (entity.tile != null)
		{
			entity.tile.environmentEntity = null;
		}
		
		environmentEntity = entity;
		entity.tile = this;
	}
	
	public int[] addObject(GameEntity obj)
	{
		GameTile oldTile = obj.tile;
		
		entity = obj;
		obj.tile = this;
		
		if (oldTile != null)
		{
			oldTile.entity = null;
			return getPosDiff(oldTile);
		}
		
		return new int[]{0, 0};
	}
	
	public int[] getPosDiff(GameTile prevTile)
	{
		int[] oldPos = new int[]{prevTile.x * Global.TileSize, prevTile.y * Global.TileSize};		
		int[] newPos = {x * Global.TileSize, y * Global.TileSize};
		
		int[] diff = {oldPos[0] - newPos[0], oldPos[1] - newPos[1]};
		
		return diff;
	}
	
	public float getDist(GameTile prevTile)
	{				
		return Vector2.dst(prevTile.x, prevTile.y, x, y);
	}
	
	@Override
	public boolean GetOpaque()
	{
		if (environmentEntity != null && environmentEntity.opaque)
		{
			 return true;
		}
		
		return tileData.opaque;
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
	public boolean getPassable(Array<Passability> travelType)
	{
		if (environmentEntity != null && !Passability.isPassable(environmentEntity.passableBy, travelType)) { return false; }
		
		boolean passable = Passability.isPassable(tileData.passableBy, travelType);
		
		if (!passable) { return false; }
		
		return travelType.contains(Passability.ENTITY, true) || entity == null;
	}

	
	@Override
	public int getInfluence()
	{
		return 0;
	}
}
