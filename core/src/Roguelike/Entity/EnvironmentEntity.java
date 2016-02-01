package Roguelike.Entity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import Roguelike.AssetManager;
import Roguelike.Entity.ActivationAction.*;
import Roguelike.Global;
import Roguelike.Global.Direction;
import Roguelike.Global.Passability;
import Roguelike.Global.Statistic;
import Roguelike.Items.TreasureGenerator;
import Roguelike.RoguelikeGame;
import Roguelike.RoguelikeGame.ScreenEnum;
import Roguelike.DungeonGeneration.DungeonFileParser;
import Roguelike.DungeonGeneration.DungeonFileParser.DFPRoom;
import Roguelike.GameEvent.GameEventHandler;
import Roguelike.Levels.Level;
import Roguelike.Save.SaveLevel;
import Roguelike.Screens.LoadingScreen;
import Roguelike.Sprite.TilingSprite;
import Roguelike.Sprite.Sprite;
import Roguelike.StatusEffect.StatusEffect;
import Roguelike.Tiles.GameTile;
import Roguelike.Tiles.Point;
import Roguelike.Util.EnumBitflag;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public class EnvironmentEntity extends Entity
{
	public boolean attachToWall = false;
	public boolean overHead = false;

	public EnumBitflag<Passability> passableBy;

	public Array<ActivationActionGroup> onActivateActions = new Array<ActivationActionGroup>(  );
	public Array<ActivationActionGroup> onTurnActions = new Array<ActivationActionGroup>(  );
	public Array<ActivationActionGroup> onHearActions = new Array<ActivationActionGroup>(  );
	public Array<ActivationActionGroup> onDeathActions = new Array<ActivationActionGroup>(  );
	public Array<ActivationActionGroup> noneActions = new Array<ActivationActionGroup>(  );

	// ----------------------------------------------------------------------
	@Override
	public void update( float cost )
	{
		for (ActivationActionGroup group : onTurnActions)
		{
			if (group.enabled)
			{
				group.activate( this, cost );
			}
		}

		for ( GameEventHandler h : getAllHandlers() )
		{
			h.onTurn( this, 1 );
		}

		processStatuses();

		if ( popupDuration > 0 )
		{
			popupDuration -= 1;
		}
	}

	// ----------------------------------------------------------------------
	@Override
	public int getStatistic( Statistic stat )
	{
		int val = statistics.get( stat );

		HashMap<String, Integer> variableMap = getBaseVariableMap();

		for ( StatusEffect se : statusEffects )
		{
			val += se.getStatistic( variableMap, stat );
		}

		return val;
	}

	// ----------------------------------------------------------------------
	@Override
	protected void internalLoad( String file )
	{

	}

	// ----------------------------------------------------------------------
	@Override
	public void removeFromTile()
	{
		for ( int x = 0; x < size; x++ )
		{
			for ( int y = 0; y < size; y++ )
			{
				if ( tile[x][y] != null )
				{
					tile[x][y].environmentEntity = null;
					tile[x][y] = null;
				}
			}
		}
	}

	// ----------------------------------------------------------------------
	@Override
	public Array<GameEventHandler> getAllHandlers()
	{
		Array<GameEventHandler> handlers = new Array<GameEventHandler>();

		for ( StatusEffect se : statusEffects )
		{
			handlers.add( se );
		}

		return handlers;
	}

	// ----------------------------------------------------------------------
	private static EnvironmentEntity CreateTransition( final Element data )
	{
		ActivationActionGroup group = new ActivationActionGroup("Go Onward");
		group.actions.add( new ActivationActionChangeLevel( data.get( "Destination" ) ) );

		Sprite stairs = null;

		Element spriteElement = data.getChildByName( "Sprite" );
		if ( spriteElement != null )
		{
			stairs = AssetManager.loadSprite( spriteElement );
		}
		else
		{
			stairs = AssetManager.loadSprite( "Oryx/uf_split/uf_terrain/floor_set_grey_9" );
		}

		final EnvironmentEntity entity = new EnvironmentEntity();
		entity.size = data.getInt( "Size", 1 );

		entity.passableBy = Passability.parse( data.get( "Passable", "true" ) );

		if ( data.get( "Opaque", null ) != null )
		{
			boolean opaque = data.getBoolean( "Opaque", false );

			if ( opaque )
			{
				entity.passableBy.clearBit( Passability.LIGHT );
			}
			else
			{
				entity.passableBy.setBit( Passability.LIGHT );
			}
		}

		entity.sprite = stairs;
		entity.canTakeDamage = false;
		entity.onActivateActions.add( group );
		entity.tile = new GameTile[entity.size][entity.size];
		stairs.size[0] = entity.size;
		stairs.size[1] = entity.size;

		return entity;
	}

	// ----------------------------------------------------------------------
	private static EnvironmentEntity CreateDoor( final Element xml )
	{
		Sprite doorHClosed = AssetManager.loadSprite( "Oryx/Custom/terrain/door_wood_h_closed", true );
		Sprite doorHOpen = AssetManager.loadSprite( "Oryx/Custom/terrain/door_wood_h_open", true );

		Sprite doorVClosed = AssetManager.loadSprite( "Oryx/Custom/terrain/door_wood_v_closed", true );
		Sprite doorVOpen = AssetManager.loadSprite( "Oryx/Custom/terrain/door_wood_v_open", true );

		Element hClosedElement = xml.getChildByName( "HClosed" );
		if (hClosedElement != null)
		{
			doorHClosed = AssetManager.loadSprite( hClosedElement );
		}

		Element hOpenElement = xml.getChildByName( "HOpen" );
		if (hOpenElement != null)
		{
			doorHOpen = AssetManager.loadSprite( hOpenElement );
		}

		Element vClosedElement = xml.getChildByName( "VClosed" );
		if (vClosedElement != null)
		{
			doorVClosed = AssetManager.loadSprite( vClosedElement );
		}

		Element vOpenElement = xml.getChildByName( "VOpen" );
		if (vOpenElement != null)
		{
			doorVOpen = AssetManager.loadSprite( vOpenElement );
		}

		final TilingSprite closedSprite = new TilingSprite(doorVClosed, doorHClosed);
		closedSprite.checkID = "wall".hashCode();

		final TilingSprite openSprite = new TilingSprite(doorVOpen, doorHOpen);
		openSprite.checkID = "wall".hashCode();

		EnvironmentEntity entity = new EnvironmentEntity();
		entity.passableBy = Passability.parse( "false" );
		entity.passableBy.clearBit( Passability.LIGHT );
		entity.tilingSprite = closedSprite;
		entity.canTakeDamage = false;

		String lockedBy = xml.get( "LockedBy", null );
		if (lockedBy != null)
		{
			ActivationActionGroup unlock = new ActivationActionGroup();
			unlock.name = "Unlock";
			unlock.enabled = true;
			unlock.conditions.add( new ActivationConditionHasItem( lockedBy, 1 ) );
			unlock.actions.add( new ActivationActionRemoveItem( lockedBy, 1 ) );
			unlock.actions.add( new ActivationActionSetEnabled( null, "Open", true ) );
			unlock.actions.add( new ActivationActionSetEnabled( null, "Unlock", false ) );

			entity.onActivateActions.add( unlock );

			ActivationActionGroup open = new ActivationActionGroup();
			open.name = "Open";
			open.enabled = false;
			open.actions.add( new ActivationActionSetSprite( null, openSprite ) );
			open.actions.add( new ActivationActionSetPassable( Passability.parse( "true" ) ) );
			open.actions.add( new ActivationActionSetEnabled( null, "Open", false ) );

			entity.onActivateActions.add( open );
		}
		else
		{
			ActivationActionGroup open = new ActivationActionGroup();
			open.name = "Open";
			open.enabled = true;
			open.actions.add( new ActivationActionSetSprite( null, openSprite ) );
			open.actions.add( new ActivationActionSetPassable( Passability.parse( "true" ) ) );
			open.actions.add( new ActivationActionSetEnabled( null, "Open", false ) );

			entity.onActivateActions.add( open );
		}

		return entity;
	}

	// ----------------------------------------------------------------------
	private static EnvironmentEntity CreateBasic( Element xml )
	{
		EnvironmentEntity entity = new EnvironmentEntity();

		entity.passableBy = Passability.parse( xml.get( "Passable", "false" ) );

		if ( xml.get( "Opaque", null ) != null )
		{
			boolean opaque = xml.getBoolean( "Opaque", false );

			if ( opaque )
			{
				entity.passableBy.clearBit( Passability.LIGHT );
			}
			else
			{
				entity.passableBy.setBit( Passability.LIGHT );
			}
		}

		entity.attachToWall = xml.getBoolean( "AttachToWall", false );
		entity.overHead = xml.getBoolean( "Overhead", false );
		entity.canTakeDamage = xml.getChildByName( "Statistics" ) != null;

		entity.baseInternalLoad( xml );

		loadActions(xml.getChildByName( "OnTurn" ), entity.onTurnActions);
		loadActions(xml.getChildByName( "OnActivate" ), entity.onActivateActions);
		loadActions(xml.getChildByName( "OnHear" ), entity.onHearActions);
		loadActions(xml.getChildByName( "OnDeath" ), entity.onDeathActions);
		loadActions( xml.getChildByName( "Actions" ), entity.noneActions );

		return entity;
	}

	private static void loadActions(Element xml, Array<ActivationActionGroup> actionList)
	{
		if (xml != null)
		{
			for (int i = 0; i < xml.getChildCount(); i++)
			{
				Element el = xml.getChild( i );
				ActivationActionGroup group = new ActivationActionGroup(  );
				group.parse( el );

				actionList.add( group );
			}
		}
	}

	// ----------------------------------------------------------------------
	public static EnvironmentEntity load( Element xml )
	{
		EnvironmentEntity entity = null;

		String type = xml.get( "Type", "" );

		if ( type.equalsIgnoreCase( "Door" ) )
		{
			entity = CreateDoor( xml );
		}
		else if ( type.equalsIgnoreCase( "Transition" ) )
		{
			entity = CreateTransition( xml );
		}
		else
		{
			entity = CreateBasic( xml );
		}
		entity.tile = new GameTile[entity.size][entity.size];
		return entity;
	}
}
