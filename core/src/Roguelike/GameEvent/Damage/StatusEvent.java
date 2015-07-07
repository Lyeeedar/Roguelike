package Roguelike.GameEvent.Damage;

import Roguelike.StatusEffect.StatusEffect;

import com.badlogic.gdx.utils.XmlReader.Element;

public class StatusEvent extends AbstractOnDamageEvent
{
	private String attackerStatus;
	private String defenderStatus;
	
	@Override
	public boolean handle(DamageObject obj)
	{
		if (attackerStatus != null)
		{
			obj.attacker.addStatusEffect(StatusEffect.load(attackerStatus));
		}
		
		if (defenderStatus != null)
		{
			obj.defender.addStatusEffect(StatusEffect.load(defenderStatus));
		}
		
		return true;
	}

	@Override
	public void parse(Element xml)
	{
		attackerStatus = xml.get("Attacker", null);
		defenderStatus = xml.get("Defender", null);
	}

}
