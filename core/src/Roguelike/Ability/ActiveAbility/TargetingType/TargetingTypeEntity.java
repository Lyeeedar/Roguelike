package Roguelike.Ability.ActiveAbility.TargetingType;

import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.XmlReader.Element;

public class TargetingTypeEntity extends AbstractTargetingType
{
	public enum TargetType
	{
		ALLY,
		ENEMY,
		BOTH
	}

	private boolean notSelf;
	private TargetType targetType;

	@Override
	public void parse(Element xml)
	{
		notSelf = xml.getBooleanAttribute("NotSelf", false);
		targetType = TargetType.valueOf(xml.getAttribute("Type", "BOTH").toUpperCase());
	}

	@Override
	public boolean isTargetValid(ActiveAbility ab, GameTile tile)
	{
		if (tile.entity == null) { return false; }
		if (notSelf && tile.entity == ab.getCaster()) { return false; }

		if (targetType == TargetType.ALLY)
		{
			return tile.entity.isAllies(ab.getCaster());
		}
		else if (targetType == TargetType.ENEMY)
		{
			return !tile.entity.isAllies(ab.getCaster());
		}
		else
		{
			return true;
		}
	}


	@Override
	public AbstractTargetingType copy()
	{
		TargetingTypeEntity t = new TargetingTypeEntity();
		t.notSelf = notSelf;
		t.targetType = targetType;

		return t;
	}

}
