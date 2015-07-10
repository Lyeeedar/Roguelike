package Roguelike.Ability.ActiveAbility.EffectType;

import java.util.EnumMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Global;
import Roguelike.Global.Direction;
import Roguelike.Global.Statistics;
import Roguelike.Global.Tier1Element;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
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
	
	@Override
	public void parse(Element xml)
	{		
		Element statsElement = xml.getChildByName("Statistics");
		for (int i = 0; i < statsElement.getChildCount(); i++)
		{
			Element sEl = statsElement.getChild(i);
			
			Statistics el = Statistics.valueOf(sEl.getName().toUpperCase());
			equations.put(el, sEl.getText());
		}
	}

	@Override
	public void update(ActiveAbility aa, float time, GameTile tile)
	{
		Light l = aa.light != null ? aa.light.copy() : null;
		
		if (tile.Entity != null)
		{
			EnumMap<Statistics, Integer> att = Statistics.getStatisticsBlock();
			for (Statistics stat : Statistics.values())
			{
				if (equations.containsKey(stat))
				{
					String eqn = equations.get(stat);
					
					ExpressionBuilder expB = new ExpressionBuilder(eqn);
					BooleanOperators.applyOperators(expB);
					expB.function(new RandomFunction());
					
					aa.caster.fillExpressionBuilderWithValues(expB, "");
					
					Expression exp = EquationHelper.tryBuild(expB);
					if (exp == null)
					{
						continue;
					}
					
					aa.caster.fillExpressionWithValues(exp, "");
					
					int raw = (int)exp.evaluate();
					
					att.put(stat, raw);
				}
			}
			
			int damage = Global.calculateDamage(att, tile.Entity.getStatistics());
			
			tile.Entity.applyDamage(damage);
			tile.Entity.SpriteEffects.add(new SpriteEffect(aa.hitSprite.copy(), Direction.CENTER, l));
		}
		else
		{
			tile.SpriteEffects.add(new SpriteEffect(aa.hitSprite.copy(), Direction.CENTER, l));
		}
	}

	
	@Override
	public AbstractEffectType copy()
	{
		EffectTypeDamage e = new EffectTypeDamage();
		e.equations = equations;
		return e;
	}
}
