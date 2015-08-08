package Roguelike.Screens;

import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.Global.Direction;
import Roguelike.Global.Passability;
import Roguelike.Global.Statistic;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Ability.PassiveAbility.PassiveAbility;
import Roguelike.Entity.Entity;
import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Entity.EnvironmentEntity.ActivationAction;
import Roguelike.Entity.GameEntity;
import Roguelike.Entity.Tasks.TaskUseAbility;
import Roguelike.Entity.Tasks.TaskWait;
import Roguelike.Items.Item;
import Roguelike.Items.Item.EquipmentSlot;
import Roguelike.Levels.Level;
import Roguelike.Save.SaveAbilityPool;
import Roguelike.Save.SaveFile;
import Roguelike.Save.SaveLevel;
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
import Roguelike.UI.HoverTextButton;
import Roguelike.UI.InventoryPanel;
import Roguelike.UI.MessageStack;
import Roguelike.UI.MessageStack.Line;
import Roguelike.UI.MessageStack.Message;
import Roguelike.UI.SpriteWidget;
import Roguelike.UI.TabPanel;
import Roguelike.UI.Tooltip;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
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
import com.badlogic.gdx.utils.PerformanceCounter;

public class GameScreen implements Screen, InputProcessor, GestureListener
{
	//####################################################################//
	//region Create

	//----------------------------------------------------------------------
	public GameScreen()
	{
		Instance = this;
		
		create();
	}
	
	//----------------------------------------------------------------------
	private void create()
	{
		batch = new SpriteBatch();

		{
			FreeTypeFontGenerator fgenerator = new FreeTypeFontGenerator(Gdx.files.internal("Sprites/GUI/stan0755.ttf"));
			FreeTypeFontParameter parameter = new FreeTypeFontParameter();
			parameter.size = 12;
			parameter.borderWidth = 1;
			parameter.kerning = true;
			parameter.borderColor = Color.BLACK;
			font = fgenerator.generateFont(parameter); // font size 12 pixels
			font.getData().markupEnabled = true;
			fgenerator.dispose(); // don't forget to dispose to avoid memory leaks!
		}
		
		{
			FreeTypeFontGenerator fgenerator = new FreeTypeFontGenerator(Gdx.files.internal("Sprites/GUI/stan0755.ttf"));
			FreeTypeFontParameter parameter = new FreeTypeFontParameter();
			parameter.size = 12;
			parameter.borderWidth = 1;
			parameter.kerning = true;
			parameter.borderColor = Color.BLACK;
			contextMenuNormalFont = fgenerator.generateFont(parameter);
			contextMenuNormalFont.getData().markupEnabled = true;
			fgenerator.dispose(); // don't forget to dispose to avoid memory leaks!
		}
		
		{
			FreeTypeFontGenerator fgenerator = new FreeTypeFontGenerator(Gdx.files.internal("Sprites/GUI/stan0755.ttf"));
			FreeTypeFontParameter parameter = new FreeTypeFontParameter();
			parameter.size = 14;
			parameter.borderWidth = 1;
			parameter.kerning = true;
			parameter.borderColor = Color.BLACK;
			contextMenuHilightFont = fgenerator.generateFont(parameter);
			contextMenuHilightFont.getData().markupEnabled = true;
			fgenerator.dispose(); // don't forget to dispose to avoid memory leaks!
		}

		blank = AssetManager.loadTexture("Sprites/blank.png");
		white = AssetManager.loadTexture("Sprites/white.png");
		border = AssetManager.loadSprite("GUI/frame");

		gestureDetector = new GestureDetector(this);
		inputMultiplexer = new InputMultiplexer();

		LoadUI();
		
		InputProcessor inputProcessorOne = this;
		InputProcessor inputProcessorTwo = stage;
	
		inputMultiplexer.addProcessor(gestureDetector);
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
		
		Global.skin = skin;

		stage = new Stage(new ScreenViewport());

		abilityPanel = new AbilityPanel(skin, stage);

		abilityPoolPanel = new AbilityPoolPanel(skin, stage);
		inventoryPanel = new InventoryPanel(skin, stage);
		messageStack = new MessageStack();
		messageStack.addLine(new Line(new Message("Welcome to the DUNGEON!")));
		
		Widget blankTab = new Widget();

		tabPane = new TabPanel();

		TabPanel.Tab tab = tabPane.addTab(AssetManager.loadSprite("blank"), blankTab);
		tabPane.addTab(AssetManager.loadSprite("GUI/Inventory"), inventoryPanel);
		tabPane.addTab(AssetManager.loadSprite("GUI/Abilities"), abilityPoolPanel);
		
		tabPane.selectTab(tab);
		
		stage.addActor(tabPane);
		stage.addActor(abilityPanel);
		
		stage.addActor(inventoryPanel);
		stage.addActor(abilityPoolPanel);
		stage.addActor(abilityPanel);
	
		relayoutUI();
	}
	
