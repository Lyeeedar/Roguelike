package Roguelike.Levels.TownEvents;

import Roguelike.Global;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Philip on 09-Feb-16.
 */
public class EventList
{
	Array<FlagEvent> flagEvents = new Array<FlagEvent>(  );

	public EventList()
	{
		XmlReader reader = new XmlReader();
		XmlReader.Element xml = null;

		try
		{
			xml = reader.parse( Gdx.files.internal( "Levels/Town/EventList.xml" ) );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}

		for (int i = 0; i < xml.getChildCount(); i++)
		{
			XmlReader.Element el = xml.getChild( i );

			flagEvents.add( new FlagEvent( el ) );
		}
	}

	public void evaluate( ObjectMap<String, String> flags )
	{
		for (FlagEvent event : flagEvents)
		{
			if ( flags.containsKey( event.name ) )
			{
				event.evaluate( flags );
			}
		}
	}

	public static class FlagEvent
	{
		String name;
		Array<ValueEvent> valueEvents = new Array<ValueEvent>(  );

		public FlagEvent( XmlReader.Element xml )
		{
			name = xml.getName().toLowerCase();
			for (int i = 0; i < xml.getChildCount(); i++)
			{
				XmlReader.Element el = xml.getChild( i );

				valueEvents.add( new ValueEvent( el ) );
			}
		}

		public void evaluate( ObjectMap<String, String> flags )
		{
			for (ValueEvent event : valueEvents)
			{
				if ( flags.get( name ).equals( event.name ) )
				{
					event.evaluate( flags );
				}
			}
		}
	}

	public static class ValueEvent
	{
		String name;
		Array<AbstractTownEvent> abstractEvents = new Array<AbstractTownEvent>(  );

		public ValueEvent( XmlReader.Element xml )
		{
			name = xml.getAttribute( "Value", "true" ).toLowerCase();
			for (int i = 0; i < xml.getChildCount(); i++)
			{
				XmlReader.Element el = xml.getChild( i );

				abstractEvents.add( AbstractTownEvent.load( el ) );
			}
		}

		public void evaluate( ObjectMap<String, String> flags )
		{
			for (AbstractTownEvent event : abstractEvents)
			{
				event.evaluate( flags );
			}
		}
	}
}
