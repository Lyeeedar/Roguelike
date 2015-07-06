package Roguelike.Ability.ActiveAbility.EffectType;

import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.StatusEffect.StatusEffect;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.XmlReader.Element;

public class EffectTypeStatus extends AbstractEffectType
{
	private String statusName;
	
	@Override
	public void parse(Element xml)
	{		
		statusName = xml.getText();
	}

	@Override
	public void update(ActiveAbility aa, float time, GameTile tile)
	{		
		if (tile.Entity != null)
		{
			tile.Entity.addStatusEffect(StatusEffect.load(statusName));
		}
	}

	
	@Override
	public AbstractEffectType copy()
	{
		EffectTypeStatus e = new EffectTypeStatus();
		e.statusName = statusName;
		return e;
	}
}
