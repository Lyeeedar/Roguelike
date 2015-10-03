package Roguelike.Dialogue;

import Roguelike.Dialogue.DialogueManager.ReturnType;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public class DialogueActionInput extends AbstractDialogueAction
{
	public String key;
	public Array<String> choices = new Array<String>();

	public int answer = -1;

	@Override
	public ReturnType process()
	{
		if ( answer != -1 )
		{
			manager.data.put( key, answer );
			manager.currentInput = null;

			answer = -1;

			return ReturnType.ADVANCE;
		}
		else
		{
			manager.currentInput = this;

			return ReturnType.RUNNING;
		}
	}

	@Override
	public void parse( Element xml )
	{
		key = xml.getAttribute( "Key", "Response" ).toLowerCase();

		for ( int i = 0; i < xml.getChildCount(); i++ )
		{
			Element el = xml.getChild( i );

			choices.add( el.getText() );
		}
	}
}
