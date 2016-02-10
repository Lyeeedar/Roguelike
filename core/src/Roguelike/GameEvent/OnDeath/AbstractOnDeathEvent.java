package Roguelike.GameEvent.OnDeath;

import java.util.HashMap;

import Roguelike.Entity.Entity;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public abstract class AbstractOnDeathEvent
{
	public abstract boolean handle( Entity entity, Entity killer );

	public abstract void parse( Element xml );

	public abstract Array<String> toString( HashMap<String, Integer> variableMap );

	public static AbstractOnDeathEvent load( Element xml )
	{
		Class<AbstractOnDeathEvent> c = ClassMap.get( xml.getName().toUpperCase() );
		AbstractOnDeathEvent type = null;

		try
		{
			type = ClassReflection.newInstance( c );
		}
		catch ( Exception e )
		{
			System.err.println(xml.getName());
			e.printStackTrace();
		}

		type.parse( xml );

		return type;
	}

	public static final HashMap<String, Class> ClassMap = new HashMap<String, Class>();
	static
	{
		ClassMap.put( "ABILITY", AbilityOnDeathEvent.class );
		ClassMap.put( "FIELD", FieldOnDeathEvent.class );
		ClassMap.put( "HEAL", HealOnDeathEvent.class );
	}
}
