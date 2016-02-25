package Roguelike.Screens;

import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.Levels.TownCreator;
import Roguelike.RoguelikeGame;
import Roguelike.RoguelikeGame.ScreenEnum;

import Roguelike.Save.SaveFile;
import Roguelike.UI.ButtonKeyboardHelper;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class MainMenuScreen implements Screen, InputProcessor
{
	public MainMenuScreen()
	{
	}

	private void create()
	{
		skin = Global.loadSkin();

		stage = new Stage( new ScreenViewport() );
		batch = new SpriteBatch();
		table = new Table();
		table.setFillParent( true );
		stage.addActor( table );

		mainTable = new Table();

		mainTable.defaults().width( 200 ).height( 50 ).pad( 5 );

		Image image = new Image( AssetManager.loadTexture( "Sprites/Unpacked/Title.png" ) );
		table.add( image ).expandX().fillX().pad( 20 );
		table.row();

		table.add( mainTable ).expand().fill();
		table.row();

		TextButton beginbutton = new TextButton( "Begin Game", skin, "big" );
		beginbutton.addListener( new ClickListener()
		{
			public void clicked( InputEvent event, float x, float y )
			{

				SaveFile save = Global.load();

				if (save == null)
				{
					Global.newWorld();
					Global.save();
					save = Global.load();
				}

				if (save.isDead)
				{
					TownCreator townCreator = new TownCreator();
					townCreator.create();
				}
				else
				{
					LoadingScreen.Instance.set( save.levelManager.current.currentLevel, null, null, null );
					RoguelikeGame.Instance.switchScreen( ScreenEnum.LOADING );
				}
			}
		} );
		mainTable.add( beginbutton ).expandX().fillX().padTop( 20 );
		mainTable.row();

		TextButton testbutton = null;
		if (!Global.RELEASE)
		{
			testbutton = new TextButton( "Test Game - Lake", skin, "big" );
			testbutton.addListener( new ClickListener()
			{
				public void clicked( InputEvent event, float x, float y )
				{

					Global.testWorld();
				}
			} );
			mainTable.add( testbutton ).expandX().fillX().padTop( 20 );
			mainTable.row();
		}

		TextButton obutton = new TextButton( "Options", skin, "big" );
		obutton.addListener( new ClickListener()
		{
			public void clicked( InputEvent event, float x, float y )
			{
				OptionsScreen.Instance.screen = ScreenEnum.MAINMENU;
				RoguelikeGame.Instance.switchScreen( ScreenEnum.OPTIONS );
			}
		} );
		mainTable.add( obutton ).expandX().fillX().padTop( 20 );
		mainTable.row();

		TextButton cbutton = new TextButton( "Credits", skin, "big" );
		cbutton.addListener( new ClickListener()
		{
			public void clicked( InputEvent event, float x, float y )
			{
				RoguelikeGame.Instance.switchScreen( ScreenEnum.CREDITS );
			}
		} );
		mainTable.add( cbutton ).expandX().fillX().padTop( 20 );
		mainTable.row();

		TextButton qbutton = new TextButton( "Quit", skin, "big" );
		qbutton.addListener( new ClickListener()
		{
			public void clicked( InputEvent event, float x, float y )
			{
				Gdx.app.exit();
			}
		} );
		mainTable.add( qbutton ).expandX().fillX().padTop( 20 );
		mainTable.row();

		keyboardHelper = new ButtonKeyboardHelper();
		keyboardHelper.add( beginbutton );

		if (testbutton != null)
		{
			keyboardHelper.add( testbutton );
		}

		keyboardHelper.add( obutton );
		keyboardHelper.add( cbutton );
		keyboardHelper.add( qbutton );

		inputMultiplexer = new InputMultiplexer();

		InputProcessor inputProcessorOne = this;
		InputProcessor inputProcessorTwo = stage;

		inputMultiplexer.addProcessor( inputProcessorTwo );
		inputMultiplexer.addProcessor( inputProcessorOne );
	}

	@Override
	public void show()
	{
		if ( !created )
		{
			create();
			created = true;
		}

		Gdx.input.setInputProcessor( inputMultiplexer );

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

		Global.changeBGM( "Myst" );
	}

	@Override
	public void render( float delta )
	{
		stage.act();

		Gdx.gl.glClearColor( 0.3f, 0.3f, 0.3f, 1 );
		Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT );

		batch.begin();

		// batch.draw( background, 0, 0, Global.Resolution[0],
		// Global.Resolution[1] );

		batch.end();

		stage.draw();

		// limit fps
		sleep( Global.FPS );
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

		float w = 360;
		float h = 480;

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

	// ----------------------------------------------------------------------
	public OrthographicCamera camera;

	boolean created;

	Stage stage;
	Skin skin;

	Table table;
	Table mainTable;

	SpriteBatch batch;

	ButtonKeyboardHelper keyboardHelper;
	InputMultiplexer inputMultiplexer;

	@Override
	public boolean keyDown( int keycode )
	{
		return keyboardHelper.keyDown( keycode );
	}

	@Override
	public boolean keyUp( int keycode )
	{
		return false;
	}

	@Override
	public boolean keyTyped( char character )
	{
		return false;
	}

	@Override
	public boolean touchDown( int screenX, int screenY, int pointer, int button )
	{
		return false;
	}

	@Override
	public boolean touchUp( int screenX, int screenY, int pointer, int button )
	{
		return false;
	}

	@Override
	public boolean touchDragged( int screenX, int screenY, int pointer )
	{
		return false;
	}

	@Override
	public boolean mouseMoved( int screenX, int screenY )
	{
		keyboardHelper.clear();
		return false;
	}

	@Override
	public boolean scrolled( int amount )
	{
		return false;
	}
}
