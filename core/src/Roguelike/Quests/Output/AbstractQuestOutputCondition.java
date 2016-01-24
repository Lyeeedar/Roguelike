package Roguelike.Quests.Output;

import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import java.util.HashMap;

/**
 * Created by Philip on 23-Jan-16.
 */
public abstract class AbstractQuestOutputCondition
{
	// ----------------------------------------------------------------------
	public abstract boolean evaluate();

	// ----------------------------------------------------------------------
	public abstract void parse( XmlReader.Element xml );

	// ----------------------------------------------------------------------
	public static AbstractQuestOutputCondition load( XmlReader.Element xml )
	{
		Class<AbstractQuestOutputCondition> c = ClassMap.get( xml.getName().toUpperCase() );
		AbstractQuestOutputCondition type = null;

		try
		{
			type = ClassReflection.newInstance( c );
		}
		catch ( ReflectionException e )
		{
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
		ClassMap.put( "ENTITYALIVE", QuestOutputConditionEntityAlive.class );
		ClassMap.put( "DIALOGUEVALUE", QuestOuputConditionDialogueValue.class );
	}
}
