package Roguelike.Ability.ActiveAbility.EffectType;

import java.util.EnumMap;
import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Global;
import Roguelike.Global.Direction;
import Roguelike.Global.Statistics;
import Roguelike.Global.Tier1Element;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Entity.Entity;
import Roguelike.Lights.Light;
import Roguelike.Sprite.SpriteEffect;
import Roguelike.StatusEffect.StatusEffect;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Functions.RandomFunction;
import exp4j.Helpers.EquationHelper;
import exp4j.Operators.BooleanOperators;

public class EffectTypeDamage extends AbstractEffectType
{
	private EnumMap<Statistics, String> equations = new EnumMap<Statistics, String>(Statistics.class);
	private String[] reliesOn;
	
	@Override
	public void parse(Element xml)
	{			
		reliesOn = xml.getAttribute("ReliesOn", "").split(",");
		
		for (int i = 0; i < xml.getChildCount(); i++)
		{
			Element sEl = xml.getChild(i);
			
			Statistics el = Statistics.valueOf(sEl.getName().toUpperCase());
			equations.put(el, sEl.getText().toLowerCase());
		}
	}

	@Override
	public void update(ActiveAbility aa, float time, GameTile tile)
	{
		Light l = aa.light != null ? aa.light.copy() : null;
		
		if (tile.entity != null)
		{
			applyToEntity(tile.entity, aa, l);
		}
		
		if (tile.environmentEntity != null)
		{
			applyToEntity(tile.environmentEntity, aa, l);
		}
		
		{
			tile.spriteEffects.add(new SpriteEffect(aa.getHitSprite().copy(), Direction.CENTER, l));
		}
	}

	private void applyToEntity(Entity target, ActiveAbility aa, Light l)
	{
		HashMap<String, Integer> variableMap = aa.caster.getVariableMap();
		
		for (String name : reliesOn)
		{
			if (!variableMap.containsKey(name.toLowerCase()))
			{
				variableMap.put(name.toLowerCase(), 0);
			}
		}
		
		EnumMap<Statistics, Integer> stats = Statistics.getStatisticsBlock();
		
		for (Statistics stat : Statistics.values())
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
		
		Global.calculateDamage(aa.caster, target, Statistics.statsBlockToVariableBlock(stats), true);			
		target.spriteEffects.add(new SpriteEffect(aa.getHitSprite().copy(), Direction.CENTER, l));
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
