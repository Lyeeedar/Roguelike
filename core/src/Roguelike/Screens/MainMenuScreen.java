package Roguelike.Screens;

import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.RoguelikeGame;
import Roguelike.RoguelikeGame.ScreenEnum;
import Roguelike.UI.HoverTextButton;
import Roguelike.UI.HoverTextButton.HorizontalAlignment;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class MainMenuScreen implements Screen
{	
	public MainMenuScreen()
	{
		create();
	}
	
	private void create()
	{
		BitmapFont font;
		{
			FreeTypeFontGenerator fgenerator = new FreeTypeFontGenerator(Gdx.files.internal("Sprites/GUI/stan0755.ttf"));
			FreeTypeFontParameter parameter = new FreeTypeFontParameter();
			parameter.size = 60;
			parameter.borderWidth = 1;
			parameter.kerning = true;
			parameter.borderColor = Color.BLACK;
			font = fgenerator.generateFont(parameter); // font size 12 pixels
			font.getData().markupEnabled = true;
			fgenerator.dispose(); // don't forget to dispose to avoid memory leaks!
		}
		
		{
			FreeTypeFontGenerator fgenerator = new FreeTypeFontGenerator(Gdx.files.internal("Sprites/GUI/stan0755.ttf"));
			FreeTypeFontParameter parameter = new FreeTypeFontParameter();
			parameter.size = 55;
			parameter.borderWidth = 1;
			parameter.kerning = true;
			parameter.borderColor = Color.BLACK;
			titleFont = fgenerator.generateFont(parameter); // font size 12 pixels
			titleFont.getData().markupEnabled = true;
			fgenerator.dispose(); // don't forget to dispose to avoid memory leaks!
		}
		
		{
			FreeTypeFontGenerator fgenerator = new FreeTypeFontGenerator(Gdx.files.internal("Sprites/GUI/stan0755.ttf"));
			FreeTypeFontParameter parameter = new FreeTypeFontParameter();
			parameter.size = 30;
			parameter.borderWidth = 1;
			parameter.kerning = true;
			parameter.borderColor = Color.BLACK;
			normalFont = fgenerator.generateFont(parameter); // font size 12 pixels
			normalFont.getData().markupEnabled = true;
			fgenerator.dispose(); // don't forget to dispose to avoid memory leaks!
		}
		
		{
			FreeTypeFontGenerator fgenerator = new FreeTypeFontGenerator(Gdx.files.internal("Sprites/GUI/stan0755.ttf"));
			FreeTypeFontParameter parameter = new FreeTypeFontParameter();
			parameter.size = 35;
			parameter.borderWidth = 1;
			parameter.kerning = true;
			parameter.borderColor = Color.BLACK;
			highlightFont = fgenerator.generateFont(parameter); // font size 12 pixels
			highlightFont.getData().markupEnabled = true;
			fgenerator.dispose(); // don't forget to dispose to avoid memory leaks!
		}
		
		skin = new Skin();
		skin.addRegions(new TextureAtlas(Gdx.files.internal("GUI/uiskin.atlas")));
		skin.add("default-font", font, BitmapFont.class);
		skin.load(Gdx.files.internal("GUI/uiskin.json"));

		stage = new Stage(new ScreenViewport());
		batch = new SpriteBatch();
		
		background = AssetManager.loadTexture("Sprites/GUI/Title.png");
		
		createUI();
	}
	
	private void createUI()
	{
		Table table = new Table();
		//table.debug();
		
		Label title = new Label("Chronicles of Aether", skin);
		title.getStyle().font = titleFont;
		table.add(title).expandY().top().padTop(100);
		table.row();
		
		Table buttonTable = new Table();
		
		HoverTextButton ngbutton = new HoverTextButton("New Game", normalFont, highlightFont);
		ngbutton.halign = HorizontalAlignment.RIGHT;
		ngbutton.addListener(new InputListener()
		{
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button)
			{
				Global.newGame();
				RoguelikeGame.Instance.switchScreen(ScreenEnum.GAME);
				
				return true;
			}
		});
		
		buttonTable.add(ngbutton).expandX().fillX();
		buttonTable.row();
		
		HoverTextButton qbutton = new HoverTextButton("Quit", normalFont, highlightFont);
		qbutton.halign = HorizontalAlignment.RIGHT;
		qbutton.addListener(new InputListener()
		{
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button)
			{
				Gdx.app.exit();
				
				return true;
			}
		});
		
		buttonTable.add(qbutton).expandX().fillX();
		buttonTable.row();
		
		table.add(buttonTable).width(Value.percentWidth(0.3f, table)).expandX().right().padRight(50).expandY().top();
		
		table.setFillParent(true);
		stage.addActor(table);
	}

	@Override
	public void show()
	{
		Gdx.input.setInputProcessor(stage);	
	}

	@Override
	public void render(float delta)
	{
		stage.act();
		
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		batch.begin();
		
		batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		batch.end();
		
		stage.draw();
	}

	@Override
	public void resize(int width, int height)
	{
		batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
		stage.getViewport().update(width, height, true);
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
	
	Stage stage;
	Skin skin;
	
	SpriteBatch batch;

	BitmapFont titleFont;
	BitmapFont normalFont;
	BitmapFont highlightFont;
	
	Texture background;
}
