package Roguelike;

import java.util.HashMap;

import Roguelike.Global.Direction;
import Roguelike.Global.Statistic;
import Roguelike.Ability.AbilityPool;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Ability.PassiveAbility.PassiveAbility;
import Roguelike.DungeonGeneration.RecursiveDockGenerator;
import Roguelike.Entity.Entity;
import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Entity.EnvironmentEntity.ActivationAction;
import Roguelike.Entity.GameEntity;
import Roguelike.Entity.Tasks.TaskMove;
import Roguelike.Entity.Tasks.TaskUseAbility;
import Roguelike.Entity.Tasks.TaskWait;
import Roguelike.Items.Item;
import Roguelike.Items.Item.EquipmentSlot;
import Roguelike.Levels.Level;
import Roguelike.Pathfinding.Pathfinder;
import Roguelike.Screens.GameOverScreen;
import Roguelike.Screens.GameScreen;
import Roguelike.Screens.LoadingScreen;
import Roguelike.Screens.MainMenuScreen;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.SpriteEffect;
import Roguelike.Sprite.SpriteAnimation.MoveAnimation;
import Roguelike.Tiles.GameTile;
import Roguelike.Tiles.SeenTile;
import Roguelike.Tiles.SeenTile.SeenHistoryItem;
import Roguelike.UI.AbilityPanel;
import Roguelike.UI.AbilityPoolPanel;
import Roguelike.UI.DragDropPayload;
import Roguelike.UI.EntityStatusRenderer;
import Roguelike.UI.HPWidget;
import Roguelike.UI.InventoryPanel;
import Roguelike.UI.MessageStack;
import Roguelike.UI.MessageStack.Line;
import Roguelike.UI.MessageStack.Message;
import Roguelike.UI.SpriteWidget;
import Roguelike.UI.TabPanel;
import Roguelike.UI.Tooltip;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class RoguelikeGame extends Game
{
	public static RoguelikeGame Instance;
	
	public RoguelikeGame()
	{
		Instance = this;
	}
	
	public enum ScreenEnum 
	{
		MAINMENU,
		GAME,
		GAMEOVER,
		LOADING
	}
	
	public final HashMap<ScreenEnum, Screen> screens = new HashMap<ScreenEnum, Screen>();

	@Override
	public void create() 
	{
		screens.put(ScreenEnum.GAME, new GameScreen());
		screens.put(ScreenEnum.MAINMENU, new MainMenuScreen());
		screens.put(ScreenEnum.GAMEOVER, new GameOverScreen());
		screens.put(ScreenEnum.LOADING, new LoadingScreen());
				
		switchScreen(ScreenEnum.MAINMENU);
	}

	public void switchScreen(ScreenEnum screen)
	{
		this.setScreen(screens.get(screen));
	}
}
