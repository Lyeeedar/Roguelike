package Roguelike.Entity;

import Roguelike.Entity.AbilityPool.AbilityLine.Ability;

import com.badlogic.gdx.utils.Array;

public class AbilityPool
{
	public final Array<AbilityLine> abilityLines = new Array<AbilityLine>(false, 16);
	
	public AbilityPool()
	{
		for (int i = 0; i < 10; i++)
		{
			AbilityLine newLine = new AbilityLine();
			
			newLine.name = "Ability Line " + i;
			
			ActiveAbility aa1 = ActiveAbility.load("firebolt");
			ActiveAbility aa2 = ActiveAbility.load("firebeam");
			Ability a1 = new Ability();
			a1.ability = aa1;
			
			Ability a2 = new Ability();
			a2.ability = aa2;
			
			Ability a3 = new Ability();
			a3.ability = PassiveAbility.load("ScionOfFlame");
			
			newLine.abilityTiers.add(new Ability[]{a3, a2, a1, a1, a1});
			newLine.abilityTiers.add(new Ability[]{a2, a2, a1, a2, a2});
			newLine.abilityTiers.add(new Ability[]{a2, a1, a2, a1, a2});
			newLine.abilityTiers.add(new Ability[]{a1, a1, a1, a2, a2});
			newLine.abilityTiers.add(new Ability[]{a1, a1, a1, a1, a2});
			
			abilityLines.add(newLine);
		}
	}
	
	public static class AbilityLine
	{
		public String name;
		public String description;
		
		public final Array<Ability[]> abilityTiers = new Array<Ability[]>();
				
		public static class Ability
		{
			public IAbility ability;
			int level;
		}
	}
}
