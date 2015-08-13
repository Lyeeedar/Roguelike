package Roguelike.Entity.AI.BehaviourTree.Actions;

import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Entity.GameEntity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;
import Roguelike.Tiles.Point;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.XmlReader.Element;

public class ActionGetAllAbilities extends AbstractAction
{
	private String Key;

	@Override
	public BehaviourTreeState evaluate(GameEntity entity)
	{
		Array<ActiveAbility> abilities = new Array<ActiveAbility>();
		
		for (ActiveAbility ab : entity.slottedActiveAbilities)
		{
			if (ab != null && ab.cooldownAccumulator <= 0)
			{
				ab.caster = entity;
				ab.source = entity.tile;
				
				Array<Point> validTargets = ab.getValidTargets();
				if (validTargets.size > 0)
				{
					abilities.add(ab);
				}
				Pools.freeAll(validTargets);
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
