package Roguelike.Levels;

import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Ability.IAbility;
import Roguelike.AssetManager;
import Roguelike.DungeonGeneration.DungeonFileParser.DFPRoom;
import Roguelike.Entity.Entity;
import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Entity.GameEntity;
import Roguelike.Entity.Tasks.AbstractTask;
import Roguelike.Fields.Field;
import Roguelike.Fields.Field.FieldLayer;
import Roguelike.GameEvent.GameEventHandler;
import Roguelike.Global;
import Roguelike.Global.Direction;
import Roguelike.Global.Passability;
import Roguelike.Global.Statistic;
import Roguelike.Items.Inventory;
import Roguelike.Items.Item;
import Roguelike.Lights.Light;
import Roguelike.Pathfinding.ShadowCastCache;
import Roguelike.Pathfinding.ShadowCaster;
import Roguelike.RoguelikeGame;
import Roguelike.RoguelikeGame.ScreenEnum;
import Roguelike.Screens.GameScreen;
import Roguelike.Sound.RepeatingSoundEffect;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.SpriteAnimation.BumpAnimation;
import Roguelike.Sprite.SpriteAnimation.MoveAnimation;
import Roguelike.Sprite.SpriteAnimation.MoveAnimation.MoveEquation;
import Roguelike.Sprite.SpriteEffect;
import Roguelike.Tiles.GameTile;
import Roguelike.Tiles.Point;
import Roguelike.Tiles.SeenTile;
import Roguelike.Util.EnumBitflag;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.Iterator;

public class Level
{
	// ####################################################################//
	// region Constructor

	private static final EnumBitflag<Passability> ItemDropPassability = new EnumBitflag<Passability>( Passability.WALK, Passability.ENTITY );

	// endregion Constructor
	// ####################################################################//
	// region Public Methods
	public final ShadowCastCache visibilityData = new ShadowCastCache();
	private final Color tempColour = new Color();

	// endregion Public Methods
	// ####################################################################//
	// region Visibility
	public Array<ActiveAbility> ActiveAbilities = new Array<ActiveAbility>( false, 16 );
	public Array<RepeatingSoundEffect> ambientSounds = new Array<RepeatingSoundEffect>();

	// endregion Visibility
	// ####################################################################//
	// region Lights
	public String bgmName;
	public String fileName;
	public int depth;
	public long seed;
	public Array<DFPRoom> requiredRooms;

	// endregion Lights
	// ####################################################################//
	// region Cleanup
	public String UID;
	public GameEntity player;
	public boolean inTurn = false;

	// endregion Cleanup
	// ####################################################################//
	// region Update
	public boolean isVisionRestricted = true;
	public Color Ambient = new Color( 0.1f, 0.1f, 0.3f, 1.0f );
	public Sprite background;

	// endregion Update
	// ####################################################################//
	// region Process
	public SeenTile[][] SeenGrid;
	public GameTile[][] Grid;
	public int width;
	public int height;

	// endregion Process
	// ####################################################################//
	// region Getters
	public boolean affectedByDayNight = false;
	private Array<GameEntity> visibleList = new Array<GameEntity>( false, 16 );
	private Array<GameEntity> invisibleList = new Array<GameEntity>( false, 16 );
	private Array<Light> lightList = new Array<Light>( false, 16 );
	private Array<EnvironmentEntity> tempEnvironmentEntityList = new Array<EnvironmentEntity>( false, 16 );
	private Array<Field> tempFieldList = new Array<Field>( false, 16 );
	private Array<Light> tempLightList = new Array<Light>( false, 16 );
	private float updateDeltaStep = 0.05f;
	private float updateAccumulator;

	// endregion Getters
	// ####################################################################//
	// region Misc
	private Array<ActiveAbility> NewActiveAbilities = new Array<ActiveAbility>( false, 16 );

	public Level( GameTile[][] grid )
	{
		this.Grid = grid;
		this.width = grid.length;
		this.height = grid[0].length;

		this.SeenGrid = new SeenTile[width][height];
		for ( int x = 0; x < width; x++ )
		{
			for ( int y = 0; y < height; y++ )
			{
				SeenGrid[x][y] = new SeenTile();
				SeenGrid[x][y].gameTile = Grid[x][y];
			}
		}
	}

