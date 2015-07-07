package Roguelike.GameEvent.Damage;

import java.util.HashMap;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public abstract class AbstractOnDamageEvent
{
	public abstract boolean handle(DamageObject obj);
	public abstract void parse(Element xml);
	
	public static AbstractOnDamageEvent load(Element xml)
	{		
		Class<AbstractOnDamageEvent> c = ClassMap.get(xml.getName().toUpperCase());
		AbstractOnDamageEvent type = null;
		
		try
		{
			type = (AbstractOnDamageEvent)ClassReflection.newInstance(c);
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
		ClassMap.put("STATUS", StatusEvent.class);
	}
}
