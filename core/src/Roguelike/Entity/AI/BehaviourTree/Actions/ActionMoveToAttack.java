package Roguelike.Entity.AI.BehaviourTree.Actions;

import Roguelike.Entity.GameEntity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;
import Roguelike.Entity.Tasks.TaskMove;
import Roguelike.Global.Direction;
import Roguelike.Global.Passability;
import Roguelike.Global.Statistic;
import Roguelike.Items.Item;
import Roguelike.Items.Item.EquipmentSlot;
import Roguelike.Items.Item.WeaponType;
import Roguelike.Pathfinding.Pathfinder;
import Roguelike.Tiles.GameTile;
import Roguelike.Tiles.Point;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.XmlReader.Element;

public class ActionMoveToAttack extends AbstractAction
{
	//----------------------------------------------------------------------
	public static final Array<Passability> WeaponPassability = new Array<Passability>(new Passability[]{Passability.LEVITATE});
	
	public String key;
	
	@Override
	public BehaviourTreeState evaluate(GameEntity entity)
	{
		Point target = (Point)getData(key, null);
		
		// if no target, fail
		if (target == null)
		{
			State = BehaviourTreeState.FAILED;
			return State;
		}
		
		Item wep = entity.getInventory().getEquip(EquipmentSlot.MAINWEAPON);
		int range = 1;
		
		if (wep != null)
		{
			WeaponType type = wep.weaponType;

			range = wep.getStatistic(entity.getBaseVariableMap(), Statistic.RANGE);
			if (range == 0) 
			{ 
				if (type == WeaponType.SPEAR)
				{
					range = 2;
				}
				else if (type == WeaponType.BOW || type == WeaponType.WAND)
				{
					range = 4;
				}
				else
				{
					range = 1;
				}
			}
		}
		
		Array<Point> possibleTiles = new Array<Point>();
		
		for (Direction dir : Direction.values())
		{
			if (dir == Direction.CENTER) { continue; }
			
			for (int i = 0; i < range; i++)
			{
				Point newPos = Pools.obtain(Point.class).set(target.x + dir.getX()*(i+1), target.y + dir.getY()*(i+1));
				GameTile tile = entity.tile.level.getGameTile(newPos);
				
				if (!tile.getPassable(WeaponPassability))
				{
					break;
				}
				if (tile.getPassable(entity.getTravelType()))
				{
					possibleTiles.add(newPos);
				}
			}
		}
		
		int bestDist = Integer.MAX_VALUE;
		Array<Point> bestPath = null;
		
		for (Point pos : possibleTiles)
		{
			if (pos.x == entity.tile.x && pos.y == entity.tile.y)
			{
				Pools.freeAll(possibleTiles);
				State = BehaviourTreeState.SUCCEEDED;
				return State;
			}
			
			Pathfinder pathFinder = new Pathfinder(entity.tile.level.getGrid(), entity.tile.x, entity.tile.y, pos.x, pos.y, true);
			Array<Point> path = pathFinder.getPath(entity.getTravelType());
			
			if (path.size > 1 && path.size < bestDist)
			{
				if (entity.tile.level.getGameTile(path.get(1)).getPassable(entity.getTravelType()))
				{
					bestDist = path.size;
					
					if (bestPath != null)
					{
						Pools.freeAll(bestPath);
					}
					bestPath = path;
				}
			}
		}
		
		Pools.freeAll(possibleTiles);
		
		if (bestPath == null)
		{
			State = BehaviourTreeState.FAILED;
			return State;
		}
		
		int[] offset = new int[]{ bestPath.get(1).x - bestPath.get(0).x, bestPath.get(1).y - bestPath.get(0).y };
		
		Pools.freeAll(bestPath);
		
		entity.tasks.add(new TaskMove(Direction.getDirection(offset)));
		
		State = BehaviourTreeState.RUNNING;
		return State;
	}

	@Override
	public void cancel()
	{
	}

	@Override
	public void parse(Element xmlElement)
	{
		key = xmlElement.getAttribute("Key");
	}

}
