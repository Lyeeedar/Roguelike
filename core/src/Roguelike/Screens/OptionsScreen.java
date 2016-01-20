package Roguelike.Screens;

import java.text.DecimalFormat;

import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.RoguelikeGame;
import Roguelike.RoguelikeGame.ScreenEnum;

import Roguelike.UI.Seperator;
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
import com.badlogic.gdx.scenes.scene2d.ui.*;
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
		skin = Global.loadSkin();

		stage = new Stage( new ScreenViewport() );
		batch = new SpriteBatch();

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

	public void createOptions()
	{
		final Preferences prefs = Global.ApplicationChanger.prefs;

		options.clear();

		Label gameTitle = new Label("Game", skin, "title");
		Label movementLabel = new Label("Movement Type:", skin);
		final SelectBox<String> movementtype = new SelectBox<String>( skin );
		movementtype.setItems( "Direction", "Pathfind" );
		if (Global.MovementTypePathfind) { movementtype.setSelectedIndex( 1 ); }
		else { movementtype.setSelectedIndex( 0 ); }

		Label audioTitle = new Label("Audio", skin, "title");

		Label musicLabel = new Label("Music Volume", skin);
		final Slider musicSlider = new Slider( 0, 100, 1, false, skin );
		musicSlider.setValue( Global.MusicVolume * 100 );

		Label ambientLabel = new Label("Ambient Volume", skin);
		final Slider ambientSlider = new Slider( 0, 100, 1, false, skin );
		ambientSlider.setValue( Global.AmbientVolume * 100 );

		Label effectLabel = new Label("Effect Volume", skin);
		final Slider effectSlider = new Slider( 0, 100, 1, false, skin );
		effectSlider.setValue( Global.EffectVolume * 100 );

		Label videoTitle = new Label( "Video", skin, "title" );

		Label resolutionLabel = new Label( "Resolution:", skin );
		final SelectBox<String> resolutions = new SelectBox<String>( skin );
		resolutions.setItems( Global.ApplicationChanger.getSupportedDisplayModes() );
		resolutions.setSelected( prefs.getInteger( "resolutionX" ) + "x" + prefs.getInteger( "resolutionY" ) );

		Label windowLabel = new Label( "Window Mode:", skin );
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

		Label fpsLabel = new Label( "Frames per second:", skin );
		final SelectBox<String> fps = new SelectBox<String>( skin );
		fps.setItems( new String[] { "30", "60", "120" } );
//		if ( prefs.getBoolean( "vSync" ) )
//		{
//			fps.setSelected( "VSync" );
//		}
//		else
		{
			fps.setSelected( "" + prefs.getInteger( "fps" ) );
		}

		Label animLabel = new Label( "Animation Speed:", skin );
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

		Label msaaLabel = new Label( "MSAA Samples:", skin );
		final SelectBox<Integer> msaa = new SelectBox<Integer>( skin );
		msaa.setItems( new Integer[] { 0, 2, 4, 8, 16, 32 } );
		msaa.setSelected( prefs.getInteger( "msaa" ) );

		TextButton apply = new TextButton( "Apply", skin );
		apply.addListener( new InputListener()
		{
			@Override
			public boolean touchDown( InputEvent event, float x, float y, int pointer, int button )
			{
				return true;
			}

			@Override
			public void touchUp( InputEvent event, float x, float y, int pointer, int button )
			{
				prefs.putBoolean( "pathfindMovement", movementtype.getSelectedIndex() == 1 );

				prefs.putFloat( "musicVolume", musicSlider.getValue() / 100.0f );
				prefs.putFloat( "ambientVolume", ambientSlider.getValue() / 100.0f );
				prefs.putFloat( "effectVolume", effectSlider.getValue() / 100.0f );

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

			}
		} );

		TextButton defaults = new TextButton( "Defaults", skin );
		defaults.addListener( new InputListener()
		{
			@Override
			public boolean touchDown( InputEvent event, float x, float y, int pointer, int button )
			{
				return true;
			}

			@Override
			public void touchUp( InputEvent event, float x, float y, int pointer, int button )
			{
				Global.ApplicationChanger.setDefaultPrefs( prefs );

				prefs.flush();

				Global.ApplicationChanger.updateApplication( prefs );

				createOptions();
			}
		} );

		TextButton backButton = new TextButton( "Back", skin );
		backButton.addListener( new InputListener()
		{
			@Override
			public boolean touchDown( InputEvent event, float x, float y, int pointer, int button )
			{
				return true;
			}

			@Override
			public void touchUp( InputEvent event, float x, float y, int pointer, int button )
			{
				RoguelikeGame.Instance.switchScreen( screen );
			}
		});

		Table table = new Table();

		table.add( gameTitle ).expandX().left().padTop( 20 );
		table.row();
		table.add( new Seperator( skin ) ).colspan( 2 ).expandX().fillX();
		table.row();
		table.add( movementLabel ).expandX().left();
		table.add( movementtype ).expandX().fillX();
		table.row();

		table.add( audioTitle ).expandX().left().padTop( 20 );
		table.row();
		table.add( new Seperator( skin ) ).colspan( 2 ).expandX().fillX();
		table.row();
		table.add( musicLabel ).expandX().left();
		table.row();
		table.add( musicSlider ).colspan( 2 ).expandX().fillX();
		table.row();
		table.add( ambientLabel ).expandX().left();
		table.row();
		table.add( ambientSlider ).colspan( 2 ).expandX().fillX();
		table.row();
		table.add( effectLabel ).expandX().left();
		table.row();
		table.add( effectSlider ).colspan( 2 ).expandX().fillX();
		table.row();

		table.add( videoTitle ).expandX().left().padTop( 20 );
		table.row();
		table.add( new Seperator( skin ) ).colspan( 2 ).expandX().fillX();
		table.row();
		table.add( resolutionLabel ).expandX().left();
		table.add( resolutions ).expandX().fillX();
		table.row();

		if (!Global.ANDROID)
		{
			table.add( windowLabel ).expandX().left();
			table.add( windowMode ).expandX().fillX();
			table.row();
		}


		table.add( fpsLabel ).expandX().left();
		table.add( fps ).expandX().fillX();
		table.row();
		table.add( animLabel ).expandX().left();
		table.add( animspeed ).expandX().fillX();
		table.row();

		if (!Global.ANDROID)
		{
			table.add( msaaLabel ).expandX().left();
			table.add( msaa ).expandX().fillX();
			table.row();
		}

		ScrollPane scrollPane = new ScrollPane( table, skin );
		scrollPane.setScrollingDisabled( true, false );
		scrollPane.setVariableSizeKnobs( true );
		scrollPane.setFadeScrollBars( false );
		scrollPane.setScrollbarsOnTop( false );
		scrollPane.setForceScroll( false, true );
		scrollPane.setFlickScroll( false );

		options.add( scrollPane ).colspan( 2 ).expand().fill();
		options.row();

		Table adTable = new Table(  );
		adTable.add( defaults );
		adTable.add( apply );

		options.add( backButton ).left().pad( 10 );
		options.add( adTable ).right().pad( 10 );

	}

	@Override
	public void show()
	{
		if ( !created )
		{
			create();
			created = true;
		}

		createOptions();

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

		Gdx.gl.glClearColor( 0.3f, 0.3f, 0.3f, 1 );
		Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT );

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
