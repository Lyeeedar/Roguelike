package Roguelike.GameEvent.Damage;

import java.util.EnumMap;
import java.util.HashMap;

import exp4j.Helpers.EquationHelper;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Entity.Entity;
import Roguelike.Global.Statistics;
import Roguelike.Global.Tier1Element;

public class DamageObject
{
	public final HashMap<String, Integer> attackerVariableMap;
	public final HashMap<String, Integer> defenderVariableMap;
	
	public final Entity attacker;
	public final Entity defender;
	
	public final EnumMap<Tier1Element, Integer> damageMap = Tier1Element.getElementBlock();
	public final HashMap<String, Integer> damageVariableMap = new HashMap<String, Integer>();
	
	public DamageObject(Entity attacker, Entity defender, HashMap<String, Integer> additionalValues)
	{
		this.attacker = attacker;
		this.defender = defender;
		
		defenderVariableMap = defender.getVariableMap();
		attackerVariableMap = attacker.getVariableMap();
		
		if (additionalValues != null)
		{
			for (String key : additionalValues.keySet())
			{
				int val = additionalValues.get(key);
				
//				if (attackerVariableMap.containsKey(key))
//				{
//					val += attackerVariableMap.get(key);
//				}
				
				attackerVariableMap.put(key, val);
			}
		}
	}
	
	public void setDamageVariables()
	{
		int total = 0;
		for (Tier1Element key : damageMap.keySet())
		{
			int dam = damageMap.get(key);
			damageVariableMap.put("DAMAGE_" + key.toString(), dam);
			
			total += dam;
		}
		
		damageVariableMap.put("DAMAGE", total);
	}
	
	public void modifyDamage(EnumMap<Tier1Element, Integer> dam)
	{
		for (Tier1Element el : Tier1Element.values())
		{
			int oldVal = damageMap.get(el);
			int newVal = dam.get(el);
			
			damageMap.put(el, oldVal + newVal);
		}
		
		setDamageVariables();
	}
	
	public void writeVariableNames(ExpressionBuilder expB, String[] reliesOn)
	{
		EquationHelper.setVariableNames(expB, attackerVariableMap, "ATTACKER_");
		EquationHelper.setVariableNames(expB, defenderVariableMap, "DEFENDER_");
		EquationHelper.setVariableNames(expB, damageVariableMap, "");
		
		for (String name : reliesOn)
		{
			String atkName = "ATTACKER_" + name.toUpperCase();
			if (!attackerVariableMap.containsKey(atkName))
			{
				expB.variable(atkName);
			}
			
			String defName = "DEFENDER_" + name.toUpperCase();
			if (!defenderVariableMap.containsKey(defName))
			{
				expB.variable(defName);
			}
		}
	}
	
	public void writeVariableValues(Expression exp, String[] reliesOn)
	{
		EquationHelper.setVariableValues(exp, attackerVariableMap, "ATTACKER_");
		EquationHelper.setVariableValues(exp, defenderVariableMap, "DEFENDER_");
		EquationHelper.setVariableValues(exp, damageVariableMap, "");
		
		for (String name : reliesOn)
		{
			String atkName = "ATTACKER_" + name.toUpperCase();
			if (!attackerVariableMap.containsKey(atkName))
			{
				exp.setVariable(atkName, 0);
			}
			
			String defName = "DEFENDER_" + name.toUpperCase();
			if (!defenderVariableMap.containsKey(defName))
			{
				exp.setVariable(defName, 0);
			}
		}
	}
}
