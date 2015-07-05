package Roguelike.Ability.ActiveAbility.EffectType;

import Roguelike.Global.Direction;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Lights.Light;
import Roguelike.Sprite.SpriteEffect;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.XmlReader.Element;

public class EffectTypeDamage extends AbstractEffectType
{
	private int power;
	
	@Override
	public void parse(Element xml)
	{		
		//power = xml.getInt("Power");
	}

	@Override
	public void update(ActiveAbility aa, float time, GameTile tile)
	{
		Light l = aa.light != null ? aa.light.copy() : null;
		
		if (tile.Entity != null)
		{
			tile.Entity.HP -= 5;
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
		
		return new EffectTypeDamage();
	}
}