	public void addActiveAbility( ActiveAbility aa )
	{
		NewActiveAbilities.add( aa );
	}

	public void update( float delta )
	{
		updateAccumulator += delta;

		if ( !hasActiveEffects() && Global.CurrentDialogue == null )
		{
			// Do player move and fill lists
			if ( visibleList.size == 0 && invisibleList.size == 0 && updateAccumulator >= updateDeltaStep && !hasAbilitiesToUpdate() )
			{
				processPlayer();

				updateAccumulator = 0;

				inTurn = true;
			}

			if ( visibleList.size > 0 || invisibleList.size > 0 )
			{
				// process invisible until empty
				processInvisibleList();

				// process visible
				processVisibleList();
			}
			else if ( inTurn )
			{
				// Global.save();

				for ( IAbility a : player.slottedAbilities )
				{
					if ( a != null && a instanceof ActiveAbility )
					{
						ActiveAbility aa = (ActiveAbility) a;

						aa.source = player.tile[0][0];
						aa.hasValidTargets = aa.getValidTargets().size > 0;
					}
				}

				inTurn = false;
			}

			if ( ActiveAbilities.size > 0 )
			{
				Iterator<ActiveAbility> itr = ActiveAbilities.iterator();
				while ( itr.hasNext() )
				{
					ActiveAbility aa = itr.next();
					boolean finished = aa.update();

					if ( finished )
					{
						itr.remove();
					}
				}
			}

			if ( NewActiveAbilities.size > 0 )
			{
				ActiveAbilities.addAll( NewActiveAbilities, 0, NewActiveAbilities.size );
				NewActiveAbilities.clear();
			}
		}

		updateVisibleTiles();
		lightList.clear();

		int playerViewRange = player.getVariable( Statistic.PERCEPTION );
		for ( int x = 0; x < width; x++ )
		{
			for ( int y = 0; y < height; y++ )
			{
				GameTile tile = Grid[x][y];

				if ( tile.visible )
				{
					updateSpritesForTile( tile, delta );
					updateSpriteEffectsForTile( tile, delta );
					updatePopupsForTile( tile, delta );
				}
				else
				{
					clearEffectsForTile( tile );
				}

				cleanUpDeadForTile( tile );

				tile.light.set( tile.ambientColour );
				if ( affectedByDayNight )
				{
					tile.light.mul( Global.DayNightFactor );
					tile.light.a = 1;
				}
				getLightsForTile( tile, lightList, playerViewRange );
			}
		}

		if ( ActiveAbilities.size > 0 )
		{
			for ( ActiveAbility aa : ActiveAbilities )
			{
				aa.getSprite().update( delta );

				if ( aa.light != null )
				{
					for ( GameTile t : aa.AffectedTiles )
					{
						int lx = t.x;
						int ly = t.y;
						if ( checkLightCloseEnough( lx, ly, (int) aa.light.baseIntensity, player.tile[0][0].x, player.tile[0][0].y, playerViewRange ) )
						{
							Light light = aa.light.copy();
							light.lx = lx;
							light.ly = ly;
							light.copied = true;
							lightList.add( light );
						}
					}
				}
			}
		}

		calculateLight( delta, lightList );

		for ( RepeatingSoundEffect sound : ambientSounds )
		{
			sound.update( delta );
		}
	}

	// endregion Misc
	// ####################################################################//
	// region Data

	public void updateVisibleTiles()
	{
		if ( !isVisionRestricted )
		{
			for ( int x = 0; x < width; x++ )
			{
				for ( int y = 0; y < height; y++ )
				{
					Grid[x][y].visible = true;
					SeenGrid[x][y].seen = true;
				}
			}
		}
		else
		{
			for ( int x = 0; x < width; x++ )
			{
				for ( int y = 0; y < height; y++ )
				{
					Grid[x][y].visible = false;
				}
			}

			Array<Point> output = visibilityData.getShadowCast( Grid, player.tile[0][0].x, player.tile[0][0].y, player.getVariable( Statistic.PERCEPTION ), player, true );

			for ( Point tilePos : output )
			{
				if ( tilePos.x >= 0 && tilePos.y >= 0 && tilePos.x < width && tilePos.y < height )
				{
					getGameTile( tilePos ).visible = true;
				}
			}

			updateSeenGrid();
		}
	}

