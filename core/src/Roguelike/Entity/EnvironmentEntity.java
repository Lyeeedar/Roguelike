package Roguelike.Entity;

import Roguelike.AssetManager;
import Roguelike.RoguelikeGame;
import Roguelike.DungeonGeneration.DungeonFileParser.DFPRoom;
import Roguelike.DungeonGeneration.RecursiveDockGenerator;
import Roguelike.DungeonGeneration.RecursiveDockGenerator.Room;
import Roguelike.Levels.Level;
import Roguelike.Lights.Light;
import Roguelike.Sprite.Sprite;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public class EnvironmentEntity
{
	public Sprite sprite;

	public GameTile tile;

	public Light light;

	public boolean passable;

	public boolean opaque;

	public Array<ActivationAction> actions = new Array<ActivationAction>();
	
	public static EnvironmentEntity CreateTransition(Element data, final Room exitRoom)
	{
		final Sprite stair = new Sprite(1, new Texture[]{AssetManager.loadTexture("Sprites/Objects/Tile.png")}, new int[]{16, 16}, new int[]{0, 1}, Color.WHITE);
		
		final EnvironmentEntity entranceEntity = new EnvironmentEntity();
		entranceEntity.passable = true;
		entranceEntity.opaque = false;
		entranceEntity.sprite = stair;
		
		final EnvironmentEntity exitEntity = new EnvironmentEntity();
		exitEntity.passable = true;
		exitEntity.opaque = false;
		exitEntity.sprite = stair;
		
		ActivationAction entranceAA = new ActivationAction("Change Level")
		{
			Level connectedLevel;
			EnvironmentEntity exit = exitEntity;
			
			public void activate(EnvironmentEntity entity)
			{
				// generate new level if required
				if (connectedLevel == null)
				{
					RecursiveDockGenerator generator = new RecursiveDockGenerator(50, 50);
					generator.toBePlaced.add(exitRoom);
					generator.generate();
					connectedLevel = generator.getLevel();
				}
				
				exit.tile.addObject(entity.tile.Level.player);
				connectedLevel.player = entity.tile.Level.player;
				
				connectedLevel.updateVisibleTiles();
				
				// switch out levels
				RoguelikeGame.Instance.level = connectedLevel;
			}
		};
		entranceEntity.actions.add(entranceAA);
		
		ActivationAction exitAA = new ActivationAction("Change Level")
		{
			EnvironmentEntity exit = entranceEntity;
			
			public void activate(EnvironmentEntity entity)
			{
				Level connectedLevel = entranceEntity.tile.Level;
				
				exit.tile.addObject(entity.tile.Level.player);
				connectedLevel.player = entity.tile.Level.player;
				
				connectedLevel.updateVisibleTiles();
				
				// switch out levels
				RoguelikeGame.Instance.level = connectedLevel;
			}
		};
		exitEntity.actions.add(exitAA);
		
		for (int x = 0; x < exitRoom.width; x++)
		{
			for (int y = 0; y < exitRoom.height; y++)
			{
				if (exitRoom.roomContents[x][y].isTransition())
				{
					if (exitRoom.roomContents[x][y].data.get("Destination").equals("this"))
					{
						exitRoom.roomContents[x][y].parsedDataBlob = exitEntity;
					}
				}
			}
		}
		
		return entranceEntity;
	}
	
	public static EnvironmentEntity CreateDoor()
	{
		final Sprite doorClosed = new Sprite(1, new Texture[]{AssetManager.loadTexture("Sprites/Objects/Door0.png")}, new int[]{16, 16}, new int[]{0, 0}, Color.WHITE);
		final Sprite doorOpen = new Sprite(1, new Texture[]{AssetManager.loadTexture("Sprites/Objects/Door1.png")}, new int[]{16, 16}, new int[]{0, 0}, Color.WHITE);
		
		ActivationAction open = new ActivationAction("Open")
		{
			public void activate(EnvironmentEntity entity)
			{
				entity.passable = true;
				entity.opaque = false;				
				entity.sprite = doorOpen;
				
				entity.actions.get(1).visible = true;
				visible = false;
			}
		};
		
		ActivationAction close = new ActivationAction("Close")
		{
			public void activate(EnvironmentEntity entity)
			{
				if (entity.tile.Entity != null)
				{
					return;
				}
				
				entity.passable = false;
				entity.opaque = true;				
				entity.sprite = doorClosed;
				
				entity.actions.get(0).visible = true;
				visible = false;
			}
		};
		close.visible = false;
		
		EnvironmentEntity entity = new EnvironmentEntity();
		entity.passable = false;
		entity.opaque = true;
		entity.sprite = doorClosed;
		entity.actions.add(open);
		entity.actions.add(close);
		
		return entity;
	}

	public static abstract class ActivationAction
	{
		public String name;
		public boolean visible = true;

		public ActivationAction(String name)
		{
			this.name = name;
		}
		
		public abstract void activate(EnvironmentEntity entity);
	}
}
