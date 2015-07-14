package Roguelike.Entity.AI.BehaviourTree.Actions;

import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Ability.ActiveAbility.EffectType.AbstractEffectType;
import Roguelike.Ability.ActiveAbility.EffectType.EffectTypeDamage;
import Roguelike.Ability.ActiveAbility.EffectType.EffectTypeHeal;
import Roguelike.Ability.ActiveAbility.EffectType.EffectTypeStatus;
import Roguelike.Entity.GameEntity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public class ActionCategoriseAbilities extends AbstractAction
{
	String abilitiesKey;
	String targetKey;

	@Override
	public BehaviourTreeState evaluate(GameEntity entity)
	{
		Array<ActiveAbility> abilities = (Array<ActiveAbility>)getData(abilitiesKey, null);
		
		if (abilities == null || abilities.size == 0)
		{
			State = BehaviourTreeState.FAILED;
			return State;
		}
		
		Array<ActiveAbility> healList = new Array<ActiveAbility>();
		Array<ActiveAbility> damageList = new Array<ActiveAbility>();
		Array<ActiveAbility> statusList = new Array<ActiveAbility>();
		
		for (ActiveAbility aa : abilities)
		{
			boolean isHeal = false;
			boolean isDamage = false;
			boolean isStatus = false;
			
			for (AbstractEffectType effect : aa.effectTypes)
			{
				if (effect instanceof EffectTypeHeal) { isHeal = true; }
				else if (effect instanceof EffectTypeDamage) { isDamage = true; }
				else if (effect instanceof EffectTypeStatus) { isStatus = true; }
			}
			
			if (isHeal) { healList.add(aa); }
			if (isDamage) { damageList.add(aa); }
			if (isStatus) { statusList.add(aa); }
		}
		
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
		abilitiesKey = xmlElement.getAttribute("Key");
		targetKey = xmlElement.getAttribute("Target");
	}
}