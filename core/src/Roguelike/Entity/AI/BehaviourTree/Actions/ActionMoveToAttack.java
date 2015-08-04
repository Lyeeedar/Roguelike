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

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public class ActionMoveToAttack extends AbstractAction
{
	//----------------------------------------------------------------------
	public static final Array<Passability> WeaponPassability = new Array<Passability>(new Passability[]{Passability.LEVITATE});
	
	public String key;
	
	@Override
	public BehaviourTreeState evaluate(GameEntity entity)
	{
		int[] target = (int[])getData(key, null);
		
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
		
		Array<int[]> possibleTiles = new Array<int[]>();
		
		for (Direction dir : Direction.values())
		{
			if (dir == Direction.CENTER) { continue; }
			
			for (int i = 0; i < range; i++)
			{
				int[] newPos = {target[0] + dir.GetX()*(i+1), target[1] + dir.GetY()*(i+1)};
				GameTile tile = entity.tile.level.getGameTile(newPos);
				
				if (!tile.getPassable(WeaponPassability))
				{
					break;
				}
				
				possibleTiles.add(newPos);
			}
		}
		
		int bestDist = Integer.MAX_VALUE;
		int[][] bestPath = null;
		
		for (int[] pos : possibleTiles)
		{
			if (pos[0] == entity.tile.x && pos[1] == entity.tile.y)
			{
				State = BehaviourTreeState.SUCCEEDED;
				return State;
			}
			
			if (!entity.tile.level.getGameTile(pos).getPassable(entity.getTravelType()))
			{
				continue;
			}
			
			Pathfinder pathFinder = new Pathfinder(entity.tile.level.getGrid(), entity.tile.x, entity.tile.y, pos[0], pos[1], true);
			int[][] path = pathFinder.getPath(entity.getTravelType());
			
			if (path.length > 1 && path.length < bestDist)
			{
				if (entity.tile.level.getGameTile(path[1]).environmentEntity != null || entity.tile.level.getGameTile(path[1]).getPassable(entity.getTravelType()))
				{
					bestDist = path.length;
					bestPath = path;
				}
			}
		}
		
		if (bestPath == null)
		{
			State = BehaviourTreeState.FAILED;
			return State;
		}
		
		int[] offset = new int[]{ bestPath[1][0] - bestPath[0][0], bestPath[1][1] - bestPath[0][1] };
		
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
