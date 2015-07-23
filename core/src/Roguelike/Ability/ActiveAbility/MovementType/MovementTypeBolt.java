package Roguelike.Ability.ActiveAbility.MovementType;

import java.util.HashSet;

import Roguelike.Global.Direction;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Pathfinding.BresenhamLine;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public class MovementTypeBolt extends AbstractMovementType
{
	private float accumulator = 1;
	private int speed;

	@Override
	public void parse(Element xml)
	{		
		speed = xml.getInt("Speed", 1);
	}
	
	int[][] path;
	int i;

	@Override
	public void init(ActiveAbility ab, int endx, int endy)
	{
		int[][] fullpath = BresenhamLine.line(ab.caster.tile.x, ab.caster.tile.y, endx, endy, ab.caster.tile.level.getGrid(), false, true, new HashSet<String>());
		
		Array<int[]> actualpath = new Array<int[]>(fullpath.length);
		for (int i = 1; i < ab.range+1 && i < fullpath.length; i++)
		{
			actualpath.add(fullpath[i]);
		}
		path = actualpath.toArray(int[].class);
		
		ab.AffectedTiles.clear();
		ab.AffectedTiles.add(ab.caster.tile.level.getGameTile(path[0]));
	}
	
	@Override
	public boolean update(ActiveAbility ab)
	{	
		if (ab.AffectedTiles.peek().entity != null)
		{
			i = path.length;
			return true;
		}
		
		float step = 1.0f / speed;
		if (accumulator >= 0)
		{
			if (i < path.length-1)
			{
				GameTile nextTile = ab.AffectedTiles.peek().level.getGameTile(path[i+1]);				
				direction = Direction.getDirection(ab.AffectedTiles.peek(), nextTile);								
								
				if (!nextTile.tileData.Passable)
				{
					i = path.length;
					return true;
				}
				
				ab.AffectedTiles.clear();
				ab.AffectedTiles.add(nextTile);
				
				if (nextTile.entity != null)
				{
					i = path.length;
					return true;
				}
				else
				{
					i++;
					
					if (i == path.length-1)
					{
						return true;
					}
				}
			}
			else
			{
				return true;
			}
			
			accumulator -= step;
		}
		
		return false;
	}

	
	@Override
	public AbstractMovementType copy()
	{
		MovementTypeBolt t = new MovementTypeBolt();
		t.speed = speed;
		t.accumulator = accumulator;
		t.path = path;
		t.i = i;
		
		return t;
	}

	
	@Override
	public void updateAccumulators(float cost)
	{
		accumulator += cost;
	}

	
	@Override
	public boolean needsUpdate()
	{
		return accumulator > 0;
	}
}
