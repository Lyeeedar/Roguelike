package Roguelike.Ability.ActiveAbility.AbilityType;

import java.util.EnumMap;
import java.util.HashMap;

import Roguelike.Global.Tier1Element;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public abstract class AbstractAbilityType
{
	public abstract AbstractAbilityType copy();
	public abstract void processElements();
	
	public void parse(Element xml)
	{

	}
	
	public static AbstractAbilityType load(Element xml)
	{		
		Class<AbstractAbilityType> c = ClassMap.get(xml.getName().toUpperCase());
		AbstractAbilityType type = null;
		
		try
		{
			type = (AbstractAbilityType)ClassReflection.newInstance(c);
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
		ClassMap.put("HARMFUL", AbilityTypeHarmful.class);
		ClassMap.put("HELPFUL", AbilityTypeHelpful.class);
	}
}
