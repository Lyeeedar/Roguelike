package Roguelike.Screens;

import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.Ability.AbilityPool;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Ability.PassiveAbility.PassiveAbility;
import Roguelike.DungeonGeneration.RecursiveDockGenerator;
import Roguelike.Entity.Entity;
import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Entity.GameEntity;
import Roguelike.Entity.EnvironmentEntity.ActivationAction;
import Roguelike.Entity.Tasks.TaskMove;
import Roguelike.Entity.Tasks.TaskUseAbility;
import Roguelike.Entity.Tasks.TaskWait;
import Roguelike.Global.Direction;
import Roguelike.Global.Statistic;
import Roguelike.Items.Item;
import Roguelike.Items.Item.EquipmentSlot;
import Roguelike.Sprite.MoveAnimation;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.SpriteEffect;
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
import Roguelike.UI.SpriteWidget;
import Roguelike.UI.TabPanel;
import Roguelike.UI.TabPanel.Tab;
import Roguelike.UI.Tooltip;
import Roguelike.UI.MessageStack.Line;
import Roguelike.UI.MessageStack.Message;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Buttons;
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

public class GameScreen implements Screen, InputProcessor
{
	//####################################################################//
	//region Create

	public GameScreen()
	{
		Instance = this;
		
		create();
	}
	
	private void create()
	{
		batch = new SpriteBatch();

		FreeTypeFontGenerator fgenerator = new FreeTypeFontGenerator(Gdx.files.internal("Sprites/GUI/stan0755.ttf"));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = 12;
		parameter.borderWidth = 1;
		parameter.kerning = true;
		parameter.borderColor = Color.BLACK;
		font = fgenerator.generateFont(parameter); // font size 12 pixels
		font.getData().markupEnabled = true;
		fgenerator.dispose(); // don't forget to dispose to avoid memory leaks!

		blank = AssetManager.loadTexture("Sprites/blank.png");
		white = AssetManager.loadTexture("Sprites/white.png");
		border = AssetManager.loadSprite("GUI/frame");

		LoadUI();

		InputProcessor inputProcessorOne = this;
		InputProcessor inputProcessorTwo = stage;
		inputMultiplexer = new InputMultiplexer();

		inputMultiplexer.addProcessor(inputProcessorTwo);
		inputMultiplexer.addProcessor(inputProcessorOne);		
	}

	//----------------------------------------------------------------------
	private void LoadUI()
	{
		skin = new Skin();
		skin.addRegions(new TextureAtlas(Gdx.files.internal("GUI/uiskin.atlas")));
		skin.add("default-font", font, BitmapFont.class);
		skin.load(Gdx.files.internal("GUI/uiskin.json"));

		stage = new Stage(new ScreenViewport());

		Table mainUITable = new Table();
		//mainUITable.debug();
		mainUITable.setFillParent(true);
		stage.addActor(mainUITable);

		abilityPanel = new AbilityPanel(skin, stage);

		abilityPoolPanel = new AbilityPoolPanel(new AbilityPool(), skin, stage);
		inventoryPanel = new InventoryPanel(skin, stage);
		messageStack = new MessageStack();
		messageStack.addLine(new Line(new Message("Welcome to the DUNGEON!")));
		
		Widget blankTab = new Widget();

		//stage.addActor(messageStack);
		stage.addActor(blankTab);
		stage.addActor(inventoryPanel);
		stage.addActor(abilityPoolPanel);

		tabPane = new TabPanel();

		TabPanel.Tab tab = tabPane.addTab(AssetManager.loadSprite("blank"), blankTab);
		tabPane.addTab(AssetManager.loadSprite("GUI/Inventory"), inventoryPanel);
		tabPane.addTab(AssetManager.loadSprite("GUI/Abilities"), abilityPoolPanel);
		//tabPane.addTab(AssetManager.loadSprite("GUI/Message"), messageStack);
		
		tabPane.selectTab(tab);

		mainUITable.add(tabPane).width(Value.percentWidth(0.5f, mainUITable)).height(Value.percentHeight(0.3f, mainUITable)).expand().bottom().left().pad(10);

		mainUITable.add(abilityPanel).expand().bottom().right().pad(20);
	}

