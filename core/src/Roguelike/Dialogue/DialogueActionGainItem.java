package Roguelike.Dialogue;

import Roguelike.Global;
import Roguelike.Items.Item;
import Roguelike.Items.TreasureGenerator;
import Roguelike.Screens.GameScreen;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.XmlReader;

/**
 * Created by Philip on 24-Jan-16.
 */
public class DialogueActionGainItem extends AbstractDialogueAction
{
	private XmlReader.Element itemData;

	@Override
	public DialogueManager.ReturnType process()
	{
		if (itemData.getChildCount() > 0)
		{
			Item item = Item.load( itemData );
			GameScreen.Instance.pickupQueue.add( item );
		}
		else
		{
			GameScreen.Instance.pickupQueue.addAll( TreasureGenerator.generateLoot( Global.getQuality(), itemData.getText(), MathUtils.random ) );
		}

		return DialogueManager.ReturnType.ADVANCE;
	}

	@Override
	public void parse( XmlReader.Element xml )
	{
		itemData = xml;
	}
}
