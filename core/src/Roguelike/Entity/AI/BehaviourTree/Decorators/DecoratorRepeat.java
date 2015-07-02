package Roguelike.Entity.AI.BehaviourTree.Decorators;

import com.badlogic.gdx.utils.XmlReader.Element;

import Roguelike.Entity.Entity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;

public class DecoratorRepeat extends AbstractDecorator
{
	
	private BehaviourTreeState UntilState;
	private int Repeats;
	private int i = 0;

	@Override
	public BehaviourTreeState evaluate(Entity entity)
	{
		BehaviourTreeState retState = node.evaluate(entity);
		
		if (UntilState != null)
		{
			if (retState == UntilState)
			{
				return BehaviourTreeState.SUCCEEDED;
			}
		}
		
		i++;
		
		if (i == Repeats)
		{
			return BehaviourTreeState.SUCCEEDED;
		}
		
		return BehaviourTreeState.RUNNING;
	}

	@Override
	public void parse(Element xmlElement)
	{
		super.parse(xmlElement);
		
		if (xmlElement.getAttribute("Until", null) != null)
		{
			UntilState = BehaviourTreeState.valueOf(xmlElement.getAttribute("State").toUpperCase());
		}
		
		Repeats = xmlElement.getInt("Repeats", -1);
	}
}
