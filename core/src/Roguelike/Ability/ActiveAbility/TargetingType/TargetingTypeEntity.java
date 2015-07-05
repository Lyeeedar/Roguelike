package Roguelike.Ability.ActiveAbility.TargetingType;

import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.XmlReader.Element;

public class TargetingTypeEntity extends AbstractTargetingType
{
	private boolean notSelf;
	
	@Override
	public void parse(Element xml)
	{
		notSelf = xml.getChildByName("NotSelf") != null;
	}

	@Override
	public boolean isTargetValid(ActiveAbility ab, GameTile tile)
	{
		if (!tile.GetVisible()) { return false; }
		
		if (notSelf)
		{
			return tile.Entity != null && tile.Entity != ab.caster;
		}
		else
		{
			return tile.Entity != null;
		}
	}

	
	@Override
	public AbstractTargetingType copy()
	{
		TargetingTypeEntity t = new TargetingTypeEntity();
		t.notSelf = notSelf;
		
		return t;
	}

}
