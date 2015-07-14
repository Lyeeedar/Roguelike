package Roguelike.Entity.AI.BehaviourTree.Decorators;

import java.util.HashMap;

import Roguelike.Entity.GameEntity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;

public class DecoratorDataScope extends AbstractDecorator
{
	public DecoratorDataScope()
	{
		Data = new HashMap<String, Object>();
	}
	
	@Override
	public BehaviourTreeState evaluate(GameEntity entity)
	{
		return node.evaluate(entity);
	}

}
