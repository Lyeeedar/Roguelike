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
	
	@Override
	public boolean handle(DamageObject obj)
	{
		if (condition != null)
		{
			ExpressionBuilder expB = new ExpressionBuilder(condition);
			BooleanOperators.applyOperators(expB);
			expB.function(new RandomFunction());
			
			obj.attacker.fillExpressionBuilderWithValues(expB, "ATK_");
			obj.defender.fillExpressionBuilderWithValues(expB, "DEF_");
			
			expB.variable("DAMAGE");
			
			Expression exp = EquationHelper.tryBuild(expB);
			if (exp == null)
			{
				return false;
			}
			
			obj.attacker.fillExpressionWithValues(exp, "ATK_");
			obj.defender.fillExpressionWithValues(exp, "DEF_");
			
			exp.setVariable("DAMAGE", obj.damage);
			
			double conditionVal = exp.evaluate();
			
			if (conditionVal == 0)
			{
				return false;
			}
		}
		
		if (attackerStatus != null)
		{
			obj.attacker.addStatusEffect(StatusEffect.load(attackerStatus));
		}
		
		if (defenderStatus != null)
		{
			obj.defender.addStatusEffect(StatusEffect.load(defenderStatus));
		}
		
		return true;
	}

	@Override
	public void parse(Element xml)
	{
		condition = xml.get("Condition", null);
		attackerStatus = xml.get("Attacker", null);
		defenderStatus = xml.get("Defender", null);
	}

}
