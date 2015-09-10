package Roguelike.Dialogue;

import Roguelike.Global;
import Roguelike.Dialogue.DialogueManager.ReturnType;

import com.badlogic.gdx.utils.XmlReader.Element;

public class DialogueActionSetVariable extends AbstractDialogueAction
{
	private boolean isGlobal;
	private String key;
	private int value;

	@Override
	public ReturnType process()
	{
		if ( isGlobal )
		{
			Global.GlobalVariables.put( key, value );
		}
		else
		{
			manager.data.put( key, value );
		}

		return ReturnType.ADVANCE;
	}

	@Override
	public void parse( Element xml )
	{
		isGlobal = xml.getBoolean( "Global", false );
		key = xml.get( "Key" ).toLowerCase();
		value = xml.getInt( "Value" );
	}

}
