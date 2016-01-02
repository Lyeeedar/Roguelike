package Roguelike.GameEvent.Constant;

import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Global;
import Roguelike.Global.Statistic;
import Roguelike.Util.FastEnumMap;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

public final class ConstantEvent
{
	public FastEnumMap<Statistic, String> equations = new FastEnumMap<Statistic, String>( Statistic.class );
	private String[] reliesOn;

	public void parse( Element xml )
	{
		reliesOn = xml.getAttribute( "ReliesOn", "" ).toLowerCase().split( "," );

		for ( int i = 0; i < xml.getChildCount(); i++ )
		{
			Element sEl = xml.getChild( i );

			Statistic el = Statistic.valueOf( sEl.getName().toUpperCase() );
			equations.put( el, sEl.getText().trim().toLowerCase() );
		}
	}

	public int getStatistic( HashMap<String, Integer> variableMap, Statistic stat )
	{
		String eqn = equations.get( stat );

		if ( eqn == null ) { return 0; }

		if ( reliesOn != null )
		{
			for ( String name : reliesOn )
			{
				if ( !variableMap.containsKey( name ) )
				{
					variableMap.put( name, 0 );
				}
			}
		}

		return EquationHelper.evaluate( eqn, variableMap );
	}

	public void putStatistic( Statistic stat, String eqn )
	{
		equations.put( stat, eqn );
	}

	public static ConstantEvent load( Element xml )
	{
		ConstantEvent ce = new ConstantEvent();

		ce.parse( xml );

		return ce;
	}

	public Array<String> toString( HashMap<String, Integer> variableMap )
	{
		Array<String> lines = new Array<String>();

		for ( Statistic stat : Statistic.BaseValues )
		{
			int val = getStatistic( variableMap, stat );

			if ( val != 0 )
			{
				lines.add( Global.capitalizeString( stat.toString() ) + ": " + val );
			}
		}

		for ( Statistic stat : Statistic.ModifierValues )
		{
			int val = getStatistic( variableMap, stat );

			if ( val != 0 )
			{
				lines.add( Global.capitalizeString( stat.toString() ) + ": " + val );
			}
		}

		return lines;
	}
}
