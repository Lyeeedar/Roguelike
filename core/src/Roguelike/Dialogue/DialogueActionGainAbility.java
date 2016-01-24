package Roguelike.Dialogue;

import Roguelike.Ability.AbilityTree;
import Roguelike.Global;
import Roguelike.Items.Item;
import Roguelike.Items.TreasureGenerator;
import Roguelike.Screens.GameScreen;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.XmlReader;

/**
 * Created by Philip on 23-Jan-16.
 */
public class DialogueActionGainAbility extends AbstractDialogueAction
{
	private String ability;

	@Override
	public DialogueManager.ReturnType process()
	{
		if (ability.toLowerCase().startsWith( "random" ))
		{
			String[] abilityParts = ability.toLowerCase().split( "[\\(\\)]" );
			String[] tags = new String[]{};

			if (abilityParts.length > 1)
			{
				tags = abilityParts[1].split( "," );
			}

			GameScreen.Instance.pickupQueue.addAll( TreasureGenerator.generateAbility( Global.getQuality(), MathUtils.random, tags ) );
		}
		else
		{
			AbilityTree tree = new AbilityTree( ability );
			Item item = new Item();
			item.ability = tree;

			GameScreen.Instance.pickupQueue.add( item );
		}

		return DialogueManager.ReturnType.ADVANCE;
	}

	@Override
	public void parse( XmlReader.Element xml )
	{
		ability = xml.getText();
	}

}