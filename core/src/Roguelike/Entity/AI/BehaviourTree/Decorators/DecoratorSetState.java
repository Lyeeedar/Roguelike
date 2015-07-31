package Roguelike.Entity.AI.BehaviourTree.Decorators;

import com.badlogic.gdx.utils.XmlReader.Element;

import Roguelike.Entity.GameEntity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;

public class DecoratorSetState extends AbstractDecorator
{
	private BehaviourTreeState succeed;
	private BehaviourTreeState failed;
	private BehaviourTreeState running;
	
	@Override
	public BehaviourTreeState evaluate(GameEntity entity)
	{
		BehaviourTreeState retState = node.evaluate(entity);
		
		if (retState == BehaviourTreeState.SUCCEEDED)
		{
			State = succeed;
		}
		else if (retState == BehaviourTreeState.RUNNING)
		{
			State = running;
		}
		else if (retState == BehaviourTreeState.FAILED)
		{
			State = failed;
		}
		
		return State;
	}

	@Override
	public void parse(Element xmlElement)
	{
		super.parse(xmlElement);
		
		succeed = failed = running = BehaviourTreeState.valueOf(xmlElement.getAttribute("State").toUpperCase());
		
		if (xmlElement.get("Succeed", null) != null) { succeed = BehaviourTreeState.valueOf(xmlElement.getAttribute("Succeed").toUpperCase()); }
		if (xmlElement.get("Running", null) != null) { running = BehaviourTreeState.valueOf(xmlElement.getAttribute("Running").toUpperCase()); }
		if (xmlElement.get("Failed", null) != null) { failed = BehaviourTreeState.valueOf(xmlElement.getAttribute("Failed").toUpperCase()); }
	}
}
