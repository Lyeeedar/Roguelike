package Roguelike.Ability.ActiveAbility.MovementType;

import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Pathfinding.BresenhamLine;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.XmlReader.Element;

public class MovementTypeRay extends AbstractMovementType
{

	@Override
	public void parse(Element xml)
	{
	}

	@Override
	public void init(ActiveAbility ab, int endx, int endy)
	{
		int[][] path = BresenhamLine.line(ab.source.x, ab.source.y, endx, endy, ab.source.level.getGrid(), false, ab.range);
		
		ab.AffectedTiles.clear();
		
		for (int i = 1; i < path.length; i++)
		{
			int[] p = path[i];
			GameTile tile = ab.source.level.getGameTile(p[0], p[1]);
			
			if (tile.GetOpaque()) { break; }
			
			ab.AffectedTiles.add(tile);
		}	
	}

	@Override
	public boolean update(ActiveAbility ab)
	{
		return true;
	}

	
	@Override
	public AbstractMovementType copy()
	{
		MovementTypeRay t = new MovementTypeRay();
		return t;
	}

	@Override
	public void updateAccumulators(float cost)
	{
	}

	
	@Override
	public boolean needsUpdate()
	{
		return false;
	}

}
