package Roguelike.Entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.utils.Array;

import Roguelike.AssetManager;
import Roguelike.RoguelikeGame;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.DungeonGeneration.BSPGenerator;
import Roguelike.Items.Item;
import Roguelike.Levels.Level;
import Roguelike.Lights.Light;
import Roguelike.Sprite.Sprite;
import Roguelike.Tiles.GameTile;
import Roguelike.UI.SpriteWidget;
import Roguelike.UI.Tooltip;

public class EnvironmentEntity
{
	public Sprite sprite;

	public GameTile tile;

	public Light light;

	public boolean passable;

	public boolean opaque;

	public Array<ActivationAction> actions = new Array<ActivationAction>();
	
	public static EnvironmentEntity CreateTransition()
	{
		final Sprite stair = new Sprite(1, new Texture[]{AssetManager.loadTexture("Sprites/Objects/Tile.png")}, new int[]{16, 16}, new int[]{0, 1}, Color.WHITE);
		
		ActivationAction transition = new ActivationAction("Change Level")
		{
			Level connectedLevel;
			
			public void activate(EnvironmentEntity entity)
			{
				// generate new level if required
				if (connectedLevel == null)
				{
					BSPGenerator generator = new BSPGenerator(100, 100);
					//VillageGenerator generator = new VillageGenerator(100, 100);
					generator.generate();
					connectedLevel = generator.getLevel();
				}
				
				boolean exit = false;
				for (int x = 0; x < connectedLevel.width; x++)
				{
					for (int y = 0; y < connectedLevel.height; y++)
					{	
						if (connectedLevel.getGameTile(x, y).TileData.Passable)
						{
							connectedLevel.player = entity.tile.Level.player;
							
							connectedLevel.getGameTile(x, y).addObject(entity.tile.Level.player);
												
							exit = true;
							break;
						}
					}
					if (exit) { break;} 
				}
				
				connectedLevel.updateVisibleTiles();
				
				// switch out levels
				RoguelikeGame.Instance.level = connectedLevel;
			}
		};
		
		EnvironmentEntity entity = new EnvironmentEntity();
		entity.passable = true;
		entity.opaque = false;
		entity.sprite = stair;
		entity.actions.add(transition);
		
		return entity;
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
