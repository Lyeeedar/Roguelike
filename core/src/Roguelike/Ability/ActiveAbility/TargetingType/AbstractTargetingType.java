package Roguelike.Ability.ActiveAbility.TargetingType;

import java.util.HashMap;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public abstract class AbstractTargetingType
{	
	public abstract void parse(Element xml);
	
	public static AbstractTargetingType load(Element xml)
	{		
		Class<AbstractTargetingType> c = ClassMap.get(xml.getName());
		AbstractTargetingType type = null;
		
		try
		{
			type = (AbstractTargetingType)ClassReflection.newInstance(c);
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
		ClassMap.put("Self", TargetingTypeSelf.class);
		ClassMap.put("Direction", TargetingTypeDirection.class);
		ClassMap.put("Entity", TargetingTypeEntity.class);
		ClassMap.put("Tile", TargetingTypeTile.class);
	}
}
