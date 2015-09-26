package Roguelike.Screens;

import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.Global.Direction;
import Roguelike.Global.Statistic;
import Roguelike.RoguelikeGame.ScreenEnum;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Entity.Entity;
import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Entity.EnvironmentEntity.ActivationAction;
import Roguelike.Entity.GameEntity;
import Roguelike.Entity.Tasks.TaskUseAbility;
import Roguelike.Entity.Tasks.TaskWait;
import Roguelike.Fields.Field;
import Roguelike.Fields.Field.FieldLayer;
import Roguelike.Items.Item;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.SpriteEffect;
import Roguelike.Sprite.SpriteAnimation.MoveAnimation;
import Roguelike.Tiles.GameTile;
import Roguelike.Tiles.Point;
import Roguelike.Tiles.SeenTile;
import Roguelike.Tiles.SeenTile.SeenHistoryItem;
import Roguelike.UI.AbilityPanel;
import Roguelike.UI.AbilityPoolPanel;
import Roguelike.UI.DragDropPayload;
import Roguelike.UI.EntityStatusRenderer;
import Roguelike.UI.HPWidget;
import Roguelike.UI.InventoryPanel;
import Roguelike.UI.MessageStack;
import Roguelike.UI.MessageStack.Line;
import Roguelike.UI.MessageStack.Message;
import Roguelike.UI.SpriteWidget;
import Roguelike.UI.TabPanel;
import Roguelike.UI.Tooltip;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class GameScreen implements Screen, InputProcessor, GestureListener
{
	// ####################################################################//
	// region Create

	// ----------------------------------------------------------------------
	public GameScreen()
	{
		Instance = this;
	}

	// ----------------------------------------------------------------------
	private void create()
	{
		batch = new SpriteBatch();

		font = AssetManager.loadFont( "Sprites/GUI/stan0755.ttf", 12 );

		blank = AssetManager.loadTextureRegion( "Sprites/blank.png" );
		white = AssetManager.loadTextureRegion( "Sprites/white.png" );
		bag = AssetManager.loadSprite( "bag" );
		orb = AssetManager.loadSprite( "orb" );
		border = AssetManager.loadSprite( "GUI/frame" );
		speechBubbleArrow = AssetManager.loadTextureRegion( "Sprites/GUI/SpeechBubbleArrow.png" );
		speechBubbleBackground = new NinePatch( AssetManager.loadTextureRegion( "Sprites/GUI/SpeechBubble.png" ), 10, 10, 10, 10 );

		gestureDetector = new GestureDetector( this );
		gestureDetector.setLongPressSeconds( 0.5f );

		inputMultiplexer = new InputMultiplexer();

		LoadUI();

		InputProcessor inputProcessorOne = this;
		InputProcessor inputProcessorTwo = stage;

		inputMultiplexer.addProcessor( gestureDetector );
		inputMultiplexer.addProcessor( inputProcessorTwo );
		inputMultiplexer.addProcessor( inputProcessorOne );
	}

	// ----------------------------------------------------------------------
	private void LoadUI()
	{
		skin = Global.loadSkin();

		Global.skin = skin;

		stage = new Stage( new ScreenViewport() );

		abilityPanel = new AbilityPanel( skin, stage );

		abilityPoolPanel = new AbilityPoolPanel( skin, stage );
		inventoryPanel = new InventoryPanel( skin, stage );
		messageStack = new MessageStack();
		messageStack.addLine( new Line( new Message( "Welcome to the DUNGEON!" ) ) );

		Widget blankTab = new Widget();

		tabPane = new TabPanel();

		TabPanel.Tab tab = tabPane.addTab( AssetManager.loadSprite( "blank" ), blankTab );
		tabPane.addTab( AssetManager.loadSprite( "GUI/All" ), inventoryPanel );
		tabPane.addTab( AssetManager.loadSprite( "GUI/Abilities" ), abilityPoolPanel );

		tabPane.selectTab( tab );

		stage.addActor( tabPane );
		stage.addActor( abilityPanel );

		stage.addActor( inventoryPanel );
		stage.addActor( abilityPoolPanel );
		stage.addActor( abilityPanel );

		relayoutUI();
	}

	// ----------------------------------------------------------------------
	public void relayoutUI()
	{
		abilityPanel.setX( stage.getWidth() - abilityPanel.getPrefWidth() - 20 );
		abilityPanel.setY( 20 );
		abilityPanel.setWidth( abilityPanel.getPrefWidth() );
		abilityPanel.setHeight( abilityPanel.getPrefHeight() );

		if ( Global.ANDROID )
		{
			tabPane.setX( 20 );
			tabPane.setY( 20 );
			tabPane.setHeight( roundTo( stage.getHeight() - 40, 32 ) );
			tabPane.setWidth( stage.getWidth() );
		}
		else
		{
			tabPane.setX( 20 );
			tabPane.setY( 20 );
			tabPane.setHeight( roundTo( stage.getHeight() / 2, 32 ) );
			tabPane.setWidth( stage.getWidth() );
		}
	}

	// ----------------------------------------------------------------------
	private float roundTo( float val, float multiple )
	{
		return (float) ( multiple * Math.floor( val / multiple ) );
	}

	// endregion Create
	// ####################################################################//
	// region Screen

	// ----------------------------------------------------------------------
	@Override
	public void show()
	{
		if ( !created )
		{
			create();
			created = true;
		}

		Gdx.input.setInputProcessor( inputMultiplexer );

		resize( Global.ScreenSize[0], Global.ScreenSize[1] );
	}

	// ----------------------------------------------------------------------
	@Override
	public void render( float delta )
	{
		frametime = ( frametime + delta ) / 2.0f;
		fpsAccumulator += delta;
		if ( fpsAccumulator > 0.5f )
		{
			storedFrametime = frametime;
			fps = (int) ( 1.0f / frametime );
			fpsAccumulator = 0;
		}

		Global.CurrentLevel.update( delta );

		int offsetx = Global.Resolution[0] / 2 - Global.CurrentLevel.player.tile[0][0].x * Global.TileSize;
		int offsety = Global.Resolution[1] / 2 - Global.CurrentLevel.player.tile[0][0].y * Global.TileSize;

		if ( Global.CurrentLevel.player.sprite.spriteAnimation instanceof MoveAnimation )
		{
			int[] offset = Global.CurrentLevel.player.sprite.spriteAnimation.getRenderOffset();

			offsetx -= offset[0];
			offsety -= offset[1];
		}

		// do screen shake
		if ( screenShakeRadius > 2 )
		{
			screenShakeAccumulator += delta;
			while ( screenShakeAccumulator >= ScreenShakeSpeed )
			{
				screenShakeAccumulator -= ScreenShakeSpeed;
				screenShakeAngle += ( 150 + MathUtils.random() * 60 );
				screenShakeRadius *= 0.9f;
			}

			offsetx += Math.sin( screenShakeAngle ) * screenShakeRadius;
			offsety += Math.cos( screenShakeAngle ) * screenShakeRadius;
		}

		int mousex = ( mousePosX - offsetx ) / Global.TileSize;
		int mousey = ( mousePosY - offsety ) / Global.TileSize;

		int tileSize3 = Global.TileSize / 3;

		Gdx.gl.glClearColor( 0, 0, 0, 1 );
		Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT );
		batch.begin();

		hasStatus.clear();
		entitiesWithSpeech.clear();

		renderBackground( offsetx, offsety );
		renderSeenTiles( offsetx, offsety, tileSize3 );

		renderVisibleTiles( offsetx, offsety, tileSize3 );
		if ( Global.CurrentDialogue == null )
		{
			renderCursor( offsetx, offsety, mousex, mousey, delta );
		}
		renderActiveAbilities( offsetx, offsety );
		renderSpriteEffects( offsetx, offsety, tileSize3 );

		flush( batch );

		renderStatus( offsetx, offsety );

		renderSpeechBubbles( offsetx, offsety, delta );

		if ( Global.CurrentDialogue == null )
		{
			if ( preparedAbility != null )
			{
				batch.setColor( 0.3f, 0.6f, 0.8f, 0.5f );
				for ( Point tile : abilityTiles )
				{
					batch.draw( white, tile.x * Global.TileSize + offsetx, tile.y * Global.TileSize + offsety, Global.TileSize, Global.TileSize );
				}
			}

			// if ( Global.ANDROID )
			// {
			// EntityStatusRenderer.draw( Global.CurrentLevel.player, batch,
			// Global.Resolution[0] - ( Global.Resolution[0] / 4 ) - 120,
			// Global.Resolution[1] - 120, Global.Resolution[0] / 4, 100, 1.0f /
			// 4.0f );
			// }
			// else
			// {
			// font.draw( batch, Global.PlayerName + " the " +
			// Global.PlayerTitle, 20, Global.Resolution[1] - 20 );
			// font.draw( batch, "Essence: " +
			// Global.CurrentLevel.player.essence, 20, Global.Resolution[1] - 40
			// );
			// EntityStatusRenderer.draw( Global.CurrentLevel.player, batch, 20,
			// Global.Resolution[1] - 160, Global.Resolution[0] / 4, 100, 1.0f /
			// 4.0f );
			// }

			batch.end();

			stage.act( delta );
			stage.draw();

			batch.begin();
		}

		if ( dragDropPayload != null && dragDropPayload.shouldDraw() )
		{
			dragDropPayload.sprite.render( batch, (int) dragDropPayload.x, (int) dragDropPayload.y, 32, 32 );
		}

		font.draw( batch, "FPS: " + fps, Global.Resolution[0] - 100, Global.Resolution[1] - 20 );
		font.draw( batch, "Frametime: " + storedFrametime, Global.Resolution[0] - 200, Global.Resolution[1] - 40 );

		batch.end();

		// limit fps
		sleep( Global.FPS );
	}

	// ----------------------------------------------------------------------
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

	// ----------------------------------------------------------------------
	private void renderBackground( int offsetx, int offsety )
	{
		if ( Global.CurrentLevel.background != null )
		{
			Sprite sprite = Global.CurrentLevel.background;

			temp.set( Global.CurrentLevel.Ambient ).mul( Global.DayNightFactor );
			temp.a = 1;

			batch.setColor( temp );

			if ( Global.CurrentLevel.isVisionRestricted )
			{
				for ( Point pos : Global.CurrentLevel.visibilityData.getCurrentShadowCast() )
				{
					if ( pos.x < 0 || pos.y < 0 || pos.x >= Global.CurrentLevel.width || pos.y >= Global.CurrentLevel.height )
					{
						int x = pos.x;
						int y = pos.y;

						int cx = x * Global.TileSize + offsetx;
						int cy = y * Global.TileSize + offsety;

						sprite.render( batch, cx, cy, Global.TileSize, Global.TileSize );
					}
				}
			}
			else
			{
				int px = Global.CurrentLevel.player.tile[0][0].x * Global.TileSize + offsetx;
				int py = Global.CurrentLevel.player.tile[0][0].y * Global.TileSize + offsety;

				int sx = px - ( (int) roundTo( px, Global.TileSize ) ) - Global.TileSize;
				int sy = py - ( (int) roundTo( py, Global.TileSize ) ) - Global.TileSize;

				for ( int cx = sx; cx < Global.Resolution[0]; cx += Global.TileSize )
				{
					for ( int cy = sy; cy < Global.Resolution[1]; cy += Global.TileSize )
					{
						sprite.render( batch, cx, cy, Global.TileSize, Global.TileSize );
					}
				}
			}
		}
	}

	// ----------------------------------------------------------------------
	private void renderVisibleTiles( int offsetx, int offsety, int tileSize3 )
	{
		for ( int x = 0; x < Global.CurrentLevel.width; x++ )
		{
			for ( int y = 0; y < Global.CurrentLevel.height; y++ )
			{
				GameTile gtile = Global.CurrentLevel.Grid[x][y];

				if ( gtile.visible )
				{
					for ( int i = 0; i < gtile.tileData.sprites.length; i++ )
					{
						queueSprite( gtile.tileData.sprites[i], gtile.light, x * Global.TileSize + offsetx, y * Global.TileSize + offsety, Global.TileSize, Global.TileSize, RenderLayer.GROUNDTILE, i );
					}

					GameTile nextTile = Global.CurrentLevel.getGameTile( x, y - 1 );
					if ( nextTile != null && nextTile.tileData.raisedSprite != null )
					{
						GameTile ogtile = nextTile;
						GameTile oprevTile = gtile;

						if ( ogtile.tileData.raisedSprite.overhangSprite != null
								&& oprevTile != null
								&& oprevTile.visible
								&& ( oprevTile.tileData.raisedSprite == null || !oprevTile.tileData.raisedSprite.name.equals( gtile.tileData.raisedSprite.name ) ) )
						{
							queueSprite( ogtile.tileData.raisedSprite.overhangSprite, oprevTile.light, x * Global.TileSize + offsetx, y
									* Global.TileSize
									+ offsety, Global.TileSize, Global.TileSize, RenderLayer.OVERHANG );
						}
					}

					if ( gtile.tileData.raisedSprite != null )
					{
						if ( nextTile != null
								&& nextTile.tileData.raisedSprite != null
								&& nextTile.tileData.raisedSprite.name.equals( gtile.tileData.raisedSprite.name ) )
						{
							queueSprite( gtile.tileData.raisedSprite.topSprite, gtile.light, x * Global.TileSize + offsetx, y * Global.TileSize + offsety, Global.TileSize, Global.TileSize, RenderLayer.OVERHANG );
						}
						else
						{
							queueSprite( gtile.tileData.raisedSprite.frontSprite, gtile.light, x * Global.TileSize + offsetx, y * Global.TileSize + offsety, Global.TileSize, Global.TileSize, RenderLayer.GROUNDTILE );
						}
					}

					if ( gtile.hasFields )
					{
						for ( FieldLayer layer : FieldLayer.values() )
						{
							Field field = gtile.fields.get( layer );
							if ( field != null )
							{
								if ( field.layer == FieldLayer.GROUND )
								{
									queueSprite( field.sprite, gtile.light, x * Global.TileSize + offsetx, y * Global.TileSize + offsety, Global.TileSize, Global.TileSize, RenderLayer.GROUNDFIELD );
								}
								else
								{
									queueSprite( field.sprite, gtile.light, x * Global.TileSize + offsetx, y * Global.TileSize + offsety, Global.TileSize, Global.TileSize, RenderLayer.OVERHEADFIELD );
								}
							}
						}
					}

					if ( gtile.environmentEntity != null && gtile.environmentEntity.tile[0][0] == gtile )
					{
						EnvironmentEntity entity = gtile.environmentEntity;

						int cx = x * Global.TileSize + offsetx;
						int cy = y * Global.TileSize + offsety;

						int width = Global.TileSize;
						int height = Global.TileSize;

						Sprite sprite = entity.sprite;

						if ( entity.raisedSprite != null )
						{
							if ( nextTile != null
									&& nextTile.tileData.raisedSprite != null
									&& nextTile.tileData.raisedSprite.name.equals( entity.raisedSprite.name ) )
							{
								sprite = entity.raisedSprite.topSprite;
							}
							else
							{
								sprite = entity.raisedSprite.frontSprite;
							}
						}

						if ( entity.location != Direction.CENTER )
						{
							if ( entity.location == Direction.EAST || entity.location == Direction.WEST )
							{
								cx += -entity.location.getX() * ( Global.TileSize / 2 );
							}
							else if ( entity.location == Direction.SOUTH )
							{
								cy += Global.TileSize;
							}
						}

						if ( entity.canTakeDamage && entity.HP < entity.statistics.get( Statistic.MAXHP ) || entity.stacks.size > 0 )
						{
							hasStatus.add( entity );
						}

						if ( entity.overHead )
						{
							queueSprite( sprite, gtile.light, cx, cy, width, height, RenderLayer.OVERHEADENTITY );
						}
						else if ( sprite.drawActualSize )
						{
							queueSprite( sprite, gtile.light, cx, cy, width, height, RenderLayer.RAISEDENTITY );
						}
						else
						{
							queueSprite( sprite, gtile.light, cx, cy, width, height, RenderLayer.GROUNDENTITY );
						}

						if ( entity.tile[0][0].visible && entity.popup != null )
						{
							entitiesWithSpeech.add( entity );
						}
					}

					GameEntity entity = gtile.entity;

					if ( entity != null && entity.tile[0][0] == gtile )
					{
						int cx = x * Global.TileSize + offsetx;
						int cy = y * Global.TileSize + offsety;

						int width = Global.TileSize;
						int height = Global.TileSize;

						Sprite sprite = entity.sprite;

						if ( entity.raisedSprite != null )
						{
							if ( nextTile != null
									&& nextTile.tileData.raisedSprite != null
									&& nextTile.tileData.raisedSprite.name.equals( entity.raisedSprite.name ) )
							{
								sprite = entity.raisedSprite.topSprite;
							}
							else
							{
								sprite = entity.raisedSprite.frontSprite;
							}
						}

						if ( entity.location != Direction.CENTER )
						{
							Direction dir = entity.location;
							cx = cx + tileSize3 * ( dir.getX() * -1 + 1 );
							cy = cy + tileSize3 * ( dir.getY() * -1 + 1 );
							width = tileSize3;
							height = tileSize3;
						}

						if ( entity.canTakeDamage && entity.HP < entity.statistics.get( Statistic.MAXHP ) || entity.stacks.size > 0 )
						{
							hasStatus.add( entity );
						}

						if ( sprite.drawActualSize )
						{
							queueSprite( sprite, gtile.light, cx, cy, width, height, RenderLayer.RAISEDENTITY );
						}
						else
						{
							queueSprite( sprite, gtile.light, cx, cy, width, height, RenderLayer.RAISEDENTITY );
						}

						if ( entity.tile[0][0].visible && entity.popup != null )
						{
							entitiesWithSpeech.add( entity );
						}
					}

					if ( gtile.items.size > 0 )
					{
						int cx = x * Global.TileSize + offsetx;
						int cy = y * Global.TileSize + offsety;

						if ( gtile.items.size == 1 )
						{
							queueSprite( gtile.items.get( 0 ).getIcon(), gtile.light, cx, cy, Global.TileSize, Global.TileSize, RenderLayer.ITEM );
						}
						else
						{
							queueSprite( bag, gtile.light, cx, cy, Global.TileSize, Global.TileSize, RenderLayer.ITEM );

							for ( Item item : gtile.items )
							{
								if ( item.getIcon().spriteAnimation != null )
								{
									queueSprite( item.getIcon(), gtile.light, cx, cy, Global.TileSize, Global.TileSize, RenderLayer.ITEM );
								}
							}
						}
					}

					if ( gtile.essence > 0 && gtile.spriteEffects.size == 0 )
					{
						int cx = x * Global.TileSize + offsetx;
						int cy = y * Global.TileSize + offsety;

						float scale = 0.5f + 0.5f * ( MathUtils.clamp( gtile.essence, 10.0f, 1000.0f ) / 1000.0f );

						float size = Global.TileSize * scale;

						queueSprite( orb, gtile.light, ( cx + Global.TileSize / 2 ) - size / 2, ( cy + Global.TileSize / 2 ) - size / 2, size, size, RenderLayer.ESSENCE );
					}
				}
			}
		}
	}

	// ----------------------------------------------------------------------
	private void renderSeenTiles( int offsetx, int offsety, int tileSize3 )
	{
		Color col = batch.getColor();

		batch.setShader( GrayscaleShader.Instance );
		for ( int x = 0; x < Global.CurrentLevel.width; x++ )
		{
			for ( int y = 0; y < Global.CurrentLevel.height; y++ )
			{
				GameTile gtile = Global.CurrentLevel.Grid[x][y];
				SeenTile stile = Global.CurrentLevel.SeenGrid[x][y];

				if ( !gtile.visible && stile.seen )
				{
					temp.set( Global.CurrentLevel.Ambient );
					temp.mul( temp.a );
					if ( Global.CurrentLevel.affectedByDayNight )
					{
						temp.mul( Global.DayNightFactor );
					}

					temp.a = 1;

					if ( !temp.equals( col ) )
					{
						batch.setColor( temp );
						col.set( temp );
					}

					for ( int i = 0; i < stile.tileHistory.size; i++ )
					{
						SeenHistoryItem hist = stile.tileHistory.items[i];

						int cx = x * Global.TileSize + offsetx;
						int cy = y * Global.TileSize + offsety;

						if ( hist.location != Direction.CENTER )
						{
							Direction dir = hist.location;
							hist.sprite.render( batch, cx + tileSize3 * ( dir.getX() * -1 + 1 ), cy + tileSize3 * ( dir.getY() * -1 + 1 ), tileSize3, tileSize3 );
						}
						else
						{
							hist.sprite.render( batch, cx, cy, Global.TileSize, Global.TileSize, hist.sprite.baseScale[0], hist.sprite.baseScale[1], hist.animationState );
						}
					}

					if ( stile.fieldHistory.size > 0 )
					{
						for ( int i = 0; i < stile.fieldHistory.size; i++ )
						{
							SeenHistoryItem hist = stile.fieldHistory.items[i];

							int cx = x * Global.TileSize + offsetx;
							int cy = y * Global.TileSize + offsety;

							if ( hist.location != Direction.CENTER )
							{
								Direction dir = hist.location;
								hist.sprite.render( batch, cx + tileSize3 * ( dir.getX() * -1 + 1 ), cy + tileSize3 * ( dir.getY() * -1 + 1 ), tileSize3, tileSize3 );
							}
							else
							{
								hist.sprite.render( batch, cx, cy, Global.TileSize, Global.TileSize, hist.sprite.baseScale[0], hist.sprite.baseScale[1], hist.animationState );
							}
						}
					}

					if ( stile.itemHistory != null )
					{
						SeenHistoryItem hist = stile.itemHistory;

						int cx = x * Global.TileSize + offsetx;
						int cy = y * Global.TileSize + offsety;

						if ( hist.location != Direction.CENTER )
						{
							Direction dir = hist.location;
							hist.sprite.render( batch, cx + tileSize3 * ( dir.getX() * -1 + 1 ), cy + tileSize3 * ( dir.getY() * -1 + 1 ), tileSize3, tileSize3 );
						}
						else
						{
							hist.sprite.render( batch, cx, cy, Global.TileSize, Global.TileSize, hist.sprite.baseScale[0], hist.sprite.baseScale[1], hist.animationState );
						}
					}

					if ( stile.essenceHistory != null )
					{
						SeenHistoryItem hist = stile.essenceHistory;

						int cx = x * Global.TileSize + offsetx;
						int cy = y * Global.TileSize + offsety;

						if ( hist.location != Direction.CENTER )
						{
							Direction dir = hist.location;
							hist.sprite.render( batch, cx + tileSize3 * ( dir.getX() * -1 + 1 ), cy + tileSize3 * ( dir.getY() * -1 + 1 ), tileSize3, tileSize3 );
						}
						else
						{
							hist.sprite.render( batch, cx, cy, Global.TileSize, Global.TileSize, hist.sprite.baseScale[0], hist.sprite.baseScale[1], hist.animationState );
						}
					}
				}
			}
		}

		for ( int x = 0; x < Global.CurrentLevel.width; x++ )
		{
			for ( int y = 0; y < Global.CurrentLevel.height; y++ )
			{
				GameTile gtile = Global.CurrentLevel.Grid[x][y];
				SeenTile stile = Global.CurrentLevel.SeenGrid[x][y];

				if ( !gtile.visible && stile.seen )
				{
					temp.set( Global.CurrentLevel.Ambient );
					temp.mul( temp.a );
					if ( Global.CurrentLevel.affectedByDayNight )
					{
						temp.mul( Global.DayNightFactor );
					}

					temp.a = 1;

					if ( !temp.equals( col ) )
					{
						batch.setColor( temp );
						col.set( temp );
					}

					if ( stile.environmentHistory != null )
					{
						SeenHistoryItem hist = stile.environmentHistory;

						int cx = x * Global.TileSize + offsetx;
						int cy = y * Global.TileSize + offsety;

						if ( hist.location != Direction.CENTER )
						{
							if ( hist.location == Direction.EAST || hist.location == Direction.WEST )
							{
								cx += -hist.location.getX() * ( Global.TileSize / 2 );
							}
							else if ( hist.location == Direction.SOUTH )
							{
								cy += Global.TileSize;
							}
						}

						hist.sprite.render( batch, cx, cy, Global.TileSize, Global.TileSize, hist.sprite.baseScale[0], hist.sprite.baseScale[1], hist.animationState );
					}

					if ( stile.entityHistory != null )
					{
						SeenHistoryItem hist = stile.entityHistory;

						int cx = x * Global.TileSize + offsetx;
						int cy = y * Global.TileSize + offsety;

						if ( hist.location != Direction.CENTER )
						{
							Direction dir = hist.location;
							hist.sprite.render( batch, cx + tileSize3 * ( dir.getX() * -1 + 1 ), cy + tileSize3 * ( dir.getY() * -1 + 1 ), tileSize3, tileSize3 );
						}
						else
						{
							hist.sprite.render( batch, cx, cy, Global.TileSize, Global.TileSize, hist.sprite.baseScale[0], hist.sprite.baseScale[1], hist.animationState );
						}
					}
				}
			}
		}

		for ( int x = 0; x < Global.CurrentLevel.width; x++ )
		{
			for ( int y = 0; y < Global.CurrentLevel.height; y++ )
			{
				GameTile gtile = Global.CurrentLevel.Grid[x][y];
				SeenTile stile = Global.CurrentLevel.SeenGrid[x][y];

				if ( !gtile.visible && stile.seen )
				{
					temp.set( Global.CurrentLevel.Ambient );
					temp.mul( temp.a );
					if ( Global.CurrentLevel.affectedByDayNight )
					{
						temp.mul( Global.DayNightFactor );
					}

					temp.a = 1;

					if ( !temp.equals( col ) )
					{
						batch.setColor( temp );
						col.set( temp );
					}

					SeenHistoryItem hist = stile.overhangHistory;

					if ( hist != null )
					{
						int cx = x * Global.TileSize + offsetx;
						int cy = y * Global.TileSize + offsety;

						if ( hist.location != Direction.CENTER )
						{
							Direction dir = hist.location;
							hist.sprite.render( batch, cx + tileSize3 * ( dir.getX() * -1 + 1 ), cy + tileSize3 * ( dir.getY() * -1 + 1 ), tileSize3, tileSize3 );
						}
						else
						{
							hist.sprite.render( batch, cx, cy, Global.TileSize, Global.TileSize, hist.sprite.baseScale[0], hist.sprite.baseScale[1], hist.animationState );
						}
					}
				}
			}
		}

		batch.setColor( Color.WHITE );
		batch.setShader( null );
	}

	// ----------------------------------------------------------------------
	private void renderCursor( int offsetx, int offsety, int mousex, int mousey, float delta )
	{
		if ( !mouseOverUI && !Global.ANDROID )
		{
			Color colour = Color.GREEN;

			if ( mousex < 0
					|| mousex >= Global.CurrentLevel.width
					|| mousey < 0
					|| mousey >= Global.CurrentLevel.height
					|| !Global.CurrentLevel.getSeenTile( mousex, mousey ).seen )
			{
				colour = Color.RED;
			}
			else
			{
				GameTile mouseTile = Global.CurrentLevel.getGameTile( mousex, mousey );
				if ( mouseTile.tileData.passableBy.intersect( Global.CurrentLevel.player.getTravelType() ) )
				{
					colour = Color.GREEN;
				}
				else
				{
					colour = Color.RED;
				}
			}

			border.update( delta );

			queueSprite( border, colour, mousex * Global.TileSize + offsetx, mousey * Global.TileSize + offsety, Global.TileSize, Global.TileSize, RenderLayer.CURSOR );
		}
	}

	// ----------------------------------------------------------------------
	private void renderStatus( int offsetx, int offsety )
	{
		batch.setColor( Color.WHITE );

		for ( Entity e : hasStatus )
		{
			int x = e.tile[0][0].x;
			int y = e.tile[0][0].y;

			int cx = x * Global.TileSize + offsetx;
			int cy = y * Global.TileSize + offsety;

			if ( e.sprite.spriteAnimation != null )
			{
				int[] offset = e.sprite.spriteAnimation.getRenderOffset();
				cx += offset[0];
				cy += offset[1];
			}

			EntityStatusRenderer.draw( e, batch, cx, cy, Global.TileSize, Global.TileSize, 1.0f / 8.0f );
		}
	}

	// ----------------------------------------------------------------------
	private void renderActiveAbilities( int offsetx, int offsety )
	{
		if ( Global.CurrentLevel.ActiveAbilities.size > 0 )
		{
			for ( ActiveAbility aa : Global.CurrentLevel.ActiveAbilities )
			{
				for ( GameTile tile : aa.AffectedTiles )
				{
					if ( tile.visible )
					{
						queueSprite( aa.getSprite(), Color.WHITE, tile.x * Global.TileSize + offsetx, tile.y * Global.TileSize + offsety, Global.TileSize, Global.TileSize, RenderLayer.ABILITY );
					}
				}
			}
		}
	}

	// ----------------------------------------------------------------------
	private void renderSpriteEffects( int offsetx, int offsety, int tileSize3 )
	{
		for ( int x = 0; x < Global.CurrentLevel.width; x++ )
		{
			for ( int y = 0; y < Global.CurrentLevel.height; y++ )
			{
				GameTile gtile = Global.CurrentLevel.Grid[x][y];

				if ( gtile.visible && gtile.spriteEffects.size > 0 )
				{
					for ( SpriteEffect e : gtile.spriteEffects )
					{
						if ( e.Corner == Direction.CENTER )
						{
							queueSprite( e.Sprite, Color.WHITE, x * Global.TileSize + offsetx, y * Global.TileSize + offsety, Global.TileSize, Global.TileSize, RenderLayer.EFFECT );
						}
						else
						{
							queueSprite( e.Sprite, Color.WHITE, x * Global.TileSize + offsetx + tileSize3 * ( e.Corner.getX() * -1 + 1 ), y
									* Global.TileSize
									+ offsety
									+ tileSize3
									* ( e.Corner.getY() * -1 + 1 ), tileSize3, tileSize3, RenderLayer.EFFECT );
						}
					}
				}
			}
		}
	}

	// ----------------------------------------------------------------------
	private void renderSpeechBubbles( int offsetx, int offsety, float delta )
	{
		for ( Entity entity : entitiesWithSpeech )
		{
			if ( entity.popupDuration <= 0 && entity.displayedPopup.length() == entity.popup.length() )
			{
				entity.popupFade -= delta;

				if ( entity.popupFade <= 0 )
				{
					entity.popup = null;
					continue;
				}
			}

			float alpha = 1;
			if ( entity.popupDuration <= 0 && entity.displayedPopup.length() == entity.popup.length() )
			{
				alpha *= entity.popupFade;

				if ( alpha < 0 )
				{
					alpha = 0;
				}
			}
			tempColour.set( 1, 1, 1, alpha );

			int x = entity.tile[0][0].x;
			int y = entity.tile[0][0].y;

			y += 1;

			int cx = x * Global.TileSize + offsetx + Global.TileSize / 2;
			int cy = y * Global.TileSize + offsety;

			if ( entity.sprite.spriteAnimation != null )
			{
				int[] offset = entity.sprite.spriteAnimation.getRenderOffset();
				cx += offset[0];
				cy += offset[1];
			}

			layout.setText( font, entity.popup, tempColour, ( stage.getWidth() / 3 ) * 2, Align.left, true );

			float left = cx - ( layout.width / 2 ) - 10;

			if ( left < 0 )
			{
				left = 0;
			}

			float right = left + layout.width + 20;

			if ( right >= stage.getWidth() )
			{
				left -= right - stage.getWidth();
			}

			float width = layout.width;
			float height = layout.height;

			layout.setText( font, entity.displayedPopup, tempColour, ( stage.getWidth() / 3 ) * 2, Align.left, true );

			batch.setColor( tempColour );

			speechBubbleBackground.draw( batch, left, cy, width + 20, height + 20 );
			batch.draw( speechBubbleArrow, cx - 4, cy - 6, 8, 8 );

			font.draw( batch, layout, left + 10, cy + layout.height + 10 );
		}

		if ( Global.CurrentDialogue != null && Global.CurrentDialogue.currentInput != null )
		{
			int padding = Global.ANDROID ? 20 : 10;

			int x = Global.CurrentDialogue.entity.tile[0][0].x;
			int y = Global.CurrentDialogue.entity.tile[0][0].y;

			int cx = x * Global.TileSize + offsetx + Global.TileSize / 2;
			int cy = y * Global.TileSize + offsety;

			float layoutwidth = 0;
			float layoutheight = 0;
			for ( int i = 0; i < Global.CurrentDialogue.currentInput.choices.size; i++ )
			{
				String message = ( i + 1 ) + ": " + Global.expandNames( Global.CurrentDialogue.currentInput.choices.get( i ) );

				layout.setText( font, message, tempColour, ( stage.getWidth() / 3 ) * 2, Align.left, true );
				if ( layout.width > layoutwidth )
				{
					layoutwidth = layout.width;
				}
				layoutheight += layout.height + padding;
			}

			cy -= layoutheight + 20;

			float left = cx - ( layoutwidth / 2 ) - 10;

			if ( left < 0 )
			{
				left = 0;
			}

			float right = left + layoutwidth + 20;

			if ( right >= stage.getWidth() )
			{
				left -= right - stage.getWidth();
			}

			speechBubbleBackground.draw( batch, left, cy, layoutwidth + 20, layoutheight + 20 );

			float voffset = padding / 2;
			for ( int i = Global.CurrentDialogue.currentInput.choices.size - 1; i >= 0; i-- )
			{
				String message = ( i + 1 ) + ": " + Global.expandNames( Global.CurrentDialogue.currentInput.choices.get( i ) );

				if ( Global.CurrentDialogue.mouseOverInput == i )
				{
					message = "[GREEN]" + message;
				}

				layout.setText( font, message, tempColour, ( stage.getWidth() / 3 ) * 2, Align.left, true );

				font.draw( batch, layout, left + 10, cy + layout.height + 10 + voffset );

				voffset += layout.height + padding;
			}
		}
	}

	// ----------------------------------------------------------------------
	private void queueSprite( Sprite sprite, Color colour, float x, float y, float width, float height, RenderLayer layer )
	{
		queueSprite( sprite, colour, x, y, width, height, layer, 0 );
	}

	// ----------------------------------------------------------------------
	private void queueSprite( Sprite sprite, Color colour, float x, float y, float width, float height, RenderLayer layer, int index )
	{
		if ( sprite != null && sprite.spriteAnimation != null )
		{
			int[] offset = sprite.spriteAnimation.getRenderOffset();
			x += offset[0];
			y += offset[1];
		}

		queuedSprites.add( renderSpritePool.obtain().set( sprite, colour, x, y, width, height, layer, index ) );
	}

	// ----------------------------------------------------------------------
	private void flush( Batch batch )
	{
		queuedSprites.sort();

		Color col = batch.getColor();
		for ( RenderSprite rs : queuedSprites )
		{
			temp.set( rs.colour );
			if ( !temp.equals( col ) )
			{
				batch.setColor( temp );
				col.set( temp );
			}

			rs.sprite.render( batch, (int) rs.x, (int) rs.y, (int) rs.width, (int) rs.height );
		}

		renderSpritePool.freeAll( queuedSprites );
		queuedSprites.clear();
	}

	// ----------------------------------------------------------------------
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

		relayoutUI();
	}

	// ----------------------------------------------------------------------
	@Override
	public void pause()
	{
		Global.save();
	}

	// ----------------------------------------------------------------------
	@Override
	public void resume()
	{
	}

	// ----------------------------------------------------------------------
	@Override
	public void hide()
	{
	}

	// ----------------------------------------------------------------------
	@Override
	public void dispose()
	{
	}

	// endregion Screen
	// ####################################################################//
	// region InputProcessor

	// ----------------------------------------------------------------------
	@Override
	public boolean keyDown( int keycode )
	{
		if ( Global.CurrentDialogue != null )
		{
			if ( Global.CurrentDialogue.currentInput != null && keycode >= Keys.NUM_1 && keycode <= Keys.NUM_9 )
			{
				int val = keycode - Keys.NUM_0;
				if ( val <= Global.CurrentDialogue.currentInput.choices.size )
				{
					Global.CurrentDialogue.currentInput.answer = val;
				}
			}

			if ( Global.CurrentDialogue.entity.popup.length() == Global.CurrentDialogue.entity.displayedPopup.length() )
			{
				Global.CurrentDialogue.advance();
			}
			else
			{
				Global.CurrentDialogue.entity.displayedPopup = Global.CurrentDialogue.entity.popup;
			}
		}
		else if ( keycode == Keys.S )
		{
			Global.save();
		}
		else if ( keycode == Keys.L )
		{
			Global.load();
		}
		else if ( keycode == Keys.W )
		{
			Field field = Field.load( "Water" );
			field.stacks = 10;
			GameTile playerTile = Global.CurrentLevel.player.tile[0][0];
			field.trySpawnInTile( playerTile, 10 );
		}
		else if ( keycode == Keys.F )
		{
			Field field = Field.load( "Fire" );
			field.stacks = 1;
			GameTile playerTile = Global.CurrentLevel.player.tile[0][0];
			field.trySpawnInTile( playerTile, 1 );
		}
		else if ( keycode == Keys.G )
		{
			Field field = Field.load( "IceFog" );
			field.stacks = 4;
			GameTile playerTile = Global.CurrentLevel.player.tile[0][0];
			field.trySpawnInTile( playerTile, 4 );
		}
		else if ( keycode == Keys.H )
		{
			Field field = Field.load( "Static" );
			GameTile playerTile = Global.CurrentLevel.player.tile[0][0];
			GameTile newTile = playerTile.level.getGameTile( playerTile.x + 1, playerTile.y + 1 );
			field.trySpawnInTile( newTile, 10 );
		}
		else if ( keycode == Keys.I )
		{
			tabPane.toggleTab( inventoryPanel );
		}
		else if ( keycode == Keys.K )
		{
			tabPane.toggleTab( abilityPoolPanel );
		}
		else if ( keycode >= Keys.NUM_1 && keycode <= Keys.NUM_9 )
		{
			int abilityIndex = keycode - Keys.NUM_1;
			if ( Global.abilityPool.slottedActiveAbilities[abilityIndex] != null && Global.abilityPool.slottedActiveAbilities[abilityIndex].isAvailable() )
			{
				prepareAbility( Global.abilityPool.slottedActiveAbilities[abilityIndex] );
			}
		}
		else if ( keycode == Keys.ENTER )
		{
			if ( Global.CurrentLevel.player.tile[0][0].environmentEntity != null )
			{
				for ( ActivationAction action : Global.CurrentLevel.player.tile[0][0].environmentEntity.actions )
				{
					if ( action.visible )
					{
						action.activate( Global.CurrentLevel.player.tile[0][0].environmentEntity );
						Global.CurrentLevel.player.tasks.add( new TaskWait() );
						break;
					}
				}
			}
		}
		else if ( keycode == Keys.ESCAPE )
		{
			OptionsScreen.Instance.screen = ScreenEnum.GAME;
			Global.Game.switchScreen( ScreenEnum.OPTIONS );
		}

		return false;
	}

	// ----------------------------------------------------------------------
	@Override
	public boolean keyUp( int keycode )
	{
		return false;
	}

	// ----------------------------------------------------------------------
	@Override
	public boolean keyTyped( char character )
	{
		return false;
	}

	// ----------------------------------------------------------------------
	@Override
	public boolean touchDown( int screenX, int screenY, int pointer, int button )
	{
		if ( tooltip != null )
		{
			tooltip.setVisible( false );
			tooltip.remove();
			tooltip = null;
		}

		clearContextMenu();
		return true;
	}

	// ----------------------------------------------------------------------
	@Override
	public boolean touchUp( int screenX, int screenY, int pointer, int button )
	{
		if ( longPressed || dragged ) { return false; }

		if ( tooltip != null )
		{
			tooltip.setVisible( false );
			tooltip.remove();
			tooltip = null;
		}

		clearContextMenu();

		if ( Global.CurrentDialogue != null )
		{
			if ( Global.CurrentDialogue.currentInput != null )
			{
				if ( Global.ANDROID )
				{
					int mouseOver = getMouseOverDialogueOption( screenX, screenY );
					Global.CurrentDialogue.mouseOverInput = mouseOver;
				}

				if ( Global.CurrentDialogue.mouseOverInput != -1 )
				{
					Global.CurrentDialogue.currentInput.answer = Global.CurrentDialogue.mouseOverInput + 1;
					Global.CurrentDialogue.mouseOverInput = -1;
				}
			}

			if ( Global.CurrentDialogue.entity.popup.length() == Global.CurrentDialogue.entity.displayedPopup.length() )
			{
				Global.CurrentDialogue.advance();
			}
			else
			{
				Global.CurrentDialogue.entity.displayedPopup = Global.CurrentDialogue.entity.popup;
			}
		}
		else
		{
			Vector3 mousePos = camera.unproject( new Vector3( screenX, screenY, 0 ) );

			int mousePosX = (int) mousePos.x;
			int mousePosY = (int) mousePos.y;

			int offsetx = Global.Resolution[0] / 2 - Global.CurrentLevel.player.tile[0][0].x * Global.TileSize;
			int offsety = Global.Resolution[1] / 2 - Global.CurrentLevel.player.tile[0][0].y * Global.TileSize;

			int x = ( mousePosX - offsetx ) / Global.TileSize;
			int y = ( mousePosY - offsety ) / Global.TileSize;

			if ( preparedAbility != null )
			{
				if ( button == Buttons.LEFT )
				{
					if ( x >= 0 && x < Global.CurrentLevel.width && y >= 0 && y < Global.CurrentLevel.height )
					{
						GameTile tile = Global.CurrentLevel.getGameTile( x, y );
						if ( preparedAbility.isTargetValid( tile, abilityTiles ) )
						{
							Global.CurrentLevel.player.tasks.add( new TaskUseAbility( Pools.obtain( Point.class ).set( x, y ), preparedAbility ) );
						}
					}
				}
				preparedAbility = null;
			}
			else
			{
				if ( button == Buttons.RIGHT )
				{
					rightClick( screenX, screenY );
				}
				else
				{
					if ( x >= 0 && x < Global.CurrentLevel.width && y >= 0 && y < Global.CurrentLevel.height && Global.CurrentLevel.getSeenTile( x, y ).seen )
					{
						Global.CurrentLevel.player.AI.setData( "ClickPos", Pools.obtain( Point.class ).set( x, y ) );
					}
					else
					{
						x = MathUtils.clamp( x, -1, 1 );
						y = MathUtils.clamp( y, -1, 1 );

						x += Global.CurrentLevel.player.tile[0][0].x;
						y += Global.CurrentLevel.player.tile[0][0].y;

						Global.CurrentLevel.player.AI.setData( "ClickPos", Pools.obtain( Point.class ).set( x, y ) );
					}
				}
			}
		}

		return true;
	}

	// ----------------------------------------------------------------------
	@Override
	public boolean touchDragged( int screenX, int screenY, int pointer )
	{
		if ( dragDropPayload != null )
		{
			dragDropPayload.x = screenX - 16;
			dragDropPayload.y = Global.Resolution[1] - screenY - 16;
		}

		if ( Math.abs( screenX - startX ) > 10 || Math.abs( screenY - startY ) > 10 )
		{
			dragged = true;
		}

		return false;
	}

	// ----------------------------------------------------------------------
	@Override
	public boolean mouseMoved( int screenX, int screenY )
	{
		if ( tooltip != null )
		{
			tooltip.remove();
			tooltip = null;
		}

		if ( Global.CurrentDialogue != null )
		{
			if ( Global.CurrentDialogue.currentInput != null )
			{
				int mouseOver = getMouseOverDialogueOption( screenX, screenY );
				Global.CurrentDialogue.mouseOverInput = mouseOver;
			}
		}
		else
		{
			Vector3 mousePos = camera.unproject( new Vector3( screenX, screenY, 0 ) );

			mousePosX = (int) mousePos.x;
			mousePosY = (int) mousePos.y;

			stage.setScrollFocus( null );

			int offsetx = Global.Resolution[0] / 2 - Global.CurrentLevel.player.tile[0][0].x * Global.TileSize;
			int offsety = Global.Resolution[1] / 2 - Global.CurrentLevel.player.tile[0][0].y * Global.TileSize;

			mouseOverUI = false;

			int x = ( mousePosX - offsetx ) / Global.TileSize;
			int y = ( mousePosY - offsety ) / Global.TileSize;

			if ( x >= 0 && x < Global.CurrentLevel.width && y >= 0 && y < Global.CurrentLevel.height )
			{
				GameTile tile = Global.CurrentLevel.getGameTile( x, y );

				if ( tile.entity != null )
				{
					Table table = EntityStatusRenderer.getMouseOverTable( tile.entity, x * Global.TileSize + offsetx, y * Global.TileSize + offsety, Global.TileSize, Global.TileSize, 1.0f / 8.0f, mousePosX, mousePosY, skin );

					if ( table != null )
					{
						tooltip = new Tooltip( table, skin, stage );
						tooltip.show( mousePosX, mousePosY );
					}
				}
			}

			{
				Table table = EntityStatusRenderer.getMouseOverTable( Global.CurrentLevel.player, 20, Global.Resolution[1] - 120, Global.Resolution[0] / 4, 100, 1.0f / 4.0f, mousePosX, mousePosY, skin );

				if ( table != null )
				{
					tooltip = new Tooltip( table, skin, stage );
					tooltip.show( mousePosX, mousePosY );
				}
			}
		}

		return false;
	}

	// ----------------------------------------------------------------------
	private int getMouseOverDialogueOption( float screenX, float screenY )
	{
		int padding = Global.ANDROID ? 20 : 10;

		Vector3 mousePos = camera.unproject( new Vector3( screenX, screenY, 0 ) );

		mousePosX = (int) mousePos.x;
		mousePosY = (int) mousePos.y;

		int offsetx = Global.Resolution[0] / 2 - Global.CurrentLevel.player.tile[0][0].x * Global.TileSize;
		int offsety = Global.Resolution[1] / 2 - Global.CurrentLevel.player.tile[0][0].y * Global.TileSize;

		int x = Global.CurrentDialogue.entity.tile[0][0].x;
		int y = Global.CurrentDialogue.entity.tile[0][0].y;

		int cx = x * Global.TileSize + offsetx + Global.TileSize / 2;
		int cy = y * Global.TileSize + offsety;

		float layoutwidth = 0;
		float layoutheight = 0;
		for ( int i = 0; i < Global.CurrentDialogue.currentInput.choices.size; i++ )
		{
			String message = ( i + 1 ) + ": " + Global.expandNames( Global.CurrentDialogue.currentInput.choices.get( i ) );

			layout.setText( font, message, tempColour, ( stage.getWidth() / 3 ) * 2, Align.left, true );
			if ( layout.width > layoutwidth )
			{
				layoutwidth = layout.width;
			}
			layoutheight += layout.height + padding;
		}

		cy -= layoutheight + 20;

		float left = cx - ( layoutwidth / 2 ) - 10;

		if ( left < 0 )
		{
			left = 0;
		}

		float right = left + layoutwidth + 20;

		if ( right >= stage.getWidth() )
		{
			left -= right - stage.getWidth();
		}

		Global.CurrentDialogue.mouseOverInput = -1;

		float voffset = padding / 2;
		for ( int i = Global.CurrentDialogue.currentInput.choices.size - 1; i >= 0; i-- )
		{
			String message = ( i + 1 ) + ": " + Global.expandNames( Global.CurrentDialogue.currentInput.choices.get( i ) );
			layout.setText( font, message, tempColour, ( stage.getWidth() / 3 ) * 2, Align.left, true );

			if ( mousePosX >= left && mousePosX <= right && mousePosY <= cy + layout.height + 10 + voffset && mousePosY >= cy + 10 + voffset ) { return i; }

			voffset += layout.height + padding;
		}

		return -1;
	}

	// ----------------------------------------------------------------------
	@Override
	public boolean scrolled( int amount )
	{
		if ( !mouseOverUI )
		{
			Global.TileSize -= amount * 5;
			if ( Global.TileSize < 2 )
			{
				Global.TileSize = 2;
			}
		}

		return false;
	}

	// ----------------------------------------------------------------------
	public void rightClick( float screenX, float screenY )
	{
		if ( Global.CurrentDialogue != null )
		{
			// Global.CurrentDialogue.advance();
		}
		else
		{
			Vector3 mousePos = camera.unproject( new Vector3( screenX, screenY, 0 ) );

			int mousePosX = (int) mousePos.x;
			int mousePosY = (int) mousePos.y;

			int offsetx = Global.Resolution[0] / 2 - Global.CurrentLevel.player.tile[0][0].x * Global.TileSize;
			int offsety = Global.Resolution[1] / 2 - Global.CurrentLevel.player.tile[0][0].y * Global.TileSize;

			int x = ( mousePosX - offsetx ) / Global.TileSize;
			int y = ( mousePosY - offsety ) / Global.TileSize;

			GameTile tile = null;
			if ( x >= 0 && x < Global.CurrentLevel.width && y >= 0 && y < Global.CurrentLevel.height )
			{
				tile = Global.CurrentLevel.getGameTile( x, y );
			}

			createContextMenu( mousePosX, mousePosY, tile );
		}
	}

	// endregion InputProcessor
	// ####################################################################//
	// region GestureListener

	// ----------------------------------------------------------------------
	@Override
	public boolean touchDown( float x, float y, int pointer, int button )
	{
		longPressed = false;
		dragged = false;
		lastZoom = 0;

		startX = x;
		startY = y;

		return false;
	}

	// ----------------------------------------------------------------------
	@Override
	public boolean tap( float x, float y, int count, int button )
	{
		return false;
	}

	// ----------------------------------------------------------------------
	@Override
	public boolean longPress( float x, float y )
	{
		rightClick( x, y );

		longPressed = true;
		return true;
	}

	// ----------------------------------------------------------------------
	@Override
	public boolean fling( float velocityX, float velocityY, int button )
	{
		return false;
	}

	// ----------------------------------------------------------------------
	@Override
	public boolean pan( float x, float y, float deltaX, float deltaY )
	{
		return false;
	}

	// ----------------------------------------------------------------------
	@Override
	public boolean panStop( float x, float y, int pointer, int button )
	{
		return false;
	}

	// ----------------------------------------------------------------------
	@Override
	public boolean zoom( float initialDistance, float distance )
	{
		distance = initialDistance - distance;

		float amount = distance - lastZoom;
		lastZoom = distance;

		Global.TileSize -= amount / 10.0f;
		if ( Global.TileSize < 2 )
		{
			Global.TileSize = 2;
		}

		return false;
	}

	// ----------------------------------------------------------------------
	@Override
	public boolean pinch( Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2 )
	{
		return false;
	}

	// endregion GestureListener
	// ####################################################################//
	// region Public Methods

	// ----------------------------------------------------------------------
	public void prepareAbility( ActiveAbility aa )
	{
		preparedAbility = aa;
		preparedAbility.caster = Global.CurrentLevel.player;
		preparedAbility.source = Global.CurrentLevel.player.tile[0][0];

		if ( abilityTiles != null )
		{
			Pools.freeAll( abilityTiles );
		}
		abilityTiles = preparedAbility.getValidTargets();
	}

	// ----------------------------------------------------------------------
	public void addConsoleMessage( Line line )
	{
		messageStack.addLine( line );
	}

	// ----------------------------------------------------------------------
	public void addAbilityAvailabilityAction( Sprite sprite )
	{
		Table table = new Table();
		table.add( new SpriteWidget( sprite, 32, 32 ) ).size( Global.TileSize / 2 );
		table.addAction( new SequenceAction( Actions.moveTo( Global.Resolution[0] / 2 + Global.TileSize / 2, Global.Resolution[1]
				/ 2
				+ Global.TileSize
				+ Global.TileSize
				/ 2, 1 ), Actions.removeActor() ) );
		table.setPosition( Global.Resolution[0] / 2 + Global.TileSize / 2, Global.Resolution[1] / 2 + Global.TileSize );
		stage.addActor( table );
		table.setVisible( true );
	}

	// ----------------------------------------------------------------------
	public void addActorDamageAction( Entity entity )
	{
		int offsetx = Global.Resolution[0] / 2 - Global.CurrentLevel.player.tile[0][0].x * Global.TileSize;
		int offsety = Global.Resolution[1] / 2 - Global.CurrentLevel.player.tile[0][0].y * Global.TileSize;

		int x = entity.tile[0][0].x;
		int y = entity.tile[0][0].y;

		int cx = x * Global.TileSize + offsetx;
		int cy = y * Global.TileSize + offsety;

		Label label = new Label( "-" + entity.damageAccumulator, skin );
		label.setColor( Color.RED );

		label.addAction( new SequenceAction( Actions.moveTo( cx, cy + Global.TileSize / 2 + Global.TileSize / 2, 0.5f ), Actions.removeActor() ) );
		label.setPosition( cx, cy + Global.TileSize / 2 );
		stage.addActor( label );
		label.setVisible( true );

		entity.damageAccumulator = 0;
	}

	// ----------------------------------------------------------------------
	public void addActorEssenceAction( Entity entity, int essence )
	{
		int offsetx = Global.Resolution[0] / 2 - Global.CurrentLevel.player.tile[0][0].x * Global.TileSize;
		int offsety = Global.Resolution[1] / 2 - Global.CurrentLevel.player.tile[0][0].y * Global.TileSize;

		int x = entity.tile[0][0].x;
		int y = entity.tile[0][0].y;

		int cx = x * Global.TileSize + offsetx;
		int cy = y * Global.TileSize + offsety;

		Label label = new Label( "+" + essence + " essence", skin );
		label.setColor( Color.YELLOW );

		label.addAction( new SequenceAction( Actions.moveTo( cx, cy + Global.TileSize / 2 + Global.TileSize / 2, 0.5f ), Actions.removeActor() ) );
		label.setPosition( cx, cy + Global.TileSize / 2 );
		stage.addActor( label );
		label.setVisible( true );
	}

	// ----------------------------------------------------------------------
	public void addActorHealingAction( Entity entity )
	{
		int offsetx = Global.Resolution[0] / 2 - Global.CurrentLevel.player.tile[0][0].x * Global.TileSize;
		int offsety = Global.Resolution[1] / 2 - Global.CurrentLevel.player.tile[0][0].y * Global.TileSize;

		int x = entity.tile[0][0].x;
		int y = entity.tile[0][0].y;

		int cx = x * Global.TileSize + offsetx;
		int cy = y * Global.TileSize + offsety;

		Label label = new Label( "+" + entity.healingAccumulator, skin );
		label.setColor( Color.GREEN );

		label.addAction( new SequenceAction( Actions.moveTo( cx, cy + Global.TileSize / 2 + Global.TileSize / 2, 0.5f ), Actions.removeActor() ) );
		label.setPosition( cx, cy + Global.TileSize / 2 );
		stage.addActor( label );
		label.setVisible( true );

		entity.healingAccumulator = 0;
	}

	// ----------------------------------------------------------------------
	public void addFullScreenMessage( String message )
	{
		Label label = new Label( message, skin );
		label.setColor( Color.WHITE );

		label.setFontScale( 5 );

		int cx = 50;
		int cy = Global.Resolution[1] - 50;

		label.addAction( new SequenceAction( Actions.moveTo( cx + 25, cy, 2.5f ), Actions.removeActor() ) );
		label.setPosition( cx, cy );
		stage.addActor( label );
		label.setVisible( true );
	}

	// ----------------------------------------------------------------------
	public void clearContextMenu()
	{
		if ( contextMenu != null )
		{
			contextMenu.remove();
			contextMenu = null;
		}
	}

	// endregion Public Methods
	// ####################################################################//
	// region Private Methods

	// ----------------------------------------------------------------------
	private void createContextMenu( int screenX, int screenY, GameTile tile )
	{
		Array<ActiveAbility> available = new Array<ActiveAbility>();

		for ( int i = 0; i < Global.NUM_ABILITY_SLOTS; i++ )
		{
			ActiveAbility aa = Global.abilityPool.slottedActiveAbilities[i];
			if ( aa != null && aa.isAvailable() )
			{
				available.add( aa );
			}
		}

		boolean entityWithinRange = false;
		if ( tile != null && tile.environmentEntity != null )
		{
			entityWithinRange = Math.abs( Global.CurrentLevel.player.tile[0][0].x - tile.x ) <= 1
					&& Math.abs( Global.CurrentLevel.player.tile[0][0].y - tile.y ) <= 1;
		}

		Table table = new Table();

		if ( available.size > 0 || entityWithinRange )
		{
			if ( tile != null && tile.environmentEntity != null )
			{
				final EnvironmentEntity entity = tile.environmentEntity;

				boolean hadAction = false;
				for ( final ActivationAction aa : entity.actions )
				{
					if ( !aa.visible )
					{
						continue;
					}

					Table row = new Table();

					TextButton button = new TextButton( aa.name, skin );
					row.add( button ).expand().fill();

					row.addListener( new InputListener()
					{

						@Override
						public boolean touchDown( InputEvent event, float x, float y, int pointer, int button )
						{
							return true;
						}

						@Override
						public void touchUp( InputEvent event, float x, float y, int pointer, int button )
						{
							clearContextMenu();
							aa.activate( entity );
							Global.CurrentLevel.player.tasks.add( new TaskWait() );
						}
					} );

					table.add( row ).width( Value.percentWidth( 1, table ) );
					table.row();

					hadAction = true;
				}

				if ( hadAction )
				{
					table.add( new Label( "-------------", skin ) );
					table.row();
				}
			}

			boolean hadAbility = false;
			for ( final ActiveAbility aa : available )
			{
				Table row = new Table();

				row.add( new SpriteWidget( aa.Icon, 32, 32 ) );

				TextButton button = new TextButton( aa.getName(), skin );
				row.add( button ).expand().fill();

				row.addListener( new InputListener()
				{
					@Override
					public boolean touchDown( InputEvent event, float x, float y, int pointer, int button )
					{
						return true;
					}

					@Override
					public void touchUp( InputEvent event, float x, float y, int pointer, int button )
					{
						clearContextMenu();
						prepareAbility( aa );
					}
				} );

				table.add( row ).width( Value.percentWidth( 1, table ) );
				table.row();

				hadAbility = true;
			}

			if ( hadAbility )
			{
				table.add( new Label( "-------------", skin ) );
				table.row();
			}
		}

		{
			Table row = new Table();

			TextButton button = new TextButton( "Rest a while", skin );
			row.add( button ).expand().fill();

			row.addListener( new InputListener()
			{
				@Override
				public boolean touchDown( InputEvent event, float x, float y, int pointer, int button )
				{
					return true;
				}

				@Override
				public void touchUp( InputEvent event, float x, float y, int pointer, int button )
				{
					clearContextMenu();
					Global.CurrentLevel.player.AI.setData( "Rest", true );
				}
			} );

			table.add( row ).width( Value.percentWidth( 1, table ) );
			table.row();
		}

		table.pack();

		contextMenu = new Tooltip( table, skin, stage );
		contextMenu.show( screenX - contextMenu.getWidth() / 2, screenY - contextMenu.getHeight() );
	}

	// endregion Private Methods
	// ####################################################################//
	// region Data

	// ----------------------------------------------------------------------
	private long diff, start = System.currentTimeMillis();

	// ----------------------------------------------------------------------
	private float lastZoom;

	// ----------------------------------------------------------------------
	private boolean created;

	// ----------------------------------------------------------------------
	private boolean longPressed;
	private boolean dragged;
	private float startX;
	private float startY;

	// ----------------------------------------------------------------------
	public GestureDetector gestureDetector;

	// ----------------------------------------------------------------------
	public OrthographicCamera camera;

	// ----------------------------------------------------------------------
	public Tooltip contextMenu;

	// ----------------------------------------------------------------------
	private int fps;
	private float storedFrametime;
	private float fpsAccumulator;
	private float frametime;
	private BitmapFont font;
	private final GlyphLayout layout = new GlyphLayout();
	private final Color temp = new Color();

	// ----------------------------------------------------------------------
	public DragDropPayload dragDropPayload;

	// ----------------------------------------------------------------------
	private static final float ScreenShakeSpeed = 0.02f;
	public float screenShakeRadius;
	public float screenShakeAngle;
	private float screenShakeAccumulator;

	// ----------------------------------------------------------------------
	private InventoryPanel inventoryPanel;
	private AbilityPanel abilityPanel;
	private AbilityPoolPanel abilityPoolPanel;
	private HPWidget hpWidget;
	private MessageStack messageStack;
	private TabPanel tabPane;

	private Skin skin;
	private Stage stage;
	private SpriteBatch batch;
	private TextureRegion blank;
	private TextureRegion white;
	public InputMultiplexer inputMultiplexer;
	private Sprite bag;
	private Sprite orb;
	private TextureRegion speechBubbleArrow;
	private NinePatch speechBubbleBackground;
	private Color tempColour = new Color();

	private Tooltip tooltip;

	public boolean mouseOverUI;

	// ----------------------------------------------------------------------
	private Array<RenderSprite> queuedSprites = new Array<RenderSprite>();
	private Array<Entity> hasStatus = new Array<Entity>();
	private Array<Entity> entitiesWithSpeech = new Array<Entity>();

	// ----------------------------------------------------------------------
	private Pool<RenderSprite> renderSpritePool = Pools.get( RenderSprite.class );

	// ----------------------------------------------------------------------
	private Sprite border;
	private int mousePosX;
	private int mousePosY;

	// ----------------------------------------------------------------------
	public static GameScreen Instance;

	// ----------------------------------------------------------------------
	public ActiveAbility preparedAbility;
	private Array<Point> abilityTiles;

	// ----------------------------------------------------------------------
	public enum RenderLayer
	{
		GROUNDTILE, GROUNDFIELD, GROUNDENTITY,

		ITEM, ESSENCE, CURSOR,

		RAISEDTILE, RAISEDENTITY,

		EFFECT, ABILITY,

		OVERHEADENTITY, OVERHEADFIELD, OVERHANG
	}

	// endregion Data
	// ####################################################################//

	public static class RenderSprite implements Comparable<RenderSprite>
	{
		public Sprite sprite;
		public final Color colour = new Color();
		public RenderLayer layer;
		public float x;
		public float y;
		public float width;
		public float height;
		public int index;

		public RenderSprite set( Sprite sprite, Color colour, float x, float y, float width, float height, RenderLayer layer, int index )
		{
			this.sprite = sprite;
			this.colour.set( colour );
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.layer = layer;
			this.index = index;

			return this;
		}

		@Override
		public int compareTo( RenderSprite o )
		{
			int comp = Integer.compare( layer.ordinal(), o.layer.ordinal() );

			if ( comp == 0 )
			{
				comp = Integer.compare( index, o.index );
			}

			if ( comp == 0 )
			{
				comp = Float.compare( o.y, y );
			}

			if ( comp == 0 )
			{
				comp = Float.compare( o.x, x );
			}

			return comp;
		}
	}

	public static class GrayscaleShader
	{
		static String vertexShader = "attribute vec4 a_position;\n"
				+ "attribute vec4 a_color;\n"
				+ "attribute vec2 a_texCoord0;\n"
				+ "\n"
				+ "uniform mat4 u_projTrans;\n"
				+ "\n"
				+ "varying vec4 v_color;\n"
				+ "varying vec2 v_texCoords;\n"
				+ "\n"
				+ "void main() {\n"
				+ "    v_color = a_color;\n"
				+ "    v_texCoords = a_texCoord0;\n"
				+ "    gl_Position = u_projTrans * a_position;\n"
				+ "}";

		static String fragmentShader = "#ifdef GL_ES\n"
				+ "    precision mediump float;\n"
				+ "#endif\n"
				+ "\n"
				+ "varying vec4 v_color;\n"
				+ "varying vec2 v_texCoords;\n"
				+ "uniform sampler2D u_texture;\n"
				+ "\n"
				+ "void main() {\n"
				+ "  vec4 c = v_color * texture2D(u_texture, v_texCoords);\n"
				+ "  float grey = (c.r + c.g + c.b) / 3.0;\n"
				+ "  gl_FragColor = vec4(grey, grey, grey, c.a);\n"
				+ "}";

		public static ShaderProgram Instance = new ShaderProgram( vertexShader, fragmentShader );
	}

}
