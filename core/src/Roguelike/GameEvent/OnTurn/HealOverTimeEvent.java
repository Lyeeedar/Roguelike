package Roguelike.GameEvent.OnTurn;

import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Entity.Entity;
import Roguelike.Entity.GameEntity;

import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Functions.RandomFunction;
import exp4j.Helpers.EquationHelper;
import exp4j.Operators.BooleanOperators;

public class HealOverTimeEvent extends AbstractOnTurnEvent
{
	String condition;
	
	String equation;
	float remainder;
	
	@Override
	public boolean handle(Entity entity, float time)
	{
		HashMap<String, Integer> variableMap = entity.getVariableMap();
		
		if (condition != null)
		{
			ExpressionBuilder expB = EquationHelper.createEquationBuilder(condition);
			EquationHelper.setVariableNames(expB, variableMap, "");
					
			Expression exp = EquationHelper.tryBuild(expB);
			if (exp == null)
			{
				return false;
			}
			
			EquationHelper.setVariableValues(exp, variableMap, "");
			
			double conditionVal = exp.evaluate();
			
			if (conditionVal == 0)
			{
				return false;
			}
		}
		
		ExpressionBuilder expB = EquationHelper.createEquationBuilder(equation);
		EquationHelper.setVariableNames(expB, variableMap, "");
				
		Expression exp = EquationHelper.tryBuild(expB);
		if (exp == null)
		{
			return false;
		}
		
		EquationHelper.setVariableValues(exp, variableMap, "");
		
		float raw = (float)exp.evaluate() * time + remainder;
		
		int rounded = (int)Math.floor(raw);
		
		remainder = raw - rounded;
		
		entity.applyHealing(rounded);
		
		return true;
	}

	@Override
	public void parse(Element xml)
	{
		condition = xml.getAttribute("Condition", null); if (condition != null) { condition = condition.toLowerCase(); }
		equation = xml.get("Heal"); if (equation != null) { equation = equation.toLowerCase(); }
	}

}