	private void updateSeenGrid()
	{
		for ( int x = 0; x < width; x++ )
		{
			for ( int y = height - 1; y >= 0; y-- )
			{
				GameTile tile = Grid[x][y];
				if ( tile.visible )
				{
					SeenTile s = SeenGrid[x][y];
					s.seen = true;

					GameTile prevTile = getGameTile( x, y + 1 );
					GameTile nextTile = getGameTile( x, y - 1 );
					SeenTile prevSeenTile = getSeenTile( x, y + 1 );

					s.set( tile, player, prevTile, nextTile, prevSeenTile );
				}
			}
		}
	}

	public void calculateAmbient()
	{
		Color acol = new Color( Ambient );
		acol.mul( acol.a );
		acol.a = 1;

		for ( int x = 0; x < width; x++ )
		{
			for ( int y = 0; y < height; y++ )
			{
				GameTile tile = Grid[x][y];
				tile.ambientColour.set( acol );
			}
		}

		// do shadows
		for ( int x = 0; x < width; x++ )
		{
			for ( int y = 0; y < height; y++ )
			{
				GameTile tile = Grid[x][y];
				if ( tile.tileData.shadow != null )
				{
					tile.tileData.shadow.apply( Grid, x, y );
				}
			}
		}
	}

	private void calculateLight( float delta, Array<Light> lights )
	{
		for ( Light l : lights )
		{
			l.update( delta );
			calculateSingleLight( l );
			if ( l.copied )
			{
				Global.LightPool.free( l );
			}
		}
	}

	private void calculateSingleLight( Light l )
	{
		Array<Point> output = l.shadowCastCache.getShadowCast( Grid, (int) l.lx, (int) l.ly, (int) l.baseIntensity + 1, null );

		for ( Point tilePos : output )
		{
			GameTile tile = getGameTile( tilePos );

			float dst = 1 - Vector2.dst2( l.lx, l.ly, tile.x, tile.y ) / ( l.actualIntensity * l.actualIntensity );
			if ( dst < 0 )
			{
				dst = 0;
			}

			tempColour.set( l.colour );
			tempColour.mul( tempColour.a );
			tempColour.a = 1;

			tempColour.mul( dst );

			tile.light.add( tempColour );
		}
	}

	private void getLightsForTile( GameTile tile, Array<Light> output, int viewRange )
	{
		int lx = tile.x;
		int ly = tile.y;

		int px = player.tile[0][0].x;
		int py = player.tile[0][0].y;

		if ( tile.hasFields )
		{
			for ( FieldLayer layer : FieldLayer.values() )
			{
				Field field = tile.fields.get( layer );
				if ( field != null && field.light != null )
				{
					if ( checkLightCloseEnough( lx, ly, (int) field.light.baseIntensity, px, py, viewRange ) )
					{
						field.light.lx = lx;
						field.light.ly = ly;
						output.add( field.light );
					}
				}
			}
		}

		if ( tile.environmentEntity != null && tile.environmentEntity.light != null && tile.environmentEntity.tile[0][0] == tile )
		{
			if ( checkLightCloseEnough( lx, ly, (int) tile.environmentEntity.light.baseIntensity, px, py, viewRange ) )
			{
				tile.environmentEntity.light.lx = lx;
				tile.environmentEntity.light.ly = ly;
				output.add( tile.environmentEntity.light );
			}
		}

		if ( tile.spriteEffects.size > 0 )
		{
			for ( SpriteEffect se : tile.spriteEffects )
			{
				if ( se.light != null )
				{
					if ( checkLightCloseEnough( lx, ly, (int) se.light.baseIntensity, px, py, viewRange ) )
					{
						se.light.lx = lx;
						se.light.ly = ly;
						output.add( se.light );
					}
				}
			}
		}

		if ( tile.entity != null && tile.entity.tile[0][0] == tile )
		{
			tempLightList.clear();
			tile.entity.getLight( tempLightList );
			for ( Light l : tempLightList )
			{
				if ( checkLightCloseEnough( lx, ly, (int) l.baseIntensity + 1, px, py, viewRange ) )
				{
					l.lx = lx;
					l.ly = ly;

					if ( tile.entity.sprite.spriteAnimation != null )
					{
						int[] offset = tile.entity.sprite.spriteAnimation.getRenderOffset();
						l.lx += (float) offset[0] / (float) Global.TileSize;
						l.ly += (float) offset[1] / (float) Global.TileSize;
					}

					output.add( l );
				}
			}
		}
	}

