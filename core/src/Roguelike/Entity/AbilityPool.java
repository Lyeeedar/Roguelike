package Roguelike.Entity;

import java.io.IOException;

import Roguelike.Entity.AbilityPool.AbilityLine.Ability;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class AbilityPool
{
	public final Array<AbilityLine> abilityLines = new Array<AbilityLine>(false, 16);
	
	public AbilityPool()
	{
		loadAbilityLine("MetalAdept");
	}
	
	public void loadAbilityLine(String name)
	{
		XmlReader xml = new XmlReader();
		Element xmlElement = null;
		
		try
		{
			xmlElement = xml.parse(Gdx.files.internal("Abilities/Lines/"+name+".xml"));
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		AbilityLine newLine = new AbilityLine();
		
		newLine.name = xmlElement.get("Name");
		newLine.description = xmlElement.get("Description");
		
		Element tiersElement = xmlElement.getChildByName("Tiers");
		for (Element tierElement : tiersElement.getChildrenByName("Tier"))
		{
			Ability[] tier = new Ability[5];
			
			for (int i = 0; i < 5; i++)
			{
				Element abilityElement = tierElement.getChild(i);
				
				Ability ab = new Ability();
				
				if (abilityElement.getName().equals("ActiveAbility"))
				{
					ab.ability = ActiveAbility.load(abilityElement.get("Name"));
				}
				else
				{
					ab.ability = PassiveAbility.load(abilityElement.get("Name"));
				}
				
				ab.cost = abilityElement.getInt("Cost");
				
				tier[i] = ab;
			}
			
			newLine.abilityTiers.add(tier);
		}
		
		abilityLines.add(newLine);
	}
	
	public static class AbilityLine
	{
		public String name;
		public String description;
		
		public final Array<Ability[]> abilityTiers = new Array<Ability[]>();
				
		public static class Ability
		{
			public IAbility ability;
			public int cost;
			public boolean unlocked;
			public boolean linked;
		}
	}
}
