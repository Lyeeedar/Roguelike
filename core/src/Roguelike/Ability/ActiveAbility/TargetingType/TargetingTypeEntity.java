package Roguelike.Ability.ActiveAbility.TargetingType;

import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.XmlReader.Element;

public class TargetingTypeEntity extends AbstractTargetingType
{
	@Override
	public void parse(Element xml)
	{

	}

	@Override
	public boolean isTargetValid(ActiveAbility ab, GameTile tile)
	{
		return tile.entity != null;
	}


	@Override
	public AbstractTargetingType copy()
	{
		TargetingTypeEntity t = new TargetingTypeEntity();

		return t;
	}

}