	private boolean checkLightCloseEnough( int lx, int ly, int intensity, int px, int py, int viewRange )
	{
		return Math.max( Math.abs( px - lx ), Math.abs( py - ly ) ) <= viewRange + intensity;

	}

	private void cleanUpDeadForTile( GameTile tile )
	{
		{
			GameEntity e = tile.entity;
			if ( e != null )
			{
				if ( tile.visible )
				{
					if ( e.canTakeDamage && e.hasDamage )
					{
						GameScreen.Instance.addActorDamageAction( e );
						e.hasDamage = false;
					}

					if ( e.canTakeDamage && e.healingAccumulator > 0 )
					{
						GameScreen.Instance.addActorHealingAction( e );
					}
				}
				else
				{
					e.damageAccumulator = 0;
					e.healingAccumulator = 0;
				}

				if ( e.canTakeDamage && e != player && e.HP <= 0 && !hasActiveEffects( e ) )
				{
					if ( e.isBoss )
					{
						// if ( dungeon.isCleared( this ) )
						// {
						// dungeon.isCleared = true;
						// EnvironmentEntity portal =
						// EnvironmentEntity.CreateDungeonExitPortal( dungeon );
						// tile.addEnvironmentEntity( portal );
						//
						// GameScreen.Instance.addFullScreenMessage(
						// "Dungeon Cleared" );
						//
						// for ( String key : dungeon.clearData.keySet() )
						// {
						// Global.GlobalVariables.put( key,
						// dungeon.clearData.get( key ) );
						// }
						// }
					}

					dropItems( e.getInventory(), e.tile[0][0], e.essence, e );
					e.removeFromTile();
				}
				else if ( e == player && e.HP <= 0 && !hasActiveEffects( e ) )
				{
					RoguelikeGame.Instance.switchScreen( ScreenEnum.GAMEOVER );
				}

				if ( e.popupDuration <= 0 && e.popup != null && e.popup.length() == e.displayedPopup.length() && e.popupFade <= 0 )
				{
					e.popupFade = 1;
				}
			}
		}

		{
			EnvironmentEntity e = tile.environmentEntity;
			if ( e != null )
			{
				if ( e.canTakeDamage && e.damageAccumulator > 0 && tile.visible )
				{
					GameScreen.Instance.addActorDamageAction( e );
				}

				if ( e.canTakeDamage && e.HP <= 0 && !hasActiveEffects( e ) )
				{
					dropItems( e.getInventory(), e.tile[0][0], e.essence, e );

					if ( e.onDeathAction != null )
					{
						e.onDeathAction.process( e );
					}

					e.removeFromTile();
				}

				if ( e.popupDuration <= 0 && e.popupFade <= 0 )
				{
					e.popupFade = 1;
				}
			}
		}
	}

