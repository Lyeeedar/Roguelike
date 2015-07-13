package Roguelike.Entity.AI.BehaviourTree.Actions;

import Roguelike.Global.Direction;
import Roguelike.Entity.Entity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;
import Roguelike.Entity.Tasks.TaskMove;
import Roguelike.Pathfinding.Pathfinder;

import com.badlogic.gdx.utils.XmlReader.Element;

public class ActionMoveTo extends AbstractAction
{
	public int dst;
	public boolean towards;
	public String key;
	
	@Override
	public BehaviourTreeState evaluate(Entity entity)
	{
		int[] target = (int[])getData(key, null);
		
		// if no target, fail
		if (target == null)
		{
			State = BehaviourTreeState.FAILED;
			return State;
		}
		
		// if we arrived at our target, succeed
		if (entity.Tile.x == target[0] && entity.Tile.y == target[1])
		{			
			State = BehaviourTreeState.SUCCEEDED;
			return State;
		}
						
		Pathfinder pathFinder = new Pathfinder(entity.Tile.Level.getGrid(), entity.Tile.x, entity.Tile.y, target[0], target[1], true, entity.m_factions);
		int[][] path = pathFinder.getPath();
		
		// if couldnt find a valid path, fail
		if (path.length < 2)
		{
			State = BehaviourTreeState.FAILED;
			return State;
		}
		
		// if next step is impassable then fail
		if (!entity.Tile.Level.getGameTile(path[1]).GetPassable(entity.m_factions))
		{
			State = BehaviourTreeState.FAILED;
			return State;
		}
		
		int[] offset = new int[]{ path[1][0] - path[0][0], path[1][1] - path[0][1] };
		
		// if moving towards path to the object
		if (towards)
		{
			if (path.length-1 <= dst)
			{
				State = BehaviourTreeState.SUCCEEDED;
				return State;
			}
			
			entity.Tasks.add(new TaskMove(Direction.getDirection(offset)));
		}
		// if moving away then just run directly away
		else
		{
			if (path.length-1 >= dst)
			{
				State = BehaviourTreeState.SUCCEEDED;
				return State;
			}
			
			entity.Tasks.add(new TaskMove(Direction.getDirection(offset[0]*-1, offset[1]*-1)));
		}
		
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
		dst = Integer.parseInt(xmlElement.getAttribute("Distance", "0"));
		towards = Boolean.parseBoolean(xmlElement.getAttribute("Towards", "true"));
		key = xmlElement.getAttribute("Key");
	}
}
