package Roguelike.Ability.ActiveAbility.EffectType;

import java.util.EnumMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Global;
import Roguelike.Global.Direction;
import Roguelike.Global.Tier1Element;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Lights.Light;
import Roguelike.Sprite.SpriteEffect;
import Roguelike.StatusEffect.StatusEffect;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Functions.RandomFunction;
import exp4j.Operators.BooleanOperators;

public class EffectTypeDamage extends AbstractEffectType
{
	private EnumMap<Tier1Element, String> m_attunement = new EnumMap<Tier1Element, String>(Tier1Element.class);
	
	@Override
	public void parse(Element xml)
	{		
		Element attunementElement = xml.getChildByName("Attunement");
		for (int i = 0; i < attunementElement.getChildCount(); i++)
		{
			Element attEl = attunementElement.getChild(i);
			
			Tier1Element el = Tier1Element.valueOf(attEl.getName().toUpperCase());
			m_attunement.put(el, attEl.getText());
		}
	}

	@Override
	public void update(ActiveAbility aa, float time, GameTile tile)
	{
		Light l = aa.light != null ? aa.light.copy() : null;
		
		if (tile.Entity != null)
		{
			EnumMap<Tier1Element, Integer> att = Tier1Element.getElementMap();
			for (Tier1Element el : Tier1Element.values())
			{
				if (m_attunement.containsKey(el))
				{
					String eqn = m_attunement.get(el);
					
					ExpressionBuilder expB = new ExpressionBuilder(eqn);
					BooleanOperators.applyOperators(expB);
					expB.function(new RandomFunction());
					aa.caster.fillExpressionBuilderWithValues(expB, "");
					Expression exp = expB.build();
					aa.caster.fillExpressionWithValues(exp, "");
					
					int raw = (int)exp.evaluate();
					
					att.put(el, raw);
				}
			}
			
			int damage = Global.calculateDamage(aa.caster.getAttunements(), tile.Entity.getAttunements(), att);
			
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
		e.m_attunement = m_attunement;
		return e;
	}
}
