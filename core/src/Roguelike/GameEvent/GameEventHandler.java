package Roguelike.GameEvent;

import java.util.EnumMap;

import Roguelike.Global.Statistics;
import Roguelike.Global.Tier1Element;
import Roguelike.Entity.Entity;
import Roguelike.GameEvent.Constant.AbstractConstantEvent;
import Roguelike.GameEvent.Damage.AbstractOnDamageEvent;
import Roguelike.GameEvent.Damage.DamageObject;
import Roguelike.GameEvent.OnTurn.AbstractOnTurnEvent;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public abstract class GameEventHandler
{
	protected Array<AbstractOnTurnEvent> onTurnEvents = new Array<AbstractOnTurnEvent>();
	protected Array<AbstractConstantEvent> constantEvents = new Array<AbstractConstantEvent>();
	protected Array<AbstractOnDamageEvent> onDealDamageEvents = new Array<AbstractOnDamageEvent>();
	protected Array<AbstractOnDamageEvent> onReceiveDamageEvents = new Array<AbstractOnDamageEvent>();
	
	public int getStatistic(Statistics s)
	{
		int val = 0;
		
		for (AbstractConstantEvent e : constantEvents)
		{
			val += e.getStatistic(s);
		}
		
		return val;
	}
	
	public EnumMap<Statistics, Integer> getStatistics()
	{
		EnumMap<Statistics, Integer> newMap = new EnumMap<Statistics, Integer>(Statistics.class);
		
		for (Statistics stat : Statistics.values())
		{
			newMap.put(stat, getStatistic(stat));
		}
		
		return newMap;
	}
	
	public int getAttunement(Tier1Element el)
	{
		int val = 0;
		
		for (AbstractConstantEvent e : constantEvents)
		{
			val += e.getAttunement(el);
		}
		
		return val;
	}
	
	public EnumMap<Tier1Element, Integer> getAttunements()
	{
		EnumMap<Tier1Element, Integer> newMap = new EnumMap<Tier1Element, Integer>(Tier1Element.class);
		
		for (Tier1Element el : Tier1Element.values())
		{
			newMap.put(el, getAttunement(el));
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
			event.handle(obj);
		}
	}
	
	public void onReceiveDamage(DamageObject obj)
	{
		for (AbstractOnDamageEvent event : onReceiveDamageEvents)
		{
			event.handle(obj);
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
		
		Element constantElements = xml.getChildByName("Constant");
		if (constantElements != null)
		{
			for (int i = 0; i < constantElements.getChildCount(); i++)
			{
				Element constantElement = constantElements.getChild(i);
				constantEvents.add(AbstractConstantEvent.load(constantElement));
			}
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
