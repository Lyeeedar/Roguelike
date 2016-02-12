package Roguelike.Dialogue;

import Roguelike.Global;
import Roguelike.StatusEffect.StatusEffect;
import com.badlogic.gdx.utils.XmlReader;
import exp4j.Helpers.EquationHelper;

/**
 * Created by Philip on 12-Feb-16.
 */
public class DialogueActionAddStatus extends AbstractDialogueAction
{
	public XmlReader.Element xml;
	public String count;

	@Override
	public DialogueManager.ReturnType process()
	{
		int stacks = EquationHelper.evaluate( count );
		for (int i = 0; i < stacks; i++)
		{
			StatusEffect effect = StatusEffect.load( xml, Global.CurrentDialogue );
			Global.CurrentLevel.player.addStatusEffect( effect );
		}

		return DialogueManager.ReturnType.ADVANCE;
	}

	@Override
	public void parse( XmlReader.Element xml )
	{
		this.xml = xml;
		count = xml.getAttribute( "Stacks", "1" ).toLowerCase();
	}
}
