package Roguelike.Fields.FieldInteractionTypes;

import java.util.HashMap;

import Roguelike.Fields.Field;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public abstract class AbstractFieldInteractionType
{
	public abstract Field process(Field src, Field dst);
	public abstract void parse(Element xml);

	//----------------------------------------------------------------------
	public static AbstractFieldInteractionType load(Element xml)
	{
		Class<AbstractFieldInteractionType> c = ClassMap.get(xml.getName().toUpperCase());
		AbstractFieldInteractionType type = null;

		try
		{
			type = (AbstractFieldInteractionType)ClassReflection.newInstance(c);
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
		ClassMap.put( "ABILITY", AbilityFieldInteractionType.class );
		ClassMap.put( "SPAWN", SpawnFieldInteractionType.class );
		ClassMap.put( "KILLTHIS", KillThisFieldInteractionType.class );
		ClassMap.put( "KILLOTHER", KillOtherFieldInteractionType.class );
		ClassMap.put( "PROPOGATE", PropogateFieldInteractionType.class );
	}
}
