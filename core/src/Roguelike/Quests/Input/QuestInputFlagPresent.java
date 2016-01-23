package Roguelike.Quests.Input;

import Roguelike.Global;
import com.badlogic.gdx.utils.XmlReader;

/**
 * Created by Philip on 23-Jan-16.
 */
public class QuestInputFlagPresent extends AbstractQuestInput
{
	private String key;
	private boolean not;

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
		key = xml.getText();
		not = xml.getBooleanAttribute( "Not", false );
	}
}
