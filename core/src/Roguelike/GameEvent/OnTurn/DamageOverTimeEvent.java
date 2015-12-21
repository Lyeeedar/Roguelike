package Roguelike.GameEvent.OnTurn;

import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Global;
import Roguelike.Global.Statistic;
import Roguelike.Entity.Entity;
import Roguelike.Util.FastEnumMap;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

public final class DamageOverTimeEvent extends AbstractOnTurnEvent
{
	private String condition;
	private String eqn;
	private String[] reliesOn;

	private float accumulator;

	@Override
	public boolean handle( Entity entity, float time )
	{
		accumulator += time;

		if ( accumulator < 0 ) { return false; }

		HashMap<String, Integer> variableMap = entity.getVariableMap();
		for ( String name : reliesOn )
		{
			if ( !variableMap.containsKey( name.toLowerCase() ) )
			{
				variableMap.put( name.toLowerCase(), 0 );
			}
		}

		if ( condition != null )
		{
			ExpressionBuilder expB = EquationHelper.createEquationBuilder( condition );
			EquationHelper.setVariableNames( expB, variableMap, "" );

			Expression exp = EquationHelper.tryBuild( expB );
			if ( exp == null )
			{
				accumulator = 0;
				return false;
			}

			EquationHelper.setVariableValues( exp, variableMap, "" );

			double conditionVal = exp.evaluate();

			if ( conditionVal == 0 )
			{
				accumulator = 0;
				return false;
			}
		}

		int raw = 0;
		if ( Global.isNumber( eqn ) )
		{
			raw = Integer.parseInt( eqn );
		}
		else
		{
			ExpressionBuilder expB = EquationHelper.createEquationBuilder( eqn );
			EquationHelper.setVariableNames( expB, variableMap, "" );

			Expression exp = EquationHelper.tryBuild( expB );
			if ( exp != null )
			{
				EquationHelper.setVariableValues( exp, variableMap, "" );
				raw = (int) exp.evaluate();
			}
		}

		while ( accumulator > 1 )
		{
			accumulator -= 1;

			Global.calculateDamage( entity, entity, raw, 0, false );
		}

		return true;
	}

	@Override
	public void parse( Element xml )
	{
		condition = xml.getAttribute( "Condition", null );
		if ( condition != null )
		{
			condition = condition.toLowerCase();
		}

		reliesOn = xml.getAttribute( "ReliesOn", "" ).split( "," );

		eqn = xml.getText();
	}

	@Override
	public Array<String> toString( HashMap<String, Integer> variableMap )
	{
		Array<String> lines = new Array<String>();

		for ( String name : reliesOn )
		{
			if ( !variableMap.containsKey( name.toLowerCase() ) )
			{
				variableMap.put( name.toLowerCase(), 0 );
			}
		}

		int raw = 0;
		if ( Global.isNumber( eqn ) )
		{
			raw = Integer.parseInt( eqn );
		}
		else
		{
			ExpressionBuilder expB = EquationHelper.createEquationBuilder( eqn );
			EquationHelper.setVariableNames( expB, variableMap, "" );

			Expression exp = EquationHelper.tryBuild( expB );
			if ( exp != null )
			{
				EquationHelper.setVariableValues( exp, variableMap, "" );
				raw = (int) exp.evaluate();
			}
		}

		lines.add( "Total Damage: " + raw );

		return lines;
	}
}
