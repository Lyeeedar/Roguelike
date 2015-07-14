package Roguelike.GameEvent.OnTurn;

import java.util.EnumMap;
import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Global;
import Roguelike.Entity.GameEntity;
import Roguelike.Global.Statistics;

import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Functions.RandomFunction;
import exp4j.Helpers.EquationHelper;
import exp4j.Operators.BooleanOperators;

public class DamageOverTimeEvent extends AbstractOnTurnEvent
{
	private String condition;	
	private EnumMap<Statistics, String> equations = new EnumMap<Statistics, String>(Statistics.class);
	private String[] reliesOn;
	
	private float accumulator;
	
	@Override
	public boolean handle(GameEntity entity, float time)
	{
		accumulator += time;
		
		if (accumulator < 0) { return false; }
		
		HashMap<String, Integer> variableMap = entity.getVariableMap();
		for (String name : reliesOn)
		{
			if (!variableMap.containsKey(name.toUpperCase()))
			{
				variableMap.put(name.toUpperCase(), 0);
			}
		}
		
		if (condition != null)
		{
			ExpressionBuilder expB = EquationHelper.createEquationBuilder(condition);
			EquationHelper.setVariableNames(expB, variableMap, "");
					
			Expression exp = EquationHelper.tryBuild(expB);
			if (exp == null)
			{
				accumulator = 0;
				return false;
			}
			
			EquationHelper.setVariableValues(exp, variableMap, "");
			
			double conditionVal = exp.evaluate();
			
			if (conditionVal == 0)
			{
				accumulator = 0;
				return false;
			}
		}
		
		EnumMap<Statistics, Integer> stats = Statistics.getStatisticsBlock();
		
		for (Statistics stat : Statistics.values())
		{
			if (equations.containsKey(stat))
			{
				String eqn = equations.get(stat);
				
				ExpressionBuilder expB = EquationHelper.createEquationBuilder(eqn);
				EquationHelper.setVariableNames(expB, variableMap, "");
									
				Expression exp = EquationHelper.tryBuild(expB);
				if (exp == null)
				{
					continue;
				}
				
				EquationHelper.setVariableValues(exp, variableMap, "");
									
				int raw = (int)exp.evaluate();
				
				stats.put(stat, raw);
			}
		}
		
		while (accumulator > 1)
		{
			accumulator -= 1;
			
			Global.calculateDamage(entity, entity, Statistics.statsBlockToVariableBlock(stats), false);	
		}
		
		return true;
	}

	@Override
	public void parse(Element xml)
	{
		condition = xml.getAttribute("Condition", null);
				
		reliesOn = xml.getAttribute("ReliesOn", "").split(",");
		
		for (int i = 0; i < xml.getChildCount(); i++)
		{
			Element sEl = xml.getChild(i);
			
			Statistics el = Statistics.valueOf(sEl.getName().toUpperCase());
			equations.put(el, sEl.getText());
		}
	}
}
