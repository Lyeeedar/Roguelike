package Roguelike.Entity.AI.BehaviourTree.Actions;

import Roguelike.Global.Direction;
import Roguelike.Entity.GameEntity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;
import Roguelike.Entity.Tasks.TaskAttack;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.XmlReader.Element;

public class ActionAttack extends AbstractAction
{
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
		
		TaskAttack task = new TaskAttack(Direction.getDirection(target[0]-entity.tile.x, target[1]-entity.tile.y));
		GameTile targetTile = entity.tile.level.getGameTile(target);
		if (targetTile.environmentEntity != null && targetTile.environmentEntity.canTakeDamage)
		{
			entity.tasks.add(task);
			
			State = BehaviourTreeState.SUCCEEDED;
			return State;
		}
		else if (task.checkHitSomething(entity))
		{
			entity.tasks.add(task);
			
			State = BehaviourTreeState.SUCCEEDED;
			return State;
		}
		
		State = BehaviourTreeState.FAILED;
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
