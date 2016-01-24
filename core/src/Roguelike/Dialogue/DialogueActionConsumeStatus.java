package Roguelike.Dialogue;

import Roguelike.Entity.GameEntity;
import Roguelike.Global;
import com.badlogic.gdx.utils.XmlReader;

/**
 * Created by Philip on 24-Jan-16.
 */
public class DialogueActionConsumeStatus extends AbstractDialogueAction
{
	public String key;

	@Override
	public DialogueManager.ReturnType process()
	{
		GameEntity e = Global.CurrentDialogue;
		e.removeStatusEffect( key );

		return DialogueManager.ReturnType.ADVANCE;
	}

	@Override
	public void parse( XmlReader.Element xml )
	{
		key = xml.getText();
	}
}
