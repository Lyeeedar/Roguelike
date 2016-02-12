package Roguelike.Entity.ActivationAction;

import Roguelike.Entity.Entity;
import Roguelike.Entity.EnvironmentEntity;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.reflect.ClassReflection;

import java.util.HashMap;

/**
 * Created by Philip on 25-Jan-16.
 */
public abstract class AbstractActivationCondition
{
	// ----------------------------------------------------------------------
	public abstract boolean evaluate( EnvironmentEntity owningEntity, Entity activatingEntity, float delta );
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
		catch ( Exception e )
		{
			System.err.println(xml.getName());
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
		ClassMap.put( "PROXIMITY", ActivationConditionProximity.class );
		ClassMap.put( "HASITEM", ActivationConditionHasItem.class );
	}
}
