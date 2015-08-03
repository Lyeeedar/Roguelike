package Roguelike.Ability.ActiveAbility.CostType;

import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Entity.Entity.StatusEffectStack;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

public class CostTypeStatus extends AbstractCostType
{
	private Array<StatusEquation> equations = new Array<StatusEquation>();
	private String[] reliesOn;
	
	@Override
	public void parse(Element xml)
	{	
		reliesOn = xml.getAttribute("ReliesOn", "").split(",");		
		for (int i = 0; i < xml.getChildCount(); i++)
		{
			Element el = xml.getChild(i);
			equations.add(new StatusEquation(el.getName(), el.getText().toLowerCase()));
		}
	}
	

	@Override
	public boolean isCostAvailable(ActiveAbility aa)
	{
		HashMap<String, Integer> variableMap = aa.variableMap;
		
		for (String name : reliesOn)
		{
			if (!variableMap.containsKey(name.toLowerCase()))
			{
				variableMap.put(name.toLowerCase(), 0);
			}
		}
		
		Array<StatusEffectStack> stacks = aa.caster.stackStatusEffects();
		
		for (StatusEquation sEqn : equations)
		{
			ExpressionBuilder expB = EquationHelper.createEquationBuilder(sEqn.eqn);
			EquationHelper.setVariableNames(expB, variableMap, "");
								
			Expression exp = EquationHelper.tryBuild(expB);
			if (exp == null)
			{
				return false;
			}
			
			EquationHelper.setVariableValues(exp, variableMap, "");
			
			int count = (int)exp.evaluate();
			
			StatusEffectStack stack = null;
			for (StatusEffectStack s : stacks)
			{
				if (s.effect.name.equals(sEqn.status))
				{
					stack = s;
					break;
				}
			}
			
			if (stack == null || stack.count < count)
			{
				return false;
			}
		}
		
		return true;
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
				
		for (StatusEquation sEqn : equations)
		{
			ExpressionBuilder expB = EquationHelper.createEquationBuilder(sEqn.eqn);
			EquationHelper.setVariableNames(expB, variableMap, "");
								
			Expression exp = EquationHelper.tryBuild(expB);
			if (exp == null)
			{
				return;
			}
			
			EquationHelper.setVariableValues(exp, variableMap, "");
			
			int count = (int)exp.evaluate();
			
			for (int i = 0; i < count; i++)
			{
				aa.caster.removeStatusEffect(sEqn.status);
			}
		}
	}
	
	@Override
	public AbstractCostType copy()
	{
		CostTypeStatus consume = new CostTypeStatus();
		consume.equations = equations;
		consume.reliesOn = reliesOn;
		
		return consume;
	}
	
	private static class StatusEquation
	{
		public String status;
		public String eqn;
		
		public StatusEquation(String status, String eqn)
		{
			this.status = status;
			this.eqn = eqn;
		}
	}
}
