package Roguelike.Quests;

import Roguelike.Global;
import Roguelike.Quests.Input.AbstractQuestInput;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.XmlReader;

import java.io.IOException;
import java.util.Random;

/**
 * Created by Philip on 23-Jan-16.
 */
public class QuestManager
{
	public Array<Quest> availableQuests = new Array<Quest>(  );
	public ObjectSet<String> usedQuests = new ObjectSet<String>(  );

	public ObjectMap<String, String> deferredFlags = new ObjectMap<String, String>(  );

	public QuestManager()
	{
		XmlReader reader = new XmlReader();
		XmlReader.Element xml = null;

		try
		{
			xml = reader.parse( Gdx.files.internal( "Quests/QuestList.xml" ) );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}

		for (int i = 0; i < xml.getChildCount(); i++)
		{
			XmlReader.Element questEl = xml.getChild( i );
			Quest quest = Quest.load( questEl.getText() );
			availableQuests.add( quest );
		}
	}

	public Quest getQuest( String level, Random ran )
	{
		Array<Quest> validQuests = new Array<Quest>(  );
		for (Quest quest : availableQuests)
		{
			if (!usedQuests.contains(quest.path) &&
				(quest.allowedLevels.contains( "all" ) || quest.allowedLevels.contains( level )) &&
				quest.evaluateInputs())
			{
				validQuests.add( quest );
			}
		}

		if (validQuests.size == 0)
		{
			return null;
		}

		Array<Quest> runQuests = new Array<Quest>(  );
		for (Quest quest : validQuests)
		{
			boolean run = false;

			for ( AbstractQuestInput input : quest.inputs )
			{
				if ( Global.RunFlags.containsKey( input.key ) )
				{
					run = true;
					break;
				}
			}

			if (run)
			{
				runQuests.add( quest );
			}
		}

		Quest chosen = null;
		if (runQuests.size > 0)
		{
			chosen = runQuests.get( ran.nextInt( runQuests.size ) );
		}
		else
		{
			chosen = validQuests.get( ran.nextInt( validQuests.size ) );
		}

		usedQuests.add( chosen.path );

		return chosen;
	}
}
