package Roguelike.GameEvent.Damage;

import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Global.ElementType;
import Roguelike.Entity.Entity;
import Roguelike.Util.FastEnumMap;
import exp4j.Helpers.EquationHelper;

public class DamageObject
{
	public final HashMap<String, Integer> attackerVariableMap;
	public final HashMap<String, Integer> defenderVariableMap;

	public final Entity attacker;
	public final Entity defender;

	public final FastEnumMap<ElementType, Integer> damageMap = ElementType.getElementBlock();
	public final HashMap<String, Integer> damageVariableMap = new HashMap<String, Integer>();

	public DamageObject( Entity attacker, Entity defender, HashMap<String, Integer> attackerVariableMap )
	{
		this.attacker = attacker;
		this.defender = defender;

		this.defenderVariableMap = defender.getVariableMap();
		this.attackerVariableMap = attackerVariableMap;
	}

	public void setDamageVariables()
	{
		int total = 0;
		for ( ElementType key : ElementType.values() )
		{
			if ( damageMap.containsKey( key ) )
			{
				int dam = damageMap.get( key );
				damageVariableMap.put( ( "DAMAGE_" + key.toString() ).toLowerCase(), dam );

				total += dam;
			}
		}

		damageVariableMap.put( "damage", total );
	}

	public void modifyDamage( FastEnumMap<ElementType, Integer> dam )
	{
		for ( ElementType el : ElementType.values() )
		{
			int oldVal = damageMap.get( el );
			int newVal = dam.get( el );

			damageMap.put( el, oldVal + newVal );
		}

		setDamageVariables();
	}

	public void writeVariableNames( ExpressionBuilder expB, String[] reliesOn )
	{
		EquationHelper.setVariableNames( expB, attackerVariableMap, "attacker_" );
		EquationHelper.setVariableNames( expB, defenderVariableMap, "defender_" );
		EquationHelper.setVariableNames( expB, damageVariableMap, "" );

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
		EquationHelper.setVariableValues( exp, damageVariableMap, "" );

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

	public int getTotalDamage()
	{
		int totalDam = 0;

		for ( ElementType el : ElementType.values() )
		{
			totalDam += damageMap.get( el );
		}

		return totalDam;
	}
}
