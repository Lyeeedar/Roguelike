package Roguelike.Fields.SpreadStyle;

import java.util.HashMap;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import Roguelike.Fields.Field;

public abstract class AbstractSpreadStyle
{
	public abstract void update(float delta, Field field);	
	public abstract void parse(Element xml);
	
	//----------------------------------------------------------------------
	public static AbstractSpreadStyle load(Element xml)
	{		
		Class<AbstractSpreadStyle> c = ClassMap.get(xml.getName().toUpperCase());
		AbstractSpreadStyle type = null;
		
		try
		{
			type = (AbstractSpreadStyle)ClassReflection.newInstance(c);
		} 
		catch (ReflectionException e)
		{
			e.printStackTrace();
		}
		
		type.parse(xml);
		
		return type;
	}
	
	//----------------------------------------------------------------------
	protected static HashMap<String, Class> ClassMap = new HashMap<String, Class>();
	
	//----------------------------------------------------------------------
	static
	{
		ClassMap.put("FLOW", FlowSpreadStyle.class);
		ClassMap.put("ADJACENT", AdjacentSpreadStyle.class);
		ClassMap.put("NONE", NoneSpreadStyle.class);
		ClassMap.put("WANDER", WanderSpreadStyle.class);
	}
}
