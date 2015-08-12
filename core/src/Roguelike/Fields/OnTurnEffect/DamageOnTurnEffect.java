package Roguelike.Fields.OnTurnEffect;

import java.util.EnumMap;
import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Global;
import Roguelike.Entity.Entity;
import Roguelike.Fields.Field;
import Roguelike.Global.Statistic;

import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

public class DamageOnTurnEffect extends AbstractOnTurnEffect
{
	private String condition;	
	private EnumMap<Statistic, String> equations = new EnumMap<Statistic, String>(Statistic.class);
	private String[] reliesOn;
	private static final float updateRate = 1;
	
	@Override
	public void update(float delta, Field field)
	{
		float updateAccumulator = (Float)field.getData("OnTurnAccumulator", 0.0f);
		updateAccumulator += delta;
		
		while (updateAccumulator >= updateRate && field.stacks > 0)
		{
			updateAccumulator -= updateRate;
			
			if (field.tile.entity != null)
			{
				doDamage(field.tile.entity, field);
			}
			
			if (field.tile.environmentEntity != null && field.tile.environmentEntity.canTakeDamage)
			{
				doDamage(field.tile.environmentEntity, field);
			}
		}
		
		field.setData("OnTurnAccumulator", updateAccumulator);
	}
	
	private void doDamage(Entity entity, Field field)
	{
		HashMap<String, Integer> variableMap = entity.getVariableMap();
		for (String name : reliesOn)
		{
			if (!variableMap.containsKey(name.toLowerCase()))
			{
				variableMap.put(name.toLowerCase(), 0);
			}
		}
		variableMap.put("stacks", field.stacks);
		
		if (condition != null)
		{
			ExpressionBuilder expB = EquationHelper.createEquationBuilder(condition);
			EquationHelper.setVariableNames(expB, variableMap, "");
					
			Expression exp = EquationHelper.tryBuild(expB);
			if (exp == null)
			{
				return;
			}
			
			EquationHelper.setVariableValues(exp, variableMap, "");
			
			double conditionVal = exp.evaluate();
			
			if (conditionVal == 0)
			{
				return;
			}
		}
		
		EnumMap<Statistic, Integer> stats = Statistic.getStatisticsBlock();
		
		for (Statistic stat : Statistic.values())
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
		
		Global.calculateDamage(entity, entity, Statistic.statsBlockToVariableBlock(stats), false);
	}

	@Override
	public void parse(Element xml)
	{
		condition = xml.getAttribute("Condition", null); if (condition != null) { condition = condition.toLowerCase(); }
		
		reliesOn = xml.getAttribute("ReliesOn", "").split(",");
		
		for (int i = 0; i < xml.getChildCount(); i++)
		{
			Element sEl = xml.getChild(i);
			
			Statistic el = Statistic.valueOf(sEl.getName().toUpperCase());
			equations.put(el, sEl.getText().toLowerCase());
		}
	}

}
