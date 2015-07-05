package Roguelike.Ability.ActiveAbility.TargetingType;

import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.XmlReader.Element;

public class TargetingTypeSelf extends AbstractTargetingType
{
	@Override
	public void parse(Element xml)
	{
	}

	@Override
	public boolean isTargetValid(ActiveAbility ab, GameTile tile)
	{
		return tile.Entity == ab.caster;
	}

	
	@Override
	public AbstractTargetingType copy()
	{
		return new TargetingTypeSelf();
	}

}
