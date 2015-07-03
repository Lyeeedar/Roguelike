package Roguelike.Ability.ActiveAbility.MovementType;

import java.util.HashMap;

import Roguelike.Ability.ActiveAbility.AbilityType.AbstractAbilityType;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public abstract class AbstractMovementType
{
	protected int range;
	
	public void parse(Element xml)
	{
		range = xml.getInt("Range");
	}
	
	public static AbstractMovementType load(Element xml)
	{
		Class<AbstractMovementType> c = ClassMap.get(xml.getName());
		AbstractMovementType type = null;
		
		try
		{
			type = (AbstractMovementType)ClassReflection.newInstance(c);
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
		ClassMap.put("Smite", MovementTypeSmite.class);
		ClassMap.put("Bolt", MovementTypeBolt.class);
		ClassMap.put("Homing", MovementTypeHoming.class);
	}
}
