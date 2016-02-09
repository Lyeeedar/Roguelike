package Roguelike.Levels.TownEvents;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import java.util.HashMap;

/**
 * Created by Philip on 09-Feb-16.
 */
public abstract class AbstractTownEvent
{
	public abstract void evaluate( ObjectMap<String, String> flags );
	public abstract void parse(XmlReader.Element xml );

	//----------------------------------------------------------------------
	public static AbstractTownEvent load(XmlReader.Element xml )
	{
		Class<AbstractTownEvent> c = ClassMap.get(xml.getName().toUpperCase());
		AbstractTownEvent type = null;

		try
		{
			type = (AbstractTownEvent) ClassReflection.newInstance( c );
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
		ClassMap.put("TEXT", TextTownEvent.class );
		ClassMap.put("ADDFUNDS", AddFundsTownEvent.class );
	}
}
