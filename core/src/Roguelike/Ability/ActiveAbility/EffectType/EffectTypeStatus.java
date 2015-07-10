package Roguelike.Ability.ActiveAbility.EffectType;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.StatusEffect.StatusEffect;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Functions.RandomFunction;
import exp4j.Helpers.EquationHelper;
import exp4j.Operators.BooleanOperators;

public class EffectTypeStatus extends AbstractEffectType
{
	private String statusName;
	private String stacksEqn;
	
	@Override
	public void parse(Element xml)
	{		
		statusName = xml.get("Name");
		stacksEqn = xml.get("Stacks", null);
	}

	@Override
	public void update(ActiveAbility aa, float time, GameTile tile)
	{		
		if (tile.Entity != null)
		{
			int stacks = 1;
			
			if (stacksEqn != null)
			{
				ExpressionBuilder expB = new ExpressionBuilder(stacksEqn);
				BooleanOperators.applyOperators(expB);
				expB.function(new RandomFunction());
				
				aa.caster.fillExpressionBuilderWithValues(expB, "ATTACKER_");
				tile.Entity.fillExpressionBuilderWithValues(expB, "DEFENDER_");
								
				Expression exp = EquationHelper.tryBuild(expB);
				if (exp != null)
				{
					aa.caster.fillExpressionWithValues(exp, "ATTACKER_");
					tile.Entity.fillExpressionWithValues(exp, "DEFENDER_");
					stacks = (int)Math.ceil(exp.evaluate());
				}
			}
			
			for (int i = 0; i < stacks; i++)
			{
				tile.Entity.addStatusEffect(StatusEffect.load(statusName));
			}
		}
	}

	
	@Override
	public AbstractEffectType copy()
	{
		EffectTypeStatus e = new EffectTypeStatus();
		e.statusName = statusName;
		e.stacksEqn = stacksEqn;
		return e;
	}
}
