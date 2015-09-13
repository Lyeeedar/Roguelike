package Roguelike.Screens;

import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.RoguelikeGame;
import Roguelike.RoguelikeGame.ScreenEnum;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Ability.PassiveAbility.PassiveAbility;
import Roguelike.DungeonGeneration.AbstractDungeonGenerator;
import Roguelike.DungeonGeneration.WorldMapGenerator;
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
		BitmapFont font = AssetManager.loadFont( "Sprites/GUI/stan0755.ttf", 60 );
		titleFont = AssetManager.loadFont( "Sprites/GUI/stan0755.ttf", 55 );
		normalFont = AssetManager.loadFont( "Sprites/GUI/stan0755.ttf", 30 );
		highlightFont = AssetManager.loadFont( "Sprites/GUI/stan0755.ttf", 35 );

		skin = new Skin();
		skin.addRegions( new TextureAtlas( Gdx.files.internal( "GUI/uiskin.atlas" ) ) );
		skin.add( "default-font", font, BitmapFont.class );
		skin.load( Gdx.files.internal( "GUI/uiskin.json" ) );

		stage = new Stage( new ScreenViewport() );
		batch = new SpriteBatch();

		background = AssetManager.loadTexture( "Sprites/GUI/Title.png" );
		white = AssetManager.loadTexture( "Sprites/white.png" );
	}

	@Override
	public void show()
	{
		if ( !created )
		{
			create();
			created = true;
		}

		Gdx.input.setInputProcessor( stage );

		camera = new OrthographicCamera( Global.Resolution[0], Global.Resolution[1] );
		camera.translate( Global.Resolution[0] / 2, Global.Resolution[1] / 2 );
		camera.setToOrtho( false, Global.Resolution[0], Global.Resolution[1] );
		camera.update();

		batch.setProjectionMatrix( camera.combined );
		stage.getViewport().setCamera( camera );
		stage.getViewport().setWorldWidth( Global.Resolution[0] );
		stage.getViewport().setWorldHeight( Global.Resolution[1] );
		stage.getViewport().setScreenWidth( Global.ScreenSize[0] );
		stage.getViewport().setScreenHeight( Global.ScreenSize[1] );
	}

	@Override
	public void render( float delta )
	{
		if ( !doCreate ) { return; }

		if ( complete && event != null )
		{
			generationString = "Executing Events";
		}

		stage.act();

		Gdx.gl.glClearColor( 0, 0, 0, 1 );
		Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT );

		batch.begin();

		batch.draw( background, 0, 0, Global.Resolution[0], Global.Resolution[1] );
		batch.draw( white, 0, Global.Resolution[1] / 2 - 20, Global.Resolution[0] * ( percent / 100.0f ), 40 );
		normalFont.draw( batch, generationString, Global.Resolution[0] / 2, Global.Resolution[1] / 2 );

		batch.end();

		stage.draw();

		if ( complete )
		{
			onComplete( generator.getLevel() );
		}

		complete = generator.generate();
		percent = generator.percent;
		generationString = generator.generationText;

		// limit fps
		sleep( Global.FPS );
	}

	public void onComplete( Level level )
	{
		if ( event == null )
		{
			Global.ChangeLevel( level, player, travelType );

			Global.CurrentLevel.player.slottedActiveAbilities.clear();
			Global.CurrentLevel.player.slottedPassiveAbilities.clear();

			for ( ActiveAbility aa : Global.abilityPool.slottedActiveAbilities )
			{
				if ( aa != null )
				{
					aa.caster = Global.CurrentLevel.player;
					Global.CurrentLevel.player.slottedActiveAbilities.add( aa );
				}

			}

			for ( PassiveAbility pa : Global.abilityPool.slottedPassiveAbilities )
			{
				if ( pa != null )
				{
					Global.CurrentLevel.player.slottedPassiveAbilities.add( pa );
				}
			}

			Global.CurrentLevel.player.isVariableMapDirty = true;
			Global.abilityPool.isVariableMapDirty = false;

			RoguelikeGame.Instance.switchScreen( ScreenEnum.GAME );
		}
		else
		{
			event.execute( level );
			event = null;
		}
	}

	// ----------------------------------------------------------------------
	private long diff, start = System.currentTimeMillis();

	public void sleep( int fps )
	{
		if ( fps > 0 )
		{
			diff = System.currentTimeMillis() - start;
			long targetDelay = 1000 / fps;
			if ( diff < targetDelay )
			{
				try
				{
					Thread.sleep( targetDelay - diff );
				}
				catch ( InterruptedException e )
				{
				}
			}
			start = System.currentTimeMillis();
		}
	}

	@Override
	public void resize( int width, int height )
	{
		Global.ScreenSize[0] = width;
		Global.ScreenSize[1] = height;

		float w = Global.TargetResolution[0];
		float h = Global.TargetResolution[1];

		if ( width < height )
		{
			h = w * ( (float) height / (float) width );
		}
		else
		{
			w = h * ( (float) width / (float) height );
		}

		Global.Resolution[0] = (int) w;
		Global.Resolution[1] = (int) h;

		camera = new OrthographicCamera( Global.Resolution[0], Global.Resolution[1] );
		camera.translate( Global.Resolution[0] / 2, Global.Resolution[1] / 2 );
		camera.setToOrtho( false, Global.Resolution[0], Global.Resolution[1] );
		camera.update();

		batch.setProjectionMatrix( camera.combined );
		stage.getViewport().setCamera( camera );
		stage.getViewport().setWorldWidth( Global.Resolution[0] );
		stage.getViewport().setWorldHeight( Global.Resolution[1] );
		stage.getViewport().setScreenWidth( Global.ScreenSize[0] );
		stage.getViewport().setScreenHeight( Global.ScreenSize[1] );
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

	public boolean set( SaveLevel level, GameEntity player, String travelType, PostGenerateEvent event, boolean unloadLevel )
	{
		this.player = player;
		this.travelType = travelType;
		this.event = event;

		if ( Global.CurrentLevel != null && !unloadLevel )
		{
			Global.LoadedLevels.put( Global.CurrentLevel.UID, Global.CurrentLevel );
		}
		else if ( Global.CurrentLevel != null && Global.LoadedLevels.containsKey( Global.CurrentLevel.UID ) )
		{
			Global.LoadedLevels.remove( Global.CurrentLevel.UID );
		}

		if ( Global.LoadedLevels.containsKey( level.UID ) )
		{
			Level loadedLevel = Global.LoadedLevels.get( level.UID );

			if ( event != null )
			{
				onComplete( loadedLevel );
			}
			onComplete( loadedLevel );
			doCreate = false;
			return false;
		}

		this.level = level;

		generator = new WorldMapGenerator( level );

		this.percent = generator.percent;
		this.generationString = generator.generationText;
		this.complete = false;
		doCreate = true;
		return true;
	}

	// ----------------------------------------------------------------------
	public OrthographicCamera camera;

	Stage stage;
	Skin skin;

	SpriteBatch batch;

	BitmapFont titleFont;
	BitmapFont normalFont;
	BitmapFont highlightFont;

	Texture background;
	Texture white;

	boolean doCreate = false;
	boolean complete;
	String generationString;
	int percent = 0;
	SaveLevel level;
	GameEntity player;
	String travelType;
	AbstractDungeonGenerator generator;
	PostGenerateEvent event;

	public static abstract class PostGenerateEvent
	{
		public abstract void execute( Level level );
	}
}