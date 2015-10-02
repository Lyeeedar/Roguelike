package Roguelike.GameEvent.Damage;

import java.util.HashMap;

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

		int raw = 0;

		if ( Global.isNumber( equation ) )
		{
			raw = Integer.parseInt( equation );
		}
		else
		{
			ExpressionBuilder expB = EquationHelper.createEquationBuilder( equation );
			obj.writeVariableNames( expB, reliesOn );

			Expression exp = EquationHelper.tryBuild( expB );
			if ( exp != null )
			{
				obj.writeVariableValues( exp, reliesOn );

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
