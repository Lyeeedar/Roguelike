package Roguelike.GameEvent.Damage;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Entity.Entity;

import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Functions.RandomFunction;
import exp4j.Helpers.EquationHelper;
import exp4j.Operators.BooleanOperators;

public class DamageEvent extends AbstractOnDamageEvent
{
	String condition;
	
	String equation;
	float remainder;
	
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
		
		ExpressionBuilder expB = new ExpressionBuilder(equation);
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
		
		obj.damage = (int)Math.max(0, obj.damage + exp.evaluate());
		
		return true;
	}

	@Override
	public void parse(Element xml)
	{
		condition = xml.get("Condition", null);
		equation = xml.get("Damage");
	}
}
