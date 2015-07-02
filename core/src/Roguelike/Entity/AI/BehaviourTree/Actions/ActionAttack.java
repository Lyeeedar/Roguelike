package Roguelike.Entity.AI.BehaviourTree.Actions;

import Roguelike.Global.Direction;
import Roguelike.Entity.Entity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;
import Roguelike.Entity.Tasks.TaskAttack;

import com.badlogic.gdx.utils.XmlReader.Element;

public class ActionAttack extends AbstractAction
{
	String key;

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
		
		// if no enemy at target, fail
		if (entity.Tile.Level.getGameTile(target).Entity == null || entity.Tile.Level.getGameTile(target).Entity.isAllies(entity))
		{
			State = BehaviourTreeState.FAILED;
			return State;
		}
		
		int[] offset = new int[]{ target[0] - entity.Tile.x, target[1] - entity.Tile.y };
		
		Direction d = Direction.getDirection(offset[0], offset[1]);
		
		if (d == Direction.CENTER)
		{
			State = BehaviourTreeState.FAILED;
			return State;
		}
		
		entity.Tasks.add(new TaskAttack(d));
		
		State = BehaviourTreeState.SUCCEEDED;
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
