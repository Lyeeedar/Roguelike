package Roguelike.Entity.AI.BehaviourTree.Actions;

import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Entity.Entity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;
import Roguelike.Entity.Tasks.TaskUseAbility;

import com.badlogic.gdx.utils.XmlReader.Element;

public class ActionUseAbility extends AbstractAction
{
	String abilityKey;
	String targetKey;

	@Override
	public BehaviourTreeState evaluate(Entity entity)
	{
		ActiveAbility ability = (ActiveAbility)getData(abilityKey, null);
		int[] target = (int[])getData(targetKey, null);
		
		// if no target or ability, fail
		if (target == null || ability == null)
		{
			State = BehaviourTreeState.FAILED;
			return State;
		}
		
		entity.Tasks.add(new TaskUseAbility(target, ability));
		
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
		targetKey = xmlElement.getAttribute("Target");
	}
}
