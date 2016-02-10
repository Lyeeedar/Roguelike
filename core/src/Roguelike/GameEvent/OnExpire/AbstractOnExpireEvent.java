package Roguelike.GameEvent.OnExpire;

import Roguelike.Entity.Entity;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import java.util.HashMap;

/**
 * Created by Philip on 26/12/2015.
 */
public abstract class AbstractOnExpireEvent
{
	public abstract boolean handle( Entity entity );

	public abstract void parse( XmlReader.Element xml );

	public abstract Array<String> toString( HashMap<String, Integer> variableMap );

	public static AbstractOnExpireEvent load( XmlReader.Element xml )
	{
		Class<AbstractOnExpireEvent> c = ClassMap.get( xml.getName().toUpperCase() );
		AbstractOnExpireEvent type = null;

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
		ClassMap.put( "ABILITY", AbilityOnExpireEvent.class );
		ClassMap.put( "KILL", KillOnExpireEvent.class );
	}
}
