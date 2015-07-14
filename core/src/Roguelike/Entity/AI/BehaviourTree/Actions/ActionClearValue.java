package Roguelike.Entity.AI.BehaviourTree.Actions;

import Roguelike.Entity.GameEntity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;

import com.badlogic.gdx.utils.XmlReader.Element;

public class ActionClearValue extends AbstractAction
{
	private String key;

	@Override
	public BehaviourTreeState evaluate(GameEntity entity)
	{
		Parent.setDataTree(key, null);
		
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