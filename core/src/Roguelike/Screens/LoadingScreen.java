package Roguelike.Screens;

import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.RoguelikeGame;
import Roguelike.RoguelikeGame.ScreenEnum;
import Roguelike.DungeonGeneration.RecursiveDockGenerator;
import Roguelike.Entity.GameEntity;
import Roguelike.Levels.Level;
import Roguelike.Save.SaveLevel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class LoadingScreen implements Screen
{	
	public static LoadingScreen Instance;
	boolean created = false;
	
	public LoadingScreen()
	{
		Instance = this;
	}
	
	private void create()
	{
		BitmapFont font = AssetManager.loadFont("Sprites/GUI/stan0755.ttf", 60);
		titleFont = AssetManager.loadFont("Sprites/GUI/stan0755.ttf", 55);
		normalFont = AssetManager.loadFont("Sprites/GUI/stan0755.ttf", 30);
		highlightFont = AssetManager.loadFont("Sprites/GUI/stan0755.ttf", 35);

		skin = new Skin();
		skin.addRegions(new TextureAtlas(Gdx.files.internal("GUI/uiskin.atlas")));
		skin.add("default-font", font, BitmapFont.class);
		skin.load(Gdx.files.internal("GUI/uiskin.json"));

		stage = new Stage(new ScreenViewport());
		batch = new SpriteBatch();
		
		background = AssetManager.loadTexture("Sprites/GUI/Title.png");
		white = AssetManager.loadTexture("Sprites/white.png");
	}

	@Override
	public void show()
	{
		if (!created)
		{
			create();
			created = true;
		}
		
		Gdx.input.setInputProcessor(stage);
		
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
	}

	@Override
	public void render(float delta)
	{
		if (complete && event != null)
		{
			generationString = "Executing Events";
		}
		
		stage.act();
		
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		batch.begin();
		
		batch.draw(background, 0, 0, Global.Resolution[0], Global.Resolution[1]);
		batch.draw(white, 0, Global.Resolution[1]/2-2, Global.Resolution[0]*((float)percent/100.0f), Global.Resolution[1]/2+2);
		normalFont.draw(batch, generationString, Global.Resolution[0]/2, Global.Resolution[1]/2);
		
		batch.end();
		
		stage.draw();
		
		if (complete)
		{
			if (event == null)
			{
				Global.ChangeLevel(generator.getLevel(), player, travelType);
				RoguelikeGame.Instance.switchScreen(ScreenEnum.GAME);
			}
			else
			{
				event.execute(generator.getLevel());
				event = null;
			}
		}
		
		complete = generator.generate();
		percent = generator.percent;
		generationString = generator.generationText;
	}

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
	}

	@Override
	public void pause()
	{
	}

	@Override
	public void resume()
	{
	}

	@Override
	public void hide()
	{
	}

	@Override
	public void dispose()
	{
	}
	
	public void set(SaveLevel level, GameEntity player, String travelType, PostGenerateEvent event)
	{
		this.level = level;
		this.player = player;
		this.travelType = travelType;
		this.event = event;
		
		generator = new RecursiveDockGenerator(level.fileName, level.depth, level.seed, !level.created, level.UID);
		level.created = true;
		
		this.percent = generator.percent;
		this.generationString = generator.generationText;
		this.complete = false;

		generator.additionalRooms.clear();
		generator.additionalRooms.addAll(level.requiredRooms);
	}
	
	//----------------------------------------------------------------------
	public OrthographicCamera camera;
	
	Stage stage;
	Skin skin;
	
	SpriteBatch batch;

	BitmapFont titleFont;
	BitmapFont normalFont;
	BitmapFont highlightFont;
	
	Texture background;
	Texture white;
	
	boolean complete;
	String generationString;
	int percent = 0;
	SaveLevel level;
	GameEntity player;
	String travelType;
	RecursiveDockGenerator generator;
	PostGenerateEvent event;
	
	public static abstract class PostGenerateEvent
	{
		public abstract void execute(Level level);
	}
}