	private void dropItems( Inventory inventory, GameTile source, int essence, Object obj )
	{
		Array<Point> possibleTiles = new Array<Point>();
		ShadowCaster sc = new ShadowCaster( Grid, 3, ItemDropPassability, obj );
		sc.ComputeFOV( source.x, source.y, possibleTiles );

		// remove nonpassable tiles
		Iterator<Point> itr = possibleTiles.iterator();
		while ( itr.hasNext() )
		{
			Point pos = itr.next();

			GameTile tile = getGameTile( pos );

			if ( !tile.getPassable( ItemDropPassability, obj ) )
			{
				itr.remove();
				Global.PointPool.free( pos );
			}
		}

		float delay = 0;

		if ( essence > 0 )
		{
			if ( possibleTiles.size > 0 )
			{
				int blockSize = MathUtils.clamp( essence / 10, 10, 100 );

				while ( essence > 0 )
				{
					int block = Math.min( blockSize, essence );
					essence -= block;

					Point target = possibleTiles.random();
					GameTile tile = getGameTile( target );

					tile.essence += block;

					int[] diff = tile.getPosDiff( source );

					Sprite sprite = AssetManager.loadSprite( "orb" );
					MoveAnimation anim = new MoveAnimation( 0.4f, diff, MoveEquation.LEAP );
					anim.leapHeight = 6;
					sprite.spriteAnimation = anim;
					sprite.renderDelay = delay;
					delay += 0.02f;

					float scale = 0.5f + 0.5f * ( MathUtils.clamp( block, 10.0f, 1000.0f ) / 1000.0f );
					sprite.baseScale[0] = scale;
					sprite.baseScale[1] = scale;

					tile.spriteEffects.add( new SpriteEffect( sprite, Direction.CENTER, null ) );
				}
			}
			else
			{
				source.essence += essence;

				int[] diff = new int[] { 0, 0 };

				Sprite sprite = AssetManager.loadSprite( "orb" );
				MoveAnimation anim = new MoveAnimation( 0.4f, diff, MoveEquation.LEAP );
				anim.leapHeight = 6;
				sprite.spriteAnimation = anim;
				sprite.renderDelay = delay;
				delay += 0.02f;

				float scale = 0.5f + 0.5f * ( MathUtils.clamp( essence, 10.0f, 1000.0f ) / 1000.0f );
				sprite.baseScale[0] = scale;
				sprite.baseScale[1] = scale;

				source.spriteEffects.add( new SpriteEffect( sprite, Direction.CENTER, null ) );
			}
		}

		for ( Item i : inventory.m_items )
		{
			if ( i.canDrop && i.shouldDrop() )
			{
				Point target = possibleTiles.random();
				GameTile tile = getGameTile( target );

				tile.items.add( i );

				int[] diff = tile.getPosDiff( source );

				MoveAnimation anim = new MoveAnimation( 0.4f, diff, MoveEquation.LEAP );
				anim.leapHeight = 6;
				i.getIcon().spriteAnimation = anim;
				i.getIcon().renderDelay = delay;
				delay += 0.02f;
			}
		}

		Global.PointPool.freeAll( possibleTiles );
	}

	private void clearEffectsForTile( GameTile tile )
	{
		if ( tile.spriteEffects.size > 0 )
		{
			tile.spriteEffects.clear();
		}

		if ( tile.environmentEntity != null && tile.environmentEntity.sprite != null )
		{
			tile.environmentEntity.sprite.spriteAnimation = null;
		}

		if ( tile.entity != null && tile.entity.sprite != null )
		{
			tile.entity.sprite.spriteAnimation = null;
		}

		if ( tile.items.size > 0 )
		{
			for ( Item i : tile.items )
			{
				i.getIcon().spriteAnimation = null;
			}
		}
	}

	private void updatePopupsForTile( GameTile tile, float delta )
	{
		if ( tile.entity != null && tile.entity.tile[0][0] == tile )
		{
			if ( tile.entity.popup != null )
			{
				if ( tile.entity.displayedPopup.length() < tile.entity.popup.length() )
				{
					tile.entity.popupAccumulator += delta;

					while ( tile.entity.popupAccumulator >= 0 && tile.entity.displayedPopup.length() < tile.entity.popup.length() )
					{
						tile.entity.popupAccumulator -= 0.03f;

						tile.entity.displayedPopup = tile.entity.popup.substring( 0, tile.entity.displayedPopup.length() + 1 );

						char letter = tile.entity.displayedPopup.charAt( tile.entity.displayedPopup.length() - 1 );
					}
				}
			}
		}
	}

