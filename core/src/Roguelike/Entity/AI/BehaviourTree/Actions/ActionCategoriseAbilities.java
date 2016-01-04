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
		Array<ActiveAbility> miscCombatList = new Array<ActiveAbility>();

		for (ActiveAbility aa : abilities)
		{
			boolean isHeal = false;
			boolean isDamage = false;

			for (AbstractEffectType effect : aa.effectTypes)
			{
				if (effect instanceof EffectTypeHeal) { isHeal = true; }
				else if (effect instanceof EffectTypeDamage) { isDamage = true; }
			}

			if (isHeal) { healList.add(aa); }
			if (isDamage) { damageList.add(aa); }
			if (!isHeal && !isDamage) { miscCombatList.add(aa); }
		}

		if (healList.size > 0) { setData( "HealAbilities", healList ); } else { setData( "HealAbilities", null ); }
		if (damageList.size > 0) { setData( "DamageAbilities", damageList ); } else { setData( "DamageAbilities", null ); }
		if (miscCombatList.size > 0) { setData( "MiscCombatAbilities", miscCombatList ); } else { setData( "MiscCombatAbilities", null ); }

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
	}
}