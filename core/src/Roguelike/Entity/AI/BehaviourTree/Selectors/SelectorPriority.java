package Roguelike.Entity.AI.BehaviourTree.Selectors;

import Roguelike.Entity.Entity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;

// A selector that will run through the nodes until the first non-failed node is found
public class SelectorPriority extends AbstractSelector
{		
	//####################################################################//
	//region Public Methods
	
	//----------------------------------------------------------------------
	@Override
	public BehaviourTreeState evaluate(Entity entity)
	{
		BehaviourTreeState state = BehaviourTreeState.FAILED;
		
		int i = 0;
		for (; i < nodes.size; i++)
		{
			BehaviourTreeState temp = nodes.get(i).evaluate(entity);
			if (temp != BehaviourTreeState.FAILED)
			{
				state = temp;
				break;
			}
		}
		i++;
		for (; i < nodes.size; i++)
		{
			nodes.get(i).cancel();
		}
		
		this.State = state;
		return state;
	}
	
	//----------------------------------------------------------------------
	@Override
	public void cancel()
	{
		for (int i = 0; i < nodes.size; i++)
		{
			nodes.get(i).cancel();
		}
	}
		
	//endregion Public Methods
	//####################################################################//
}
