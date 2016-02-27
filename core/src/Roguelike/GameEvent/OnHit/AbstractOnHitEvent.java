package Roguelike.GameEvent.OnHit;

import Roguelike.Entity.GameEntity;
import Roguelike.Tiles.GameTile;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.reflect.ClassReflection;

import java.util.HashMap;

public abstract class AbstractOnHitEvent
{
	public abstract boolean handle( GameEntity entity, GameTile tile );

	public abstract void parse( XmlReader.Element xml );

	public abstract Array<String> toString( HashMap<String, Integer> variableMap );

	public static AbstractOnHitEvent load( XmlReader.Element xml )
	{
		Class<AbstractOnHitEvent> c = ClassMap.get( xml.getName().toUpperCase() );
		AbstractOnHitEvent type = null;

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
		ClassMap.put( "ABILITY", AbilityOnHitEvent.class );
		//ClassMap.put( "FIELD", FieldOnHitEvent.class );
	}
}