package Roguelike.Ability.ActiveAbility.TargetingType;

import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.XmlReader.Element;

public class TargetingTypeDirection extends AbstractTargetingType
{

	@Override
	public void parse(Element xml)
	{
	}

	@Override
	public boolean isTargetValid(ActiveAbility ab, GameTile tile)
	{
		if (!tile.GetVisible()) { return false; }
		
		int x = tile.x - ab.caster.Tile.x;
		int y = tile.y - ab.caster.Tile.y;
		
		if (x == 0 && y == 0) { return false; }
		
		if (
			(x == 0 && y != 0) || // vertical
			(x != 0 && y == 0) || // horizontal
			(x == y) || // SW / NE
			(x == y*-1) // SE / NW
			)
		{
			return true;
		}

		return false;
	}
	
	@Override
	public AbstractTargetingType copy()
	{
		return new TargetingTypeDirection();
	}

}
