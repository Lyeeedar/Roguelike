package Roguelike.Entity.ActivationAction;

import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Global;
import com.badlogic.gdx.utils.XmlReader;

/**
 * Created by Philip on 30-Jan-16.
 */
public class ActivationActionRemoveItem extends AbstractActivationAction
{
	public String itemName;
	public int count;

	public ActivationActionRemoveItem()
	{

	}

	public ActivationActionRemoveItem(String itemName, int count)
	{
		this.itemName = itemName;
		this.count = count;
	}

	@Override
	public void evaluate( EnvironmentEntity entity, float delta )
	{
		Global.CurrentLevel.player.inventory.removeItem( itemName, count );
	}

	@Override
	public void parse( XmlReader.Element xml )
	{
		itemName = xml.getText();
		count = xml.getIntAttribute( "Count", 1 );
	}
}
