package Roguelike.GameEvent.Damage;

import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Entity.Entity;
import exp4j.Helpers.EquationHelper;

public class DamageObject
{
	public int damage;

	public DamageObject( int damage )
	{
		this.damage = damage;
	}
}
