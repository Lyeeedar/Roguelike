package Roguelike.GameEvent.Damage;

import java.util.HashMap;

import Roguelike.Entity.Entity;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Global;
import Roguelike.GameEvent.IGameObject;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

public final class DamageEvent extends AbstractOnDamageEvent
{
	private String condition;
	private String equation;
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

		int raw = 0;

		if ( Global.isNumber( equation ) )
		{
			raw = Integer.parseInt( equation );
		}
		else
		{
			ExpressionBuilder expB = EquationHelper.createEquationBuilder( equation );
			EquationHelper.setVariableNames( expB, variableMap, "" );
			expB.variable( "damage" );

			Expression exp = EquationHelper.tryBuild( expB );
			if ( exp != null )
			{
				EquationHelper.setVariableValues( exp, variableMap, "" );
				exp.setVariable( "damage", obj.damage );

				raw = (int) exp.evaluate();
			}
		}

		obj.damage += raw;

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
		equation = xml.getText().toLowerCase();
	}

	@Override
	public Array<String> toString( HashMap<String, Integer> variableMap, IGameObject parent )
	{
		Array<String> lines = new Array<String>();

		lines.add( equation );

		return lines;
	}
}
