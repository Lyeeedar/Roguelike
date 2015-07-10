package Roguelike.GameEvent.Damage;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.StatusEffect.StatusEffect;

import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Functions.RandomFunction;
import exp4j.Helpers.EquationHelper;
import exp4j.Operators.BooleanOperators;

public class StatusEvent extends AbstractOnDamageEvent
{
	private String condition;
	private String attackerStatus;
	private String defenderStatus;
	private String stacksEqn;
	
	@Override
	public boolean handle(DamageObject obj)
	{
		if (condition != null)
		{
			ExpressionBuilder expB = new ExpressionBuilder(condition);
			BooleanOperators.applyOperators(expB);
			expB.function(new RandomFunction());
			
			obj.attacker.fillExpressionBuilderWithValues(expB, "ATTACKER_");
			obj.defender.fillExpressionBuilderWithValues(expB, "DEFENDER_");
			
			expB.variable("DAMAGE");
			
			Expression exp = EquationHelper.tryBuild(expB);
			if (exp == null)
			{
				return false;
			}
			
			obj.attacker.fillExpressionWithValues(exp, "ATTACKER_");
			obj.defender.fillExpressionWithValues(exp, "DEFENDER_");
			
			exp.setVariable("DAMAGE", obj.damage);
			
			double conditionVal = exp.evaluate();
			
			if (conditionVal == 0)
			{
				return false;
			}
		}
		
		int stacks = 1;
		
		if (stacksEqn != null)
		{
			ExpressionBuilder expB = new ExpressionBuilder(stacksEqn);
			BooleanOperators.applyOperators(expB);
			expB.function(new RandomFunction());
			
			obj.attacker.fillExpressionBuilderWithValues(expB, "ATTACKER_");
			obj.defender.fillExpressionBuilderWithValues(expB, "DEFENDER_");
			
			expB.variable("DAMAGE");
			
			Expression exp = EquationHelper.tryBuild(expB);
			if (exp != null)
			{
				obj.attacker.fillExpressionWithValues(exp, "ATTACKER_");
				obj.defender.fillExpressionWithValues(exp, "DEFENDER_");
				
				exp.setVariable("DAMAGE", obj.damage);
				
				stacks = (int)Math.ceil(exp.evaluate());
			}
		}
		
		if (attackerStatus != null)
		{
			for (int i = 0; i < stacks; i++)
			{
				obj.attacker.addStatusEffect(StatusEffect.load(attackerStatus));
			}
		}
		
		if (defenderStatus != null)
		{
			for (int i = 0; i < stacks; i++)
			{
				obj.defender.addStatusEffect(StatusEffect.load(defenderStatus));
			}
		}
		
		return true;
	}

	@Override
	public void parse(Element xml)
	{
		condition = xml.get("Condition", null);
		attackerStatus = xml.get("Attacker", null);
		defenderStatus = xml.get("Defender", null);
		stacksEqn = xml.get("Stacks", null);
	}

}
