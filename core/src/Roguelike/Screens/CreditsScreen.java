package Roguelike.Screens;

import Roguelike.Global;
import Roguelike.RoguelikeGame;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Created by Philip on 26-Jan-16.
 */
public class CreditsScreen implements Screen
{
	public CreditsScreen Instance;

	public static final String[][] creditData =
			{
					{"LibGDX - Engine", "https://libgdx.badlogicgames.com/"},
					{"Oryx - Sprites", "http://oryxdesignlab.com/"},
					{"Kevin Macleod - Music", "https://incompetech.com/"},
					{"FreeSFX - Sounds", "http://www.freesfx.co.uk/"}
	};

	public CreditsScreen()
	{
		Instance = this;
	}

	public void create()
	{
		skin = Global.loadSkin();

		stage = new Stage( new ScreenViewport() );
		batch = new SpriteBatch();

		Table table = new Table();
		stage.addActor( table );
		table.setFillParent( true );

		Table creditsTable = new Table(  );
		creditsTable.defaults().pad( 10 );

		for (final String[] pair : creditData)
		{
			creditsTable.add( new Label(pair[0], skin, "title") ).expandX().left();

			Label linkLabel = new Label( pair[1], skin );
			linkLabel.addListener(
					new ClickListener()
					{
						@Override
						public void clicked( InputEvent event, float x, float y )
						{
							Gdx.net.openURI( pair[1] );
						}
					});

			creditsTable.add( linkLabel ).expandX().left();
			creditsTable.row();
		}

		ScrollPane scrollPane = new ScrollPane( creditsTable, skin );

		table.add( scrollPane ).pad( 30 ).expand().fill();
		table.row();

		TextButton button = new TextButton( "Back", skin, "big" );
		button.addListener( new ClickListener()
		{
			public void clicked( InputEvent event, float x, float y )
			{
				RoguelikeGame.Instance.switchScreen( RoguelikeGame.ScreenEnum.MAINMENU );
			}
		} );
		table.add( button ).expandX().left();
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
}
