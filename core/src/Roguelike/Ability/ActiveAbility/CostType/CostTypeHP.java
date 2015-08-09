package Roguelike.Ability.ActiveAbility.CostType;

import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Ability.ActiveAbility.ActiveAbility;

import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

public class CostTypeHP extends AbstractCostType
{
	private String[] reliesOn;
	private String equation;
	
	@Override
	public boolean isCostAvailable(ActiveAbility aa)
	{
		HashMap<String, Integer> variableMap = aa.caster.getVariableMap();
		
		for (String name : reliesOn)
		{
			if (!variableMap.containsKey(name.toLowerCase()))
			{
				variableMap.put(name.toLowerCase(), 0);
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
							
		int raw = (int)exp.evaluate();
				
		return raw < aa.caster.HP;
	}

	@Override
	public void spendCost(ActiveAbility aa)
	{
		HashMap<String, Integer> variableMap = aa.variableMap;
		
		for (String name : reliesOn)
		{
			if (!variableMap.containsKey(name.toLowerCase()))
			{
				variableMap.put(name.toLowerCase(), 0);
			}
		}
		
		ExpressionBuilder expB = EquationHelper.createEquationBuilder(equation);
		EquationHelper.setVariableNames(expB, variableMap, "");
							
		Expression exp = EquationHelper.tryBuild(expB);
		if (exp == null)
		{
			return;
		}
		
		EquationHelper.setVariableValues(exp, variableMap, "");
							
		int raw = (int)exp.evaluate();
		
		aa.caster.applyDamage(raw, aa.caster);
	}

	@Override
	public void parse(Element xml)
	{
		reliesOn = xml.getAttribute("ReliesOn", "").split(",");		
		equation = xml.getText().toLowerCase();
	}

	@Override
	public AbstractCostType copy()
	{
		CostTypeHP hp = new CostTypeHP();
		
		hp.reliesOn = reliesOn;
		hp.equation = equation;
		
		return hp;
	}

}
