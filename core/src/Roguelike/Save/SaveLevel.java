package Roguelike.Save;

import Roguelike.DungeonGeneration.RecursiveDockGenerator;
import Roguelike.DungeonGeneration.DungeonFileParser.DFPRoom;
import Roguelike.DungeonGeneration.RecursiveDockGenerator.Room;
import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Entity.GameEntity;
import Roguelike.Items.Item;
import Roguelike.Levels.Level;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class SaveLevel extends SaveableObject<Level>
{
	public String fileName;
	public int depth;
	public long seed;
	public String UID;
	public boolean created = false;
	
	public Array<DFPRoom> requiredRooms = new Array<DFPRoom>();
	
	public Array<SaveGameEntity> gameEntities = new Array<SaveGameEntity>();
	public Array<SaveLevelItem> items = new Array<SaveLevelItem>();
	public Array<SaveEnvironmentEntity> environmentEntities = new Array<SaveEnvironmentEntity>();
	
	public SaveSeenTile[][] seenTiles;

	public SaveLevel()
	{
		
	}
	
	public SaveLevel(String UID)
	{
		this.UID = UID;
	}
	
	public SaveLevel(String fileName, int depth, Array<DFPRoom> requiredRooms, long seed)
	{
		this.fileName = fileName;
		this.depth = depth;
		if (requiredRooms != null) { this.requiredRooms = requiredRooms; }
		this.seed = seed;
		
		createUID();
	}
	
	@Override
	public void store(Level obj)
	{
		created = true;
		fileName = obj.fileName;
		depth = obj.depth;
		seed = obj.seed;
		UID = obj.UID;
		
		requiredRooms.clear();
		requiredRooms.addAll(obj.requiredRooms);
		
		gameEntities.clear();
		for (GameEntity entity : obj.getAllEntities())
		{
			SaveGameEntity saveObj = new SaveGameEntity();
			saveObj.store(entity);
			gameEntities.add(saveObj);
			
			if (entity == obj.player)
			{
				saveObj.isPlayer = true;
			}
		}
		
		items.clear();
		for (int x = 0; x < obj.width; x++)
		{
			for (int y = 0; y < obj.height; y++)
			{
				GameTile tile = obj.getGameTile(x, y);
				for (Item item : tile.items)
				{
					items.add(new SaveLevelItem(tile, item));
				}
			}
		}
		
		environmentEntities.clear();
		for (EnvironmentEntity entity : obj.getAllEnvironmentEntities())
		{
			if (entity.canTakeDamage)
			{
				SaveEnvironmentEntity saveObj = new SaveEnvironmentEntity();
				saveObj.store(entity);
				environmentEntities.add(saveObj);
			}
		}
		
		seenTiles = new SaveSeenTile[obj.width][obj.height];
		for (int x = 0; x < obj.width; x++)
		{
			for (int y = 0; y < obj.height; y++)
			{
				SaveSeenTile save = new SaveSeenTile();
				save.store(obj.getSeenTile(x, y));
				
				seenTiles[x][y] = save;
			}
		}
	}
	
	@Override
	public Level create()
	{
		RecursiveDockGenerator generator = new RecursiveDockGenerator(fileName, depth, seed, !created, UID);
		created = true;

		generator.additionalRooms.clear();
		generator.additionalRooms.addAll(requiredRooms);
				
		generator.generate();
		Level level = generator.getLevel();
		
		for (SaveGameEntity entity : gameEntities)
		{
			GameTile tile = level.getGameTile(entity.pos);
			GameEntity ge = entity.create();
			tile.addGameEntity(ge);
			
			if (entity.isPlayer)
			{
				level.player = ge;
			}
		}
		
		for (SaveLevelItem item : items)
		{
			GameTile tile = level.getGameTile(item.pos);
			tile.items.add(item.item);
		}
		
		for (SaveEnvironmentEntity entity : environmentEntities)
		{
			GameTile tile = level.getGameTile(entity.pos);
			tile.addEnvironmentEntity(entity.create());
		}
		
		if (seenTiles != null)
		{
			for (int x = 0; x < level.width; x++)
			{
				for (int y = 0; y < level.height; y++)
				{
					level.SeenGrid[x][y] = seenTiles[x][y].create();
				}
			}
		}
		
		return level;
	}
	
	public void createUID()
	{
		UID = "Level " + fileName + ": Depth " + depth + ": ID " + this.hashCode();
	}
	
	public static class SaveLevelItem
	{
		public int[] pos;
		public Item item;
		
		public SaveLevelItem()
		{
			
		}
		
		public SaveLevelItem(GameTile tile, Item item)
		{
			pos = new int[]{tile.x, tile.y};
			this.item = item;
		}
	}
}
