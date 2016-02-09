package Roguelike.Levels.TownEvents;

import Roguelike.Global;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;

import java.util.HashMap;

/**
 * Created by Philip on 09-Feb-16.
 */
public class AddFundsTownEvent extends AbstractTownEvent
{
	int num;

	@Override
	public void evaluate( ObjectMap<String, String> flags )
	{
		int current = Integer.parseInt( Global.WorldFlags.get( "startingfunds" ) );
		current += num;
		Global.WorldFlags.put( "startingfunds", ""+current );
	}

	@Override
	public void parse( XmlReader.Element xml )
	{
		num = Integer.parseInt( xml.getText() );
	}
}
