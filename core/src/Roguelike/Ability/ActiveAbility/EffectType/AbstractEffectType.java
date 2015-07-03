package Roguelike.Ability.ActiveAbility.EffectType;

import java.util.HashMap;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public abstract class AbstractEffectType
{
	protected int aoe;
	protected int duration;
	
	public void parse(Element xml)
	{
		aoe = xml.getInt("AOE", 0);
		duration = xml.getInt("Duration", 0);
	}
	
	public static AbstractEffectType load(Element xml)
	{		
		Class<AbstractEffectType> c = ClassMap.get(xml.getName());
		AbstractEffectType type = null;
		
		try
		{
			type = (AbstractEffectType)ClassReflection.newInstance(c);
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
