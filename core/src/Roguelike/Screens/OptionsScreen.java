package Roguelike.Screens;

import java.text.DecimalFormat;

import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.RoguelikeGame;
import Roguelike.RoguelikeGame.ScreenEnum;

import Roguelike.UI.ButtonKeyboardHelper;
import Roguelike.UI.Seperator;
import Roguelike.UI.TabPanel;
import Roguelike.Util.Controls;
import Roguelike.Util.FastEnumMap;
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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
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

		inputMultiplexer = new InputMultiplexer();

		InputProcessor inputProcessorOne = this;
		InputProcessor inputProcessorTwo = stage;

		inputMultiplexer.addProcessor( inputProcessorTwo );
		inputMultiplexer.addProcessor( inputProcessorOne );
	}

	public void createOptions()
	{
		table.clear();

		keyboardHelper = new ButtonKeyboardHelper(  );
		final Preferences prefs = Global.ApplicationChanger.prefs;

		TextButton apply = new TextButton( "Apply", skin );
		apply.addListener( new ClickListener()
		{
			public void clicked( InputEvent event, float x, float y )
			{

				saveControls.save( prefs );
				saveAudio.save( prefs );
				saveVideo.save( prefs );

				prefs.flush();

				Global.ApplicationChanger.updateApplication( prefs );

				RoguelikeGame.Instance.switchScreen( screen );

			}
		} );

		TextButton backButton = new TextButton( "Back", skin );
		backButton.addListener( new ClickListener()
		{
			public void clicked( InputEvent event, float x, float y )
			{
				RoguelikeGame.Instance.switchScreen( screen );
			}
		});

		tabPanel = new TabPanel( skin );

		final ButtonKeyboardHelper controlsHelper = new ButtonKeyboardHelper(  );
		final ButtonKeyboardHelper audioHelper = new ButtonKeyboardHelper(  );
		final ButtonKeyboardHelper videoHelper = new ButtonKeyboardHelper(  );

		createControls(controlsHelper);
		createAudioOptions(audioHelper);
		createVideoOptions(videoHelper);

		table.add( tabPanel ).colspan( 2 ).expand().fill().pad( 25 );
		table.row();
		table.add( backButton ).expandX().left().pad( 0, 25, 25, 25 );
		table.add( apply ).expandX().right().pad( 0, 25, 25, 25 );
		table.row();

		for (Actor a : tabPanel.tabTitleTable.getChildren())
		{
			controlsHelper.add( a, 0, 0 );
			audioHelper.add( a, 0, 0 );
			videoHelper.add( a, 0, 0 );
		}

		controlsHelper.trySetCurrent( 0, 0, 0 );
		audioHelper.trySetCurrent( 0, 0, 0 );
		videoHelper.trySetCurrent( 0, 0, 0 );

		controlsHelper.add( backButton, apply );
		controlsHelper.cancel = backButton;

		audioHelper.add( backButton, apply );
		audioHelper.cancel = backButton;

		videoHelper.add( backButton, apply );
		videoHelper.cancel = backButton;

		tabPanel.addListener( new ChangeListener() {
			@Override
			public void changed( ChangeEvent event, Actor actor )
			{
				ButtonKeyboardHelper oldHelper = keyboardHelper;

				if (tabPanel.getSelectedIndex() == 0)
				{
					keyboardHelper = controlsHelper;
				}
				else if (tabPanel.getSelectedIndex() == 1)
				{
					keyboardHelper = audioHelper;
				}
				else if (tabPanel.getSelectedIndex() == 2)
				{
					keyboardHelper = videoHelper;
				}

				keyboardHelper.trySetCurrent( oldHelper.currentx, oldHelper.currenty, oldHelper.currentz );
			}
		} );

		keyboardHelper = controlsHelper;
	}

	public void createControls(ButtonKeyboardHelper keyboardHelper)
	{
		Table table = new Table(  );
		table.defaults().pad( 5 );
		Skin skin = Global.loadSkin();

		Label movementLabel = new Label("Movement Type:", skin);
		final SelectBox<String> movementtype = new SelectBox<String>( skin );
		movementtype.setItems( "Direction", "Pathfind" );
		if (Global.MovementTypePathfind) { movementtype.setSelectedIndex( 1 ); }
		else { movementtype.setSelectedIndex( 0 ); }

		table.add( movementLabel ).expandX().left();
		table.add( movementtype ).expandX().fillX();
		table.row();

		table.add( new Seperator( skin ) ).colspan( 2 ).expandX().fillX();
		table.row();

		keyboardHelper.add( movementtype, 0, 1 );

		final FastEnumMap<Controls.Keys, TextButton> keyMap = new FastEnumMap<Controls.Keys, TextButton>( Controls.Keys.class );

		if (!Global.ANDROID)
		{

			Label defaultLabel = new Label( "Default Keybindings", skin );
			table.add( defaultLabel ).colspan( 2 ).expandX().left();
			table.row();

			Table defaultsTable = new Table();
			defaultsTable.defaults().pad( 5 );
			table.add( defaultsTable ).expandX().colspan( 2 ).left();
			table.row();

			TextButton defaultArrow = new TextButton( "Arrow", skin );
			defaultArrow.addListener( new ClickListener()
			{
				public void clicked( InputEvent event, float x, float y )
				{
					Global.Controls.defaultArrow();

					for ( Controls.Keys key : Controls.Keys.values() )
					{
						keyMap.get( key ).setText( Keys.toString( Global.Controls.getKeyCode( key ) ) );
					}
				}
			} );
			defaultsTable.add( defaultArrow );

			TextButton defaultWASD = new TextButton( "WASD", skin );
			defaultWASD.addListener( new ClickListener()
			{
				public void clicked( InputEvent event, float x, float y )
				{
					Global.Controls.defaultWASD();

					for ( Controls.Keys key : Controls.Keys.values() )
					{
						keyMap.get( key ).setText( Keys.toString( Global.Controls.getKeyCode( key ) ) );
					}
				}
			} );
			defaultsTable.add( defaultWASD );

			TextButton defaultNumpad = new TextButton( "Numpad", skin );
			defaultNumpad.addListener( new ClickListener()
			{
				public void clicked( InputEvent event, float x, float y )
				{
					Global.Controls.defaultNumPad();

					for ( Controls.Keys key : Controls.Keys.values() )
					{
						keyMap.get( key ).setText( Keys.toString( Global.Controls.getKeyCode( key ) ) );
					}
				}
			} );
			defaultsTable.add( defaultNumpad );

			keyboardHelper.add( defaultArrow, defaultWASD, defaultNumpad );

			table.add( new Seperator( skin ) ).colspan( 2 ).expandX().fillX();
			table.row();

			for ( final Controls.Keys key : Controls.Keys.values() )
			{
				Label label = new Label( Global.capitalizeString( key.toString() ), skin );
				final TextButton button = new TextButton( Keys.toString( Global.Controls.getKeyCode( key ) ), skin, "keybinding" );
				button.addListener( new ClickListener()
				{
					public void clicked( InputEvent event, float x, float y )
					{
						button.setText( "Press key to map" );
						mappingTo = button;
						mapKey = key;
					}
				} );

				keyMap.put( key, button );

				table.add( label ).width( Value.percentWidth( 0.5f, table ) ).left();
				table.add( button ).width( Value.percentWidth( 0.5f, table ) );
				table.row();

				keyboardHelper.add( button );
			}

		}

		saveControls = new SaveAction() {
			@Override
			public void save( Preferences prefs )
			{
				prefs.putBoolean( "pathfindMovement", movementtype.getSelectedIndex() == 1 );

				if (!Global.ANDROID)
				{
					for ( Controls.Keys key : Controls.Keys.values() )
					{
						String keyName = keyMap.get( key ).getText().toString();
						int keycode = Keys.valueOf( keyName );

						prefs.putInteger( key.toString(), keycode );
					}
				}
			}
		};

		ScrollPane scrollPane = new ScrollPane( table, skin );
		scrollPane.setScrollingDisabled( true, false );
		scrollPane.setVariableSizeKnobs( true );
		scrollPane.setFadeScrollBars( false );
		scrollPane.setScrollbarsOnTop( false );
		scrollPane.setForceScroll( false, true );
		scrollPane.setFlickScroll( false );

		keyboardHelper.scrollPane = scrollPane;

		tabPanel.addTab( "Controls", scrollPane );
	}

	public void createVideoOptions(ButtonKeyboardHelper keyboardHelper)
	{
		final Preferences prefs = Global.ApplicationChanger.prefs;
		Skin skin = Global.loadSkin();

		Table table = new Table();
		table.defaults().pad( 5 );

		Label resolutionLabel = new Label( "Resolution:", skin );
		final SelectBox<String> resolutions = new SelectBox<String>( skin );
		resolutions.setItems( Global.ApplicationChanger.getSupportedDisplayModes() );
		resolutions.setSelected( prefs.getInteger( "resolutionX" ) + "x" + prefs.getInteger( "resolutionY" ) );

		keyboardHelper.add( resolutions, 0, 1 );

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

		keyboardHelper.add( windowMode );

		Label fpsLabel = new Label( "Frames per second:", skin );
		final SelectBox<String> fps = new SelectBox<String>( skin );
		fps.setItems( new String[] { "30", "60", "120" } );
		fps.setSelected( "" + prefs.getInteger( "fps" ) );

		keyboardHelper.add( fps );

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

		keyboardHelper.add( animspeed );

		Label msaaLabel = new Label( "MSAA Samples:", skin );
		final SelectBox<Integer> msaa = new SelectBox<Integer>( skin );
		msaa.setItems( new Integer[] { 0, 2, 4, 8, 16, 32 } );
		msaa.setSelected( prefs.getInteger( "msaa" ) );

		keyboardHelper.add( msaa );

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

		saveVideo = new SaveAction( ) {
			@Override
			public void save( Preferences prefs )
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
			}
		};

		ScrollPane scrollPane = new ScrollPane( table, skin );
		scrollPane.setScrollingDisabled( true, false );
		scrollPane.setVariableSizeKnobs( true );
		scrollPane.setFadeScrollBars( false );
		scrollPane.setScrollbarsOnTop( false );
		scrollPane.setForceScroll( false, true );
		scrollPane.setFlickScroll( false );

		keyboardHelper.scrollPane = scrollPane;

		tabPanel.addTab( "Video", scrollPane );
	}

	public void createAudioOptions(ButtonKeyboardHelper keyboardHelper)
	{
		final Preferences prefs = Global.ApplicationChanger.prefs;
		Skin skin = Global.loadSkin();

		Table table = new Table();
		table.defaults().pad( 5 );

		Label musicLabel = new Label("Music Volume", skin);
		final Slider musicSlider = new Slider( 0, 100, 1, false, skin );
		musicSlider.setValue( Global.MusicVolume * 100 );

		keyboardHelper.add( musicSlider, 0, 1 );

		Label ambientLabel = new Label("Ambient Volume", skin);
		final Slider ambientSlider = new Slider( 0, 100, 1, false, skin );
		ambientSlider.setValue( Global.AmbientVolume * 100 );

		keyboardHelper.add( ambientSlider );

		Label effectLabel = new Label("Effect Volume", skin);
		final Slider effectSlider = new Slider( 0, 100, 1, false, skin );
		effectSlider.setValue( Global.EffectVolume * 100 );

		keyboardHelper.add( effectSlider );

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

		saveAudio = new SaveAction() {
			@Override
			public void save( Preferences prefs )
			{
				prefs.putFloat( "musicVolume", musicSlider.getValue() / 100.0f );
				prefs.putFloat( "ambientVolume", ambientSlider.getValue() / 100.0f );
				prefs.putFloat( "effectVolume", effectSlider.getValue() / 100.0f );
			}
		};

		ScrollPane scrollPane = new ScrollPane( table, skin );
		scrollPane.setScrollingDisabled( true, false );
		scrollPane.setVariableSizeKnobs( true );
		scrollPane.setFadeScrollBars( false );
		scrollPane.setScrollbarsOnTop( false );
		scrollPane.setForceScroll( false, true );
		scrollPane.setFlickScroll( false );

		keyboardHelper.scrollPane = scrollPane;

		tabPanel.addTab( "Audio", scrollPane );
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
		keyboardHelper.update( delta );
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

	SaveAction saveAudio;
	SaveAction saveVideo;
	SaveAction saveControls;

	TabPanel tabPanel;
	Table table;

	Stage stage;
	Skin skin;

	SpriteBatch batch;

	Controls.Keys mapKey;
	TextButton mappingTo;

	public InputMultiplexer inputMultiplexer;
	public ButtonKeyboardHelper keyboardHelper;

	private abstract class SaveAction
	{
		public abstract void save( Preferences prefs );
	}

	@Override
	public boolean keyDown( int keycode )
	{
		if (mappingTo != null)
		{
			String name = Keys.toString( keycode );
			mappingTo.setText( name );

			Global.Controls.setKeyMap(mapKey, keycode);

			mappingTo = null;
			mapKey = null;
		}
		else
		{
			keyboardHelper.keyDown( keycode );
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
		keyboardHelper.clear();
		return false;
	}

	@Override
	public boolean scrolled( int amount )
	{
		return false;
	}
}
