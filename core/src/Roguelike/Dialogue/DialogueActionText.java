package Roguelike.Dialogue;

import Roguelike.Global;
import Roguelike.Dialogue.DialogueManager.ReturnType;
import Roguelike.Sound.SoundInstance;

import com.badlogic.gdx.utils.XmlReader.Element;

public class DialogueActionText extends AbstractDialogueAction
{
	public String text;
	public SoundInstance sound;

	@Override
	public ReturnType process()
	{
		manager.popupText = Global.expandNames( text );
		manager.soundToBePlayed = sound;

		return ReturnType.COMPLETED;
	}

	@Override
	public void parse( Element xml )
	{
		if ( xml.getChildCount() == 0 )
		{
			text = xml.getText();
		}
		else
		{
			Element soundEl = xml.getChildByName( "Sound" );
			if ( soundEl != null )
			{
				sound = SoundInstance.load( soundEl );
			}

			text = xml.get( "Text" );
		}
	}

}
