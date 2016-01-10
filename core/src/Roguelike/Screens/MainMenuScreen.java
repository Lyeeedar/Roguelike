package Roguelike.Screens;

import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.RoguelikeGame;
import Roguelike.RoguelikeGame.ScreenEnum;

import Roguelike.Save.SaveFile;
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

		background = AssetManager.loadTexture( "Sprites/GUI/Title.png" );

		createUI();
	}

	private void createUI()
	{
		Table table = new Table();
		// table.debug();

		// Label title = new Label( "Chronicles of Aether", skin );
		// table.add( title ).expandY().top().padTop( 100 );
		// table.row();
		Image image = new Image( AssetManager.loadTextureRegion( "Sprites/GUI/Title.png" ) );
		table.add( image ).expandX().fillX();
		table.row();

		Table buttonTable = new Table();
		buttonTable.defaults().width( 200 ).pad( 5 );

		boolean hasFile = false;

		try
		{
			SaveFile save = new SaveFile();
			save.load();
			hasFile = true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		if (hasFile)
		{
			TextButton cbutton = new TextButton( "Continue", skin, "big" );
			cbutton.addListener( new InputListener()
			{
				@Override
				public boolean touchDown( InputEvent event, float x, float y, int pointer, int button )
				{
					return true;
				}

				@Override
				public void touchUp( InputEvent event, float x, float y, int pointer, int button )
				{
					Global.load();
				}
			} );
			buttonTable.add( cbutton ).expandX().fillX();
			buttonTable.row();
		}

		TextButton ngbutton = new TextButton( "New Game", skin, "big" );
		ngbutton.addListener( new InputListener()
		{
			@Override
			public boolean touchDown( InputEvent event, float x, float y, int pointer, int button )
			{
				return true;
			}

			@Override
			public void touchUp( InputEvent event, float x, float y, int pointer, int button )
			{
				RoguelikeGame.Instance.switchScreen( ScreenEnum.CHARACTERCREATION );
			}
		} );
		buttonTable.add( ngbutton ).expandX().fillX();
		buttonTable.row();

		TextButton qbutton = new TextButton( "Quit", skin, "big" );
		qbutton.addListener( new InputListener()
		{
			@Override
			public boolean touchDown( InputEvent event, float x, float y, int pointer, int button )
			{
				return true;
			}

			@Override
			public void touchUp( InputEvent event, float x, float y, int pointer, int button )
			{
				Gdx.app.exit();
			}
		} );
		buttonTable.add( qbutton ).expandX().fillX();
		buttonTable.row();

		if (Global.ANDROID)
		{
			table.add( buttonTable ).width( Value.percentWidth( 0.3f, table ) ).expand().fill().pad( 20 );
		}
		else
		{
			table.add( buttonTable ).width( Value.percentWidth( 0.3f, table ) ).expand().pad( 20 ).bottom().right();
		}

		table.setFillParent( true );
		stage.addActor( table );
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

	SpriteBatch batch;

	Texture background;
}
