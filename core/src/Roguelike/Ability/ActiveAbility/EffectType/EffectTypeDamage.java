package Roguelike.Ability.ActiveAbility.EffectType;

import java.util.EnumMap;
import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Global;
import Roguelike.Global.Direction;
import Roguelike.Global.Statistic;
import Roguelike.Global.Tier1Element;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Entity.Entity;
import Roguelike.Lights.Light;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.SpriteEffect;
import Roguelike.StatusEffect.StatusEffect;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Functions.RandomFunction;
import exp4j.Helpers.EquationHelper;
import exp4j.Operators.BooleanOperators;

public class EffectTypeDamage extends AbstractEffectType
{
	private EnumMap<Statistic, String> equations = new EnumMap<Statistic, String>(Statistic.class);
	private String[] reliesOn;
	
	@Override
	public void parse(Element xml)
	{			
		reliesOn = xml.getAttribute("ReliesOn", "").split(",");
		
		for (int i = 0; i < xml.getChildCount(); i++)
		{
			Element sEl = xml.getChild(i);
			
			if (sEl.getName().toUpperCase().equals("ATK"))
			{
				for (Tier1Element el : Tier1Element.values())
				{
					String expanded = sEl.getText().toLowerCase();
					expanded = expanded.replaceAll("(?<!_)atk", el.Attack.toString().toLowerCase());
					
					equations.put(el.Attack, expanded);
				}
			}
			else
			{
				Statistic stat = Statistic.valueOf(sEl.getName().toUpperCase());
				equations.put(stat, sEl.getText().toLowerCase());
			}
		}
	}

	@Override
	public void update(ActiveAbility aa, float time, GameTile tile)
	{		
		if (tile.entity != null)
		{
			applyToEntity(tile.entity, aa);
		}
		
		if (tile.environmentEntity != null)
		{
			applyToEntity(tile.environmentEntity, aa);
		}
	}

	private void applyToEntity(Entity target, ActiveAbility aa)
	{
		HashMap<String, Integer> variableMap = aa.variableMap;
		
		for (String name : reliesOn)
		{
			if (!variableMap.containsKey(name.toLowerCase()))
			{
				variableMap.put(name.toLowerCase(), 0);
			}
		}
		
		EnumMap<Statistic, Integer> stats = Statistic.getStatisticsBlock();
		
		for (Statistic stat : Statistic.values())
		{
			if (equations.containsKey(stat))
			{
				String eqn = equations.get(stat);
				
				ExpressionBuilder expB = EquationHelper.createEquationBuilder(eqn);
				EquationHelper.setVariableNames(expB, variableMap, "");
									
				Expression exp = EquationHelper.tryBuild(expB);
				if (exp == null)
				{
					continue;
				}
				
				EquationHelper.setVariableValues(exp, variableMap, "");
									
				int raw = (int)exp.evaluate();
				
				stats.put(stat, raw);
			}
		}
		
		Global.calculateDamage(aa.caster, target, Statistic.statsBlockToVariableBlock(stats), true);			
	}
	
	@Override
	public AbstractEffectType copy()
	{
		EffectTypeDamage e = new EffectTypeDamage();
		e.equations = equations;
		e.reliesOn = reliesOn;
		return e;
	}
}