	private void updateSpriteEffectsForTile( GameTile tile, float delta )
	{
		if ( tile.spriteEffects.size > 0 )
		{
			Iterator<SpriteEffect> itr = tile.spriteEffects.iterator();
			while ( itr.hasNext() )
			{
				SpriteEffect e = itr.next();
				boolean finished = e.Sprite.update( delta );

				if ( finished )
				{
					itr.remove();
				}
			}
		}
	}

	private void updateSpritesForTile( GameTile tile, float delta )
	{
		for ( Sprite sprite : tile.tileData.sprites )
		{
			sprite.update( delta );
		}

		if ( tile.tileData.raisedSprite != null )
		{
			tile.tileData.raisedSprite.frontSprite.update( delta );
			tile.tileData.raisedSprite.topSprite.update( delta );
		}

		if ( tile.hasFields )
		{
			for ( FieldLayer layer : FieldLayer.values() )
			{
				Field field = tile.fields.get( layer );
				if ( field != null )
				{
					field.sprite.update( delta );
				}
			}
		}

		if ( tile.environmentEntity != null && tile.environmentEntity.tile[0][0] == tile && tile.environmentEntity.sprite != null )
		{
			tile.environmentEntity.sprite.update( delta );
		}

		if ( tile.entity != null && tile.entity.tile[0][0] == tile && tile.entity.sprite != null )
		{
			tile.entity.sprite.update( delta );
		}

		if ( tile.items.size > 0 )
		{
			for ( Item i : tile.items )
			{
				i.getIcon().update( delta );
			}
		}
	}

	private void processPlayer()
	{
		if ( player.tasks.size == 0 )
		{
			player.AI.update( player );
		}

		if ( player.tasks.size > 0 )
		{
			AbstractTask task = player.tasks.removeIndex( 0 );
			for ( GameEventHandler handler : player.getAllHandlers() )
			{
				handler.onTask( player, task );
			}

			if ( !task.cancel )
			{
				task.processTask( player );
			}

			float actionCost = task.cost * player.getActionDelay();

			Global.AUT += actionCost;
			Global.DayNightFactor = (float) ( 0.1f + ( ( ( Math.sin( Global.AUT / 100.0f ) + 1.0f ) / 2.0f ) * 0.9f ) );

			// Global.abilityPool.update( actionCost );

			player.update( actionCost );

			getAllEntitiesToBeProcessed( actionCost );

			tempEnvironmentEntityList.clear();
			getAllEnvironmentEntities( tempEnvironmentEntityList );
			for ( EnvironmentEntity ee : tempEnvironmentEntityList )
			{
				ee.update( actionCost );
			}

			tempFieldList.clear();
			getAllFields( tempFieldList );
			for ( Field f : tempFieldList )
			{
				if ( f.tile == null )
				{
					continue;
				}

				f.update( actionCost );
				if ( f.stacks < 1 )
				{
					f.tile.fields.put( f.layer, null );
					f.tile = null;
				}
			}

			if ( player.tile[0][0].essence > 0 )
			{
				GameScreen.Instance.addActorEssenceAction( player, player.tile[0][0].essence );

				player.essence += player.tile[0][0].essence;
				player.tile[0][0].essence = 0;
			}

			if ( player.tile[0][0].items.size > 0 )
			{
				for ( Item item : player.tile[0][0].items )
				{
					GameScreen.Instance.pickupQueue.add( item );

					//GameScreen.Instance.addActorItemPickupAction( player, item );

					//player.inventory.addItem( item );
				}

				player.tile[0][0].items.clear();
			}

			player.tile[0][0].processFieldEffectsForEntity( player, actionCost );

			// check if enemy visible
			if ( enemyVisible() )
			{
				// Clear pending moves
				player.AI.setData( "Pos", null );
				player.AI.setData( "Rest", null );
			}

			if ( player.sprite.spriteAnimation instanceof BumpAnimation )
			{
				player.AI.setData( "Pos", null );
				player.AI.setData( "Rest", null );
			}
		}
	}

