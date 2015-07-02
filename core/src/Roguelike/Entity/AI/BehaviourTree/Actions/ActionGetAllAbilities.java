package Roguelike.Entity.AI.BehaviourTree.Actions;

import Roguelike.Entity.ActiveAbility;
import Roguelike.Entity.Entity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public class ActionGetAllAbilities extends AbstractAction
{
	private String Key;

	@Override
	public BehaviourTreeState evaluate(Entity entity)
	{
		Array<ActiveAbility> abilities = new Array<ActiveAbility>();
		
		for (ActiveAbility ab : entity.getSlottedActiveAbilities())
		{
			if (ab != null && ab.cooldownAccumulator <= 0)
			{
				abilities.add(ab);
			}
		}		
		
		setData(Key, abilities);
		State = abilities.size > 0 ? BehaviourTreeState.SUCCEEDED : BehaviourTreeState.FAILED;
		return State;
	}

	@Override
	public void cancel()
	{
	}

	@Override
	public void parse(Element xmlElement)
	{
		Key = xmlElement.getAttribute("Key");
	}

}
