package Roguelike.Save;

import Roguelike.DungeonGeneration.RecursiveDockGenerator;
import Roguelike.DungeonGeneration.DungeonFileParser.DFPRoom;
import Roguelike.DungeonGeneration.RecursiveDockGenerator.Room;
import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Entity.GameEntity;
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
	
	public Array<DFPRoom> requiredRooms = new Array<DFPRoom>();
	
	public Array<SaveGameEntity> gameEntities = new Array<SaveGameEntity>();
	//public Array<SaveEnvironmentEntity> environmentEntities = new Array<SaveEnvironmentEntity>();

	@Override
	public void store(Level obj)
	{
		fileName = obj.fileName;
		depth = obj.depth;
		seed = obj.seed;
		
		requiredRooms = obj.requiredRooms;
		
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
		
//		for (EnvironmentEntity entity : obj.getAllEnvironmentEntities())
//		{
//			if (entity.canTakeDamage)
//			{
//				SaveEnvironmentEntity saveObj = new SaveEnvironmentEntity();
//				saveObj.store(entity);
//				environmentEntities.store(saveObj);
//			}
//		}
	}
	
	@Override
	public Level create()
	{
		RecursiveDockGenerator generator = new RecursiveDockGenerator(fileName, depth, seed, false);
		
		for (DFPRoom dfpRoom : requiredRooms)
		{
			final Room room = new Room();
			dfpRoom.fillRoom(room, generator.ran, generator.dfp);
			
			generator.toBePlaced.add(room);
		}
				
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
		
//		for (SaveEnvironmentEntity entity : environmentEntities)
//		{
//			GameTile tile = level.getGameTile(entity.pos);
//			tile.addEnvironmentEntity(entity.create());
//		}
		
		return level;
	}
}
