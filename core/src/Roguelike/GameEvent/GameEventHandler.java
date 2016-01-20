package Roguelike.GameEvent;

import java.util.HashMap;

import Roguelike.GameEvent.OnExpire.AbstractOnExpireEvent;
import Roguelike.Global;
import Roguelike.Global.Statistic;
import Roguelike.Entity.Entity;
import Roguelike.Entity.Tasks.AbstractTask;
import Roguelike.Entity.Tasks.TaskAttack;
import Roguelike.Entity.Tasks.TaskMove;
import Roguelike.Entity.Tasks.TaskUseAbility;
import Roguelike.Entity.Tasks.TaskWait;
import Roguelike.GameEvent.Constant.ConstantEvent;
import Roguelike.GameEvent.Damage.AbstractOnDamageEvent;
import Roguelike.GameEvent.Damage.DamageObject;
import Roguelike.GameEvent.OnDeath.AbstractOnDeathEvent;
import Roguelike.GameEvent.OnTask.AbstractOnTaskEvent;
import Roguelike.GameEvent.OnTurn.AbstractOnTurnEvent;
import Roguelike.Util.FastEnumMap;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public abstract class GameEventHandler implements IGameObject
{
	public ConstantEvent constantEvent;
	public Array<AbstractOnTurnEvent> onTurnEvents = new Array<AbstractOnTurnEvent>();
	public Array<AbstractOnDamageEvent> onDealDamageEvents = new Array<AbstractOnDamageEvent>();
	public Array<AbstractOnDamageEvent> onReceiveDamageEvents = new Array<AbstractOnDamageEvent>();
	public Array<AbstractOnTaskEvent> onTaskEvents = new Array<AbstractOnTaskEvent>();
	public Array<AbstractOnTaskEvent> onMoveEvents = new Array<AbstractOnTaskEvent>();
	public Array<AbstractOnTaskEvent> onAttackEvents = new Array<AbstractOnTaskEvent>();
	public Array<AbstractOnTaskEvent> onWaitEvents = new Array<AbstractOnTaskEvent>();
	public Array<AbstractOnTaskEvent> onUseAbilityEvents = new Array<AbstractOnTaskEvent>();
	public Array<AbstractOnDeathEvent> onDeathEvents = new Array<AbstractOnDeathEvent>();
	public Array<AbstractOnExpireEvent> onExpireEvents = new Array<AbstractOnExpireEvent>();

	public Array<Object[]> extraData = new Array<Object[]>();

	// ----------------------------------------------------------------------
	protected void appendExtraVariables(HashMap<String, Integer> variableMap)
	{
		for (Object[] data : extraData)
		{
			variableMap.put( (String)data[0], (Integer)data[1] );
		}
	}

	// ----------------------------------------------------------------------
	public int getStatistic( HashMap<String, Integer> variableMap, Statistic s )
	{
		appendExtraVariables( variableMap );

		int val = 0;

		if ( constantEvent != null )
		{
			val += constantEvent.getStatistic( variableMap, s );
		}

		return val;
	}

	// ----------------------------------------------------------------------
	public FastEnumMap<Statistic, Integer> getStatistics( HashMap<String, Integer> variableMap )
	{
		FastEnumMap<Statistic, Integer> newMap = new FastEnumMap<Statistic, Integer>( Statistic.class );

		for ( Statistic stat : Statistic.values() )
		{
			newMap.put( stat, getStatistic( variableMap, stat ) );
		}

		return newMap;
	}

	// ----------------------------------------------------------------------
	public FastEnumMap<Statistic, String> getStatisticsObject()
	{
		return constantEvent.equations;
	}

	// ----------------------------------------------------------------------
	public void onDeath( Entity entity, Entity killer )
	{
		boolean successfulProcess = false;
		for ( AbstractOnDeathEvent event : onDeathEvents )
		{
			appendExtraVariables( entity.getVariableMap() );
			boolean success = event.handle( entity, killer );

			if ( success )
			{
				successfulProcess = true;
			}
		}

		if ( successfulProcess )
		{
			processed();
		}
	}

	// ----------------------------------------------------------------------
	public void onTurn( Entity entity, float cost )
	{
		boolean successfulProcess = false;
		for ( AbstractOnTurnEvent event : onTurnEvents )
		{
			appendExtraVariables( entity.getVariableMap() );
			boolean success = event.handle( entity, cost );

			if ( success )
			{
				successfulProcess = true;
			}
		}

		if ( successfulProcess )
		{
			processed();
		}
	}

	// ----------------------------------------------------------------------
	public void onDealDamage( Entity entity, DamageObject obj )
	{
		boolean successfulProcess = false;
		for ( AbstractOnDamageEvent event : onDealDamageEvents )
		{
			appendExtraVariables( entity.getVariableMap() );
			boolean success = event.handle( entity, obj, this );

			if ( success )
			{
				successfulProcess = true;
			}
		}

		if ( successfulProcess )
		{
			processed();
		}
	}

	// ----------------------------------------------------------------------
	public void onReceiveDamage( Entity entity, DamageObject obj )
	{
		boolean successfulProcess = false;
		for ( AbstractOnDamageEvent event : onReceiveDamageEvents )
		{
			appendExtraVariables( entity.getVariableMap() );
			boolean success = event.handle( entity, obj, this );

			if ( success )
			{
				successfulProcess = true;
			}
		}

		if ( successfulProcess )
		{
			processed();
		}
	}

	// ----------------------------------------------------------------------
	public void onTask( Entity entity, AbstractTask task )
	{
		if ( task instanceof TaskMove )
		{
			onMove( entity, (TaskMove) task );
		}
		else if ( task instanceof TaskAttack )
		{
			onAttack( entity, (TaskAttack) task );
		}
		else if ( task instanceof TaskWait )
		{
			onWait( entity, (TaskWait) task );
		}
		else if ( task instanceof TaskUseAbility )
		{
			onUseAbility( entity, (TaskUseAbility) task );
		}

		boolean successfulProcess = false;
		for ( AbstractOnTaskEvent event : onTaskEvents )
		{
			appendExtraVariables( entity.getVariableMap() );
			boolean success = event.handle( entity, task, this );

			if ( success )
			{
				successfulProcess = true;
			}
		}

		if ( successfulProcess )
		{
			processed();
		}
	}

	// ----------------------------------------------------------------------
	public void onMove( Entity entity, TaskMove task )
	{
		boolean successfulProcess = false;
		for ( AbstractOnTaskEvent event : onMoveEvents )
		{
			appendExtraVariables( entity.getVariableMap() );
			boolean success = event.handle( entity, task, this );

			if ( success )
			{
				successfulProcess = true;
			}
		}

		if ( successfulProcess )
		{
			processed();
		}
	}

	// ----------------------------------------------------------------------
	public void onAttack( Entity entity, TaskAttack task )
	{
		boolean successfulProcess = false;
		for ( AbstractOnTaskEvent event : onAttackEvents )
		{
			appendExtraVariables( entity.getVariableMap() );
			boolean success = event.handle( entity, task, this );

			if ( success )
			{
				successfulProcess = true;
			}
		}

		if ( successfulProcess )
		{
			processed();
		}
	}

	// ----------------------------------------------------------------------
	public void onWait( Entity entity, TaskWait task )
	{
		boolean successfulProcess = false;
		for ( AbstractOnTaskEvent event : onWaitEvents )
		{
			appendExtraVariables( entity.getVariableMap() );
			boolean success = event.handle( entity, task, this );

			if ( success )
			{
				successfulProcess = true;
			}
		}

		if ( successfulProcess )
		{
			processed();
		}
	}

	// ----------------------------------------------------------------------
	public void onUseAbility( Entity entity, TaskUseAbility task )
	{
		boolean successfulProcess = false;
		for ( AbstractOnTaskEvent event : onUseAbilityEvents )
		{
			appendExtraVariables( entity.getVariableMap() );
			boolean success = event.handle( entity, task, this );

			if ( success )
			{
				successfulProcess = true;
			}
		}

		if ( successfulProcess )
		{
			processed();
		}
	}

	// ----------------------------------------------------------------------
	public void onExpire( Entity entity )
	{
		boolean successfulProcess = false;
		for ( AbstractOnExpireEvent event : onExpireEvents )
		{
			appendExtraVariables( entity.getVariableMap() );
			boolean success = event.handle( entity );

			if ( success )
			{
				successfulProcess = true;
			}
		}

		if ( successfulProcess )
		{
			processed();
		}
	}

	// ----------------------------------------------------------------------
	public void processed()
	{

	}

	// ----------------------------------------------------------------------
	public void applyQuality( int quality )
	{
		HashMap<String, Integer> variables = new HashMap<String, Integer>(  );
		variables.put( "quality", quality );

		if (constantEvent != null && constantEvent.equations != null)
		{
			for ( Global.Statistic stat : Global.Statistic.values() )
			{
				if ( constantEvent.equations.containsKey( stat ) )
				{
					constantEvent.putStatistic( stat, ""+constantEvent.getStatistic( variables, stat ) );
				}
			}
		}

		for ( AbstractOnDamageEvent event : onDealDamageEvents )
		{
			event.applyQuality( quality );
		}

		for (AbstractOnDamageEvent event : onReceiveDamageEvents )
		{
			event.applyQuality( quality );
		}
	}

	// ----------------------------------------------------------------------
	protected void parse( Element xml )
	{
		Element onTurnElements = xml.getChildByName( "OnTurn" );
		if ( onTurnElements != null )
		{
			for ( int i = 0; i < onTurnElements.getChildCount(); i++ )
			{
				Element onTurnElement = onTurnElements.getChild( i );
				onTurnEvents.add( AbstractOnTurnEvent.load( onTurnElement ) );
			}
		}

		Element onDeathElements = xml.getChildByName( "OnDeath" );
		if ( onDeathElements != null )
		{
			for ( int i = 0; i < onDeathElements.getChildCount(); i++ )
			{
				Element onDeathElement = onDeathElements.getChild( i );
				onDeathEvents.add( AbstractOnDeathEvent.load( onDeathElement ) );
			}
		}

		Element constantElement = xml.getChildByName( "Constant" );
		if ( constantElement != null )
		{
			constantEvent = ConstantEvent.load( constantElement );
		}

		Element onDealDamageElements = xml.getChildByName( "OnDealDamage" );
		if ( onDealDamageElements != null )
		{
			for ( int i = 0; i < onDealDamageElements.getChildCount(); i++ )
			{
				Element onDealDamageElement = onDealDamageElements.getChild( i );
				onDealDamageEvents.add( AbstractOnDamageEvent.load( onDealDamageElement ) );
			}
		}

		Element onReceiveDamageElements = xml.getChildByName( "OnReceiveDamage" );
		if ( onReceiveDamageElements != null )
		{
			for ( int i = 0; i < onReceiveDamageElements.getChildCount(); i++ )
			{
				Element onReceiveDamageElement = onReceiveDamageElements.getChild( i );
				onReceiveDamageEvents.add( AbstractOnDamageEvent.load( onReceiveDamageElement ) );
			}
		}

		Element onTaskElements = xml.getChildByName( "OnTask" );
		if ( onTaskElements != null )
		{
			for ( int i = 0; i < onTaskElements.getChildCount(); i++ )
			{
				Element onTaskElement = onTaskElements.getChild( i );
				onTaskEvents.add( AbstractOnTaskEvent.load( onTaskElement ) );
			}
		}

		Element onMoveElements = xml.getChildByName( "OnMove" );
		if ( onMoveElements != null )
		{
			for ( int i = 0; i < onMoveElements.getChildCount(); i++ )
			{
				Element onMoveElement = onMoveElements.getChild( i );
				onMoveEvents.add( AbstractOnTaskEvent.load( onMoveElement ) );
			}
		}

		Element onAttackElements = xml.getChildByName( "OnAttack" );
		if ( onAttackElements != null )
		{
			for ( int i = 0; i < onAttackElements.getChildCount(); i++ )
			{
				Element onAttackElement = onAttackElements.getChild( i );
				onAttackEvents.add( AbstractOnTaskEvent.load( onAttackElement ) );
			}
		}

		Element onWaitElements = xml.getChildByName( "OnWait" );
		if ( onWaitElements != null )
		{
			for ( int i = 0; i < onWaitElements.getChildCount(); i++ )
			{
				Element onWaitElement = onWaitElements.getChild( i );
				onWaitEvents.add( AbstractOnTaskEvent.load( onWaitElement ) );
			}
		}

		Element onUseAbilityElements = xml.getChildByName( "OnUseAbility" );
		if ( onUseAbilityElements != null )
		{
			for ( int i = 0; i < onUseAbilityElements.getChildCount(); i++ )
			{
				Element onUseAbilityElement = onUseAbilityElements.getChild( i );
				onUseAbilityEvents.add( AbstractOnTaskEvent.load( onUseAbilityElement ) );
			}
		}

		Element onExpireElements = xml.getChildByName( "OnExpire" );
		if ( onExpireElements != null )
		{
			for ( int i = 0; i < onExpireElements.getChildCount(); i++ )
			{
				Element onExpireElement = onExpireElements.getChild( i );
				onExpireEvents.add( AbstractOnExpireEvent.load( onExpireElement ) );
			}
		}
	}

	// ----------------------------------------------------------------------
	public Array<String> toString( HashMap<String, Integer> variableMap, boolean skipStats )
	{
		appendExtraVariables( variableMap );

		Array<String> lines = new Array<String>();

		if ( constantEvent != null && !skipStats )
		{
			lines.addAll( constantEvent.toString( variableMap ) );
		}

		if ( onDealDamageEvents.size > 0 )
		{
			lines.add( "On Deal Damage:" );

			for ( AbstractOnDamageEvent event : onDealDamageEvents )
			{
				Array<String> elines = event.toString( variableMap, this );
				for ( String line : elines )
				{
					lines.add( "   " + line );
				}
			}
		}

		if ( onReceiveDamageEvents.size > 0 )
		{
			lines.add( "On Receive Damage:" );

			for ( AbstractOnDamageEvent event : onReceiveDamageEvents )
			{
				Array<String> elines = event.toString( variableMap, this );
				for ( String line : elines )
				{
					lines.add( "   " + line );
				}
			}
		}

		if ( onTurnEvents.size > 0 )
		{
			lines.add( "On Turn:" );

			for ( AbstractOnTurnEvent event : onTurnEvents )
			{
				Array<String> elines = event.toString( variableMap );
				for ( String line : elines )
				{
					lines.add( "   " + line );
				}
			}
		}

		if ( onTaskEvents.size > 0 )
		{
			lines.add( "On Task:" );

			for ( AbstractOnTaskEvent event : onTaskEvents )
			{
				Array<String> elines = event.toString( variableMap, "any", this );
				for ( String line : elines )
				{
					lines.add( "   " + line );
				}
			}
		}

		if ( onMoveEvents.size > 0 )
		{
			lines.add( "On Move:" );

			for ( AbstractOnTaskEvent event : onMoveEvents )
			{
				Array<String> elines = event.toString( variableMap, "move", this );
				for ( String line : elines )
				{
					lines.add( "   " + line );
				}
			}
		}

		if ( onAttackEvents.size > 0 )
		{
			lines.add( "On Attack:" );

			for ( AbstractOnTaskEvent event : onAttackEvents )
			{
				Array<String> elines = event.toString( variableMap, "attack", this );
				for ( String line : elines )
				{
					lines.add( "   " + line );
				}
			}
		}

		if ( onWaitEvents.size > 0 )
		{
			lines.add( "On Wait:" );

			for ( AbstractOnTaskEvent event : onWaitEvents )
			{
				Array<String> elines = event.toString( variableMap, "wait", this );
				for ( String line : elines )
				{
					lines.add( "   " + line );
				}
			}
		}

		if ( onUseAbilityEvents.size > 0 )
		{
			lines.add( "On Use Ability:" );

			for ( AbstractOnTaskEvent event : onUseAbilityEvents )
			{
				Array<String> elines = event.toString( variableMap, "use ability", this );
				for ( String line : elines )
				{
					lines.add( "   " + line );
				}
			}
		}

		if ( onDeathEvents.size > 0 )
		{
			lines.add( "On Death:" );

			for ( AbstractOnDeathEvent event : onDeathEvents )
			{
				Array<String> elines = event.toString( variableMap );
				for ( String line : elines )
				{
					lines.add( "   " + line );
				}
			}
		}

		if ( onExpireEvents.size > 0 )
		{
			lines.add( "On Expire:" );

			for ( AbstractOnExpireEvent event : onExpireEvents )
			{
				Array<String> elines = event.toString( variableMap );
				for ( String line : elines )
				{
					lines.add( "   " + line );
				}
			}
		}

		return lines;
	}
}
