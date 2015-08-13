package Roguelike.GameEvent.OnDeath;

import java.util.HashMap;

import Roguelike.Entity.Entity;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public abstract class AbstractOnDeathEvent
{
	public abstract boolean handle(Entity entity, Entity killer);
	public abstract void parse(Element xml);
	
	public static AbstractOnDeathEvent load(Element xml)
	{		
		Class<AbstractOnDeathEvent> c = ClassMap.get(xml.getName().toUpperCase());
		AbstractOnDeathEvent type = null;
		
		try
		{
			type = (AbstractOnDeathEvent)ClassReflection.newInstance(c);
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
		ClassMap.put("FIELD", FieldOnDeathEvent.class);
		ClassMap.put("HEAL", HealOnDeathEvent.class);
	}
}
