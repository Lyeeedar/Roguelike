package Roguelike.Entity.AI.BehaviourTree.Actions;

import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Entity.GameEntity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;
import Roguelike.Entity.Tasks.TaskUseAbility;

import com.badlogic.gdx.utils.XmlReader.Element;

public class ActionUseAbility extends AbstractAction
{
	String abilityKey;

	@Override
	public BehaviourTreeState evaluate(GameEntity entity)
	{
		ActiveAbility ability = (ActiveAbility)getData(abilityKey, null);
		
		// if no target or ability, fail
		if (ability == null)
		{
			State = BehaviourTreeState.FAILED;
			return State;
		}
		
		int[][] validTargets = ability.getValidTargets();
		
		if (validTargets.length == 0)
		{
			State = BehaviourTreeState.FAILED;
			return State;
		}
		
		entity.tasks.add(new TaskUseAbility(validTargets[0], ability));
		
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
		abilityKey = xmlElement.getAttribute("Key");
	}
}
