package Roguelike.Entity.AI.BehaviourTree.Actions;

import com.badlogic.gdx.utils.XmlReader.Element;

import Roguelike.Entity.Entity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;

public class ActionSetValue extends AbstractAction
{
	private String value;
	private String key;

	@Override
	public BehaviourTreeState evaluate(Entity entity)
	{
		Parent.setDataTree(key, value);
		
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
		value = xmlElement.getAttribute("Value");
	}
}