	private void processVisibleList()
	{
		Iterator<GameEntity> itr = visibleList.iterator();
		while ( itr.hasNext() )
		{
			GameEntity e = itr.next();

			if ( e.HP <= 0 )
			{
				itr.remove();
				continue;
			}

			// If entity can take action
			if ( e.actionDelayAccumulator > 0 )
			{
				// If no tasks queued, process the ai
				if ( e.tasks.size == 0 )
				{
					e.AI.update( e );
				}

				// If a task is queued, process it
				if ( e.tasks.size > 0 )
				{
					AbstractTask task = e.tasks.removeIndex( 0 );
					for ( GameEventHandler handler : e.getAllHandlers() )
					{
						handler.onTask( e, task );
					}

					if ( !task.cancel )
					{
						task.processTask( e );
					}

					float actionCost = task.cost * e.getActionDelay();
					e.actionDelayAccumulator -= actionCost * e.getActionDelay();

					e.tile[0][0].processFieldEffectsForEntity( e, actionCost );
				}
				else
				{
					e.actionDelayAccumulator -= e.getActionDelay();
				}
			}

			if ( e.actionDelayAccumulator <= 0 )
			{
				itr.remove();
			}
			else if ( !e.tile[0][0].visible )
			{
				// If entity is now invisible, submit to the invisible list to
				// be processed
				itr.remove();
				invisibleList.add( e );
			}
		}
	}

	private void processInvisibleList()
	{
		while ( invisibleList.size > 0 )
		{
			Iterator<GameEntity> itr = invisibleList.iterator();

			// Repeat full pass through list
			while ( itr.hasNext() )
			{
				GameEntity e = itr.next();

				if ( e.HP <= 0 )
				{
					itr.remove();
					continue;
				}

				// If entity can take action
				if ( e.actionDelayAccumulator > 0 )
				{
					// If no tasks queued, process the ai
					if ( e.tasks.size == 0 )
					{
						e.AI.update( e );
					}

					// If a task is queued, process it
					if ( e.tasks.size > 0 )
					{
						AbstractTask task = e.tasks.removeIndex( 0 );
						for ( GameEventHandler handler : e.getAllHandlers() )
						{
							handler.onTask( e, task );
						}

						if ( !task.cancel )
						{
							task.processTask( e );
						}

						float actionCost = task.cost * e.getActionDelay();
						e.actionDelayAccumulator -= actionCost * e.getActionDelay();

						e.tile[0][0].processFieldEffectsForEntity( e, actionCost );
					}
					else
					{
						e.actionDelayAccumulator -= e.getActionDelay();
					}
				}

				if ( e.actionDelayAccumulator <= 0 )
				{
					itr.remove();
				}
				else if ( e.tile[0][0].visible )
				{
					// If entity is now visible, submit to the visible list to
					// be processed
					itr.remove();
					visibleList.add( e );
				}
			}
		}
	}

	private void getAllEntitiesToBeProcessed( float cost )
	{
		for ( int x = 0; x < width; x++ )
		{
			for ( int y = 0; y < height; y++ )
			{
				boolean visible = Grid[x][y].visible;
				GameEntity e = Grid[x][y].entity;
				if ( e != null && e != player && e.tile[0][0] == Grid[x][y] )
				{
					if ( e.tile[0][0].visible )
					{
						e.seen = true;
					}
					if ( !e.seen )
					{
						continue;
					}

					if ( Math.min( Math.abs( x - player.tile[0][0].x ), Math.abs( y - player.tile[0][0].y ) ) > 25 )
					{
						continue;
					}

					e.update( cost );

					if ( e.actionDelayAccumulator > 0 )
					{
						if ( visible )
						{
							visibleList.add( e );
						}
						else
						{
							invisibleList.add( e );
						}
					}
				}
			}
		}

		for ( ActiveAbility aa : ActiveAbilities )
		{
			aa.updateAccumulators( cost );
		}
	}

	public final GameTile[][] getGrid()
	{
		return Grid;
	}

	public final GameTile getGameTile( Point pos )
	{
		return getGameTile( pos.x, pos.y );
	}

	public final SeenTile getSeenTile( Point pos )
	{
		return getSeenTile( pos.x, pos.y );
	}

