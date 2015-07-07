package Roguelike.GameEvent.OnTurn;

import Roguelike.Entity.Entity;

import com.badlogic.gdx.utils.XmlReader.Element;

public class HealOverTimeEvent extends AbstractOnTurnEvent
{
	float hps;
	float remainder;
	
	@Override
	public boolean handle(Entity entity, float time)
	{
		float raw = hps * time + remainder;
		
		int rounded = (int)Math.floor(raw);
		
		remainder = raw - rounded;
		
		entity.applyHealing(rounded);
		
		return true;
	}

	@Override
	public void parse(Element xml)
	{
		hps = xml.getFloat("Heal");
	}

}