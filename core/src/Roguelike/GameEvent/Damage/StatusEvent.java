package Roguelike.GameEvent.Damage;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.GameEvent.IGameObject;
import Roguelike.StatusEffect.StatusEffect;

import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Functions.RandomFunction;
import exp4j.Helpers.EquationHelper;
import exp4j.Operators.BooleanOperators;

public class StatusEvent extends AbstractOnDamageEvent
{
	public String condition;
	public Element attackerStatus;
	public Element defenderStatus;
	public String stacksEqn;
	
	private String[] reliesOn;
	
	@Override
	public boolean handle(DamageObject obj, IGameObject parent)
	{		
		if (condition != null)
		{
			ExpressionBuilder expB = EquationHelper.createEquationBuilder(condition);
			EquationHelper.setVariableNames(expB, obj.attackerVariableMap, "ATTACKER_");
			EquationHelper.setVariableNames(expB, obj.defenderVariableMap, "DEFENDER_");
			EquationHelper.setVariableNames(expB, obj.damageVariableMap, "");
			
			for (String name : reliesOn)
			{
				String atkName = "ATTACKER_" + name.toUpperCase();
				if (!obj.attackerVariableMap.containsKey(atkName))
				{
					expB.variable(atkName);
				}
				
				String defName = "DEFENDER_" + name.toUpperCase();
				if (!obj.defenderVariableMap.containsKey(defName))
				{
					expB.variable(defName);
				}
			}

			Expression exp = EquationHelper.tryBuild(expB);
			if (exp == null)
			{
				return false;
			}
			
			EquationHelper.setVariableValues(exp, obj.attackerVariableMap, "ATTACKER_");
			EquationHelper.setVariableValues(exp, obj.defenderVariableMap, "DEFENDER_");
			EquationHelper.setVariableValues(exp, obj.damageVariableMap, "");
			
			for (String name : reliesOn)
			{
				String atkName = "ATTACKER_" + name.toUpperCase();
				if (!obj.attackerVariableMap.containsKey(atkName))
				{
					exp.setVariable(atkName, 0);
				}
				
				String defName = "DEFENDER_" + name.toUpperCase();
				if (!obj.defenderVariableMap.containsKey(defName))
				{
					exp.setVariable(defName, 0);
				}
			}
						
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
			EquationHelper.setVariableNames(expB, obj.attackerVariableMap, "ATTACKER_");
			EquationHelper.setVariableNames(expB, obj.defenderVariableMap, "DEFENDER_");
			EquationHelper.setVariableNames(expB, obj.damageVariableMap, "");
			
			for (String name : reliesOn)
			{
				if (!obj.attackerVariableMap.containsKey(name.toUpperCase()))
				{
					expB.variable("ATTACKER_" + name.toUpperCase());
				}
				
				if (!obj.defenderVariableMap.containsKey(name.toUpperCase()))
				{
					expB.variable("DEFENDER_" + name.toUpperCase());
				}
			}
			
			Expression exp = EquationHelper.tryBuild(expB);
			if (exp != null)
			{
				EquationHelper.setVariableValues(exp, obj.attackerVariableMap, "ATTACKER_");
				EquationHelper.setVariableValues(exp, obj.defenderVariableMap, "DEFENDER_");
				EquationHelper.setVariableValues(exp, obj.damageVariableMap, "");
				
				for (String name : reliesOn)
				{
					if (!obj.attackerVariableMap.containsKey(name.toUpperCase()))
					{
						exp.setVariable("ATTACKER_" + name.toUpperCase(), 0);
					}
					
					if (!obj.defenderVariableMap.containsKey(name.toUpperCase()))
					{
						exp.setVariable("DEFENDER_" + name.toUpperCase(), 0);
					}
				}
				
				stacks = (int)Math.ceil(exp.evaluate());
			}
		}
		
		if (attackerStatus != null)
		{
			for (int i = 0; i < stacks; i++)
			{
				obj.attacker.addStatusEffect(StatusEffect.load(attackerStatus, parent));
			}
		}
		
		if (defenderStatus != null)
		{
			for (int i = 0; i < stacks; i++)
			{
				obj.defender.addStatusEffect(StatusEffect.load(defenderStatus, parent));
			}
		}
		
		return true;
	}

	@Override
	public void parse(Element xml)
	{
		reliesOn = xml.getAttribute("ReliesOn", "").split(",");
		condition = xml.getAttribute("Condition", null);
		attackerStatus = xml.getChildByName("Attacker");
		defenderStatus = xml.getChildByName("Defender");
		stacksEqn = xml.get("Stacks", null);
	}

}