	public final SeenTile getSeenTile( int x, int y )
	{
		if ( x < 0 || x >= width || y < 0 || y >= height ) { return null; }

		return SeenGrid[x][y];
	}

	public final Entity getEntityWithUID( String UID )
	{
		for ( int x = 0; x < width; x++ )
		{
			for ( int y = 0; y < height; y++ )
			{
				GameTile tile = getGameTile( x, y );

				if ( tile.environmentEntity != null )
				{
					if ( tile.environmentEntity.UID.equals( UID ) ) { return tile.environmentEntity; }
				}

				if ( tile.entity != null )
				{
					if ( tile.entity.UID.equals( UID ) ) { return tile.entity; }
				}
			}
		}

		return null;
	}

	public final GameTile getGameTile( int x, int y )
	{
		if ( x < 0 || x >= width || y < 0 || y >= height ) { return null; }

		return Grid[ x ][ y ];
	}

	public final void getAllFields( Array<Field> list )
	{
		for ( int x = 0; x < width; x++ )
		{
			for ( int y = 0; y < height; y++ )
			{
				if ( Grid[x][y].hasFields )
				{
					for ( FieldLayer layer : FieldLayer.values() )
					{
						Field field = Grid[x][y].fields.get( layer );
						if ( field != null )
						{
							list.add( field );
						}
					}
				}
			}
		}
	}

	public final void getAllEntities( Array<GameEntity> list )
	{
		for ( int x = 0; x < width; x++ )
		{
			for ( int y = 0; y < height; y++ )
			{
				if ( Grid[x][y].entity != null && Grid[x][y].entity.tile[0][0] == Grid[x][y] )
				{
					list.add( Grid[x][y].entity );
				}
			}
		}
	}

	public final void getAllEnvironmentEntities( Array<EnvironmentEntity> list )
	{
		for ( int x = 0; x < width; x++ )
		{
			for ( int y = 0; y < height; y++ )
			{
				if ( Grid[x][y].environmentEntity != null && Grid[x][y].environmentEntity.tile[0][0] == Grid[x][y] )
				{
					list.add( Grid[x][y].environmentEntity );
				}
			}
		}
	}

	private boolean hasAbilitiesToUpdate()
	{
		if ( ActiveAbilities.size > 0 )
		{
			for ( ActiveAbility aa : ActiveAbilities )
			{
				if ( aa.needsUpdate() ) { return true; }
			}
		}

		return false;
	}

	private boolean enemyVisible()
	{
		boolean enemy = false;

		for ( int x = 0; x < width; x++ )
		{
			for ( int y = 0; y < height; y++ )
			{
				if ( Grid[x][y].visible && Grid[x][y].entity != null )
				{
					if ( !Grid[x][y].entity.isAllies( player ) )
					{
						enemy = true;
						break;
					}
				}
			}
			if ( enemy )
			{
				break;
			}
		}

		return enemy;
	}

	private boolean hasActiveEffects()
	{
		boolean activeEffects = false;

		for ( int x = 0; x < width; x++ )
		{
			for ( int y = 0; y < height; y++ )
			{
				if ( !Grid[x][y].visible )
				{
					continue;
				}

				if ( Grid[x][y].spriteEffects.size > 0 )
				{
					activeEffects = true;
					break;

				}

				{
					Entity e = Grid[x][y].entity;
					if ( e != null )
					{
						boolean active = hasActiveEffects( e );
						if ( active )
						{
							activeEffects = true;
							break;
						}
					}
				}

				{
					Entity e = Grid[x][y].environmentEntity;
					if ( e != null )
					{
						boolean active = hasActiveEffects( e );
						if ( active )
						{
							activeEffects = true;
							break;
						}
					}
				}
			}

			if ( activeEffects )
			{
				break;
			}
		}

		return activeEffects;
	}

	private boolean hasActiveEffects( Entity e )
	{
		if ( e.tile[0][0].spriteEffects.size > 0 ) { return true; }

		boolean activeEffects = false;

		if ( e.sprite != null && e.sprite.spriteAnimation != null )
		{
			activeEffects = true;
		}

		return activeEffects;
	}

	// endregion Data
	// ####################################################################//
}
