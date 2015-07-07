package Roguelike.GameEvent.OnTurn;

import Roguelike.Entity.Entity;

import com.badlogic.gdx.utils.XmlReader.Element;

public class DamageOverTimeEvent extends AbstractOnTurnEvent
{
	float dps;
	float remainder;
	
	@Override
	public boolean handle(Entity entity, float time)
	{
		float rawDamage = dps * time + remainder;
		
		int rounded = (int)Math.floor(rawDamage);
		
		remainder = rawDamage - rounded;
		
		entity.applyDamage(rounded);
		
		return true;
	}

	@Override
	public void parse(Element xml)
	{
		dps = xml.getFloat("Damage");
	}

}
