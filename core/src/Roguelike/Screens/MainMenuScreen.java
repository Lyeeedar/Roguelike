package Roguelike.Screens;

import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.Levels.TownCreator;
import Roguelike.RoguelikeGame;
import Roguelike.RoguelikeGame.ScreenEnum;

import Roguelike.Save.SaveFile;
import Roguelike.UI.SaveSlotButton;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class MainMenuScreen implements Screen
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

		saveTable = new Table();
		mainTable = new Table();

		saveTable.defaults().width( 200 ).height( 50 ).pad( 5 );
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
//				mainTable.remove();
//				table.add( saveTable ).expand().fill();
//				table.row();
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

		recreateUI();
	}

	private void recreateUI()
	{
		saveTable.clear();

		SaveSlotButton s1button = new SaveSlotButton( skin, 1 );
		saveTable.add( s1button ).expandX().fillX();
		saveTable.row();

		SaveSlotButton s2button = new SaveSlotButton( skin, 2 );
		saveTable.add( s2button ).expandX().fillX();
		saveTable.row();

		SaveSlotButton s3button = new SaveSlotButton( skin, 3 );
		saveTable.add( s3button ).expandX().fillX();
		saveTable.row();

		TextButton bbutton = new TextButton( "Back", skin, "big" );
		bbutton.addListener( new ClickListener()
		{
			public void clicked( InputEvent event, float x, float y )
			{
				saveTable.remove();
				table.add( mainTable ).expand().fill();
				table.row();
			}
		} );
		saveTable.add( bbutton ).expandX().fillX().padTop( 20 );
		saveTable.row();
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

		Global.changeBGM( "Myst" );

		recreateUI();
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
	Table saveTable;

	SpriteBatch batch;
}
