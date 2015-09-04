package Roguelike.Dialogue;

import Roguelike.Global;

import com.badlogic.gdx.utils.XmlReader.Element;

public class DialogueActionText extends AbstractDialogueAction
{
	public String text;

	@Override
	public boolean process()
	{
		manager.entity.popup = Global.expandNames( text );
		manager.entity.popupDuration = 2;

		return true;
	}

	@Override
	public void parse( Element xml )
	{
		text = xml.getText();
	}

}
