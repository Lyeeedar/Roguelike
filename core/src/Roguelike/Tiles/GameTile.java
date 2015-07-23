package Roguelike.Tiles;

import java.util.HashSet;

import Roguelike.RoguelikeGame;
import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Entity.GameEntity;
import Roguelike.Items.Item;
import Roguelike.Levels.Level;
import Roguelike.Pathfinding.PathfindingTile;
import Roguelike.Shadows.ShadowCastTile;
import Roguelike.Sprite.MoveAnimation;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.SpriteEffect;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;

public class GameTile implements ShadowCastTile, PathfindingTile
{
	public int x;
	public int y;
	
	public TileData TileData;
	
	public Color Light;
	
	public GameEntity Entity;
	public EnvironmentEntity environmentEntity;
		
	public Level Level;
	
	public Array<SpriteEffect> SpriteEffects = new Array<SpriteEffect>();
	
	public Array<Item> Items = new Array<Item>(false, 16);
	
	public String metaValue;
		
	boolean visible;
	
	public GameTile(int x, int y, Level level, TileData tileData)
	{
		this.x = x;
		this.y = y;
		this.Level = level;
		
		this.TileData = tileData;
		
		Light = new Color(Color.WHITE);
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
	
	public void addObject(GameEntity obj)
	{
		int[] oldPos = null;
		
		if (obj.tile != null)
		{
			obj.tile.Entity = null;
			
			oldPos = new int[]{obj.tile.x * RoguelikeGame.TileSize, obj.tile.y * RoguelikeGame.TileSize};
		}
		
		Entity = obj;
		obj.tile = this;
		
		if (oldPos != null)
		{
			int[] newPos = {obj.tile.x * RoguelikeGame.TileSize, obj.tile.y * RoguelikeGame.TileSize};
			
			int[] diff = {oldPos[0] - newPos[0], oldPos[1] - newPos[1]};
			
			obj.sprite.SpriteAnimation = new MoveAnimation(0.05f, diff);
		}
	}
	
	@Override
	public boolean GetOpaque()
	{
		if (environmentEntity != null && environmentEntity.opaque)
		{
			 return true;
		}
		
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
	public boolean getPassable(HashSet<String> factions)
	{
		if (environmentEntity != null && !environmentEntity.passable) { return false; }
		if (!TileData.Passable) { return false; }
		
		return Entity != null ? !Entity.isAllies(factions) : true ;
	}

	
	@Override
	public int getInfluence()
	{
		return 0;
	}
}
