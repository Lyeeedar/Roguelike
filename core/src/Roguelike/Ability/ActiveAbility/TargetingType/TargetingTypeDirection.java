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
		int x = tile.x - ab.source.x;
		int y = tile.y - ab.source.y;
		
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
