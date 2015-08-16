package Roguelike.Ability.ActiveAbility.CostType;

import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Items.Item;
import Roguelike.Items.Item.EquipmentSlot;

import com.badlogic.gdx.utils.XmlReader.Element;

public class CostTypeEquipped extends AbstractCostType
{
	private String equipped;

	@Override
	public boolean isCostAvailable(ActiveAbility aa)
	{
		for (EquipmentSlot slot : EquipmentSlot.values())
		{
			Item item = aa.caster.getInventory().getEquip(slot);
			if (item != null && item.type != null)
			{
				if (item.type.equals(equipped))
				{
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public String getCostString(ActiveAbility aa)
	{
		String colour = isCostAvailable(aa) ? "[GREEN]" : "[RED]";	
		return colour+"Requires "+equipped+" equipped.";
	}

	@Override
	public void spendCost(ActiveAbility aa)
	{
	}

	@Override
	public void parse(Element xml)
	{
		equipped = xml.getText().toLowerCase();
	}

	@Override
	public AbstractCostType copy()
	{
		CostTypeEquipped cost = new CostTypeEquipped();
		cost.equipped = equipped;
		
		return cost;
	}

}
