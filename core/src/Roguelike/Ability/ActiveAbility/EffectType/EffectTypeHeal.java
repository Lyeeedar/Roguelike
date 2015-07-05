package Roguelike.Ability.ActiveAbility.EffectType;

import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.XmlReader.Element;

public class EffectTypeHeal extends AbstractEffectType
{
	private int power;
	
	@Override
	public void parse(Element xml)
	{		
		power = xml.getInt("Power");
	}

	@Override
	public void update(ActiveAbility aa, float time, GameTile tile)
	{
	}

	
	@Override
	public AbstractEffectType copy()
	{
		return new EffectTypeHeal();
	}
}
