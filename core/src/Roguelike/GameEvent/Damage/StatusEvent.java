package Roguelike.GameEvent.Damage;

import java.util.HashMap;

import Roguelike.Entity.Entity;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Global;
import Roguelike.GameEvent.IGameObject;
import Roguelike.StatusEffect.StatusEffect;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

public final class StatusEvent extends AbstractOnDamageEvent
{
	public String condition;
	public Element statusData;
	public String stacksEqn;

	private String[] reliesOn;

	@Override
	public boolean handle( Entity entity, DamageObject obj, IGameObject parent )
	{
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
			expB.variable( "damage" );

			Expression exp = EquationHelper.tryBuild( expB );
			if ( exp == null ) { return false; }

			EquationHelper.setVariableValues( exp, variableMap, "" );
			exp.setVariable( "damage", obj.damage );

			double conditionVal = exp.evaluate();

			if ( conditionVal == 0 ) { return false; }
		}

		int stacks = 1;

		if ( stacksEqn != null )
		{
			if ( Global.isNumber( stacksEqn ) )
			{
				stacks = Integer.parseInt( stacksEqn );
			}
			else
			{
				ExpressionBuilder expB = EquationHelper.createEquationBuilder( stacksEqn );
				EquationHelper.setVariableNames( expB, variableMap, "" );
				expB.variable( "damage" );

				Expression exp = EquationHelper.tryBuild( expB );
				if ( exp != null )
				{
					EquationHelper.setVariableValues( exp, variableMap, "" );
					exp.setVariable( "damage", obj.damage );

					stacks = (int) Math.ceil( exp.evaluate() );
				}
			}
		}

		for ( int i = 0; i < stacks; i++ )
		{
			entity.addStatusEffect( StatusEffect.load( statusData, parent ) );
		}

		return true;
	}

	@Override
	public void parse( Element xml )
	{
		reliesOn = xml.getAttribute( "ReliesOn", "" ).split( "," );
		condition = xml.getAttribute( "Condition", null );
		if ( condition != null )
		{
			condition = condition.toLowerCase();
		}
		stacksEqn = xml.getAttribute( "Stacks", null );
		if ( stacksEqn != null )
		{
			stacksEqn = stacksEqn.toLowerCase();
		}

		statusData = xml;
	}

	@Override
	public Array<String> toString( HashMap<String, Integer> variableMap, IGameObject parent )
	{
		Array<String> lines = new Array<String>();

		lines.add( "Spawns a status:" );

		StatusEffect status = StatusEffect.load( statusData, parent );
		lines.addAll( status.toString( variableMap, false ) );

		return lines;
	}

}
