package Roguelike.Entity;

import Roguelike.Ability.AbilityTree;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Ability.IAbility;
import Roguelike.Ability.PassiveAbility.PassiveAbility;
import Roguelike.AssetManager;
import Roguelike.Dialogue.DialogueManager;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree;
import Roguelike.Entity.Tasks.AbstractTask;
import Roguelike.GameEvent.GameEventHandler;
import Roguelike.Global;
import Roguelike.Global.Direction;
import Roguelike.Global.Passability;
import Roguelike.Global.Statistic;
import Roguelike.Items.Item;
import Roguelike.Items.Item.EquipmentSlot;
import Roguelike.Lights.Light;
import Roguelike.Sprite.Sprite;
import Roguelike.StatusEffect.StatusEffect;
import Roguelike.Util.EnumBitflag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class GameEntity extends Entity
{
	// ####################################################################//
	// region Constructor

	// ----------------------------------------------------------------------
	private GameEntity()
	{
	}

	// endregion Constructor
	// ####################################################################//
	// region Public Methods

	// ----------------------------------------------------------------------
	public void attack( Entity other, Direction dir )
	{
		Global.calculateDamage( this, other, getVariable( Statistic.ATTACK ), other.getVariable( Statistic.DEFENSE ), true );
	}

	// ----------------------------------------------------------------------
	@Override
	public void update( float cost )
	{
		if ( inventory.isVariableMapDirty )
		{
			isVariableMapDirty = true;
			inventory.isVariableMapDirty = false;
		}

		actionDelayAccumulator += cost;

		for ( AbilityTree a : slottedAbilities )
		{
			a.current.current.onTurn();
		}

		for ( GameEventHandler h : getAllHandlers() )
		{
			h.onTurn( this, cost );
		}

		Iterator<StatusEffect> itr = statusEffects.iterator();
		while ( itr.hasNext() )
		{
			StatusEffect se = itr.next();

			if ( se.duration <= 0 )
			{
				itr.remove();
			}
		}

		stacks = stackStatusEffects();

		if ( popupDuration > 0 )
		{
			popupDuration -= cost;
		}

		if ( dialogue != null && dialogue.exclamationManager != null )
		{
			dialogue.exclamationManager.update( cost );
		}
	}

	// ----------------------------------------------------------------------
	@Override
	public int getStatistic( Statistic stat )
	{
		int val = statistics.get( stat ) + inventory.getStatistic( Statistic.emptyMap, stat );

		HashMap<String, Integer> variableMap = getBaseVariableMap();

		variableMap.put( stat.toString().toLowerCase(), val );

		for ( AbilityTree a : slottedAbilities )
		{
			if ( a != null && a.current.current instanceof PassiveAbility )
			{
				PassiveAbility passive = (PassiveAbility) a.current.current;
				val += passive.getStatistic( variableMap, stat );
			}
		}

		for ( StatusEffect se : statusEffects )
		{
			val += se.getStatistic( variableMap, stat );
		}

		return val;
	}

	// ----------------------------------------------------------------------
	@Override
	protected void internalLoad( String entity )
	{
		XmlReader xml = new XmlReader();
		Element xmlElement = null;

		try
		{
			xmlElement = xml.parse( Gdx.files.internal( "Entities/" + entity + ".xml" ) );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}

		String extendsElement = xmlElement.getAttribute( "Extends", null );
		if ( extendsElement != null )
		{
			internalLoad( extendsElement );
		}

		super.baseInternalLoad( xmlElement );

		defaultHitEffect = xmlElement.getChildByName( "HitEffect" ) != null ? AssetManager.loadSprite( xmlElement.getChildByName( "HitEffect" ) ) : defaultHitEffect;
		AI = xmlElement.getChildByName( "AI" ) != null ? BehaviourTree.load( Gdx.files.internal( "AI/" + xmlElement.get( "AI" ) + ".xml" ) ) : AI;
		canSwap = xmlElement.getBoolean( "CanSwap", false );
		canMove = xmlElement.getBoolean( "CanMove", true );

		Element factionElement = xmlElement.getChildByName( "Factions" );
		if ( factionElement != null )
		{
			for ( Element faction : factionElement.getChildrenByName( "Faction" ) )
			{
				factions.add( faction.getText() );
			}
		}

		Element abilitiesElement = xmlElement.getChildByName( "Abilities" );
		if ( abilitiesElement != null )
		{
			for ( int i = 0; i < abilitiesElement.getChildCount(); i++ )
			{
				Element abilityElement = abilitiesElement.getChild( i );

				IAbility ability = null;
				if ( abilityElement.getName() == "Active" )
				{
					ability = ActiveAbility.load( abilityElement );
				}
				else
				{
					ability = PassiveAbility.load( abilityElement );
				}

				slottedAbilities.add( new AbilityTree( ability ) );
			}
		}

		String dialoguePath = xmlElement.get( "Dialogue", null );
		if ( dialoguePath != null )
		{
			dialogue = DialogueManager.load( dialoguePath, this );
		}

		essence = xmlElement.getInt( "Essence", MathUtils.random( HP ) );
	}

	// ----------------------------------------------------------------------
	@Override
	public Array<GameEventHandler> getAllHandlers()
	{
		Array<GameEventHandler> handlers = new Array<GameEventHandler>();

		for ( AbilityTree a : slottedAbilities )
		{
			if ( a != null && a.current.current instanceof PassiveAbility )
			{
				handlers.add( (PassiveAbility) a.current.current );
			}
		}

		for ( StatusEffect se : statusEffects )
		{
			handlers.add( se );
		}

		for ( EquipmentSlot slot : EquipmentSlot.values() )
		{
			Item i = inventory.getEquip( slot );
			if ( i != null )
			{
				handlers.add( i );
			}
		}

		return handlers;
	}

	// ----------------------------------------------------------------------
	@Override
	public void applyDamage( int dam, Entity damager )
	{
		super.applyDamage( dam, damager );

		AI.setData( "EnemyPos", Global.PointPool.obtain().set( damager.tile[ 0 ][ 0 ].x, damager.tile[ 0 ][ 0 ].y ) );
	}

	// ----------------------------------------------------------------------
	@Override
	public void removeFromTile()
	{
		for ( int x = 0; x < size; x++ )
		{
			for ( int y = 0; y < size; y++ )
			{
				if ( tile[ x ][ y ] != null && tile[ x ][ y ].entity == this )
				{
					tile[ x ][ y ].entity = null;
				}

				tile[ x ][ y ] = null;
			}
		}
	}

	// ----------------------------------------------------------------------
	public boolean isAllies( GameEntity other )
	{
		for ( String faction : factions )
		{
			if ( other.factions.contains( faction ) ) { return true; }
		}

		return false;
	}

	// ----------------------------------------------------------------------
	public boolean isAllies( HashSet<String> other )
	{
		if ( other == null ) { return false; }

		for ( String faction : factions )
		{
			if ( other.contains( faction ) ) { return true; }
		}

		return false;
	}

	// ----------------------------------------------------------------------
	public float getActionDelay()
	{
		float speed = getStatistic( Statistic.SPEED ) / 10.0f;

		if (speed == 0)
		{
			speed = 0.1f;
		}

		return 1.0f / speed;
	}

	// ----------------------------------------------------------------------
	public void getLight( Array<Light> output )
	{
		if ( light != null )
		{
			output.add( light );
		}

//		for ( EquipmentSlot slot : EquipmentSlot.values() )
//		{
//			Item i = inventory.getEquip( slot );
//
//			if ( i != null && i.light != null )
//			{
//				output.add( i.light );
//			}
//		}
	}

	// ----------------------------------------------------------------------
	public static GameEntity load( String name )
	{
		GameEntity e = new GameEntity();
		e.fileName = name;

		e.internalLoad( name );

		e.HP = e.getStatistic( Statistic.CONSTITUTION ) * 10;

		e.statistics.put( Statistic.WALK, 1 );

		e.isVariableMapDirty = true;
		e.recalculateMaps();

		return e;
	}

	// ----------------------------------------------------------------------
	public EnumBitflag<Passability> getTravelType()
	{
		recalculateMaps();
		return travelType;
	}

	// endregion Public Methods
	// ####################################################################//
	// region Data

	// ----------------------------------------------------------------------
	public boolean seen = false;
	public String fileName;

	// ----------------------------------------------------------------------
	public Array<AbilityTree> slottedAbilities = new Array<AbilityTree>();

	// ----------------------------------------------------------------------
	public Sprite defaultHitEffect = AssetManager.loadSprite( "strike/strike", 0.1f );

	// ----------------------------------------------------------------------
	public Array<AbstractTask> tasks = new Array<AbstractTask>();
	public float actionDelayAccumulator;
	public boolean canSwap;
	public boolean canMove;

	// ----------------------------------------------------------------------
	public DialogueManager dialogue;

	// ----------------------------------------------------------------------
	public HashSet<String> factions = new HashSet<String>();
	public BehaviourTree AI;

	// endregion Data
	// ####################################################################//
}
