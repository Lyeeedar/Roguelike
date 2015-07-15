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
import Roguelike.Ability.ActiveAbility.ActiveAbility;
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
