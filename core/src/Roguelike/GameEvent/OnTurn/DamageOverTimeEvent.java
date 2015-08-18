package Roguelike.GameEvent.OnTurn;

import java.util.EnumMap;
import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Global;
import Roguelike.Global.Statistic;
import Roguelike.Global.Tier1Element;
import Roguelike.Entity.Entity;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

public class DamageOverTimeEvent extends AbstractOnTurnEvent
{
	private String condition;
	private EnumMap<Statistic, String> equations = new EnumMap<Statistic, String>( Statistic.class );
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

		EnumMap<Statistic, Integer> stats = Statistic.getStatisticsBlock();

		for ( Statistic stat : Statistic.values() )
		{
			if ( equations.containsKey( stat ) )
			{
				String eqn = equations.get( stat );

				ExpressionBuilder expB = EquationHelper.createEquationBuilder( eqn );
				EquationHelper.setVariableNames( expB, variableMap, "" );

				Expression exp = EquationHelper.tryBuild( expB );
				if ( exp == null )
				{
					continue;
				}

				EquationHelper.setVariableValues( exp, variableMap, "" );

				int raw = (int) exp.evaluate();

				stats.put( stat, raw );
			}
		}

		while ( accumulator > 1 )
		{
			accumulator -= 1;

			Global.calculateDamage( entity, entity, Statistic.statsBlockToVariableBlock( stats ), false );
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

		for ( int i = 0; i < xml.getChildCount(); i++ )
		{
			Element sEl = xml.getChild( i );

			Statistic el = Statistic.valueOf( sEl.getName().toUpperCase() );
			equations.put( el, sEl.getText().toLowerCase() );
		}
	}

	@Override
	public Array<String> toString( HashMap<String, Integer> variableMap )
	{
		Array<String> lines = new Array<String>();

		for ( Tier1Element el : Tier1Element.values() )
		{
			if ( equations.containsKey( el.Attack ) )
			{
				int atkVal = variableMap.get( el.Attack.toString().toLowerCase() );

				if ( atkVal > 0 )
				{
					String line = "Deals ";
					line += "[" + el.toString() + "] ";
					line += atkVal;

					if ( equations.containsKey( el.Pierce ) )
					{
						line += " (" + variableMap.get( el.Pierce.toString().toLowerCase() ) + ")";
					}

					line += " " + Global.capitalizeString( el.toString() ) + "[] DPS";

					lines.add( line );
				}
			}
		}

		return lines;
	}
}
