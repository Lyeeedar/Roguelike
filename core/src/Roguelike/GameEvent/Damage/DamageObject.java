package Roguelike.GameEvent.Damage;

import java.util.EnumMap;
import java.util.HashMap;

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
			damageVariableMap.put(key.toString(), dam);
			
			total += dam;
		}
		
		damageVariableMap.put("DAMAGE", total);
	}
}
