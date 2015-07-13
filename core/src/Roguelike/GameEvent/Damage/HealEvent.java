package Roguelike.GameEvent.Damage;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.GameEvent.IGameObject;

import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

public class HealEvent extends AbstractOnDamageEvent
{
	private String condition;
	private String attacker;
	private String defender;
	private String[] reliesOn;
	
	@Override
	public boolean handle(DamageObject obj, IGameObject parent)
	{
		if (condition != null)
		{
			ExpressionBuilder expB = EquationHelper.createEquationBuilder(condition);
			obj.writeVariableNames(expB, reliesOn);

			Expression exp = EquationHelper.tryBuild(expB);
			if (exp == null)
			{
				return false;
			}
			
			obj.writeVariableValues(exp, reliesOn);
						
			double conditionVal = exp.evaluate();
			
			if (conditionVal == 0)
			{
				return false;
			}
		}
		
		if (attacker != null)
		{
			ExpressionBuilder expB = EquationHelper.createEquationBuilder(attacker);
			obj.writeVariableNames(expB, reliesOn);

			Expression exp = EquationHelper.tryBuild(expB);
			if (exp != null)
			{
				obj.writeVariableValues(exp, reliesOn);
				
				int raw = (int)exp.evaluate();
				
				obj.attacker.applyHealing(raw);
			}
		}
		
		if (defender != null)
		{
			ExpressionBuilder expB = EquationHelper.createEquationBuilder(defender);
			obj.writeVariableNames(expB, reliesOn);

			Expression exp = EquationHelper.tryBuild(expB);
			if (exp != null)
			{
				obj.writeVariableValues(exp, reliesOn);
				
				int raw = (int)exp.evaluate();
				
				obj.defender.applyHealing(raw);
			}
		}
		
		return true;
	}

	@Override
	public void parse(Element xml)
	{
		reliesOn = xml.getAttribute("ReliesOn", "").split(",");		
		condition = xml.getAttribute("Condition", null);
		attacker = xml.get("Attacker", null);
		defender = xml.get("Defender", null);
	}

}
