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
	
	private static EnvironmentEntity CreateTransition(final Element data)
	{
		final Sprite stairdown = AssetManager.loadSprite("dc-dngn/gateways/stone_stairs_down");
		final Sprite stairup = AssetManager.loadSprite("dc-dngn/gateways/stone_stairs_up");
		
		final EnvironmentEntity entranceEntity = new EnvironmentEntity();
		entranceEntity.passable = true;
		entranceEntity.opaque = false;
		entranceEntity.sprite = stairdown;
		
		final EnvironmentEntity exitEntity = new EnvironmentEntity();
		exitEntity.passable = true;
		exitEntity.opaque = false;
		exitEntity.sprite = stairup;
		
		ActivationAction entranceAA = new ActivationAction("Change Level")
		{
			Level connectedLevel;
			EnvironmentEntity exit = exitEntity;
			
			public void activate(EnvironmentEntity entity)
			{
				// generate new level if required
				if (connectedLevel == null)
				{
					RecursiveDockGenerator generator = new RecursiveDockGenerator(data.get("Destination"));
					
					DFPRoom dfpRoom = DFPRoom.parse(data.getChildByName("ExitRoom"), generator.dfp.sharedSymbolMap);
					final Room room = new Room();
					dfpRoom.fillRoom(room);
					
					for (int x = 0; x < room.width; x++)
					{
						for (int y = 0; y < room.height; y++)
						{
							if (room.roomContents[x][y].environmentData != null)
							{
								if (room.roomContents[x][y].environmentData.get("Destination", "").equals("this"))
								{
									room.roomContents[x][y].environmentEntityObject = exitEntity;
								}
							}
						}
					}
					
					generator.toBePlaced.add(room);
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
		
		return entranceEntity;
	}
	
	private static EnvironmentEntity CreateDoor()
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
		
	private static EnvironmentEntity CreateBasic(Element xml)
	{
		EnvironmentEntity entity = new EnvironmentEntity();
		entity.passable = xml.getBoolean("Passable", true);
		entity.opaque = xml.getBoolean("Opaque", false);
		entity.sprite = AssetManager.loadSprite(xml.getChildByName("Sprite"));
		entity.light = xml.getChildByName("Light") != null ? Light.load(xml.getChildByName("Light")) : null;
		
		return entity;
	}
	
	public static EnvironmentEntity load(Element xml)
	{
		String type = xml.get("Type", "");
		if (type.equalsIgnoreCase("Door"))
		{
			return CreateDoor();
		}
		else if (type.equalsIgnoreCase("Transition"))
		{
			return CreateTransition(xml);
		}
		else
		{
			return CreateBasic(xml);
		}
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
