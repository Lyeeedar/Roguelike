package Roguelike.GameEvent.OnTask;

import java.util.EnumMap;
import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Entity.Entity;
import Roguelike.Entity.Tasks.AbstractTask;
import Roguelike.GameEvent.IGameObject;

import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

public class CancelTaskEvent extends AbstractOnTaskEvent
{
	private String condition;	
	private String[] reliesOn;
	
	@Override
	public boolean handle(Entity entity, AbstractTask task, IGameObject parent)
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
		
		task.cancel = true;
		
		return true;
	}

	@Override
	public void parse(Element xml)
	{
		condition = xml.getAttribute("Condition", null); if (condition != null) { condition = condition.toLowerCase(); }
		
		reliesOn = xml.getAttribute("ReliesOn", "").split(",");
	}

}