	//----------------------------------------------------------------------
	public void relayoutUI()
	{
		abilityPanel.setX(stage.getWidth() - abilityPanel.getPrefWidth() - 20);
		abilityPanel.setY(20);
		abilityPanel.setWidth(abilityPanel.getPrefWidth());
		abilityPanel.setHeight(abilityPanel.getPrefHeight());
		
		if (Global.ANDROID)
		{
			tabPane.setX(20);
			tabPane.setY(20);
			tabPane.setHeight(roundTo(stage.getHeight()-40, 32));
			tabPane.setWidth(stage.getWidth());
		}
		else
		{
			tabPane.setX(20);
			tabPane.setY(20);
			tabPane.setHeight(roundTo(stage.getHeight()/2, 32));
			tabPane.setWidth(stage.getWidth());
		}
	}
	
	//----------------------------------------------------------------------
	private float roundTo(float val, float multiple)
	{
		return (float) (multiple * Math.floor(val / multiple));
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

		updateCounter.start();
		Global.CurrentLevel.update(delta);
		updateCounter.stop();
		
		int offsetx = Global.Resolution[0] / 2 - Global.CurrentLevel.player.tile.x * Global.TileSize;
		int offsety = Global.Resolution[1] / 2 - Global.CurrentLevel.player.tile.y * Global.TileSize;

		if (Global.CurrentLevel.player.sprite.spriteAnimation instanceof MoveAnimation)
		{
			int[] offset = Global.CurrentLevel.player.sprite.spriteAnimation.getRenderOffset();

			offsetx -= offset[0];
			offsety -= offset[1];
		}
		
		// do screen shake
		if (screenShakeRadius > 2)
		{
			screenShakeAccumulator += delta;
			while (screenShakeAccumulator >= ScreenShakeSpeed)
			{
				screenShakeAccumulator -= ScreenShakeSpeed;				
				screenShakeAngle += (150 + MathUtils.random()*60);
				screenShakeRadius *= 0.9f;
			}
			
			offsetx += Math.sin(screenShakeAngle) * screenShakeRadius;
			offsety += Math.cos(screenShakeAngle) * screenShakeRadius;
		}

		int mousex = (mousePosX - offsetx) / Global.TileSize;
		int mousey = (mousePosY - offsety) / Global.TileSize;
		
		int tileSize3 = Global.TileSize / 3;

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();

		toBeDrawn.clear();
		hpBars.clear();
		overHead.clear();

		tileRender.start();
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
						if (gtile.environmentEntity.overHead)
						{
							overHead.add(gtile.environmentEntity);
						}
						else
						{
							int cx = x*Global.TileSize + offsetx;
							int cy = y*Global.TileSize + offsety;
							
							if (gtile.environmentEntity.location == Direction.CENTER)
							{
								gtile.environmentEntity.sprite.render(batch, cx, cy, Global.TileSize, Global.TileSize);
							}
							else
							{
								Direction dir = gtile.environmentEntity.location;
								gtile.environmentEntity.sprite.render(batch, cx + tileSize3*(dir.GetX()*-1+1), cy + tileSize3*(dir.GetY()*-1+1), tileSize3, tileSize3);
							}

							if (gtile.environmentEntity.HP < gtile.environmentEntity.statistics.get(Statistic.MAXHP) || gtile.environmentEntity.stacks.size > 0)
							{
								hpBars.add(gtile.environmentEntity);
							}
						}
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
						int cx = x*Global.TileSize + offsetx;
						int cy = y*Global.TileSize + offsety;
						
						if (hist.location != Direction.CENTER)
						{
							Direction dir = hist.location;
							hist.sprite.render(batch, cx + tileSize3*(dir.GetX()*-1+1), cy + tileSize3*(dir.GetY()*-1+1), tileSize3, tileSize3);
						}
						else
						{
							hist.sprite.render(batch, cx, cy, Global.TileSize, Global.TileSize, hist.animationState);
						}
					}				

