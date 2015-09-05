package Roguelike.Dialogue;

import java.util.HashMap;

import Roguelike.Dialogue.DialogueManager.ReturnType;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public abstract class AbstractDialogueAction
{
	// ----------------------------------------------------------------------
	public DialogueManager manager;

	// ----------------------------------------------------------------------
	public abstract ReturnType process();

	// ----------------------------------------------------------------------
	public abstract void parse( Element xml );

	// ----------------------------------------------------------------------
	public static AbstractDialogueAction load( Element xml, DialogueManager manager )
	{
		Class<AbstractDialogueAction> c = ClassMap.get( xml.getName().toUpperCase() );
		AbstractDialogueAction type = null;

		try
		{
			type = ClassReflection.newInstance( c );
		}
		catch ( ReflectionException e )
		{
			e.printStackTrace();
		}

		type.manager = manager;
		type.parse( xml );

		return type;
	}

	// ----------------------------------------------------------------------
	protected static HashMap<String, Class> ClassMap = new HashMap<String, Class>();

	// ----------------------------------------------------------------------
	static
	{
		ClassMap.put( "TEXT", DialogueActionText.class );
		ClassMap.put( "INPUT", DialogueActionInput.class );
		ClassMap.put( "BRANCH", DialogueActionBranch.class );
	}
}
