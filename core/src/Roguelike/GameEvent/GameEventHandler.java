package Roguelike.GameEvent;

import java.util.EnumMap;
import java.util.HashMap;

import Roguelike.Global.Statistic;
import Roguelike.Global.Tier1Element;
import Roguelike.Entity.Entity;
import Roguelike.Entity.GameEntity;
import Roguelike.Entity.Tasks.AbstractTask;
import Roguelike.Entity.Tasks.TaskAttack;
import Roguelike.Entity.Tasks.TaskMove;
import Roguelike.Entity.Tasks.TaskUseAbility;
import Roguelike.Entity.Tasks.TaskWait;
import Roguelike.GameEvent.Constant.ConstantEvent;
import Roguelike.GameEvent.Damage.AbstractOnDamageEvent;
import Roguelike.GameEvent.Damage.DamageObject;
import Roguelike.GameEvent.OnTask.AbstractOnTaskEvent;
import Roguelike.GameEvent.OnTurn.AbstractOnTurnEvent;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public abstract class GameEventHandler implements IGameObject
{
	public Array<AbstractOnTurnEvent> onTurnEvents = new Array<AbstractOnTurnEvent>();
	public ConstantEvent constantEvent;
	public Array<AbstractOnDamageEvent> onDealDamageEvents = new Array<AbstractOnDamageEvent>();
	public Array<AbstractOnDamageEvent> onReceiveDamageEvents = new Array<AbstractOnDamageEvent>();	
	public Array<AbstractOnTaskEvent> onMoveEvents = new Array<AbstractOnTaskEvent>();
	public Array<AbstractOnTaskEvent> onAttackEvents = new Array<AbstractOnTaskEvent>();
	public Array<AbstractOnTaskEvent> onWaitEvents = new Array<AbstractOnTaskEvent>();
	public Array<AbstractOnTaskEvent> onUseAbilityEvents = new Array<AbstractOnTaskEvent>();
	
	public int getStatistic(HashMap<String, Integer> variableMap, Statistic s)
	{
		int val = 0;
		
		if (constantEvent != null)
		{
			val += constantEvent.getStatistic(variableMap, s);
		}
		
		return val;
	}
	
	public EnumMap<Statistic, Integer> getStatistics(HashMap<String, Integer> variableMap)
	{
		EnumMap<Statistic, Integer> newMap = new EnumMap<Statistic, Integer>(Statistic.class);
		
		for (Statistic stat : Statistic.values())
		{
			newMap.put(stat, getStatistic(variableMap, stat));
		}
		
		return newMap;
	}
	
	public EnumMap<Statistic, String> getStatisticsObject()
	{
		return constantEvent.equations;
	}
		
	public void onTurn(Entity entity, float cost)
	{		
		for (AbstractOnTurnEvent event : onTurnEvents)
		{
			event.handle(entity, cost);
		}
		
		if (onTurnEvents.size > 0)
		{
			processed();
		}
	}
	
	public void onDealDamage(DamageObject obj)
	{
		for (AbstractOnDamageEvent event : onDealDamageEvents)
		{
			event.handle(obj, this);
		}
		
		if (onDealDamageEvents.size > 0)
		{
			processed();
		}
	}
	
	public void onReceiveDamage(DamageObject obj)
	{
		for (AbstractOnDamageEvent event : onReceiveDamageEvents)
		{
			event.handle(obj, this);
		}
		
		if (onReceiveDamageEvents.size > 0)
		{
			processed();
		}
	}
	
	public void onTask(Entity entity, AbstractTask task)
	{
		if (task instanceof TaskMove)
		{
			onMove(entity, (TaskMove)task);
		}
		else if (task instanceof TaskAttack)
		{
			onAttack(entity, (TaskAttack)task);
		}
		else if (task instanceof TaskWait)
		{
			onWait(entity, (TaskWait)task);
		}
		else if (task instanceof TaskUseAbility)
		{
			onUseAbility(entity, (TaskUseAbility)task);
		}
	}
	
	public void onMove(Entity entity, TaskMove task)
	{
		for (AbstractOnTaskEvent event : onMoveEvents)
		{
			event.handle(entity, task, this);
		}
		
		if (onMoveEvents.size > 0)
		{
			processed();
		}
	}
	
	public void onAttack(Entity entity, TaskAttack task)
	{
		for (AbstractOnTaskEvent event : onAttackEvents)
		{
			event.handle(entity, task, this);
		}
		
		if (onAttackEvents.size > 0)
		{
			processed();
		}
	}
	
	public void onWait(Entity entity, TaskWait task)
	{
		for (AbstractOnTaskEvent event : onWaitEvents)
		{
			event.handle(entity, task, this);
		}
		
		if (onWaitEvents.size > 0)
		{
			processed();
		}
	}
	
	public void onUseAbility(Entity entity, TaskUseAbility task)
	{
		for (AbstractOnTaskEvent event : onUseAbilityEvents)
		{
			event.handle(entity, task, this);
		}
		
		if (onUseAbilityEvents.size > 0)
		{
			processed();
		}
	}
	
	public void processed()
	{
		
	}
	
 	protected void parse(Element xml)
	{
		Element onTurnElements = xml.getChildByName("OnTurn");
		if (onTurnElements != null)
		{
			for (int i = 0; i < onTurnElements.getChildCount(); i++)
			{
				Element onTurnElement = onTurnElements.getChild(i);
				onTurnEvents.add(AbstractOnTurnEvent.load(onTurnElement));
			}
		}
		
		Element constantElement = xml.getChildByName("Constant");
		if (constantElement != null)
		{
			constantEvent = ConstantEvent.load(constantElement);
		}
		
		Element onDealDamageElements = xml.getChildByName("OnDealDamage");
		if (onDealDamageElements != null)
		{
			for (int i = 0; i < onDealDamageElements.getChildCount(); i++)
			{
				Element onDealDamageElement = onDealDamageElements.getChild(i);
				onDealDamageEvents.add(AbstractOnDamageEvent.load(onDealDamageElement));
			}
		}
		
		Element onReceiveDamageElements = xml.getChildByName("OnReceiveDamage");
		if (onReceiveDamageElements != null)
		{
			for (int i = 0; i < onReceiveDamageElements.getChildCount(); i++)
			{
				Element onReceiveDamageElement = onReceiveDamageElements.getChild(i);
				onReceiveDamageEvents.add(AbstractOnDamageEvent.load(onReceiveDamageElement));
			}
		}
		
		Element onMoveElements = xml.getChildByName("OnMove");
		if (onMoveElements != null)
		{
			for (int i = 0; i < onMoveElements.getChildCount(); i++)
			{
				Element onMoveElement = onMoveElements.getChild(i);
				onMoveEvents.add(AbstractOnTaskEvent.load(onMoveElement));
			}
		}
		
		Element onAttackElements = xml.getChildByName("OnAttack");
		if (onAttackElements != null)
		{
			for (int i = 0; i < onAttackElements.getChildCount(); i++)
			{
				Element onAttackElement = onAttackElements.getChild(i);
				onAttackEvents.add(AbstractOnTaskEvent.load(onAttackElement));
			}
		}
		
		Element onWaitElements = xml.getChildByName("OnWait");
		if (onWaitElements != null)
		{
			for (int i = 0; i < onWaitElements.getChildCount(); i++)
			{
				Element onWaitElement = onWaitElements.getChild(i);
				onWaitEvents.add(AbstractOnTaskEvent.load(onWaitElement));
			}
		}
		
		Element onUseAbilityElements = xml.getChildByName("OnUseAbility");
		if (onUseAbilityElements != null)
		{
			for (int i = 0; i < onUseAbilityElements.getChildCount(); i++)
			{
				Element onUseAbilityElement = onUseAbilityElements.getChild(i);
				onUseAbilityEvents.add(AbstractOnTaskEvent.load(onUseAbilityElement));
			}
		}
	}
}