	//endregion Create
	//####################################################################//

	//----------------------------------------------------------------------
	@Override
	public void show()
	{
		Gdx.input.setInputProcessor(inputMultiplexer);	
	}

	//----------------------------------------------------------------------
	@Override
	public void render(float delta)
	{
		frametime = (frametime + delta) / 2.0f;
		fpsAccumulator += delta;
		if (fpsAccumulator > 0.5f)
		{
			fps = (int)(1.0f / frametime);
			fpsAccumulator = 0;
		}

		Global.CurrentLevel.update(delta);

		int offsetx = Gdx.graphics.getWidth() / 2 - Global.CurrentLevel.player.tile.x * Global.TileSize;
		int offsety = Gdx.graphics.getHeight() / 2 - Global.CurrentLevel.player.tile.y * Global.TileSize;

		if (Global.CurrentLevel.player.sprite.spriteAnimation instanceof MoveAnimation)
		{
			int[] offset = Global.CurrentLevel.player.sprite.spriteAnimation.getRenderOffset();

			offsetx -= offset[0];
			offsety -= offset[1];
		}

		int mousex = (mousePosX - offsetx) / Global.TileSize;
		int mousey = (mousePosY - offsety) / Global.TileSize;
		
		int tileSize3 = Global.TileSize / 3;

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();

		toBeDrawn.clear();
		hpBars.clear();

		for (int x = 0; x < Global.CurrentLevel.width; x++)
		{
			for (int y = 0; y < Global.CurrentLevel.height; y++)
			{
				GameTile gtile = Global.CurrentLevel.getGameTile(x, y);

				if (gtile.GetVisible())
				{
					batch.setColor(gtile.light);	

					for (Sprite s : gtile.tileData.sprites)
					{
						s.render(batch, x*Global.TileSize + offsetx, y*Global.TileSize + offsety, Global.TileSize, Global.TileSize);
					}

					if (gtile.environmentEntity != null) 
					{ 
						gtile.environmentEntity.sprite.render(batch, x*Global.TileSize + offsetx, y*Global.TileSize + offsety, Global.TileSize, Global.TileSize);

						if (gtile.environmentEntity.HP < gtile.environmentEntity.statistics.get(Statistic.MAXHP) || gtile.environmentEntity.stacks.size > 0)
						{
							hpBars.add(gtile.environmentEntity);
						}
					}

					for (Item i : gtile.items)
					{
						i.getIcon().render(batch, x*Global.TileSize + offsetx, y*Global.TileSize + offsety, Global.TileSize, Global.TileSize);
					}

					GameEntity entity = gtile.entity;

					if (entity != null)
					{
						toBeDrawn.add(entity);
					}

					batch.setColor(Color.WHITE);
				}
			}
		}

		for (int x = 0; x < Global.CurrentLevel.width; x++)
		{
			for (int y = 0; y < Global.CurrentLevel.height; y++)
			{
				GameTile gtile = Global.CurrentLevel.getGameTile(x, y);

				if (gtile.GetVisible())
				{
					for (SpriteEffect e : gtile.spriteEffects)
					{
						if (e.Corner == Direction.CENTER)
						{
							e.Sprite.render(batch, x*Global.TileSize + offsetx, y*Global.TileSize + offsety, Global.TileSize, Global.TileSize);
						}
						else
						{								
							e.Sprite.render(batch, x*Global.TileSize + offsetx + tileSize3*(e.Corner.GetX()*-1+1), y*Global.TileSize + offsety + tileSize3*(e.Corner.GetY()*-1+1), tileSize3, tileSize3);
						}					
					}
				}
			}
		}

		if (!mouseOverUI)
		{					
			if (
					mousex < 0 || mousex >= Global.CurrentLevel.width ||
					mousey < 0 || mousey >= Global.CurrentLevel.height ||
					!Global.CurrentLevel.getSeenTile(mousex, mousey).seen)
			{				
				batch.setColor(Color.RED);
			}
			else
			{
				if (Global.CurrentLevel.getGameTile(mousex, mousey).tileData.passable)
				{
					batch.setColor(Color.GREEN);
				}
				else
				{
					batch.setColor(Color.RED);
				}				
			}

			border.update(delta);
			border.render(batch, mousex*Global.TileSize + offsetx, mousey*Global.TileSize + offsety, Global.TileSize, Global.TileSize);
			batch.setColor(Color.WHITE);
		}

		for (GameEntity entity : toBeDrawn)
		{
			if (entity == Global.CurrentLevel.player)
			{
				Sprite sprite = Global.CurrentLevel.player.sprite;
				sprite.extraLayers.clear();

				for (EquipmentSlot slot : EquipmentSlot.values())
				{
					Item equip = Global.CurrentLevel.player.getInventory().getEquip(slot);

					if (equip != null)
					{
						sprite.extraLayers.add(equip.getEquipTexture());
					}
				}
			}

			int x = entity.tile.x;
			int y = entity.tile.y;

			int cx = x*Global.TileSize + offsetx;
			int cy = y*Global.TileSize + offsety;

			batch.setColor(entity.tile.light);

			entity.sprite.render(batch, cx, cy, Global.TileSize, Global.TileSize);

			for (SpriteEffect e : entity.spriteEffects)
			{
				if (e.Corner == Direction.CENTER)
				{
					e.Sprite.render(batch, cx, cy, Global.TileSize, Global.TileSize);
				}
				else
				{								
					e.Sprite.render(batch, cx + tileSize3*(e.Corner.GetX()*-1+1), cy + tileSize3*(e.Corner.GetY()*-1+1), tileSize3, tileSize3);
				}				
			}

			batch.setColor(Color.WHITE);

			if (entity.sprite.spriteAnimation != null)
			{
				int[] offset = entity.sprite.spriteAnimation.getRenderOffset();
				cx += offset[0];
				cy += offset[1];
			}

			EntityStatusRenderer.draw(entity, batch, cx, cy, Global.TileSize, Global.TileSize, 1.0f/8.0f);
		}

		for (Entity e : hpBars)
		{
			int x = e.tile.x;
			int y = e.tile.y;

			int cx = x*Global.TileSize + offsetx;
			int cy = y*Global.TileSize + offsety;

			EntityStatusRenderer.draw(e, batch, cx, cy, Global.TileSize, Global.TileSize, 1.0f/8.0f);
		}

		for (ActiveAbility aa : Global.CurrentLevel.ActiveAbilities)
		{
			for (GameTile tile : aa.AffectedTiles)
			{
				if (tile.GetVisible())
				{
					aa.getSprite().render(batch, tile.x*Global.TileSize + offsetx, tile.y*Global.TileSize + offsety, Global.TileSize, Global.TileSize);
				}
			}			
		}

		if (preparedAbility != null)
		{
			batch.setColor(0.3f, 0.6f, 0.8f, 0.5f);
			for (int[] tile : abilityTiles)
			{
				batch.draw(white, tile[0]*Global.TileSize + offsetx, tile[1]*Global.TileSize + offsety, Global.TileSize, Global.TileSize);
			}
		}

		batch.setShader(GrayscaleShader.Instance);
		for (int x = 0; x < Global.CurrentLevel.width; x++)
		{
			for (int y = 0; y < Global.CurrentLevel.height; y++)
			{
				GameTile gtile = Global.CurrentLevel.getGameTile(x, y);
				SeenTile stile = Global.CurrentLevel.getSeenTile(x, y);

				if (!gtile.GetVisible() && stile.seen)
				{					
					batch.setColor(Global.CurrentLevel.Ambient);

					for (SeenHistoryItem hist : stile.History)
					{
						hist.sprite.render(batch, x*Global.TileSize + offsetx, y*Global.TileSize + offsety, Global.TileSize, Global.TileSize, hist.animationState);
					}				

					batch.setColor(Color.WHITE);				
				}
				else if (!gtile.GetVisible())
				{
					batch.setColor(Color.BLACK);

					batch.draw(white, x*Global.TileSize + offsetx, y*Global.TileSize + offsety, Global.TileSize, Global.TileSize);

					batch.setColor(Color.WHITE);
				}
			}
		}
		batch.setShader(null);

		if (!mouseOverUI)
		{	
			if (
					mousex >= 0 && mousex < Global.CurrentLevel.width &&
					mousey >= 0 && mousey < Global.CurrentLevel.height &&
					!Global.CurrentLevel.getGameTile(mousex, mousey).GetVisible())
			{
				if (
						!Global.CurrentLevel.getSeenTile(mousex, mousey).seen ||
						!Global.CurrentLevel.getGameTile(mousex, mousey).tileData.passable)
				{
					batch.setColor(Color.RED);
				}
				else
				{
					batch.setColor(Color.GREEN);
				}

				border.render(batch, mousex*Global.TileSize + offsetx, mousey*Global.TileSize + offsety, Global.TileSize, Global.TileSize);
				batch.setColor(Color.WHITE);
			}
		}

		EntityStatusRenderer.draw(Global.CurrentLevel.player, batch, 20, Gdx.graphics.getHeight() - 120, Gdx.graphics.getWidth()/4, 100, 1.0f/4.0f);

		batch.end();

		stage.act(delta);
		stage.draw();

		batch.begin();

		if (dragDropPayload != null && dragDropPayload.shouldDraw())
		{
			dragDropPayload.sprite.render(batch, (int)dragDropPayload.x, (int)dragDropPayload.y, 32, 32);
		}

		font.draw(batch, "FPS: "+fps, Gdx.graphics.getWidth()-100, Gdx.graphics.getHeight() - 20);

		batch.end();
	}

