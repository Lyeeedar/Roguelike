package Roguelike.Screens;

import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.RoguelikeGame;
import Roguelike.RoguelikeGame.ScreenEnum;
import Roguelike.UI.HoverTextButton;
import Roguelike.UI.HoverTextButton.HorizontalAlignment;

import com.badlogic.gdx.Gdx;
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
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class GameOverScreen implements Screen
{
	public GameOverScreen()
	{
	}

	private void create()
	{
		BitmapFont font = AssetManager.loadFont( "Sprites/GUI/stan0755.ttf", 20 );
		titleFont = AssetManager.loadFont( "Sprites/GUI/stan0755.ttf", 30 );
		normalFont = AssetManager.loadFont( "Sprites/GUI/stan0755.ttf", 20 );
		highlightFont = AssetManager.loadFont( "Sprites/GUI/stan0755.ttf", 25 );

		skin = new Skin();
		skin.addRegions( new TextureAtlas( Gdx.files.internal( "GUI/uiskin.atlas" ) ) );
		skin.add( "default-font", font, BitmapFont.class );
		skin.load( Gdx.files.internal( "GUI/uiskin.json" ) );

		stage = new Stage( new ScreenViewport() );
		batch = new SpriteBatch();

		background = AssetManager.loadTexture( "Sprites/GUI/Title.png" );

		createUI();
	}

	private void createUI()
	{
		Table table = new Table();
		// table.debug();

		Label title = new Label( "Game Over", skin );

		LabelStyle style = new LabelStyle();
		style.font = titleFont;
		title.setStyle( style );
		table.add( title ).expandY().top().padTop( 100 );
		table.row();

		detailLabel = new Label( "", skin );
		table.add( detailLabel );
		table.row();

		Table buttonTable = new Table();

		HoverTextButton mmbutton = new HoverTextButton( "Main Menu", normalFont, highlightFont );
		mmbutton.halign = HorizontalAlignment.RIGHT;
		mmbutton.addListener( new InputListener()
		{
			@Override
			public boolean touchDown( InputEvent event, float x, float y, int pointer, int button )
			{
				RoguelikeGame.Instance.switchScreen( ScreenEnum.MAINMENU );

				return true;
			}
		} );

		buttonTable.add( mmbutton ).expandX().fillX();
		buttonTable.row();

		table.add( buttonTable ).width( Value.percentWidth( 0.3f, table ) ).expandX().expandY().top();

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

		String infoText = "You died :(\n";
		infoText += "You got to depth " + Global.CurrentLevel.depth + ".\n";
		infoText += "You survived for " + Global.AUT + " Arbitrary Units of Time.\n";
		detailLabel.setText( infoText );

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

		Gdx.gl.glClearColor( 0, 0, 0, 1 );
		Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT );

		batch.begin();

		// batch.draw(background, 0, 0, Global.Resolution[0],
		// Global.Resolution[1]);

		batch.end();

		stage.draw();
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

	BitmapFont titleFont;
	BitmapFont normalFont;
	BitmapFont highlightFont;

	Texture background;

	Label detailLabel;
}
