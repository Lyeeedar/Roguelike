package Roguelike.Entity.ActivationAction;

import Roguelike.Entity.EnvironmentEntity;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import java.util.HashMap;

/**
 * Created by Philip on 25-Jan-16.
 */
public abstract class AbstractActivationCondition
{
	// ----------------------------------------------------------------------
	public abstract boolean evaluate( EnvironmentEntity entity, float delta );
	public abstract void parse( XmlReader.Element xml );

	// ----------------------------------------------------------------------
	public static AbstractActivationCondition load( XmlReader.Element xml )
	{
		Class<AbstractActivationCondition> c = ClassMap.get( xml.getName().toUpperCase() );
		AbstractActivationCondition type = null;

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

	// ----------------------------------------------------------------------
	protected static HashMap<String, Class> ClassMap = new HashMap<String, Class>();

	// ----------------------------------------------------------------------
	static
	{
		ClassMap.put( "PLAYERPROXIMITY", ActivationConditionPlayerProximity.class );
	}
}
