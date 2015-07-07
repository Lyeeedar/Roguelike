package Roguelike.GameEvent.Constant;

import java.util.HashMap;

import Roguelike.Global.Statistics;
import Roguelike.Global.Tier1Element;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public abstract class AbstractConstantEvent
{
	public abstract void parse(Element xml);
	public abstract int getStatistic(Statistics s);
	public abstract int getAttunement(Tier1Element el);
	
	public static AbstractConstantEvent load(Element xml)
	{		
		Class<AbstractConstantEvent> c = ClassMap.get(xml.getName());
		AbstractConstantEvent type = null;
		
		try
		{
			type = (AbstractConstantEvent)ClassReflection.newInstance(c);
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
		
	}
}
