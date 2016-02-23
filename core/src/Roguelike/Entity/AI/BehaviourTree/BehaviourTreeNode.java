package Roguelike.Entity.AI.BehaviourTree;

import java.util.HashMap;

import Roguelike.Entity.AI.BehaviourTree.Actions.*;
import Roguelike.Entity.GameEntity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;
import Roguelike.Entity.AI.BehaviourTree.Conditionals.ConditionalCheckValue;
import Roguelike.Entity.AI.BehaviourTree.Decorators.DecoratorDataScope;
import Roguelike.Entity.AI.BehaviourTree.Decorators.DecoratorImport;
import Roguelike.Entity.AI.BehaviourTree.Decorators.DecoratorInvert;
import Roguelike.Entity.AI.BehaviourTree.Decorators.DecoratorRepeat;
import Roguelike.Entity.AI.BehaviourTree.Decorators.DecoratorSetState;
import Roguelike.Entity.AI.BehaviourTree.Selectors.SelectorAny;
import Roguelike.Entity.AI.BehaviourTree.Selectors.SelectorPriority;
import Roguelike.Entity.AI.BehaviourTree.Selectors.SelectorRandom;
import Roguelike.Entity.AI.BehaviourTree.Selectors.SelectorSequence;
import Roguelike.Entity.AI.BehaviourTree.Selectors.SelectorUntil;

import com.badlogic.gdx.utils.XmlReader.Element;

public abstract class BehaviourTreeNode
{
	// ####################################################################//
	// region Public Methods

	// ----------------------------------------------------------------------
	public void setData( String key, Object value )
	{
		Parent.setData( key, value );
	}

	// ----------------------------------------------------------------------
	public Object getData( String key, Object fallback )
	{
		Object o = Data.get( key );
		return o != null ? o : fallback;
	}

	// ----------------------------------------------------------------------
	public abstract BehaviourTreeState evaluate( GameEntity entity );

	// ----------------------------------------------------------------------
	public abstract void cancel();

	// ----------------------------------------------------------------------
	public abstract void parse( Element xmlElement );

	// endregion Public Methods
	// ####################################################################//
	// region Data

	// ----------------------------------------------------------------------
	protected static HashMap<String, Class> ClassMap = new HashMap<String, Class>();

	// ----------------------------------------------------------------------
	static
	{
		// Selectors
		ClassMap.put( "PRIORITY", SelectorPriority.class );
		ClassMap.put( "ANY", SelectorAny.class );
		ClassMap.put( "SEQUENCE", SelectorSequence.class );
		ClassMap.put( "RANDOM", SelectorRandom.class );
		ClassMap.put( "UNTIL", SelectorUntil.class );

		// Decorators
		ClassMap.put( "DATASCOPE", DecoratorDataScope.class );
		ClassMap.put( "IMPORT", DecoratorImport.class );
		ClassMap.put( "INVERT", DecoratorInvert.class );
		ClassMap.put( "REPEAT", DecoratorRepeat.class );
		ClassMap.put( "SETSTATE", DecoratorSetState.class );

		// Conditionals
		ClassMap.put( "CHECKVALUE", ConditionalCheckValue.class );

		// Actions
		ClassMap.put( "ATTACK", ActionAttack.class );
		ClassMap.put( "CLEARVALUE", ActionClearValue.class );
		ClassMap.put( "CONVERTTO", ActionConvertTo.class );
		ClassMap.put( "EXCLAIM", ActionExclaim.class );
		ClassMap.put( "GETALLVISIBLE", ActionGetAllVisible.class );
		ClassMap.put( "GETALLABILITIES", ActionGetAllAbilities.class );
		ClassMap.put( "CATEGORISEABILITIES", ActionCategoriseAbilities.class );
		ClassMap.put( "MOVETO", ActionMoveTo.class );
		ClassMap.put( "MOVETOATTACK", ActionMoveToAttack.class );
		ClassMap.put( "PICKCLOSEST", ActionPickClosest.class );
		ClassMap.put( "PICKPLAYER", ActionPickPlayer.class );
		ClassMap.put( "PICKRANDOM", ActionPickRandom.class );
		ClassMap.put( "PROCESSINPUT", ActionProcessInput.class );
		ClassMap.put( "REST", ActionRest.class );
		ClassMap.put( "SETVALUE", ActionSetValue.class );
		ClassMap.put( "USEABILITY", ActionUseAbility.class );
		ClassMap.put( "WAIT", ActionWait.class );
	}

	// ----------------------------------------------------------------------
	public BehaviourTreeContainer Parent;

	// ----------------------------------------------------------------------
	protected BehaviourTreeState State;

	// ----------------------------------------------------------------------
	public HashMap<String, Object> Data;

	// endregion Data
	// ####################################################################//
}