					batch.setColor(Color.WHITE);				
				}
			}
		}
		batch.setShader(null);
		tileRender.stop();
		
		itemRender.start();
		for (int x = 0; x < Global.CurrentLevel.width; x++)
		{
			for (int y = 0; y < Global.CurrentLevel.height; y++)
			{
				GameTile gtile = Global.CurrentLevel.getGameTile(x, y);

				if (gtile.GetVisible())
				{
					batch.setColor(gtile.light);
					
					for (Item i : gtile.items)
					{
						i.getIcon().render(batch, x*Global.TileSize + offsetx, y*Global.TileSize + offsety, Global.TileSize, Global.TileSize);
					}
				}
			}
		}
		itemRender.stop();

		if (!mouseOverUI && !Global.ANDROID)
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
				GameTile mouseTile = Global.CurrentLevel.getGameTile(mousex, mousey);
				if (Passability.isPassable(mouseTile.tileData.passableBy, Global.CurrentLevel.player.getTravelType()))
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

		entityRender.start();
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

			hpBars.add(entity);
		}
		
		for (EnvironmentEntity ee : overHead)
		{
			int cx = ee.tile.x*Global.TileSize + offsetx;
			int cy = ee.tile.y*Global.TileSize + offsety;
			
			if (ee.location == Direction.CENTER)
			{
				ee.sprite.render(batch, cx, cy, Global.TileSize, Global.TileSize);
			}
			else
			{
				Direction dir = ee.location;
				ee.sprite.render(batch, cx + tileSize3*(dir.GetX()*-1+1), cy + tileSize3*(dir.GetY()*-1+1), tileSize3, tileSize3);
			}

			if (ee.HP < ee.statistics.get(Statistic.MAXHP) || ee.stacks.size > 0)
			{
				hpBars.add(ee);
			}
		}
		
		entityRender.stop();

		for (Entity e : hpBars)
		{
			int x = e.tile.x;
			int y = e.tile.y;

			int cx = x*Global.TileSize + offsetx;
			int cy = y*Global.TileSize + offsety;
			
			if (e.sprite.spriteAnimation != null)
			{
				int[] offset = e.sprite.spriteAnimation.getRenderOffset();
				cx += offset[0];
				cy += offset[1];
			}

			EntityStatusRenderer.draw(e, batch, cx, cy, Global.TileSize, Global.TileSize, 1.0f/8.0f);
		}

		effectRender.start();
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

		if (preparedAbility != null)
		{
			batch.setColor(0.3f, 0.6f, 0.8f, 0.5f);
			for (int[] tile : abilityTiles)
			{
				batch.draw(white, tile[0]*Global.TileSize + offsetx, tile[1]*Global.TileSize + offsety, Global.TileSize, Global.TileSize);
			}
		}

		if (Global.ANDROID)
		{
			EntityStatusRenderer.draw(Global.CurrentLevel.player, batch, Global.Resolution[0]-(Global.Resolution[0]/4)-120, Global.Resolution[1] - 120, Global.Resolution[0]/4, 100, 1.0f/4.0f);
		}
		else
		{
			EntityStatusRenderer.draw(Global.CurrentLevel.player, batch, 20, Global.Resolution[1] - 120, Global.Resolution[0]/4, 100, 1.0f/4.0f);
		}
		
		effectRender.stop();

		batch.end();

		uiRender.start();
		stage.act(delta);
		stage.draw();
		uiRender.stop();

		batch.begin();

		if (dragDropPayload != null && dragDropPayload.shouldDraw())
		{
			dragDropPayload.sprite.render(batch, (int)dragDropPayload.x, (int)dragDropPayload.y, 32, 32);
		}

		font.draw(batch, "FPS: "+fps, Global.Resolution[0]-100, Global.Resolution[1] - 20);

		batch.end();
		
		updateCounter.tick();
		tileRender.tick();
		itemRender.tick();
		entityRender.tick();
		effectRender.tick();
		uiRender.tick();
		
		Level.updateDead.tick();
		Level.updateLights.tick();
		Level.updateSounds.tick();
		Level.updateSprites.tick();
		Level.updateVisible.tick();

		Level.updatePart1.tick();
		Level.updatePart2.tick();
		Level.updatePart3.tick();
		Level.updatePart4.tick();
		
		debugOut += delta;
		if (debugOut > 1)
		{
			debugOut = 0;
			
			System.out.println("Timers:");
			
			System.out.println(updateCounter.toString());
			System.out.println(tileRender.toString());
			System.out.println(itemRender.toString());
			System.out.println(entityRender.toString());
			System.out.println(effectRender.toString());
			System.out.println(uiRender.toString());
			
			System.out.print("\n");
			
			System.out.println(Level.updateDead.toString());
			System.out.println(Level.updateLights.toString());
			System.out.println(Level.updateSounds.toString());
			System.out.println(Level.updateSprites.toString());
			System.out.println(Level.updateVisible.toString());
			System.out.println(Level.updatePart1.toString());
			System.out.println(Level.updatePart2.toString());
			System.out.println(Level.updatePart3.toString());
			System.out.println(Level.updatePart4.toString());
			
			System.out.println("\n");
		}
	}
	float debugOut = 0;

	//----------------------------------------------------------------------
	@Override
	public void resize(int width, int height)
	{
		Global.ScreenSize[0] = width;
		Global.ScreenSize[1] = height;

        float w = (float)Global.TargetResolution[0];
        float h = (float)Global.TargetResolution[1];
        
        if (width < height)
        {
        	h = w * ((float)height/(float)width);
        }
        else
        {
        	w = h * ((float)width/(float)height);
        }
        
        Global.Resolution[0] = (int)w;
        Global.Resolution[1] = (int)h;	
		
		camera = new OrthographicCamera(Global.Resolution[0], Global.Resolution[1]);
		camera.translate(Global.Resolution[0] / 2, Global.Resolution[1] / 2);
		camera.setToOrtho(false, Global.Resolution[0], Global.Resolution[1]);
		camera.update();
		
		batch.setProjectionMatrix(camera.combined);
		stage.getViewport().setCamera(camera);
		stage.getViewport().setWorldWidth(Global.Resolution[0]);
		stage.getViewport().setWorldHeight(Global.Resolution[1]);
		stage.getViewport().setScreenWidth(Global.ScreenSize[0]);
		stage.getViewport().setScreenHeight(Global.ScreenSize[1]);
		
		relayoutUI();
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
		if (keycode == Keys.S)
		{
			SaveFile save = new SaveFile();
			
			Global.AllLevels.get(Global.CurrentLevel.UID).store(Global.CurrentLevel);
			save.allLevels = Global.AllLevels;
			save.currentLevel = Global.CurrentLevel.UID;
			
			save.abilityPool = new SaveAbilityPool();
			save.abilityPool.store(Global.abilityPool);
			
			save.save();
		}
		else if (keycode == Keys.L)
		{
			SaveFile save = new SaveFile();
			save.load();
			
			Global.AllLevels = save.allLevels;
			Global.ChangeLevel(Global.AllLevels.get(save.currentLevel).create());
			Global.abilityPool = save.abilityPool.create();
		}
		else if (keycode == Keys.I)
		{
			tabPane.toggleTab(inventoryPanel);
		}
		else if (keycode == Keys.K)
		{
			tabPane.toggleTab(abilityPoolPanel);
		}
		
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
		if (tooltip != null)
		{
			tooltip.setVisible(false);
			tooltip.remove();
			tooltip = null;
		}
		
		clearContextMenu();
		return true;
	}

	//----------------------------------------------------------------------
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button)
	{
		if (tooltip != null)
		{
			tooltip.setVisible(false);
			tooltip.remove();
			tooltip = null;
		}
		
		clearContextMenu();
		
		if (longPressed || dragged)
		{
			return false;
		}
		
		Vector3 mousePos = camera.unproject(new Vector3(screenX, screenY, 0));
		
		int mousePosX = (int) mousePos.x;
		int mousePosY = (int) mousePos.y;

		int offsetx = Global.Resolution[0] / 2 - Global.CurrentLevel.player.tile.x * Global.TileSize;
		int offsety = Global.Resolution[1] / 2 - Global.CurrentLevel.player.tile.y * Global.TileSize;

		int x = (mousePosX - offsetx) / Global.TileSize;
		int y = (mousePosY - offsety) / Global.TileSize;

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

				createContextMenu(mousePosX, mousePosY, tile);
			}
			else
			{
				if (x >= 0 && x < Global.CurrentLevel.width && y >= 0 && y < Global.CurrentLevel.height && Global.CurrentLevel.getSeenTile(x, y).seen)
				{
					Global.CurrentLevel.player.AI.setData("ClickPos", new int[]{x, y});
				}
				else
				{
					x = MathUtils.clamp(x, -1, 1);
					y = MathUtils.clamp(y, -1, 1);
					
					x += Global.CurrentLevel.player.tile.x;
					y += Global.CurrentLevel.player.tile.y;
					
					Global.CurrentLevel.player.AI.setData("ClickPos", new int[]{x, y});
				}
			}
		}

		return true;
	}

	//----------------------------------------------------------------------
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer)
	{
		if (dragDropPayload != null)
		{
			dragDropPayload.x = screenX - 16;
			dragDropPayload.y = Global.Resolution[1] - screenY - 16;
		}
		
		if (Math.abs(screenX-startX) > 10 || Math.abs(screenY-startY) > 10)
		{
			dragged = true;
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

		mouseOverUI = false;

		Vector3 mousePos = camera.unproject(new Vector3(screenX, screenY, 0));
		
		mousePosX = (int) mousePos.x;
		mousePosY = (int) mousePos.y;

		stage.setScrollFocus(null);

		int offsetx = Global.Resolution[0] / 2 - Global.CurrentLevel.player.tile.x * Global.TileSize;
		int offsety = Global.Resolution[1] / 2 - Global.CurrentLevel.player.tile.y * Global.TileSize;

		int x = (mousePosX - offsetx) / Global.TileSize;
		int y = (mousePosY - offsety) / Global.TileSize;

		if (x >= 0 && x < Global.CurrentLevel.width && y >= 0 && y < Global.CurrentLevel.height)
		{
			GameTile tile = Global.CurrentLevel.getGameTile(x, y);

			if (tile.entity != null)
			{
				Table table = EntityStatusRenderer.getMouseOverTable(tile.entity, x*Global.TileSize+offsetx, y*Global.TileSize+offsety, Global.TileSize, Global.TileSize, 1.0f/8.0f, mousePosX, mousePosY, skin);

				if (table != null)
				{
					tooltip = new Tooltip(table, skin, stage);
					tooltip.show(mousePosX, mousePosY);
				}					
			}
		}

		{
			Table table = EntityStatusRenderer.getMouseOverTable(Global.CurrentLevel.player, 20, Global.Resolution[1] - 120, Global.Resolution[0]/4, 100, 1.0f/4.0f, mousePosX, mousePosY, skin);

			if (table != null)
			{
				tooltip = new Tooltip(table, skin, stage);
				tooltip.show(mousePosX, mousePosY);
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
	//region GestureListener
	
	//----------------------------------------------------------------------
	@Override
	public boolean touchDown(float x, float y, int pointer, int button)
	{
		longPressed = false;
		dragged = false;
		
		startX = x;
		startY = y;
		
		return false;
	}

	//----------------------------------------------------------------------
	@Override
	public boolean tap(float x, float y, int count, int button)
	{
		return false;
	}

	//----------------------------------------------------------------------
	@Override
	public boolean longPress(float x, float y)
	{
		longPressed = true;
		return false;
	}

	//----------------------------------------------------------------------
	@Override
	public boolean fling(float velocityX, float velocityY, int button)
	{
		return false;
	}

	//----------------------------------------------------------------------
	@Override
	public boolean pan(float x, float y, float deltaX, float deltaY)
	{
		return false;
	}

	//----------------------------------------------------------------------
	@Override
	public boolean panStop(float x, float y, int pointer, int button)
	{
		return false;
	}

	//----------------------------------------------------------------------
	@Override
	public boolean zoom(float initialDistance, float distance)
	{
		float amount = distance - initialDistance;
		
		Global.TileSize -= amount/5;
		if (Global.TileSize < 8)
		{
			Global.TileSize = 8;
		}
		
		return false;
	}

	//----------------------------------------------------------------------
	@Override
	public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2)
	{
		return false;
	}
	
	//endregion GestureListener
	//####################################################################//
	//region Public Methods

	//----------------------------------------------------------------------
	public void prepareAbility(ActiveAbility aa)
	{
		preparedAbility = aa;
		preparedAbility.caster = Global.CurrentLevel.player;
		preparedAbility.source = Global.CurrentLevel.player.tile;

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
				Actions.moveTo(Global.Resolution[0]/2+Global.TileSize/2, Global.Resolution[1]/2+Global.TileSize+Global.TileSize/2, 1),
				Actions.removeActor()));		
		table.setPosition(Global.Resolution[0]/2+Global.TileSize/2, Global.Resolution[1]/2+Global.TileSize);		
		stage.addActor(table);	
		table.setVisible(true);
	}

	//----------------------------------------------------------------------
	public void addPopupBubble(Entity entity)
	{
		int offsetx = Global.Resolution[0] / 2 - Global.CurrentLevel.player.tile.x * Global.TileSize;
		int offsety = Global.Resolution[1] / 2 - Global.CurrentLevel.player.tile.y * Global.TileSize;

		int x = entity.tile.x;
		int y = entity.tile.y;

		int cx = x*Global.TileSize + offsetx;
		int cy = y*Global.TileSize + offsety;

		entity.popup.addAction(new SequenceAction(
				Actions.fadeOut(2),
				Actions.removeActor()));		
		entity.popup.setPosition(cx, cy+Global.TileSize/2);		
		stage.addActor(entity.popup);	
		entity.popup.setVisible(true);
		
		entity.popup = null;
	}
	
	//----------------------------------------------------------------------
	public void addActorDamageAction(Entity entity)
	{
		int offsetx = Global.Resolution[0] / 2 - Global.CurrentLevel.player.tile.x * Global.TileSize;
		int offsety = Global.Resolution[1] / 2 - Global.CurrentLevel.player.tile.y * Global.TileSize;

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
	public void addActorHealingAction(Entity entity)
	{
		int offsetx = Global.Resolution[0] / 2 - Global.CurrentLevel.player.tile.x * Global.TileSize;
		int offsety = Global.Resolution[1] / 2 - Global.CurrentLevel.player.tile.y * Global.TileSize;

		int x = entity.tile.x;
		int y = entity.tile.y;

		int cx = x*Global.TileSize + offsetx;
		int cy = y*Global.TileSize + offsety;

		Label label = new Label("+"+entity.healingAccumulator, skin);
		label.setColor(Color.GREEN);

		label.addAction(new SequenceAction(
				Actions.moveTo(cx, cy+Global.TileSize/2+Global.TileSize/2, 0.5f),
				Actions.removeActor()));		
		label.setPosition(cx, cy+Global.TileSize/2);		
		stage.addActor(label);	
		label.setVisible(true);

		entity.healingAccumulator = 0;
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
			ActiveAbility aa = Global.abilityPool.slottedActiveAbilities[i];
			if (aa != null && aa.isAvailable())
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
		
		Table table = new Table();

		if (available.size > 0 || entityWithinRange)
		{
			if (tile != null && tile.environmentEntity != null)
			{
				final EnvironmentEntity entity = tile.environmentEntity;

				boolean hadAction = false;
				for (final ActivationAction aa : entity.actions)
				{
					if (!aa.visible) { continue; }

					Table row = new Table();
					
					HoverTextButton button = new HoverTextButton(aa.name, contextMenuNormalFont, contextMenuHilightFont);
					button.changePadding(5, 5);					
					row.add(button).expand().fill();

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
					
					hadAction = true;
				}

				if (hadAction)
				{
					table.add(new Label("-------------", skin));
					table.row();
				}
			}

			boolean hadAbility = false;
			for (final ActiveAbility aa : available)
			{
				Table row = new Table();

				row.add(new SpriteWidget(aa.Icon));
				
				HoverTextButton button = new HoverTextButton(aa.getName(), contextMenuNormalFont, contextMenuHilightFont);
				button.changePadding(5, 5);
				row.add(button).expand().fill();

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
				
				hadAbility = true;
			}
			
			if (hadAbility)
			{
				table.add(new Label("-------------", skin));
				table.row();
			}
		}
		
		{
			Table row = new Table();

			HoverTextButton button = new HoverTextButton("Rest a while", contextMenuNormalFont, contextMenuHilightFont);
			button.changePadding(5, 5);			
			row.add(button).expand().fill();

			row.addListener(new InputListener()
			{
				@Override
				public boolean touchDown (InputEvent event, float x, float y, int pointer, int button)
				{	
					Global.CurrentLevel.player.AI.setData("Rest", true);
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

	//endregion Private Methods
	//####################################################################//
	//region Data
	
	private PerformanceCounter updateCounter = new PerformanceCounter("Update");
	private PerformanceCounter tileRender = new PerformanceCounter("TileRender");
	private PerformanceCounter itemRender = new PerformanceCounter("ItemRender");
	private PerformanceCounter entityRender = new PerformanceCounter("EntityRender");
	private PerformanceCounter effectRender = new PerformanceCounter("EffectRender");
	private PerformanceCounter uiRender = new PerformanceCounter("UIRender");
	
	//----------------------------------------------------------------------
	private boolean longPressed;
	private boolean dragged;
	private float startX;
	private float startY;

	//----------------------------------------------------------------------
	public GestureDetector gestureDetector;
	
	//----------------------------------------------------------------------
	public OrthographicCamera camera;
	
	//----------------------------------------------------------------------
	public Tooltip contextMenu;

	//----------------------------------------------------------------------
	private int fps;
	private float fpsAccumulator;
	private float frametime;
	private BitmapFont font;
	
	//----------------------------------------------------------------------
	private BitmapFont contextMenuNormalFont;
	private BitmapFont contextMenuHilightFont;

	//----------------------------------------------------------------------
	public DragDropPayload dragDropPayload;
	
	//----------------------------------------------------------------------
	private static final float ScreenShakeSpeed = 0.02f;
	public float screenShakeRadius;
	public float screenShakeAngle;
	private float screenShakeAccumulator;	

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
	public InputMultiplexer inputMultiplexer;

	Tooltip tooltip;

	public boolean mouseOverUI;

	//----------------------------------------------------------------------
	Array<GameEntity> toBeDrawn = new Array<GameEntity>();
	Array<EnvironmentEntity> overHead = new Array<EnvironmentEntity>();
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
