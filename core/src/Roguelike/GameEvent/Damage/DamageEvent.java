package Roguelike.GameEvent.Damage;

import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Global;
import Roguelike.Global.Tier1Element;
import Roguelike.GameEvent.IGameObject;
import Roguelike.Util.FastEnumMap;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

public class DamageEvent extends AbstractOnDamageEvent
{
	private String condition;
	private FastEnumMap<Tier1Element, String> equations = new FastEnumMap<Tier1Element, String>( Tier1Element.class );
	private String[] reliesOn;

	@Override
	public boolean handle( DamageObject obj, IGameObject parent )
	{
		if ( condition != null )
		{
			ExpressionBuilder expB = EquationHelper.createEquationBuilder( condition );
			obj.writeVariableNames( expB, reliesOn );

			Expression exp = EquationHelper.tryBuild( expB );
			if ( exp == null ) { return false; }

			obj.writeVariableValues( exp, reliesOn );

			double conditionVal = exp.evaluate();

			if ( conditionVal == 0 ) { return false; }
		}

		FastEnumMap<Tier1Element, Integer> els = Tier1Element.getElementBlock();

		for ( Tier1Element el : Tier1Element.values() )
		{
			if ( equations.containsKey( el ) )
			{
				int raw = 0;
				String eqn = equations.get( el );

				if ( Global.isNumber( eqn ) )
				{
					raw = Integer.parseInt( eqn );
				}
				else
				{
					ExpressionBuilder expB = EquationHelper.createEquationBuilder( eqn );
					obj.writeVariableNames( expB, reliesOn );

					Expression exp = EquationHelper.tryBuild( expB );
					if ( exp == null )
					{
						continue;
					}

					obj.writeVariableValues( exp, reliesOn );

					raw = (int) exp.evaluate();
				}

				els.put( el, raw );
			}
		}

		obj.modifyDamage( els );

		return true;
	}

	@Override
	public void parse( Element xml )
	{
		reliesOn = xml.getAttribute( "ReliesOn", "" ).toLowerCase().split( "," );
		condition = xml.getAttribute( "Condition", null );
		if ( condition != null )
		{
			condition = condition.toLowerCase();
		}

		for ( int i = 0; i < xml.getChildCount(); i++ )
		{
			Element sEl = xml.getChild( i );

			if ( sEl.getName().toLowerCase().equals( "damage" ) )
			{
				for ( Tier1Element el : Tier1Element.values() )
				{
					String expanded = sEl.getText().toLowerCase();
					expanded = expanded.replaceAll( "damage(?!_)", "damage_" + el.toString().toLowerCase() );

					equations.put( el, expanded );
				}
			}
			else
			{
				Tier1Element el = Tier1Element.valueOf( sEl.getName().toUpperCase() );
				equations.put( el, sEl.getText().toLowerCase() );
			}
		}
	}

	@Override
	public Array<String> toString( HashMap<String, Integer> variableMap, IGameObject parent )
	{
		Array<String> lines = new Array<String>();

		for ( Tier1Element el : Tier1Element.values() )
		{
			if ( equations.containsKey( el ) )
			{
				String eqn = equations.get( el );

				String line = "[" + el.toString() + "]" + eqn + "[]";
				lines.add( line );
			}
		}

		return lines;
	}
}
