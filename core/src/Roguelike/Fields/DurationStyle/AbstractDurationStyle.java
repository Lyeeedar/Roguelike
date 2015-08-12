package Roguelike.Fields.DurationStyle;

import java.util.HashMap;

import Roguelike.Fields.Field;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public abstract class AbstractDurationStyle
{
	public abstract void update(float delta, Field field);	
	public abstract void parse(Element xml);
	
	//----------------------------------------------------------------------
	public static AbstractDurationStyle load(Element xml)
	{		
		Class<AbstractDurationStyle> c = ClassMap.get(xml.getName().toUpperCase());
		AbstractDurationStyle type = null;
		
		try
		{
			type = (AbstractDurationStyle)ClassReflection.newInstance(c);
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
		//ClassMap.put("FLOW", FlowSpreadStyle.class);
	}
}
