package Roguelike.Screens;

import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.Ability.AbilityPool.AbilityLine;
import Roguelike.Items.Item;
import Roguelike.Sprite.Sprite;
import Roguelike.UI.ClassList;
import Roguelike.UI.ClassList.ClassDesc;
import Roguelike.UI.HoverTextButton;
import Roguelike.UI.HoverTextButton.HorizontalAlignment;
import Roguelike.UI.SpriteWidget;

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

		Table nameTable = new Table();

		nameTable.add( new Label( "Name:", skin ) );
		nameTable.add( textArea );

		rightTable.add( nameTable ).padTop( 50 ).padBottom( 20 ).expandX().left();
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

		selectedClass = new Table();
		rightTable.add( selectedClass ).expand().fill();
		rightTable.row();

		rightTable.add( ngbutton ).expandX().fillX().padTop( 20 ).padBottom( 50 ).center();
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
		else
		{
			classList.reparse();
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

	// ----------------------------------------------------------------------
	private float roundTo( float val, float multiple )
	{
		return (float) ( multiple * Math.floor( val / multiple ) );
	}

	private void fillClassDesc()
	{
		selectedClass.clearChildren();

		if ( lastSelected == null ) { return; }
		selectedClass.add( new Label( lastSelected.name, skin ) ).expandX().left().top();
		selectedClass.row();

		Label descLabel = new Label( lastSelected.description, skin );
		descLabel.setWrap( true );
		selectedClass.add( descLabel ).expandX().left().top();
		selectedClass.row();

		Table lineTable = new Table();
		lineTable.add( new Label( "Ability Lines: ", skin ) );

		String[] lines = lastSelected.lines.split( "," );
		for ( String line : lines )
		{
			SpriteWidget sprite = new SpriteWidget( AbilityLine.getSprite( line + "/" + line ), 32, 32 );
			lineTable.add( sprite ).pad( 10 );
		}

		selectedClass.add( lineTable ).expandX().left().top();
		selectedClass.row();

		selectedClass.add( new Label( "Starting Inventory:", skin ) ).expandX().left().top();
		selectedClass.row();

		for ( Item item : lastSelected.entity.getInventory().m_items )
		{
			selectedClass.add( new Label( item.name, skin ) ).expandX().left().padLeft( 30 ).top();
			selectedClass.row();
		}
	}

	@Override
	public void render( float delta )
	{
		if ( classList.chosen != lastSelected )
		{
			lastSelected = classList.chosen;
			fillClassDesc();
		}

		stage.act();

		Gdx.gl.glClearColor( 0, 0, 0, 1 );
		Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT );

		batch.begin();

		// batch.draw(background, 0, 0, Global.Resolution[0],
		// Global.Resolution[1]);

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

		classList.setWidth( classList.getPrefWidth() );
		classList.setHeight( roundTo( h - 40, 64 ) );
		fillClassDesc();
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

	Table selectedClass;
	ClassDesc lastSelected;

	Stage stage;
	Skin skin;

	SpriteBatch batch;

	TextArea textArea;
	BitmapFont normalFont;
	BitmapFont highlightFont;

	private Sprite tileBackground;
	private Sprite tileBorder;

}
