package Roguelike.GameEvent.OnTurn;

import java.util.HashMap;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import Roguelike.Entity.GameEntity;

public abstract class AbstractOnTurnEvent
{
	public abstract boolean handle(GameEntity entity, float time);
	public abstract void parse(Element xml);
	
	public static AbstractOnTurnEvent load(Element xml)
	{		
		Class<AbstractOnTurnEvent> c = ClassMap.get(xml.getName().toUpperCase());
		AbstractOnTurnEvent type = null;
		
		try
		{
			type = (AbstractOnTurnEvent)ClassReflection.newInstance(c);
		} 
		catch (ReflectionException e)
		{
			e.printStackTrace();
		}
		
		type.parse(xml);
		
		return type;
	}
	
	public static final HashMap<String, Class> ClassMap = new HashMap<String, Class>();
	static
	{
		ClassMap.put("DOT", DamageOverTimeEvent.class);
		ClassMap.put("HOT", HealOverTimeEvent.class);
	}
}