	//----------------------------------------------------------------------
	@Override
	public void resize(int width, int height)
	{
		batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
		stage.getViewport().update(width, height, true);
	}

	//----------------------------------------------------------------------
	@Override
	public void pause()
	{
	}

	//----------------------------------------------------------------------
	@Override
	public void resume()
	{
	}

	//----------------------------------------------------------------------
	@Override
	public void hide()
	{
	}

	//----------------------------------------------------------------------
	@Override
	public void dispose()
	{
	}

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
		if (!mouseOverUI)
		{
			clearContextMenu();

			screenY = Gdx.graphics.getHeight() - screenY;

			int offsetx = Gdx.graphics.getWidth() / 2 - Global.CurrentLevel.player.tile.x * Global.TileSize;
			int offsety = Gdx.graphics.getHeight() / 2 - Global.CurrentLevel.player.tile.y * Global.TileSize;

			int x = (screenX - offsetx) / Global.TileSize;
			int y = (screenY - offsety) / Global.TileSize;

			if (preparedAbility != null)
			{
				if (button == Buttons.LEFT)
				{
					if (x >= 0 && x < Global.CurrentLevel.width && y >= 0 && y < Global.CurrentLevel.height)
					{
						GameTile tile = Global.CurrentLevel.getGameTile(x, y);
						if (preparedAbility.isTargetValid(tile, abilityTiles))
						{
							Global.CurrentLevel.player.tasks.add(new TaskUseAbility(new int[]{x, y}, preparedAbility));
						}
					}
				}
				preparedAbility = null;
			}
			else
			{
				if (button == Buttons.RIGHT)
				{
					GameTile tile = null;
					if (x >= 0 && x < Global.CurrentLevel.width && y >= 0 && y < Global.CurrentLevel.height)
					{
						tile = Global.CurrentLevel.getGameTile(x, y);
					}

					createContextMenu(screenX, screenY, tile);
				}
				else
				{
					if (x >= 0 && x < Global.CurrentLevel.width && y >= 0 && y < Global.CurrentLevel.height && Global.CurrentLevel.getSeenTile(x, y).seen)
					{
						// check if should be an attack
						Direction dir = Direction.getDirection(Global.CurrentLevel.player.tile, Global.CurrentLevel.getGameTile(x, y));
						TaskMove task = new TaskMove(dir);
						if (task.checkHitSomething(Global.CurrentLevel.player))
						{
							Global.CurrentLevel.player.tasks.add(task);
						}
						else
						{
							Global.CurrentLevel.player.AI.setData("ClickPos", new int[]{x, y});
						}
					}
				}
			}

			return true;
		}
		else
		{
			preparedAbility = null;
		}

