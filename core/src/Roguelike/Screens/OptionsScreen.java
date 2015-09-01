package Roguelike.Screens;

import java.text.DecimalFormat;

import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.RoguelikeGame.ScreenEnum;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class OptionsScreen implements Screen, InputProcessor
{
	public static OptionsScreen Instance;

	public OptionsScreen()
	{
		Instance = this;
	}

	public void create()
	{
		BitmapFont font = AssetManager.loadFont( "Sprites/GUI/stan0755.ttf", 15 );

		skin = new Skin();
		skin.addRegions( new TextureAtlas( Gdx.files.internal( "GUI/uiskin.atlas" ) ) );
		skin.add( "default-font", font, BitmapFont.class );
		skin.load( Gdx.files.internal( "GUI/uiskin.json" ) );

		stage = new Stage( new ScreenViewport() );
		batch = new SpriteBatch();

		background = AssetManager.loadTexture( "Sprites/GUI/Title.png" );

		table = new Table();
		stage.addActor( table );
		table.setFillParent( true );

		buttons = new Table();
		options = new Table();

		table.add( buttons );
		table.row();
		table.add( options );

		inputMultiplexer = new InputMultiplexer();

		InputProcessor inputProcessorOne = this;
		InputProcessor inputProcessorTwo = stage;

		inputMultiplexer.addProcessor( inputProcessorTwo );
		inputMultiplexer.addProcessor( inputProcessorOne );
	}

	public void createVideo()
	{
		final Preferences prefs = Global.ApplicationChanger.prefs;

		options.clear();

		Label resolutionLabel = new Label( "Resolution ", skin );
		final SelectBox<String> resolutions = new SelectBox<String>( skin );
		resolutions.setItems( Global.ApplicationChanger.getSupportedDisplayModes() );
		resolutions.setSelected( prefs.getString( "resolutionX" ) + "x" + prefs.getString( "resolutionY" ) );

		Label windowLabel = new Label( "Window Mode ", skin );
		final SelectBox<String> windowMode = new SelectBox<String>( skin );
		windowMode.setItems( new String[] { "Window", "Borderless Window", "Fullscreen" } );
		if ( prefs.getBoolean( "fullscreen" ) )
		{
			windowMode.setSelected( "Fullscreen" );
		}
		else if ( prefs.getBoolean( "borderless" ) )
		{
			windowMode.setSelected( "Borderless Window" );
		}
		else
		{
			windowMode.setSelected( "Window" );
		}

		Label fpsLabel = new Label( "Frames per second ", skin );
		final SelectBox<String> fps = new SelectBox<String>( skin );
		fps.setItems( new String[] { "VSync", "30", "60", "120" } );
		if ( prefs.getBoolean( "vSync" ) )
		{
			fps.setSelected( "VSync" );
		}
		else
		{
			fps.setSelected( "" + prefs.getInteger( "fps" ) );
		}

		Label animLabel = new Label( "Animation Speed", skin );
		final SelectBox<String> animspeed = new SelectBox<String>( skin );
		animspeed.setItems( new String[] { "None", "0.25x", "0.5x", "0.75x", "1x", "2x", "4x", "8x" } );
		if ( prefs.getFloat( "animspeed" ) == 0 )
		{
			animspeed.setSelected( "None" );
		}
		else
		{
			animspeed.setSelected( new DecimalFormat( "#.##" ).format( prefs.getFloat( "animspeed" ) ) + "x" );
		}

		Label msaaLabel = new Label( "MSAA Samples", skin );
		final SelectBox<Integer> msaa = new SelectBox<Integer>( skin );
		msaa.setItems( new Integer[] { 0, 2, 4, 8, 16, 32 } );
		msaa.setSelected( prefs.getInteger( "msaa" ) );

		TextButton apply = new TextButton( "Apply", skin );
		apply.addListener( new InputListener()
		{
			@Override
			public boolean touchDown( InputEvent event, float x, float y, int pointer, int button )
			{

				String selectedResolution = resolutions.getSelected();

				int split = selectedResolution.indexOf( "x" );
				int rX = Integer.parseInt( selectedResolution.substring( 0, split ) );
				int rY = Integer.parseInt( selectedResolution.substring( split + 1, selectedResolution.length() ) );

				prefs.putInteger( "resolutionX", rX );
				prefs.putInteger( "resolutionY", rY );

				prefs.putBoolean( "fullscreen", windowMode.getSelected().equals( "Fullscreen" ) );
				prefs.putBoolean( "borderless", windowMode.getSelected().equals( "Borderless Window" ) );

				if ( fps.getSelected().equals( "VSync" ) )
				{
					prefs.putBoolean( "vSync", true );
					prefs.putInteger( "fps", 0 );
				}
				else
				{
					prefs.putBoolean( "vSync", false );
					prefs.putInteger( "fps", Integer.parseInt( fps.getSelected() ) );
				}

				if ( animspeed.getSelected().equals( "None" ) )
				{
					prefs.putFloat( "animspeed", 0 );
				}
				else
				{
					String s = animspeed.getSelected();
					float val = Float.parseFloat( s.substring( 0, s.length() - 1 ) );

					prefs.putFloat( "animspeed", val );
				}

				prefs.putInteger( "msaa", msaa.getSelected() );

				prefs.flush();

				Global.ApplicationChanger.updateApplication( prefs );

				return false;
			}
		} );
		TextButton restore = new TextButton( "Restore", skin );
		restore.addListener( new InputListener()
		{
			@Override
			public boolean touchDown( InputEvent event, float x, float y, int pointer, int button )
			{
				createVideo();
				return false;
			}
		} );
		TextButton defaults = new TextButton( "Defaults", skin );
		defaults.addListener( new InputListener()
		{
			@Override
			public boolean touchDown( InputEvent event, float x, float y, int pointer, int button )
			{

				prefs.putInteger( "resolutionX", 800 );
				prefs.putInteger( "resolutionY", 600 );
				prefs.putBoolean( "fullscreen", false );
				prefs.putBoolean( "borderless", false );
				prefs.putBoolean( "vSync", true );
				prefs.putInteger( "fps", 0 );
				prefs.putInteger( "msaa", 16 );
				prefs.putFloat( "animspeed", 1 );

				prefs.flush();

				Global.ApplicationChanger.updateApplication( prefs );

				createVideo();

				return false;
			}
		} );

		TextButton nativeRes = new TextButton( "Use Native Resolution", skin );
		nativeRes.addListener( new InputListener()
		{
			@Override
			public boolean touchDown( InputEvent event, float x, float y, int pointer, int button )
			{
				prefs.putBoolean( "fullscreen", windowMode.getSelected().equals( "Fullscreen" ) );
				prefs.putBoolean( "borderless", windowMode.getSelected().equals( "Borderless Window" ) );

				if ( fps.getSelected().equals( "VSync" ) )
				{
					prefs.putBoolean( "vSync", true );
					prefs.putInteger( "fps", 0 );
				}
				else
				{
					prefs.putBoolean( "vSync", false );
					prefs.putInteger( "fps", Integer.parseInt( fps.getSelected() ) );
				}

				if ( animspeed.getSelected().equals( "None" ) )
				{
					prefs.putFloat( "animspeed", 0 );
				}
				else
				{
					String s = animspeed.getSelected();
					float val = Float.parseFloat( s.substring( 0, s.length() - 1 ) );

					prefs.putFloat( "animspeed", val );
				}

				prefs.putInteger( "msaa", msaa.getSelected() );

				Global.ApplicationChanger.setToNativeResolution( prefs );
				createVideo();
				return false;
			}
		} );

		options.add( resolutionLabel );
		options.add( resolutions );
		options.add( nativeRes );
		options.row();
		options.add( windowLabel );
		options.add( windowMode );
		options.row();
		options.add( fpsLabel );
		options.add( fps );
		options.row();
		options.add( animLabel );
		options.add( animspeed );
		options.row();
		options.add( msaaLabel );
		options.add( msaa );
		options.row();
		options.add( apply );
		options.add( restore );
		options.add( defaults );
	}

	@Override
	public void show()
	{
		if ( !created )
		{
			create();
			created = true;
		}

		createVideo();

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
	}

	@Override
	public void render( float delta )
	{
		stage.act();

		Gdx.gl.glClearColor( 0, 0, 0, 1 );
		Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT );

		batch.begin();

		batch.draw( background, 0, 0, Global.Resolution[0], Global.Resolution[1] );

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

	public ScreenEnum screen;

	boolean created;

	Table table;
	Table buttons;
	Table options;

	Stage stage;
	Skin skin;

	SpriteBatch batch;

	Texture background;

	public InputMultiplexer inputMultiplexer;

	@Override
	public boolean keyDown( int keycode )
	{
		if ( keycode == Keys.ESCAPE )
		{
			Global.Game.switchScreen( screen );
		}

		return false;
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
		return false;
	}

	@Override
	public boolean scrolled( int amount )
	{
		return false;
	}
}
