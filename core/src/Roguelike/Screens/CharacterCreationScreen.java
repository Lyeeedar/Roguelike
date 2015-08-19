package Roguelike.Screens;

import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.Sprite.Sprite;
import Roguelike.UI.ClassList;
import Roguelike.UI.HoverTextButton;
import Roguelike.UI.HoverTextButton.HorizontalAlignment;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class CharacterCreationScreen implements Screen
{
	private void create()
	{
		BitmapFont font = AssetManager.loadFont( "Sprites/GUI/stan0755.ttf", 14 );

		normalFont = AssetManager.loadFont( "Sprites/GUI/stan0755.ttf", 30 );
		highlightFont = AssetManager.loadFont( "Sprites/GUI/stan0755.ttf", 35 );

		skin = new Skin();
		skin.addRegions( new TextureAtlas( Gdx.files.internal( "GUI/uiskin.atlas" ) ) );
		skin.add( "default-font", font, BitmapFont.class );
		skin.load( Gdx.files.internal( "GUI/uiskin.json" ) );

		stage = new Stage( new ScreenViewport() );
		batch = new SpriteBatch();

		this.tileBackground = AssetManager.loadSprite( "GUI/TileBackground" );
		this.tileBorder = AssetManager.loadSprite( "GUI/TileBorder" );

		createUI();
	}

	private void createUI()
	{
		classList = new ClassList( skin, stage, tileBackground, tileBorder );
		textArea = new TextArea( "Jeff", skin );

		Table table = new Table();
		// table.debug();

		table.add( classList ).left().expandY().fillY().pad( 20 );

		Table rightTable = new Table();
		table.add( rightTable ).expand().fill();

		rightTable.add( new Label( "Name:", skin ) );
		rightTable.add( textArea );
		rightTable.row();

		HoverTextButton ngbutton = new HoverTextButton( "New Game", normalFont, highlightFont );
		ngbutton.halign = HorizontalAlignment.RIGHT;
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
				Global.PlayerName = textArea.getText();
				Global.PlayerTitle = classList.chosen.name;
				Global.newGame( classList.chosen.entity, classList.chosen.lines );
			}
		} );

		rightTable.add( ngbutton ).expandX().fillX();
		rightTable.row();

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

		classList.setWidth( classList.getPrefWidth() );
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

	ClassList classList;

	Stage stage;
	Skin skin;

	SpriteBatch batch;

	TextArea textArea;
	BitmapFont normalFont;
	BitmapFont highlightFont;

	private Sprite tileBackground;
	private Sprite tileBorder;

}
