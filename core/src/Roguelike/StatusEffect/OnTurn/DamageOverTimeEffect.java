package Roguelike.StatusEffect.OnTurn;

import com.badlogic.gdx.utils.XmlReader.Element;

import Roguelike.StatusEffect.StatusEffect;

public class DamageOverTimeEffect extends AbstractOnTurnEffect
{
	float dps;
	float remainder;
	
	@Override
	public void evaluate(StatusEffect effect, float time)
	{
		float rawDamage = dps * time + remainder;
		
		int rounded = (int)Math.floor(rawDamage);
		
		remainder = rawDamage - rounded;
		
		effect.attachedTo.HP -= rounded;
	}

	@Override
	public void parse(Element xml)
	{
		dps = xml.getFloat("DPS");
	}

}
