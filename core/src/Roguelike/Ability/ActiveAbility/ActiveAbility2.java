package Roguelike.Ability.ActiveAbility;

import Roguelike.Ability.ActiveAbility.AbilityType.AbstractAbilityType;
import Roguelike.Ability.ActiveAbility.EffectType.AbstractEffectType;
import Roguelike.Ability.ActiveAbility.MovementType.AbstractMovementType;
import Roguelike.Ability.ActiveAbility.TargetingType.AbstractTargetingType;

import com.badlogic.gdx.utils.Array;

public class ActiveAbility2
{
	private AbstractAbilityType abilityType;
	private Array<AbstractTargetingType> targetingTypes = new Array<AbstractTargetingType>();
	private AbstractMovementType movementType;
	private Array<AbstractEffectType> effectTypes = new Array<AbstractEffectType>();
}
