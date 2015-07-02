package Roguelike.Entity.AI.BehaviourTree.Selectors;

import java.util.Iterator;

import com.badlogic.gdx.utils.Array;

import Roguelike.Entity.Entity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;

public class SelectorAny extends AbstractSelector
{
	Array<Integer> runningList = new Array<Integer>();
	
	@Override
	public BehaviourTreeState evaluate(Entity entity)
	{
		BehaviourTreeState state = BehaviourTreeState.FAILED;
		
		if (runningList.size > 0)
		{
			Iterator<Integer> itr = runningList.iterator();
			while (itr.hasNext())
			{
				BehaviourTreeState temp = nodes.get(itr.next()).evaluate(entity);
				if (state != BehaviourTreeState.RUNNING && temp == BehaviourTreeState.SUCCEEDED)
				{
					state = temp;
					itr.remove();
				}
				else if (temp == BehaviourTreeState.RUNNING)
				{
					state = temp;
				}
				else
				{
					itr.remove();
				}
			}
		}
		else
		{
			for (int i = 0; i < nodes.size; i++)
			{
				BehaviourTreeState temp = nodes.get(i).evaluate(entity);
				if (state != BehaviourTreeState.RUNNING && temp == BehaviourTreeState.SUCCEEDED)
				{
					state = temp;
				}
				else if (temp == BehaviourTreeState.RUNNING)
				{
					state = temp;
					runningList.add(i);
				}
			}
		}
		
		this.State = state;
		return state;
	}

	@Override
	public void cancel()
	{
		for (int i = 0; i < nodes.size; i++)
		{
			nodes.get(i).cancel();
		}
		
		runningList.clear();
	}

}
