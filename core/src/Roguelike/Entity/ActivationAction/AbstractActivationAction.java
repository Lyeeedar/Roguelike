package Roguelike.Entity.ActivationAction;

import Roguelike.Entity.Entity;
import Roguelike.Entity.EnvironmentEntity;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import java.util.HashMap;

/**
 * Created by Philip on 25-Jan-16.
 */
public abstract class AbstractActivationAction
{
	// ----------------------------------------------------------------------
	public abstract void evaluate( EnvironmentEntity owningEntity, Entity activatingEntity, float delta );
	public abstract void parse( XmlReader.Element xml );

	// ----------------------------------------------------------------------
	public static AbstractActivationAction load( XmlReader.Element xml )
	{
		Class<AbstractActivationAction> c = ClassMap.get( xml.getName().toUpperCase() );
		AbstractActivationAction type = null;

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
		ClassMap.put( "ADDITEM", ActivationActionAddItem.class );
		ClassMap.put( "SETSPRITE", ActivationActionSetSprite.class );
		ClassMap.put( "SETPASSABLE", ActivationActionSetPassable.class );
		ClassMap.put( "SETENABLED", ActivationActionSetEnabled.class );
		ClassMap.put( "CHANGELEVEL", ActivationActionChangeLevel.class );
		ClassMap.put( "SPAWNENTITY", ActivationActionSpawnEntity.class );
		ClassMap.put( "ACTIVATE", ActivationActionActivate.class );
		ClassMap.put( "KILLTHIS", ActivationActionKillThis.class );
		ClassMap.put( "SPAWNFIELD", ActivationActionSpawnField.class );
		ClassMap.put( "REMOVEITEM", ActivationActionRemoveItem.class );
		ClassMap.put( "DEALDAMAGE", ActivationActionDealDamage.class );
		ClassMap.put( "ADDSTATUS", ActivationActionAddStatus.class );
	}
}
