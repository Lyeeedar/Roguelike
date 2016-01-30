package Roguelike.Quests.Input;

import Roguelike.Global;
import com.badlogic.gdx.utils.XmlReader;

/**
 * Created by Philip on 24-Jan-16.
 */
public class QuestInputFlagEquals extends AbstractQuestInput
{
	@Override
	public boolean evaluate()
	{
		if ( Global.WorldFlags.containsKey( key ) )
		{
			String val = Global.WorldFlags.get( key );

			return !not && val.equalsIgnoreCase( value );
		}
		else if ( Global.RunFlags.containsKey( key ) )
		{
			String val = Global.RunFlags.get( key );

			return !not && val.equalsIgnoreCase( value );
		}
		else
		{
			return false;
		}
	}

	@Override
	public void parse( XmlReader.Element xml )
	{
		key = xml.get( "Key" ).toLowerCase();
		value = xml.get( "Value" ).toLowerCase();


		not = xml.getBooleanAttribute( "Not", false );
	}
}
