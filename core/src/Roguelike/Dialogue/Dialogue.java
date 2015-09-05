package Roguelike.Dialogue;

import Roguelike.Dialogue.DialogueManager.ReturnType;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public class Dialogue
{
	// ----------------------------------------------------------------------
	public Array<AbstractDialogueAction> actions = new Array<AbstractDialogueAction>();
	public int index = 0;
	public DialogueManager manager;

	// ----------------------------------------------------------------------
	public ReturnType advance()
	{
		ReturnType returnType = actions.get( index ).process();
		if ( returnType == ReturnType.COMPLETED )
		{
			index++;
		}
		else if ( returnType == ReturnType.ADVANCE )
		{
			index++;
			return advance();
		}

		return returnType;
	}

	// ----------------------------------------------------------------------
	public void parse( Element xml )
	{
		for ( int i = 0; i < xml.getChildCount(); i++ )
		{
			Element child = xml.getChild( i );
			actions.add( AbstractDialogueAction.load( child, manager ) );
		}
	}

	// ----------------------------------------------------------------------
	public static Dialogue load( Element xml, DialogueManager manager )
	{
		Dialogue dialogue = new Dialogue();
		dialogue.manager = manager;
		dialogue.parse( xml );
		return dialogue;
	}
}
