package Roguelike.Entity.ActivationAction;

import Roguelike.Entity.Entity;
import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Global;
import com.badlogic.gdx.utils.XmlReader;

/**
 * Created by Philip on 30-Jan-16.
 */
public class ActivationConditionHasItem extends AbstractActivationCondition
{
	public String itemName;
	public int count;

	public ActivationConditionHasItem()
	{

	}

	public ActivationConditionHasItem(String itemName, int count)
	{
		this.itemName = itemName;
		this.count = count;
	}

	@Override
	public boolean evaluate( EnvironmentEntity owningEntity, Entity activatingEntity, float delta )
	{
		if (Global.CurrentLevel.player.inventory.getItemCount( itemName ) >= count )
		{
			return true;
		}

		return false;
	}

	@Override
	public void parse( XmlReader.Element xml )
	{
		itemName = xml.getText();
		count = xml.getIntAttribute( "Count", 1 );
	}
}
