package Roguelike.Entity.AI.BehaviourTree.Decorators;

import com.badlogic.gdx.utils.XmlReader.Element;

import Roguelike.Entity.GameEntity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;

public class DecoratorSetState extends AbstractDecorator
{
	@Override
	public BehaviourTreeState evaluate(GameEntity entity)
	{
		node.evaluate(entity);
		return State;
	}

	@Override
	public void parse(Element xmlElement)
	{
		super.parse(xmlElement);
		
		State = BehaviourTreeState.valueOf(xmlElement.getAttribute("State").toUpperCase());
	}
}
