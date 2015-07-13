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
	private float accumulator;
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
		int[][] fullpath = BresenhamLine.line(ab.caster.Tile.x, ab.caster.Tile.y, endx, endy, ab.caster.Tile.Level.getGrid(), false, true, new HashSet<String>());
		
		Array<int[]> actualpath = new Array<int[]>(fullpath.length);
		for (int i = 1; i < ab.range+1 && i < fullpath.length; i++)
		{
			actualpath.add(fullpath[i]);
		}
		path = actualpath.toArray(int[].class);
		
		ab.AffectedTiles.clear();
		ab.AffectedTiles.add(ab.caster.Tile.Level.getGameTile(path[0]));
	}
	
	@Override
	public boolean update(ActiveAbility ab)
	{	
		if (ab.AffectedTiles.peek().Entity != null)
		{
			i = path.length;
			return true;
		}
		
		float step = 1.0f / speed;
		if (accumulator >= 0)
		{
			if (i < path.length-1)
			{
				GameTile nextTile = ab.AffectedTiles.peek().Level.getGameTile(path[i+1]);				
				direction = Direction.getDirection(ab.AffectedTiles.peek(), nextTile);								
								
				if (!nextTile.TileData.Passable)
				{
					i = path.length;
					return true;
				}
				
				ab.AffectedTiles.clear();
				ab.AffectedTiles.add(nextTile);
				
				if (nextTile.Entity != null)
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