		return false;
	}

	//----------------------------------------------------------------------
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button)
	{
		if (dragDropPayload != null && dragDropPayload.shouldDraw())
		{
			if (isInBounds(screenX, screenY, abilityPanel))
			{
				Vector2 tmp = new Vector2(screenX, Gdx.graphics.getHeight() - screenY);
				tmp = abilityPanel.stageToLocalCoordinates(tmp);
				abilityPanel.handleDrop(tmp.x, tmp.y);
			}
			else
			{
				if (dragDropPayload.obj instanceof ActiveAbility)
				{
					int index = Global.CurrentLevel.player.getActiveAbilityIndex((ActiveAbility)dragDropPayload.obj);
					if (index != -1)
					{
						Global.CurrentLevel.player.slotActiveAbility(null, index);
					}
				}
				else if (dragDropPayload.obj instanceof PassiveAbility)
				{
					int index = Global.CurrentLevel.player.getPassiveAbilityIndex((PassiveAbility)dragDropPayload.obj);
					if (index != -1)
					{
						Global.CurrentLevel.player.slotPassiveAbility(null, index);
					}
				}
			}
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
		if (tooltip != null)
		{
			tooltip.remove();
			tooltip = null;
		}

		if (
				(tabPane != null && isInBounds(screenX, screenY, tabPane)) ||
				(abilityPanel != null && isInBounds(screenX, screenY, abilityPanel)) ||
				(hpWidget != null && isInBounds(screenX, screenY, hpWidget))
				)
		{
			mouseOverUI = true;
			tabPane.focusCurrentTab(stage);
		}
		else
		{
			mouseOverUI = false;

			mousePosX = screenX;
			mousePosY = Gdx.graphics.getHeight() - screenY;

			stage.setScrollFocus(null);

			screenY = Gdx.graphics.getHeight() - screenY;

			int offsetx = Gdx.graphics.getWidth() / 2 - Global.CurrentLevel.player.tile.x * Global.TileSize;
			int offsety = Gdx.graphics.getHeight() / 2 - Global.CurrentLevel.player.tile.y * Global.TileSize;

			int x = (screenX - offsetx) / Global.TileSize;
			int y = (screenY - offsety) / Global.TileSize;

			if (x >= 0 && x < Global.CurrentLevel.width && y >= 0 && y < Global.CurrentLevel.height)
			{
				GameTile tile = Global.CurrentLevel.getGameTile(x, y);

				if (tile.entity != null)
				{
					Table table = EntityStatusRenderer.getMouseOverTable(tile.entity, x*Global.TileSize+offsetx, y*Global.TileSize+offsety, Global.TileSize, Global.TileSize, 1.0f/8.0f, screenX, screenY, skin);

					if (table != null)
					{
						tooltip = new Tooltip(table, skin, stage);
						tooltip.show(screenX, screenY);
					}					
				}
			}

			{
				Table table = EntityStatusRenderer.getMouseOverTable(Global.CurrentLevel.player, 20, Gdx.graphics.getHeight() - 120, Gdx.graphics.getWidth()/4, 100, 1.0f/4.0f, screenX, screenY, skin);

				if (table != null)
				{
					tooltip = new Tooltip(table, skin, stage);
					tooltip.show(screenX, screenY);
				}	
			}
		}

		return false;
	}

	//----------------------------------------------------------------------
	@Override
	public boolean scrolled(int amount)
	{
		if (!mouseOverUI)
		{
			Global.TileSize -= amount*5;
			if (Global.TileSize < 8)
			{
				Global.TileSize = 8;
			}
		}

		return false;
	}

	//endregion InputProcessor
	//####################################################################//
	//region Public Methods

	//----------------------------------------------------------------------
	public void prepareAbility(ActiveAbility aa)
	{
		preparedAbility = aa;
		preparedAbility.caster = Global.CurrentLevel.player;

		abilityTiles = preparedAbility.getValidTargets();
	}


	//----------------------------------------------------------------------
	public void addConsoleMessage(Line line)
	{
		messageStack.addLine(line);
	}

	//----------------------------------------------------------------------
	public void addAbilityAvailabilityAction(Sprite sprite)
	{
		Table table = new Table();
		table.add(new SpriteWidget(sprite)).size(Global.TileSize/2);
		table.addAction(new SequenceAction(
				Actions.moveTo(Gdx.graphics.getWidth()/2+Global.TileSize/2, Gdx.graphics.getHeight()/2+Global.TileSize+Global.TileSize/2, 1),
				Actions.removeActor()));		
		table.setPosition(Gdx.graphics.getWidth()/2+Global.TileSize/2, Gdx.graphics.getHeight()/2+Global.TileSize);		
		stage.addActor(table);	
		table.setVisible(true);
	}

	//----------------------------------------------------------------------
	public void addActorDamageAction(Entity entity)
	{
		int offsetx = Gdx.graphics.getWidth() / 2 - Global.CurrentLevel.player.tile.x * Global.TileSize;
		int offsety = Gdx.graphics.getHeight() / 2 - Global.CurrentLevel.player.tile.y * Global.TileSize;

		int x = entity.tile.x;
		int y = entity.tile.y;

		int cx = x*Global.TileSize + offsetx;
		int cy = y*Global.TileSize + offsety;

		Label label = new Label("-"+entity.damageAccumulator, skin);
		label.setColor(Color.RED);

		label.addAction(new SequenceAction(
				Actions.moveTo(cx, cy+Global.TileSize/2+Global.TileSize/2, 0.5f),
				Actions.removeActor()));		
		label.setPosition(cx, cy+Global.TileSize/2);		
		stage.addActor(label);	
		label.setVisible(true);

		entity.damageAccumulator = 0;
	}

	//----------------------------------------------------------------------
	public boolean isInBounds(float x, float y, Widget actor)
	{
		Vector2 tmp = new Vector2(x, Gdx.graphics.getHeight() - y);
		tmp = actor.stageToLocalCoordinates(tmp);
		return tmp.x >= 0 && tmp.x <= actor.getPrefWidth() && tmp.y >= 0 && tmp.y <= actor.getPrefHeight();
	}

	//----------------------------------------------------------------------
	public void clearContextMenu()
	{
		if (contextMenu != null)
		{
			contextMenu.remove();
			contextMenu = null;
		}
	}

	//endregion Public Methods
	//####################################################################//
	//region Private Methods

	//----------------------------------------------------------------------
	private void createContextMenu(int screenX, int screenY, GameTile tile)
	{
		Array<ActiveAbility> available = new Array<ActiveAbility>();

		for (int i = 0; i < Global.NUM_ABILITY_SLOTS; i++)
		{
			ActiveAbility aa = Global.CurrentLevel.player.getSlottedActiveAbilities()[i];
			if (aa != null && aa.cooldownAccumulator <= 0)
			{
				available.add(aa);
			}
		}

		boolean entityWithinRange = false;
		if (tile != null && tile.environmentEntity != null)
		{
			entityWithinRange = 
					Math.abs(Global.CurrentLevel.player.tile.x - tile.x) <= 1 &&
					Math.abs(Global.CurrentLevel.player.tile.y - tile.y) <= 1;
		}

		if (available.size > 0 || entityWithinRange)
		{
			Table table = new Table();

			if (tile != null && tile.environmentEntity != null)
			{
				final EnvironmentEntity entity = tile.environmentEntity;

				for (final ActivationAction aa : entity.actions)
				{
					if (!aa.visible) { continue; }

					Table row = new Table();

					row.add(new Label(aa.name, skin)).expand().fill();

					row.addListener(new InputListener()
					{
						@Override
						public boolean touchDown (InputEvent event, float x, float y, int pointer, int button)
						{	
							aa.activate(entity);
							Global.CurrentLevel.player.tasks.add(new TaskWait());
							clearContextMenu();

							return true;
						}
					});

					table.add(row).width(Value.percentWidth(1, table));
					table.row();
				}

				if (entity.actions.size > 0)
				{
					table.add(new Label("-------------", skin));
					table.row();
				}
			}

			for (final ActiveAbility aa : available)
			{
				Table row = new Table();

				row.add(new SpriteWidget(aa.Icon));
				row.add(new Label(aa.getName(), skin)).expand().fill();

				row.addListener(new InputListener()
				{
					@Override
					public boolean mouseMoved (InputEvent event, float x, float y)
					{
						mouseOverUI = true;

						return true;
					}

					@Override
					public boolean touchDown (InputEvent event, float x, float y, int pointer, int button)
					{	
						prepareAbility(aa);
						clearContextMenu();

						return true;
					}
				});

				table.add(row).width(Value.percentWidth(1, table));
				table.row();
			}

			table.pack();

			contextMenu = new Tooltip(table, skin, stage);
			contextMenu.show(screenX-contextMenu.getWidth()/2, screenY-contextMenu.getHeight());
		}
	}

	//endregion Private Methods
	//####################################################################//
	//region Data

	//----------------------------------------------------------------------
	public Tooltip contextMenu;

	//----------------------------------------------------------------------
	private int fps;
	private float fpsAccumulator;
	private float frametime;
	private BitmapFont font;

	//----------------------------------------------------------------------
	public DragDropPayload dragDropPayload;

	//----------------------------------------------------------------------
	InventoryPanel inventoryPanel;
	AbilityPanel abilityPanel;
	AbilityPoolPanel abilityPoolPanel;
	HPWidget hpWidget;
	MessageStack messageStack;
	TabPanel tabPane;

	Skin skin;
	Stage stage;
	SpriteBatch batch;
	Texture blank;
	Texture white;
	InputMultiplexer inputMultiplexer;

	Tooltip tooltip;

	boolean mouseOverUI;

	//----------------------------------------------------------------------
	Array<GameEntity> toBeDrawn = new Array<GameEntity>();
	Array<Entity> hpBars = new Array<Entity>();

	//----------------------------------------------------------------------
	Sprite border;
	int mousePosX;
	int mousePosY;

	//----------------------------------------------------------------------
	public static GameScreen Instance;

	//----------------------------------------------------------------------
	public ActiveAbility preparedAbility;
	private int[][] abilityTiles;

	//endregion Data
	//####################################################################//

	public static class GrayscaleShader 
	{
		static String vertexShader = "attribute vec4 a_position;\n" +
				"attribute vec4 a_color;\n" +
				"attribute vec2 a_texCoord0;\n" +
				"\n" +
				"uniform mat4 u_projTrans;\n" +
				"\n" +
				"varying vec4 v_color;\n" +
				"varying vec2 v_texCoords;\n" +
				"\n" +
				"void main() {\n" +
				"    v_color = a_color;\n" +
				"    v_texCoords = a_texCoord0;\n" +
				"    gl_Position = u_projTrans * a_position;\n" +
				"}";

		static String fragmentShader = "#ifdef GL_ES\n" +
				"    precision mediump float;\n" +
				"#endif\n" +
				"\n" +
				"varying vec4 v_color;\n" +
				"varying vec2 v_texCoords;\n" +
				"uniform sampler2D u_texture;\n" +
				"\n" +
				"void main() {\n" +
				"  vec4 c = v_color * texture2D(u_texture, v_texCoords);\n" +
				"  float grey = (c.r + c.g + c.b) / 3.0;\n" +
				"  gl_FragColor = vec4(grey, grey, grey, c.a);\n" +
				"}";

		public static ShaderProgram Instance = new ShaderProgram(vertexShader,
				fragmentShader);
	}
}
