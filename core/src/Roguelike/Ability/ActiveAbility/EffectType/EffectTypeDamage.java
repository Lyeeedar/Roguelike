package Roguelike.Ability.ActiveAbility.EffectType;

import java.util.EnumMap;

import Roguelike.Global;
import Roguelike.Global.Direction;
import Roguelike.Global.Tier1Element;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Lights.Light;
import Roguelike.Sprite.SpriteEffect;
import Roguelike.StatusEffect.StatusEffect;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.XmlReader.Element;

public class EffectTypeDamage extends AbstractEffectType
{
	private EnumMap<Tier1Element, Integer> m_attunement = Tier1Element.getElementMap();
	
	@Override
	public void parse(Element xml)
	{		
		m_attunement = Tier1Element.load(xml.getChildByName("Attunement"), m_attunement);
	}

	@Override
	public void update(ActiveAbility aa, float time, GameTile tile)
	{
		Light l = aa.light != null ? aa.light.copy() : null;
		
		if (tile.Entity != null)
		{
			int damage = Global.calculateDamage(aa.caster.getAttunements(), tile.Entity.getAttunements(), m_attunement);
			
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
		e.m_attunement = Tier1Element.copy(m_attunement);
		return e;
	}
}
