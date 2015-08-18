package Roguelike.GameEvent.OnTask;

import java.util.HashMap;

import Roguelike.Entity.Entity;
import Roguelike.Entity.Tasks.AbstractTask;
import Roguelike.GameEvent.IGameObject;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public abstract class AbstractOnTaskEvent
{
	public abstract boolean handle( Entity entity, AbstractTask task, IGameObject parent );

	public abstract void parse( Element xml );

	public abstract Array<String> toString( HashMap<String, Integer> variableMap, String eventType, IGameObject parent );

	public static AbstractOnTaskEvent load( Element xml )
	{
		Class<AbstractOnTaskEvent> c = ClassMap.get( xml.getName().toUpperCase() );
		AbstractOnTaskEvent type = null;

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
		ClassMap.put( "CANCEL", CancelTaskEvent.class );
		ClassMap.put( "COST", CostTaskEvent.class );
		ClassMap.put( "DAMAGE", DamageTaskEvent.class );
		ClassMap.put( "STATUS", StatusTaskEvent.class );
	}
}
