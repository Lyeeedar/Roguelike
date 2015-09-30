package Roguelike.GameEvent.Damage;

import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Entity.Entity;
import exp4j.Helpers.EquationHelper;

public class DamageObject
{
	public final HashMap<String, Integer> attackerVariableMap;
	public final HashMap<String, Integer> defenderVariableMap;

	public final Entity attacker;
	public final Entity defender;

	public int damage;

	public DamageObject( Entity attacker, Entity defender, HashMap<String, Integer> attackerVariableMap )
	{
		this.attacker = attacker;
		this.defender = defender;

		this.defenderVariableMap = defender.getVariableMap();
		this.attackerVariableMap = attackerVariableMap;
	}

	public void writeVariableNames( ExpressionBuilder expB, String[] reliesOn )
	{
		EquationHelper.setVariableNames( expB, attackerVariableMap, "attacker_" );
		EquationHelper.setVariableNames( expB, defenderVariableMap, "defender_" );

		expB.variable( "damage" );

		for ( String name : reliesOn )
		{
			String atkName = "attacker_" + name;
			if ( !attackerVariableMap.containsKey( name ) )
			{
				expB.variable( atkName );
			}

			String defName = "defender_" + name;
			if ( !defenderVariableMap.containsKey( name ) )
			{
				expB.variable( defName );
			}
		}
	}

	public void writeVariableValues( Expression exp, String[] reliesOn )
	{
		EquationHelper.setVariableValues( exp, attackerVariableMap, "attacker_" );
		EquationHelper.setVariableValues( exp, defenderVariableMap, "defender_" );

		exp.setVariable( "damage", damage );

		for ( String name : reliesOn )
		{
			String atkName = "attacker_" + name;
			if ( !attackerVariableMap.containsKey( name ) )
			{
				exp.setVariable( atkName, 0 );
			}

			String defName = "defender_" + name;
			if ( !defenderVariableMap.containsKey( name ) )
			{
				exp.setVariable( defName, 0 );
			}
		}
	}
}
