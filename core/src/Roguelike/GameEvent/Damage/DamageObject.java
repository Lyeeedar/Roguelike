package Roguelike.GameEvent.Damage;

import Roguelike.Entity.Entity;

public class DamageObject
{
	public Entity attacker;
	public Entity defender;	
	public int damage;
	
	public DamageObject(Entity attacker, Entity defender, int damage)
	{
		this.attacker = attacker;
		this.defender = defender;
		this.damage = damage;
	}
}
