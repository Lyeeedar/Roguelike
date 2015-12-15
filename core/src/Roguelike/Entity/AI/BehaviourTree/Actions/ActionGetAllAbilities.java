package Roguelike.Entity.AI.BehaviourTree.Actions;

import Roguelike.Ability.AbilityTree;
import Roguelike.Global;
import Roguelike.Ability.IAbility;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Entity.GameEntity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;
import Roguelike.Tiles.Point;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public class ActionGetAllAbilities extends AbstractAction
{
	private String Key;

	@Override
	public BehaviourTreeState evaluate( GameEntity entity )
	{
		Array<ActiveAbility> abilities = new Array<ActiveAbility>();

		for ( AbilityTree a : entity.slottedAbilities )
		{
			if ( a != null && a.current.current instanceof ActiveAbility )
			{
				ActiveAbility ab = (ActiveAbility) a.current.current;

				if ( ab.cooldownAccumulator <= 0 )
				{
					ab.caster = entity;
					ab.source = entity.tile[0][0];

					Array<Point> validTargets = ab.getValidTargets();
					if ( validTargets.size > 0 )
					{
						abilities.add( ab );
					}
					Global.PointPool.freeAll( validTargets );
				}
			}
		}

		setData( Key, abilities );
		State = abilities.size > 0 ? BehaviourTreeState.SUCCEEDED : BehaviourTreeState.FAILED;
		return State;
	}

	@Override
	public void cancel()
	{
	}

	@Override
	public void parse( Element xmlElement )
	{
		Key = xmlElement.getAttribute( "Key" );
	}

}
