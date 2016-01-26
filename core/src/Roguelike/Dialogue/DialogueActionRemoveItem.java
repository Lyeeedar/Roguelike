package Roguelike.Dialogue;

import Roguelike.Global;
import com.badlogic.gdx.utils.XmlReader;

/**
 * Created by Philip on 26-Jan-16.
 */
public class DialogueActionRemoveItem extends AbstractDialogueAction
{
	public String name;
	public int count;

	@Override
	public DialogueManager.ReturnType process()
	{
		Global.CurrentLevel.player.inventory.removeItem( name, count );

		return DialogueManager.ReturnType.ADVANCE;
	}

	@Override
	public void parse( XmlReader.Element xml )
	{
		name = xml.getText();
		count = xml.getIntAttribute( "Count", 0 );
	}
}
