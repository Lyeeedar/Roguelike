package Roguelike.Ability.ActiveAbility.TargetingType;

import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.XmlReader.Element;

public class TargetingTypeTile extends AbstractTargetingType
{
	public enum TileType
	{
		ANY,
		NOTSELF,
		EMPTY
	}

	public TileType type;

	@Override
	public void parse(Element xml)
	{
		type = TileType.valueOf( xml.getAttribute( "Type", "Any" ).toUpperCase() );
	}

	@Override
	public boolean isTargetValid(ActiveAbility ab, GameTile tile)
	{
		if (type == TileType.EMPTY)
		{
			return tile.entity == null;
		}
		else if ( type != TileType.NOTSELF )
		{
			return tile.entity == null || tile.entity != ab.getCaster();
		}
		else
		{
			return true;
		}
	}


	@Override
	public AbstractTargetingType copy()
	{
		TargetingTypeTile t = new TargetingTypeTile();
		t.type = type;

		return t;
	}
}
