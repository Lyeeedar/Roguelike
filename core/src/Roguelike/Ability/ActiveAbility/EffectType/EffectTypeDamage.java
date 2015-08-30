package Roguelike.Ability.ActiveAbility.EffectType;

import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Global;
import Roguelike.Global.Statistic;
import Roguelike.Global.Tier1Element;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Entity.Entity;
import Roguelike.GameEvent.GameEventHandler;
import Roguelike.Items.Item;
import Roguelike.Tiles.GameTile;
import Roguelike.Util.FastEnumMap;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

public class EffectTypeDamage extends AbstractEffectType
{
	private FastEnumMap<Statistic, String> equations = new FastEnumMap<Statistic, String>( Statistic.class );
	private String[] reliesOn;

	@Override
	public void parse( Element xml )
	{
		reliesOn = xml.getAttribute( "ReliesOn", "" ).split( "," );

		for ( int i = 0; i < xml.getChildCount(); i++ )
		{
			Element sEl = xml.getChild( i );

			if ( sEl.getName().toUpperCase().equals( "ATK" ) )
			{
				for ( Tier1Element el : Tier1Element.values() )
				{
					String expanded = sEl.getText().toLowerCase();
					expanded = expanded.replaceAll( "(?<!_)atk", el.Attack.toString().toLowerCase() );

					equations.put( el.Attack, expanded );
				}
			}
			else if ( sEl.getName().toUpperCase().equals( "PIERCE" ) )
			{
				for ( Tier1Element el : Tier1Element.values() )
				{
					String expanded = sEl.getText().toLowerCase();
					expanded = expanded.replaceAll( "(?<!_)pierce", el.Pierce.toString().toLowerCase() );

					equations.put( el.Pierce, expanded );
				}
			}
			else
			{
				Statistic stat = Statistic.valueOf( sEl.getName().toUpperCase() );
				equations.put( stat, sEl.getText().toLowerCase() );
			}
		}
	}

	@Override
	public void update( ActiveAbility aa, float time, GameTile tile )
	{
		if ( tile.entity != null || tile.environmentEntity != null )
		{
			HashMap<String, Integer> variableMap = calculateVariableMap( aa );

			if ( tile.entity != null )
			{
				applyToEntity( tile.entity, aa, variableMap );
			}

			if ( tile.environmentEntity != null )
			{
				applyToEntity( tile.environmentEntity, aa, variableMap );
			}
		}
	}

	private void applyToEntity( Entity target, ActiveAbility aa, HashMap<String, Integer> variableMap )
	{
		Global.calculateDamage( aa.caster, target, variableMap, true );
	}

	private HashMap<String, Integer> calculateVariableMap( ActiveAbility aa )
	{
		HashMap<String, Integer> variableMap = aa.caster.getBaseVariableMap();

		for ( String name : reliesOn )
		{
			if ( !variableMap.containsKey( name.toLowerCase() ) )
			{
				variableMap.put( name.toLowerCase(), 0 );
			}
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

					raw = (int) exp.evaluate();
				}

				stats.put( stat, raw );
			}
		}

		variableMap = Statistic.statsBlockToVariableBlock( stats );

		for ( GameEventHandler handler : aa.caster.getAllHandlers() )
		{
			if ( handler instanceof Item )
			{
				continue;
			}

			for ( Statistic s : Statistic.values() )
			{
				int val = handler.getStatistic( variableMap, s );

				variableMap.put( s.toString().toLowerCase(), variableMap.get( s.toString().toLowerCase() ) + val );
			}
		}

		return variableMap;
	}

	@Override
	public AbstractEffectType copy()
	{
		EffectTypeDamage e = new EffectTypeDamage();
		e.equations = equations;
		e.reliesOn = reliesOn;
		return e;
	}

	@Override
	public String toString( ActiveAbility aa )
	{
		HashMap<String, Integer> variableMap = calculateVariableMap( aa );

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

					line += " " + Global.capitalizeString( el.toString() ) + "[] Damage";

					lines.add( line );
				}
			}
		}

		return String.join( "\n", lines );
	}
}
