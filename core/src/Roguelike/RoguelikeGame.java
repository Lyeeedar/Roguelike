package Roguelike;

import java.util.Iterator;

import MobiDevelop.UI.HorizontalFlowGroup;
import MobiDevelop.UI.VerticalFlowGroup;
import Roguelike.Global.Direction;
import Roguelike.Global.Statistics;
import Roguelike.DungeonGeneration.DungeonRoomGenerator;
import Roguelike.DungeonGeneration.VillageGenerator;
import Roguelike.Entity.AbilityPool;
import Roguelike.Entity.ActiveAbility.AbilityTile;
import Roguelike.Entity.Entity;
import Roguelike.Entity.ActiveAbility;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree;
import Roguelike.Entity.Tasks.TaskUseAbility;
import Roguelike.Entity.Tasks.TaskMove;
import Roguelike.Entity.Tasks.TaskWait;
import Roguelike.Items.Item;
import Roguelike.Items.Item.EquipmentSlot;
import Roguelike.Items.Item.ItemType;
import Roguelike.Levels.Level;
import Roguelike.Lights.Light;
import Roguelike.Pathfinding.AStarPathfind;
import Roguelike.Pathfinding.Pathfinder;
import Roguelike.Shadows.ShadowCaster;
import Roguelike.Sprite.SpriteEffect;
import Roguelike.Sprite.Sprite;
import Roguelike.Tiles.GameTile;
import Roguelike.Tiles.SeenTile;
import Roguelike.Tiles.SeenTile.SeenHistoryItem;
import Roguelike.UI.AbilityPanel;
import Roguelike.UI.AbilityPoolPanel;
import Roguelike.UI.DragDropPayload;
import Roguelike.UI.HPWidget;
import Roguelike.UI.SpriteWidget;
import Roguelike.UI.TabPane;
import Roguelike.UI.InventoryPanel;
import Roguelike.UI.Tooltip;
import Roguelike.UI.TooltipListener;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class RoguelikeGame extends ApplicationAdapter implements InputProcessor
{
	//####################################################################//
	//region Constructor
	
	public RoguelikeGame()
	{
		Instance = this;
	}
		
	//endregion Constructor
	//####################################################################//
	//region Public Methods
	
	//####################################################################//
	//region Create
	
	//----------------------------------------------------------------------
	@Override
	public void create () 
	{
		batch = new SpriteBatch();
		
		blank = AssetManager.loadTexture("Sprites/blank.png");
		white = AssetManager.loadTexture("Sprites/white.png");
		
		DungeonRoomGenerator generator = new DungeonRoomGenerator(100, 100);
		//VillageGenerator generator = new VillageGenerator(100, 100);
		generator.generate();
		level = generator.getLevel();
		level.revealWholeLevel();
		
		border = AssetManager.loadSprite("GUI/frame", 0.5f, new int[]{32, 32}, new int[]{0, 0});
		
		boolean exit = false;
		for (int x = 0; x < level.width; x++)
		{
			for (int y = 0; y < level.height; y++)
			{	
				if (level.getGameTile(x, y).TileData.Passable)
				{
					level.player = Entity.load("player");
					
					for (int i = 0; i < 40; i++)
					{
						//level.player.Inventory.Items.add(AssetManager.loadItem("Jewelry/Necklace/GoldNecklace"));
					}
					level.player.getInventory().m_items.add(Item.load("rock"));
					level.player.getInventory().m_items.add(Item.load("Weapon/MainWeapon/sword"));
					level.player.getInventory().m_items.add(Item.load("Armour/Body/WoodArmour"));
					level.player.getInventory().m_items.add(Item.load("Jewelry/Necklace/GoldNecklace"));
										
					level.getGameTile(x, y).addObject(level.player);
										
					exit = true;
					break;
				}
			}
			if (exit) { break;} 
		}
		
		ShadowCaster shadow = new ShadowCaster(level.player.getStatistic(Statistics.RANGE));
		shadow.ComputeFieldOfViewWithShadowCasting((int)level.player.Tile.x, (int)level.player.Tile.y, level.getGrid());	
		
		LoadUI();
		
		InputProcessor inputProcessorOne = this;
		InputProcessor inputProcessorTwo = stage;
		InputMultiplexer inputMultiplexer = new InputMultiplexer();
		inputMultiplexer.addProcessor(inputProcessorOne);
		inputMultiplexer.addProcessor(inputProcessorTwo);
		Gdx.input.setInputProcessor(inputMultiplexer);		
	}
	
	//----------------------------------------------------------------------
	public void LoadUI()
	{
		skin = new Skin(Gdx.files.internal("GUI/uiskin.json"));
		stage = new Stage(new ScreenViewport());
		
		Table mainUITable = new Table();
		//mainUITable.debug();
		mainUITable.setFillParent(true);
		stage.addActor(mainUITable);
		
		Table abHPTable = new Table();	
		abilityPanel = new AbilityPanel(level.player, skin, stage);
		hpWidget = new HPWidget(level.player);
		
		abHPTable.add(abilityPanel).uniform().fill();
		abHPTable.row();
		abHPTable.add(hpWidget).uniform().fill();
		
		inventoryPanel = new InventoryPanel(level.player, skin, stage);
		abilityPoolPanel = new AbilityPoolPanel(new AbilityPool(), skin, stage);

		messageStack = new Table();
		
		messageScrollPane = new ScrollPane(messageStack);
		messageScrollPane.setScrollingDisabled(true, false);
		messageScrollPane.setFillParent(true);
				
		tabPane = new TabPane();

		tabPane.addTab(new SpriteWidget(AssetManager.loadSprite("Skills/skill", 1, new int[]{26, 26}, new int[]{0, 1})), messageScrollPane);
		tabPane.addTab(new SpriteWidget(AssetManager.loadSprite("Skills/skill", 1, new int[]{26, 26}, new int[]{2, 3})), inventoryPanel);
		tabPane.addTab(new SpriteWidget(AssetManager.loadSprite("Skills/skill", 1, new int[]{26, 26}, new int[]{1, 3})), abilityPoolPanel);
		
		
		mainUITable.add(tabPane).width(Value.percentWidth(0.5f, mainUITable)).height(Value.percentHeight(0.3f, mainUITable)).expand().bottom().left().pad(5);
		
		mainUITable.add(abHPTable).expand().bottom().right().pad(20);
	}
	
	//endregion Create
	//####################################################################//
	//region ApplicationAdapter
	
	//----------------------------------------------------------------------
	@Override
	public void resize (int width, int height) 
	{
		batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
		stage.getViewport().update(width, height, true);
		
		//createInventoryUI();
	}
	
	//----------------------------------------------------------------------
	@Override
	public void render () 
	{
		float delta = Gdx.graphics.getDeltaTime();
		
		level.update(delta);
		
		int offsetx = Gdx.graphics.getWidth() / 2 - level.player.Tile.x * TileSize;
		int offsety = Gdx.graphics.getHeight() / 2 - level.player.Tile.y * TileSize;
		
		int mousex = (mousePosX - offsetx) / TileSize;
		int mousey = (mousePosY - offsety) / TileSize;
		
		boolean drawCeiling = true;
		
		if (
				mousex >= 0 && mousex < level.width &&
				mousey >= 0 && mousey < level.height &&
				level.getSeenTile(mousex, mousey).seen &&
				level.getGameTile(mousex, mousey).TileData.CeilingSprite != null)
		{	
			drawCeiling = false;
		}
		else if (level.player.Tile.TileData.CeilingSprite != null)
		{
			drawCeiling = false;
		}

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		
		toBeDrawn.clear();
		
		for (int x = 0; x < level.width; x++)
		{
			for (int y = 0; y < level.height; y++)
			{
				GameTile gtile = level.getGameTile(x, y);
				SeenTile stile = level.getSeenTile(x, y);
				
				if (gtile.GetVisible())
				{
					batch.setColor(gtile.Light);					
					gtile.TileData.FloorSprite.render(batch, x*TileSize + offsetx, y*TileSize + offsety, TileSize, TileSize);
					
					for (Item i : gtile.Items)
					{
						i.Icon.render(batch, x*TileSize + offsetx, y*TileSize + offsety, TileSize, TileSize);
					}
					
					Entity entity = gtile.Entity;
					
					if (entity != null)
					{
						toBeDrawn.add(entity);
					}
					
					batch.setColor(Color.WHITE);
				}
				else if (stile.seen)
				{
					batch.setColor(level.Ambient);
					
					for (SeenHistoryItem hist : stile.History)
					{
						hist.sprite.render(batch, x*TileSize + offsetx, y*TileSize + offsety, TileSize, TileSize, hist.spriteIndex);
					}				
					
					batch.setColor(Color.WHITE);
				}
			}
		}
		
		for (Entity entity : toBeDrawn)
		{
			int x = entity.Tile.x;
			int y = entity.Tile.y;
			
			batch.setColor(entity.Tile.Light);
			
			int cx = x*TileSize + offsetx;
			int cy = y*TileSize + offsety;
			
			if (entity.Sprite.SpriteAnimation != null)
			{
				cx += TileSize3 * entity.Sprite.SpriteAnimation.Alpha * entity.Sprite.SpriteAnimation.Direction.GetX();
				cy += TileSize3 * entity.Sprite.SpriteAnimation.Alpha * entity.Sprite.SpriteAnimation.Direction.GetY();
			}
			
			entity.Sprite.render(batch, cx, cy, TileSize, TileSize);
			
			for (SpriteEffect e : entity.SpriteEffects)
			{
				if (e.Corner == Direction.CENTER)
				{
					e.Sprite.render(batch, cx, cy, TileSize, TileSize);
				}
				else
				{								
					e.Sprite.render(batch, cx + TileSize3*(e.Corner.GetX()*-1+1), cy + TileSize3*(e.Corner.GetY()*-1+1), TileSize3, TileSize3);
				}
			}
			
			if (entity.HP != entity.getStatistic(Statistics.MAXHP))
			{
				float val = (float)entity.HP / (float)entity.getStatistic(Statistics.MAXHP);
				
				batch.setColor(new Color(Color.RED).lerp(Color.GREEN, val));
				batch.draw(white, x*TileSize + offsetx, y*TileSize + offsety, TileSize*val, TileSize/10);
			}
			
			batch.setColor(Color.WHITE);
		}
		
		for (ActiveAbility s : level.ActiveAbilities)
		{
			for (AbilityTile tile : s.AffectedTiles)
			{
				if (tile.Tile.GetVisible())
				{
					int x = tile.Tile.x;
					int y = tile.Tile.y;
					
					batch.setColor(tile.Tile.Light);
					
					int cx = x*TileSize + offsetx;
					int cy = y*TileSize + offsety;
					
					tile.Sprite.render(batch, cx, cy, TileSize, TileSize);
				}
			}
		}
		
		border.update(delta);
		
		if (!mouseOverTabs)
		{					
			if (
					mousex < 0 || mousex >= level.width ||
					mousey < 0 || mousey >= level.height ||
					!level.getSeenTile(mousex, mousey).seen)
			{				
				batch.setColor(Color.RED);
			}
			else
			{
				if (level.getGameTile(mousex, mousey).TileData.Passable)
				{
					batch.setColor(Color.GREEN);
				}
				else
				{
					batch.setColor(Color.RED);
				}				
			}
			
			border.render(batch, mousex*TileSize + offsetx, mousey*TileSize + offsety, TileSize, TileSize);
			batch.setColor(Color.WHITE);
		}
		
		if (drawCeiling)
		{
			for (int x = 0; x < level.width; x++)
			{
				for (int y = 0; y < level.height; y++)
				{
					GameTile gtile = level.getGameTile(x, y);
					SeenTile stile = level.getSeenTile(x, y);
					
					if (gtile.GetVisible())
					{
						if (gtile.TileData.CeilingSprite != null)
						{
							batch.setColor(gtile.Light);	
							
							gtile.TileData.CeilingSprite.render(batch, x*TileSize + offsetx, y*TileSize + offsety, TileSize, TileSize);

							batch.setColor(Color.WHITE);
						}	
					}
					else if (stile.seen)
					{
						if (stile.CeilingItem != null)
						{
							batch.setColor(level.Ambient);
							
							stile.CeilingItem.sprite.render(batch, x*TileSize + offsetx, y*TileSize + offsety, TileSize, TileSize, stile.CeilingItem.spriteIndex);		
							
							batch.setColor(Color.WHITE);
						}						
					}
				}
			}
		}
		
		batch.end();
		
		stage.act(delta);
		stage.draw();
		
		batch.begin();
		
		if (dragDropPayload != null && dragDropPayload.shouldDraw())
		{
			dragDropPayload.sprite.render(batch, (int)dragDropPayload.x, (int)dragDropPayload.y, 32, 32);
		}
		
		batch.end();
	}

	//endregion ApplicationAdapter
	//####################################################################//
	//region InputProcessor
	
	//----------------------------------------------------------------------
	@Override
	public boolean keyDown(int keycode)
	{	
		return false;
	}

	//----------------------------------------------------------------------
	@Override
	public boolean keyUp(int keycode)
	{
		return false;
	}

	//----------------------------------------------------------------------
	@Override
	public boolean keyTyped(char character)
	{
		return false;
	}

	//----------------------------------------------------------------------
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button)
	{
		if (!mouseOverTabs)
		{
			screenY = Gdx.graphics.getHeight() - screenY;
			
			int offsetx = Gdx.graphics.getWidth() / 2 - level.player.Tile.x * TileSize;
			int offsety = Gdx.graphics.getHeight() / 2 - level.player.Tile.y * TileSize;
			
			int x = (screenX - offsetx) / TileSize;
			int y = (screenY - offsety) / TileSize;
			
			if (button == Buttons.RIGHT)
			{
				ActiveAbility aa = abilityPanel.getSelectedAbility();
				if (aa != null && aa.cooldownAccumulator <= 0)
				{
					level.player.Tasks.add(new TaskUseAbility(new int[]{x, y}, aa));
				}
			}
			else
			{
				if (x >= 0 && x < level.width && y >= 0 && y < level.height && level.getSeenTile(x, y).seen)
				{
					level.player.AI.setData("ClickPos", new int[]{x, y});
				}
			}
		}
				
		return false;
	}

	//----------------------------------------------------------------------
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button)
	{
		if (dragDropPayload != null && isInBounds(screenX, screenY, abilityPanel))
		{
			Vector2 tmp = new Vector2(screenX, Gdx.graphics.getHeight() - screenY);
			tmp = abilityPanel.stageToLocalCoordinates(tmp);
			abilityPanel.handleDrop(tmp.x, tmp.y);
		}
		
		dragDropPayload = null;
		
		return false;
	}

	//----------------------------------------------------------------------
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer)
	{
		if (dragDropPayload != null)
		{
			dragDropPayload.x = screenX - 16;
			dragDropPayload.y = Gdx.graphics.getHeight() - screenY - 16;
		}
		
		return false;
	}

	//----------------------------------------------------------------------
	@Override
	public boolean mouseMoved(int screenX, int screenY)
	{		
		if (
		//	(tabPane != null && isInBounds(screenX, screenY, tabPane)) ||
			(abilityPanel != null && isInBounds(screenX, screenY, abilityPanel)) ||
			(hpWidget != null && isInBounds(screenX, screenY, hpWidget))
			)
		{
			mouseOverTabs = true;
			tabPane.focusCurrentTab(stage);
		}
		else
		{
			mouseOverTabs = false;
			
			mousePosX = screenX;
			mousePosY = Gdx.graphics.getHeight() - screenY;
			
			stage.setScrollFocus(null);
		}
		
		return false;
	}

	//----------------------------------------------------------------------
	@Override
	public boolean scrolled(int amount)
	{
		if (!mouseOverTabs)
		{
			TileSize -= amount*5;
			if (TileSize < 8)
			{
				TileSize = 8;
			}
			
			TileSize3 = TileSize / 3;
		}
		
		return false;
	}
	
	//endregion InputProcessor
	//####################################################################//
	
	public void addConsoleMessage(String message)
	{
		Label l = new Label(message, skin);
		l.setWrap(true);
		messageStack.row();
		messageStack.add(l).width(Value.percentWidth(1, messageStack));
		
		messageScrollPane.layout();
		messageScrollPane.setScrollY(messageScrollPane.getMaxY());
	}
	
	//endregion Public Methods
	//####################################################################//
	//region Private Methods
	
	public boolean isInBounds(float x, float y, Widget actor)
	{
		Vector2 tmp = new Vector2(x, Gdx.graphics.getHeight() - y);
		tmp = actor.stageToLocalCoordinates(tmp);
		return tmp.x >= 0 && tmp.x <= actor.getPrefWidth() && tmp.y >= 0 && tmp.y <= actor.getPrefHeight();
	}
		
	//endregion Private Methods
	//####################################################################//
	//region Data
	
	//----------------------------------------------------------------------
	private static int TileSize = 32;
	private static int TileSize3 = TileSize / 3;
	
	//----------------------------------------------------------------------
	public DragDropPayload dragDropPayload;

	//----------------------------------------------------------------------
	InventoryPanel inventoryPanel;
	AbilityPanel abilityPanel;
	AbilityPoolPanel abilityPoolPanel;
	HPWidget hpWidget;
	Table messageStack;
	TabPane tabPane;
	ScrollPane messageScrollPane;
	
	Skin skin;
	Stage stage;
	SpriteBatch batch;
	Texture blank;
	Texture white;
	
	boolean mouseOverTabs;
	
	//----------------------------------------------------------------------
	Level level;
	
	//----------------------------------------------------------------------
	Array<Entity> toBeDrawn = new Array<Entity>();
	
	//----------------------------------------------------------------------
	Sprite border;
	int mousePosX;
	int mousePosY;
	
	//----------------------------------------------------------------------
	public static RoguelikeGame Instance;
		
	//endregion Data
	//####################################################################//
}
