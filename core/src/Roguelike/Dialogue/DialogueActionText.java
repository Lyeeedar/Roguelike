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
		manager.entity.setPopupText( Global.expandNames( text ), 2 );

		if ( sound != null )
		{
			sound.play( manager.entity.tile );
		}

		return ReturnType.COMPLETED;
	}

	@Override
	public void parse( Element xml )
	{
		text = xml.getText();

		Element soundEl = xml.getChildByName( "Sound" );
		if ( soundEl != null )
		{
			sound = SoundInstance.load( soundEl );
		}

		text = xml.get( "Text" );
	}

}
