package Roguelike.Entity.AI.AbilityAI;

import Roguelike.Global.Direction;
import Roguelike.Global.Statistics;
import Roguelike.Entity.ActiveAbility;
import Roguelike.Pathfinding.BresenhamLine;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public class AbilityAIBolt extends AbstractAbilityAI
{
	int[][] path;
	int i = 0;
	
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
		GameTile currentTile = ab.AffectedTiles.get(0).Tile;
		
		if (currentTile.Entity != null && !currentTile.Entity.isAllies(ab.Caster))
		{
			ab.advanceAI();
			i = path.length;
		}
		else if (i < path.length-1)
		{
			GameTile nextTile = ab.level.getGameTile(path[i+1]);
			int[] offset = new int[]{ path[i+1][0] - path[i][0], path[i+1][1] - path[i][1] };
			Direction d = Direction.getDirection(offset);
			ab.AffectedTiles.clear();
			ab.addAffectedTile(nextTile, d.GetAngle());
			
			if (!nextTile.TileData.Passable)
			{
				ab.advanceAI();
			}
			else if (nextTile.Entity != null && !nextTile.Entity.isAllies(ab.Caster))
			{
				ab.advanceAI();
				i = path.length;
			}
			else
			{
				i++;
			}
		}
		else
		{
			ab.advanceAI();
		}
	}

	@Override
	public void parse(Element xml)
	{
	}

	@Override
	public AbstractAbilityAI copy()
	{
		return new AbilityAIBolt();
	}
}
