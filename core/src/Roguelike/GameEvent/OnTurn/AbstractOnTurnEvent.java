package Roguelike.GameEvent.OnTurn;

import java.util.HashMap;

import Roguelike.Entity.Entity;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public abstract class AbstractOnTurnEvent
{
	public abstract boolean handle( Entity entity, float time );

	public abstract void parse( Element xml );

	public abstract Array<String> toString( HashMap<String, Integer> variableMap );

	public static AbstractOnTurnEvent load( Element xml )
	{
		Class<AbstractOnTurnEvent> c = ClassMap.get( xml.getName().toUpperCase() );
		AbstractOnTurnEvent type = null;

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
		ClassMap.put( "DAMAGE", DamageOverTimeEvent.class );
		ClassMap.put( "HEAL", HealOverTimeEvent.class );
	}
}
