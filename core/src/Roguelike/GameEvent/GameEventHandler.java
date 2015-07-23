package Roguelike.GameEvent;

import java.util.EnumMap;

import Roguelike.Global.Statistics;
import Roguelike.Global.Tier1Element;
import Roguelike.Entity.Entity;
import Roguelike.Entity.GameEntity;
import Roguelike.GameEvent.Constant.ConstantEvent;
import Roguelike.GameEvent.Damage.AbstractOnDamageEvent;
import Roguelike.GameEvent.Damage.DamageObject;
import Roguelike.GameEvent.OnTurn.AbstractOnTurnEvent;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public abstract class GameEventHandler implements IGameObject
{
	protected Array<AbstractOnTurnEvent> onTurnEvents = new Array<AbstractOnTurnEvent>();
	protected ConstantEvent constantEvent;
	protected Array<AbstractOnDamageEvent> onDealDamageEvents = new Array<AbstractOnDamageEvent>();
	protected Array<AbstractOnDamageEvent> onReceiveDamageEvents = new Array<AbstractOnDamageEvent>();
	
	public int getStatistic(Entity entity, Statistics s)
	{
		int val = 0;
		
		if (constantEvent != null)
		{
			val += constantEvent.getStatistic(entity, s);
		}
		
		return val;
	}
	
	public EnumMap<Statistics, Integer> getStatistics(Entity entity)
	{
		EnumMap<Statistics, Integer> newMap = new EnumMap<Statistics, Integer>(Statistics.class);
		
		for (Statistics stat : Statistics.values())
		{
			newMap.put(stat, getStatistic(entity, stat));
		}
		
		return newMap;
	}
		
	public void onTurn(Entity entity, float cost)
	{		
		for (AbstractOnTurnEvent event : onTurnEvents)
		{
			event.handle(entity, cost);
		}
	}
	
	public void onDealDamage(DamageObject obj)
	{
		for (AbstractOnDamageEvent event : onDealDamageEvents)
		{
			event.handle(obj, this);
		}
	}
	
	public void onReceiveDamage(DamageObject obj)
	{
		for (AbstractOnDamageEvent event : onReceiveDamageEvents)
		{
			event.handle(obj, this);
		}
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
	}
}
