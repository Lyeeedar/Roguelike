package Roguelike.GameEvent.Damage;

import java.util.HashMap;

import Roguelike.Entity.Entity;
import Roguelike.GameEvent.IGameObject;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public abstract class AbstractOnDamageEvent
{
	public abstract boolean handle( Entity entity, DamageObject obj, IGameObject parent );

	public abstract void parse( Element xml );

	public abstract Array<String> toString( HashMap<String, Integer> variableMap, IGameObject parent );

	public static AbstractOnDamageEvent load( Element xml )
	{
		Class<AbstractOnDamageEvent> c = ClassMap.get( xml.getName().toUpperCase() );
		AbstractOnDamageEvent type = null;

		try
		{
			type = ClassReflection.newInstance( c );
		}
		catch ( ReflectionException e )
		{
			e.printStackTrace();
		}

		type.parse( xml );

		return type;
	}

	public static final HashMap<String, Class> ClassMap = new HashMap<String, Class>();
	static
	{
		ClassMap.put( "STATUS", StatusEvent.class );
		ClassMap.put( "HEAL", HealEvent.class );
		ClassMap.put( "DAMAGE", DamageEvent.class );
		ClassMap.put( "FIELD", FieldEvent.class );
	}
}
