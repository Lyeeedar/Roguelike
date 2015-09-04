package Roguelike.GameEvent.Constant;

import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Global;
import Roguelike.Global.Statistic;
import Roguelike.Global.Tier1Element;
import Roguelike.Util.FastEnumMap;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

public class ConstantEvent
{
	public FastEnumMap<Statistic, String> equations = new FastEnumMap<Statistic, String>( Statistic.class );
	private String[] reliesOn;

	public void parse( Element xml )
	{
		reliesOn = xml.getAttribute( "ReliesOn", "" ).toLowerCase().split( "," );

		for ( int i = 0; i < xml.getChildCount(); i++ )
		{
			Element sEl = xml.getChild( i );

			if ( sEl.getName().toUpperCase().equals( "ATK" ) )
			{
				for ( Tier1Element el : Tier1Element.values() )
				{
					String expanded = sEl.getText().trim().toLowerCase();
					expanded = expanded.replaceAll( "(?<!_)atk", el.Attack.toString().toLowerCase() );

					equations.put( el.Attack, expanded );
				}
			}
			else if ( sEl.getName().toUpperCase().equals( "DEF" ) )
			{
				for ( Tier1Element el : Tier1Element.values() )
				{
					String expanded = sEl.getText().trim().toLowerCase();
					expanded = expanded.replaceAll( "(?<!_)def", el.Defense.toString().toLowerCase() );

					equations.put( el.Defense, expanded );
				}
			}
			else if ( sEl.getName().toUpperCase().equals( "PIERCE" ) )
			{
				for ( Tier1Element el : Tier1Element.values() )
				{
					String expanded = sEl.getText().trim().toLowerCase();
					expanded = expanded.replaceAll( "(?<!_)pierce", el.Pierce.toString().toLowerCase() );

					equations.put( el.Pierce, expanded );
				}
			}
			else if ( sEl.getName().toUpperCase().equals( "HARDINESS" ) )
			{
				for ( Tier1Element el : Tier1Element.values() )
				{
					String expanded = sEl.getText().trim().toLowerCase();
					expanded = expanded.replaceAll( "(?<!_)hardiness", el.Hardiness.toString().toLowerCase() );

					equations.put( el.Hardiness, expanded );
				}
			}
			else
			{
				Statistic el = Statistic.valueOf( sEl.getName().toUpperCase() );
				equations.put( el, sEl.getText().trim().toLowerCase() );
			}
		}
	}

	public int getStatistic( HashMap<String, Integer> variableMap, Statistic stat )
	{
		String eqn = equations.get( stat );

		if ( eqn == null ) { return 0; }

		if ( Global.isNumber( eqn ) )
		{
			return Integer.parseInt( eqn );
		}
		else
		{
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

			ExpressionBuilder expB = EquationHelper.createEquationBuilder( eqn );
			EquationHelper.setVariableNames( expB, variableMap, "" );

			Expression exp = EquationHelper.tryBuild( expB );
			if ( exp == null ) { return 0; }

			EquationHelper.setVariableValues( exp, variableMap, "" );

			int val = (int) exp.evaluate();

			return val;
		}
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

		{
			int val = getStatistic( variableMap, Statistic.MAXHP );
			if ( val > 0 )
			{
				String line = "MaxHP " + val;
				lines.add( line );
			}
		}

		for ( Tier1Element el : Tier1Element.values() )
		{
			if ( equations.containsKey( el.Attack ) )
			{
				int atkVal = variableMap.get( el.Attack.toString().toLowerCase() );

				if ( atkVal > 0 )
				{
					String line = Global.capitalizeString( el.toString() ) + " attack ";
					line += "[" + el.toString() + "] ";
					line += atkVal;

					if ( equations.containsKey( el.Pierce ) )
					{
						line += " (" + variableMap.get( el.Pierce.toString().toLowerCase() ) + ")";
					}

					line += "[]";

					lines.add( line );
				}
			}
		}

		for ( Tier1Element el : Tier1Element.values() )
		{
			if ( equations.containsKey( el.Defense ) )
			{
				int defVal = variableMap.get( el.Defense.toString().toLowerCase() );

				if ( defVal > 0 )
				{
					String line = Global.capitalizeString( el.toString() ) + " defense ";
					line += "[" + el.toString() + "] ";
					line += defVal;

					if ( equations.containsKey( el.Hardiness ) )
					{
						line += " (" + variableMap.get( el.Hardiness.toString().toLowerCase() ) + ")";
					}

					line += "[]";

					lines.add( line );
				}
			}
		}

		return lines;
	}
}
