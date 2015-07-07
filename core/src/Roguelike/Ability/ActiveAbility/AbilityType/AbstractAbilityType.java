package Roguelike.Ability.ActiveAbility.AbilityType;

import java.util.EnumMap;
import java.util.HashMap;

import Roguelike.Global.Tier1Element;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public abstract class AbstractAbilityType
{
	public EnumMap<Tier1Element, Integer> additionalMap = Tier1Element.getElementMap();	
	public EnumMap<Tier1Element, Integer> elementMap = Tier1Element.getElementMap();
	
	public abstract AbstractAbilityType copy();
	public abstract void processElements();
	
	public void parse(Element xml)
	{
		for (int i = 0; i < xml.getChildCount(); i++)
		{
			Element child = xml.getChild(i);
			Tier1Element element = Tier1Element.valueOf(child.getName().toUpperCase());
			
			elementMap.put(element, Integer.parseInt(child.getText()));
		}
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
