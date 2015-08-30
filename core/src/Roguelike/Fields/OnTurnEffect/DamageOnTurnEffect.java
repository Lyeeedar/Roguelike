package Roguelike.Fields.OnTurnEffect;

import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Global;
import Roguelike.Global.Statistic;
import Roguelike.Entity.Entity;
import Roguelike.Fields.Field;
import Roguelike.Util.FastEnumMap;

import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

public class DamageOnTurnEffect extends AbstractOnTurnEffect
{
	private String condition;
	private FastEnumMap<Statistic, String> equations = new FastEnumMap<Statistic, String>( Statistic.class );
	private String[] reliesOn;

	private void doDamage( Entity entity, Field field, float cost )
	{
		HashMap<String, Integer> variableMap = entity.getVariableMap();
		for ( String name : reliesOn )
		{
			if ( !variableMap.containsKey( name.toLowerCase() ) )
			{
				variableMap.put( name.toLowerCase(), 0 );
			}
		}
		variableMap.put( "stacks", field.stacks );

		if ( condition != null )
		{
			ExpressionBuilder expB = EquationHelper.createEquationBuilder( condition );
			EquationHelper.setVariableNames( expB, variableMap, "" );

			Expression exp = EquationHelper.tryBuild( expB );
			if ( exp == null ) { return; }

			EquationHelper.setVariableValues( exp, variableMap, "" );

			double conditionVal = exp.evaluate();

			if ( conditionVal == 0 ) { return; }
		}

		FastEnumMap<Statistic, Integer> stats = Statistic.getStatisticsBlock();

		for ( Statistic stat : Statistic.values() )
		{
			if ( equations.containsKey( stat ) )
			{
				int raw = 0;
				String eqn = equations.get( stat );

				if ( Global.isNumber( eqn ) )
				{
					raw = Integer.parseInt( eqn );
				}
				else
				{
					ExpressionBuilder expB = EquationHelper.createEquationBuilder( eqn );
					EquationHelper.setVariableNames( expB, variableMap, "" );

					Expression exp = EquationHelper.tryBuild( expB );
					if ( exp == null )
					{
						continue;
					}

					EquationHelper.setVariableValues( exp, variableMap, "" );

					raw = (int) ( exp.evaluate() * cost );
				}

				stats.put( stat, raw );
			}
		}

		Global.calculateDamage( entity, entity, Statistic.statsBlockToVariableBlock( stats ), false );
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
	public void process( Field field, Entity entity, float cost )
	{
		doDamage( entity, field, cost );
	}

}
