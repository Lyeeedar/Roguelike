package Roguelike.Dialogue;

import Roguelike.Global;
import Roguelike.Dialogue.DialogueManager.ReturnType;

import com.badlogic.gdx.utils.XmlReader.Element;

public class DialogueActionText extends AbstractDialogueAction
{
	public String text;

	@Override
	public ReturnType process()
	{
		manager.entity.popup = Global.expandNames( text );
		manager.entity.popupDuration = 2;

		return ReturnType.COMPLETED;
	}

	@Override
	public void parse( Element xml )
	{
		text = xml.getText();
	}

}
