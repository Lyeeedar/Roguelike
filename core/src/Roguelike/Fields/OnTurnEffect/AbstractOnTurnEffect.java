package Roguelike.Fields.OnTurnEffect;

import java.util.HashMap;

import Roguelike.Fields.Field;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public abstract class AbstractOnTurnEffect
{
	public abstract void process(Field field);	
	public abstract void parse(Element xml);
	
	//----------------------------------------------------------------------
	public static AbstractOnTurnEffect load(Element xml)
	{		
		Class<AbstractOnTurnEffect> c = ClassMap.get(xml.getName().toUpperCase());
		AbstractOnTurnEffect type = null;
		
		try
		{
			type = (AbstractOnTurnEffect)ClassReflection.newInstance(c);
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
		ClassMap.put("DAMAGE", DamageOnTurnEffect.class);
		ClassMap.put("STATUS", StatusOnTurnEffect.class);
	}
}
