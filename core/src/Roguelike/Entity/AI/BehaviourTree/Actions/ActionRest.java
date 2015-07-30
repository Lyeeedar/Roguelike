package Roguelike.Entity.AI.BehaviourTree.Actions;

import java.util.HashSet;

import Roguelike.Entity.GameEntity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;
import Roguelike.Entity.Tasks.TaskWait;
import Roguelike.Global.Statistic;

import com.badlogic.gdx.utils.XmlReader.Element;

public class ActionRest extends AbstractAction
{
	private HashSet<String> interestedValues = new HashSet<String>();	
	private HashSet<String> tempValues = new HashSet<String>();

	@Override
	public BehaviourTreeState evaluate(GameEntity entity)
	{
		tempValues.clear();
		
		if (entity.HP < entity.getStatistic(Statistic.MAXHP))
		{
			tempValues.add("HP");
		}
		// add ability cooldown checks here
		
		boolean completed = false;
		for (String item : interestedValues)
		{
			if (!tempValues.contains(item))
			{
				completed = true;
				break;
			}
		}
		
		//swap
		HashSet<String> temp = interestedValues;
		interestedValues = tempValues;
		tempValues = temp;
		
		boolean hasRestingToDo = completed || interestedValues.size() > 0;
		
		if (hasRestingToDo)
		{
			entity.tasks.add(new TaskWait());
			State =  BehaviourTreeState.RUNNING;
		}
		else
		{
			State =  BehaviourTreeState.SUCCEEDED;
		}
		
		return State;
	}

	@Override
	public void cancel()
	{
		interestedValues.clear();
	}

	@Override
	public void parse(Element xmlElement)
	{
	}

}
