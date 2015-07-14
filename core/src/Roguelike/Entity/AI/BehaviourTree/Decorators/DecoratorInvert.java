package Roguelike.Entity.AI.BehaviourTree.Decorators;

import Roguelike.Entity.GameEntity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;

public class DecoratorInvert extends AbstractDecorator
{
	@Override
	public BehaviourTreeState evaluate(GameEntity entity)
	{
		BehaviourTreeState state = node.evaluate(entity);
		
		if (state == BehaviourTreeState.SUCCEEDED) { return BehaviourTreeState.FAILED; }
		else if (state == BehaviourTreeState.FAILED) { return BehaviourTreeState.SUCCEEDED; }
		else { return BehaviourTreeState.RUNNING; }
	}

}
