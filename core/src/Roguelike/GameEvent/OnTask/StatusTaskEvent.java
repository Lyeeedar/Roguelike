package Roguelike.GameEvent.OnTask;

import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Entity.Entity;
import Roguelike.Entity.Tasks.AbstractTask;
import Roguelike.StatusEffect.StatusEffect;

import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

public class StatusTaskEvent extends AbstractOnTaskEvent
{
	private String condition;	
	private String[] reliesOn;
	public String stacksEqn;
	public Element status;
	
	@Override
	public boolean handle(Entity entity, AbstractTask task)
	{
		HashMap<String, Integer> variableMap = entity.getVariableMap();
		for (String name : reliesOn)
		{
			if (!variableMap.containsKey(name.toLowerCase()))
			{
				variableMap.put(name.toLowerCase(), 0);
			}
		}
		
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
		
		int stacks = 1;
		
		if (stacksEqn != null)
		{
			ExpressionBuilder expB = EquationHelper.createEquationBuilder(stacksEqn);
			EquationHelper.setVariableNames(expB, variableMap, "");
			
			Expression exp = EquationHelper.tryBuild(expB);
			if (exp != null)
			{
				EquationHelper.setVariableValues(exp, variableMap, "");
				
				stacks = (int)Math.ceil(exp.evaluate());
			}
		}
		
		for (int i = 0; i < stacks; i++)
		{
			entity.addStatusEffect(StatusEffect.load(status, null));
		}
		
		return true;
	}

	@Override
	public void parse(Element xml)
	{
		condition = xml.getAttribute("Condition", null); if (condition != null) { condition = condition.toLowerCase(); }
		
		reliesOn = xml.getAttribute("ReliesOn", "").split(",");
		
		status = xml.getChildByName("Status");
		stacksEqn = xml.get("Stacks", null); if (stacksEqn != null) { stacksEqn = stacksEqn.toLowerCase(); }
	}

}
