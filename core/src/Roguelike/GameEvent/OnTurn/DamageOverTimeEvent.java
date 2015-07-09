package Roguelike.GameEvent.OnTurn;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Entity.Entity;

import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Functions.RandomFunction;
import exp4j.Helpers.EquationHelper;
import exp4j.Operators.BooleanOperators;

public class DamageOverTimeEvent extends AbstractOnTurnEvent
{
	String condition;
	
	String equation;
	float remainder;
	
	@Override
	public boolean handle(Entity entity, float time)
	{
		if (condition != null)
		{
			ExpressionBuilder expB = new ExpressionBuilder(condition);
			BooleanOperators.applyOperators(expB);
			expB.function(new RandomFunction());
			entity.fillExpressionBuilderWithValues(expB, "");
			
			Expression exp = EquationHelper.tryBuild(expB);
			if (exp == null)
			{
				return false;
			}
			
			entity.fillExpressionWithValues(exp, "");
			
			double conditionVal = exp.evaluate();
			
			if (conditionVal == 0)
			{
				return false;
			}
		}
		
		ExpressionBuilder expB = new ExpressionBuilder(equation);
		BooleanOperators.applyOperators(expB);
		expB.function(new RandomFunction());
		entity.fillExpressionBuilderWithValues(expB, "");
		
		Expression exp = EquationHelper.tryBuild(expB);
		if (exp == null)
		{
			return false;
		}
		
		entity.fillExpressionWithValues(exp, "");
		
		float raw = (float)exp.evaluate() * time + remainder;
		
		int rounded = (int)Math.floor(raw);
		
		remainder = raw - rounded;
		
		entity.applyDamage(rounded);
		
		return true;
	}

	@Override
	public void parse(Element xml)
	{
		condition = xml.get("Condition", null);
		equation = xml.get("Damage");
	}

}
