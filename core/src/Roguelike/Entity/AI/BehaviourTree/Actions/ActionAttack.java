package Roguelike.Entity.AI.BehaviourTree.Actions;

import Roguelike.Global.Direction;
import Roguelike.Entity.GameEntity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;
import Roguelike.Entity.Tasks.TaskAttack;
import Roguelike.Tiles.GameTile;
import Roguelike.Tiles.Point;

import com.badlogic.gdx.utils.XmlReader.Element;

public class ActionAttack extends AbstractAction
{
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
		
		TaskAttack task = new TaskAttack(Direction.getDirection(target.x-entity.tile.x, target.y-entity.tile.y));
		GameTile targetTile = entity.tile.level.getGameTile(target);
		if (targetTile.environmentEntity != null && targetTile.environmentEntity.canTakeDamage && task.canAttackTile(entity, targetTile))
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
