package Roguelike.Screens;

import Roguelike.Ability.AbilityTree;
import Roguelike.AssetManager;
import Roguelike.Entity.GameEntity;
import Roguelike.Global;
import Roguelike.Items.Item;
import Roguelike.RoguelikeGame;
import Roguelike.Sprite.Sprite;
import Roguelike.UI.ClassList;
import Roguelike.UI.ClassList.ClassDesc;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class CharacterCreationScreen implements Screen
{
	private void create()
	{
		skin = Global.loadSkin();

		stage = new Stage( new ScreenViewport() );
		batch = new SpriteBatch();

		this.tileBackground = AssetManager.loadSprite( "GUI/TileBackground" );
		// tileBackground.colour = Color.GREEN;
		// this.tileBorder = AssetManager.loadSprite( "GUI/TileBorder" );

		background = AssetManager.loadTexture( "Sprites/GUI/Background.png" );

		createUI();
	}

	private void createUI()
	{
		NinePatch background = new NinePatch( AssetManager.loadTextureRegion( "Sprites/GUI/TilePanel.png" ), 12, 12, 12, 12 );

		classList = new ClassList( skin, stage, tileBackground, tileBorder );


		// table.debug();

		Table optionsTable = new Table();
		optionsTable.defaults().space( 20 );
		optionsTable.background( new NinePatchDrawable( background ) );

		male = new CheckBox( "Male", skin );
		male.addListener( new ChangeListener() {
			@Override
			public void changed( ChangeEvent event, Actor actor )
			{
				classList.male = male.isChecked();
			}
		} );
		female = new CheckBox( "Female", skin );

		ButtonGroup<Button> group = new ButtonGroup<Button>();
		group.add( male, female );
		male.setChecked( true );

		Table genderTable = new Table();
		genderTable.add( new Label( "Gender", skin ) ).colspan( 2 );
		genderTable.row();

		genderTable.add( male );
		genderTable.add( female );

		optionsTable.add( genderTable );

		final Table classTable = new Table();
		classTable.defaults().uniformY().space( 10 );

		// classTable.debug();
		selectedClass = new Table();
		selectedClass.background( new NinePatchDrawable( background ) );

		classTable.add( classList ).left().fillY().expandY();
		classTable.add( selectedClass ).fill().expand();

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
				GameEntity entity = male.isChecked() ? classList.chosen.male : classList.chosen.female;

				Global.newGame( entity );
				Global.RunFlags.put( "class", classList.chosen.name );
			}
		} );

		TextButton mainMenubutton = new TextButton( "Main Menu", skin, "big" );
		mainMenubutton.addListener( new InputListener()
		{
			@Override
			public boolean touchDown( InputEvent event, float x, float y, int pointer, int button )
			{
				return true;
			}

			@Override
			public void touchUp( InputEvent event, float x, float y, int pointer, int button )
			{
				RoguelikeGame.Instance.switchScreen( RoguelikeGame.ScreenEnum.MAINMENU );
			}
		} );

		Table table = new Table();

		table.add( optionsTable ).colspan( 2 ).pad( 10 ).expandX().fillX();
		table.row();

		table.add( classTable ).colspan( 2 ).pad( 10 ).expand().fill();
		table.row();

		table.add( mainMenubutton ).expandX().width( 200 ).pad( 10 ).left();
		table.add( ngbutton ).expandX().width( 200 ).pad( 10 ).right();
		table.row();

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
		selectedClass.add( new Label( lastSelected.name, skin, "title" ) ).expandX().left().top();
		selectedClass.row();

		Label descLabel = new Label( lastSelected.description, skin );
		descLabel.setWrap( true );
		selectedClass.add( descLabel ).expandX().left().top();
		selectedClass.row();

		selectedClass.add( new Label( "Starting Abilities:", skin ) ).expandX().left().top();
		selectedClass.row();

		GameEntity entity = male.isChecked() ? lastSelected.male : lastSelected.female;

		for (AbilityTree tree : entity.slottedAbilities)
		{
			if (tree != null)
			{
				selectedClass.add( new Label( tree.current.current.getName(), skin ) ).expandX().left().padLeft( 30 ).top();
				selectedClass.row();
			}
		}

		selectedClass.add( new Label( "Starting Inventory:", skin ) ).expandX().left().top();
		selectedClass.row();

		for ( Item item : entity.getInventory().m_items )
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

	CheckBox male;
	CheckBox female;

	Texture background;

	private Sprite tileBackground;
	private Sprite tileBorder;

}
