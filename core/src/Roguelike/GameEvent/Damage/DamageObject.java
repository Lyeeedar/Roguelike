package Roguelike.GameEvent.Damage;

import java.util.EnumMap;
import java.util.HashMap;

import exp4j.Helpers.EquationHelper;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Entity.Entity;
import Roguelike.Entity.GameEntity;
import Roguelike.Global.Statistic;
import Roguelike.Global.Tier1Element;

public class DamageObject
{
	public final HashMap<String, Integer> attackerVariableMap;
	public final HashMap<String, Integer> defenderVariableMap;
	
	public final Entity attacker;
	public final Entity defender;
	
	public final EnumMap<Tier1Element, Integer> damageMap = Tier1Element.getElementBlock();
	public final HashMap<String, Integer> damageVariableMap = new HashMap<String, Integer>();
	
	public DamageObject(Entity attacker, Entity defender, HashMap<String, Integer> attackerVariableMap)
	{
		this.attacker = attacker;
		this.defender = defender;
		
		this.defenderVariableMap = defender.getVariableMap();
		this.attackerVariableMap = attackerVariableMap;
	}
	
	public void setDamageVariables()
	{
		int total = 0;
		for (Tier1Element key : damageMap.keySet())
		{
			int dam = damageMap.get(key);
			damageVariableMap.put(("DAMAGE_" + key.toString()).toLowerCase(), dam);
			
			total += dam;
		}
		
		damageVariableMap.put("damage", total);
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
		EquationHelper.setVariableNames(expB, attackerVariableMap, "attacker_");
		EquationHelper.setVariableNames(expB, defenderVariableMap, "defender_");
		EquationHelper.setVariableNames(expB, damageVariableMap, "");
		
		for (String name : reliesOn)
		{
			String atkName = "attacker_" + name.toLowerCase();
			if (!attackerVariableMap.containsKey(atkName))
			{
				expB.variable(atkName);
			}
			
			String defName = "defender_" + name.toLowerCase();
			if (!defenderVariableMap.containsKey(defName))
			{
				expB.variable(defName);
			}
		}
	}
	
	public void writeVariableValues(Expression exp, String[] reliesOn)
	{
		EquationHelper.setVariableValues(exp, attackerVariableMap, "attacker_");
		EquationHelper.setVariableValues(exp, defenderVariableMap, "defender_");
		EquationHelper.setVariableValues(exp, damageVariableMap, "");
		
		for (String name : reliesOn)
		{
			String atkName = "attacker_" + name.toLowerCase();
			if (!attackerVariableMap.containsKey(atkName))
			{
				exp.setVariable(atkName, 0);
			}
			
			String defName = "defender_" + name.toLowerCase();
			if (!defenderVariableMap.containsKey(defName))
			{
				exp.setVariable(defName, 0);
			}
		}
	}

	public int getTotalDamage()
	{
		int totalDam = 0;
		
		for (Tier1Element el : Tier1Element.values())
		{
			totalDam += damageMap.get(el);
		}
		
		return totalDam;
	}
}
