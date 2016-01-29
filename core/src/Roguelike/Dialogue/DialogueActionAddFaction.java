package Roguelike.Dialogue;

import Roguelike.Global;
import com.badlogic.gdx.utils.XmlReader;

/**
 * Created by Philip on 28-Jan-16.
 */
public class DialogueActionAddFaction extends AbstractDialogueAction
{
	public String faction;

	@Override
	public DialogueManager.ReturnType process()
	{
		Global.CurrentDialogue.factions.add( faction );

		return DialogueManager.ReturnType.ADVANCE;
	}

	@Override
	public void parse( XmlReader.Element xml )
	{
		faction = xml.getText().toLowerCase();
	}
}
