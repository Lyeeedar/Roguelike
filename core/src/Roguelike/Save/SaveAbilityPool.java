package Roguelike.Save;

import com.badlogic.gdx.utils.Array;

import Roguelike.Global;
import Roguelike.Ability.AbilityPool;
import Roguelike.Ability.AbilityPool.Ability;
import Roguelike.Ability.AbilityPool.AbilityLine;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Ability.PassiveAbility.PassiveAbility;

public class SaveAbilityPool extends SaveableObject<AbilityPool>
{
	public Array<SaveAbilityLine> lines = new Array<SaveAbilityLine>();
	public Float[] abilityCooldown = new Float[Global.NUM_ABILITY_SLOTS];

	@Override
	public void store(AbilityPool obj)
	{
		for (AbilityLine line : obj.abilityLines)
		{
			SaveAbilityLine sline = new SaveAbilityLine();
			lines.add(sline);
			
			sline.fileName = line.fileName;
			
			for (Ability[] tier : line.abilityTiers)
			{
				SaveAbilityPoolItem[] stier = new SaveAbilityPoolItem[5];
				sline.tiers.add(stier);
				
				for (int i = 0; i < 5; i++)
				{
					SaveAbilityPoolItem sitem = new SaveAbilityPoolItem();
					stier[i] = sitem;
					
					sitem.unlocked = tier[i].unlocked;
					
					if (tier[i].ability instanceof ActiveAbility)
					{
						sitem.socketIndex = obj.getActiveAbilityIndex((ActiveAbility)tier[i].ability);
					}
					else
					{
						sitem.socketIndex = obj.getPassiveAbilityIndex((PassiveAbility)tier[i].ability);
					}
				}	
			}
		}
		
		for (int i = 0; i < obj.slottedActiveAbilities.length; i++)
		{
			if (obj.slottedActiveAbilities[i] != null)
			{
				abilityCooldown[i] = obj.slottedActiveAbilities[i].cooldownAccumulator;
			}
		}
	}

	@Override
	public AbilityPool create()
	{
		AbilityPool pool = new AbilityPool();
		
		for (SaveAbilityLine sline : lines)
		{
			AbilityLine line = AbilityLine.load(sline.fileName);
			pool.addAbilityLine(line);
			
			for (int t = 0; t < sline.tiers.size; t++)
			{
				for (int i = 0; i < 5; i++)
				{
					SaveAbilityPoolItem item = sline.tiers.get(t)[i];
					Ability ab = line.abilityTiers.get(t)[i];
					
					ab.unlocked = item.unlocked;
					
					if (item.socketIndex >= 0)
					{
						if (ab.ability instanceof ActiveAbility)
						{
							pool.slotActiveAbility((ActiveAbility)ab.ability, item.socketIndex);
						}
						else
						{
							pool.slotPassiveAbility((PassiveAbility)ab.ability, item.socketIndex);
						}
					}
				}
			}
		}
		
		for (int i = 0; i < pool.slottedActiveAbilities.length; i++)
		{
			if (pool.slottedActiveAbilities[i] != null)
			{
				pool.slottedActiveAbilities[i].cooldownAccumulator = abilityCooldown[i];
			}
		}
		
		return pool;
	}
	
	public static class SaveAbilityLine
	{
		public String fileName;
		public Array<SaveAbilityPoolItem[]> tiers = new Array<SaveAbilityPoolItem[]>();
	}

	public static class SaveAbilityPoolItem
	{
		public boolean unlocked;
		public int socketIndex;
	}
}
