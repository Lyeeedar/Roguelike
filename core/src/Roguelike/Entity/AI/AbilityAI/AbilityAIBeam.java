package Roguelike.Entity.AI.AbilityAI;

import Roguelike.Global.Direction;
import Roguelike.Global.Statistics;
import Roguelike.Entity.ActiveAbility;
import Roguelike.Pathfinding.BresenhamLine;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public class AbilityAIBeam extends AbstractAbilityAI
{
	int[][] path;
	int furthestIndex = 0;
	
	public void Init(ActiveAbility ab, int targetx, int targety)
	{
		int[][] fullpath = BresenhamLine.line(ab.Caster.Tile.x, ab.Caster.Tile.y, targetx, targety, ab.level.getGrid(), false, true);
		
		Array<int[]> actualpath = new Array<int[]>(fullpath.length);
		for (int i = 0; i < ab.getStatistic(Statistics.RANGE) && i < fullpath.length; i++)
		{
			actualpath.add(fullpath[i]);
		}
		path = actualpath.toArray(int[].class);
	}

	@Override
	public void update(ActiveAbility ab)
	{
		ab.AffectedTiles.clear();
				
		int i = 0;
		for (; i < furthestIndex+1 && i+1 < path.length; i++)
		{
			GameTile nextTile = ab.level.getGameTile(path[i+1]);
			int[] offset = new int[]{ path[i+1][0] - path[i][0], path[i+1][1] - path[i][1] };
			Direction d = Direction.getDirection(offset);
			
			ab.addAffectedTile(nextTile, d.GetAngle());
			
			if (!nextTile.TileData.Passable)
			{
				break;
			}
			else if (nextTile.Entity != null && !nextTile.Entity.isAllies(ab.Caster))
			{
				break;
			}
		}
		furthestIndex = i;
	}

	@Override
	public void parse(Element xml)
	{
	}

	@Override
	public AbstractAbilityAI copy()
	{
		return new AbilityAIBeam();
	}
}
