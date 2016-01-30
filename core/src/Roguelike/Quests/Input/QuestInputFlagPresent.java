package Roguelike.Quests.Input;

import Roguelike.Global;
import com.badlogic.gdx.utils.XmlReader;

/**
 * Created by Philip on 23-Jan-16.
 */
public class QuestInputFlagPresent extends AbstractQuestInput
{
	@Override
	public boolean evaluate()
	{
		if ( Global.WorldFlags.containsKey( key ) || Global.RunFlags.containsKey( key ) )
		{
			return !not;
		}

		return not;
	}

	@Override
	public void parse( XmlReader.Element xml )
	{
		key = xml.getText().toLowerCase();
		not = xml.getBooleanAttribute( "Not", false );
	}
}
