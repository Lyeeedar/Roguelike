package Roguelike.Entity.AI.BehaviourTree.Actions;

import com.badlogic.gdx.utils.XmlReader.Element;

import Roguelike.Entity.GameEntity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;
import Roguelike.Entity.Tasks.TaskWait;

public class ActionWait extends AbstractAction
{
	@Override
	public BehaviourTreeState evaluate(GameEntity entity)
	{
		entity.Tasks.add(new TaskWait());
		
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
	}
}
