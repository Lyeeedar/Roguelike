package Roguelike.GameEvent.OnTurn;

import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Global;
import Roguelike.Entity.Entity;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

public final class HealOverTimeEvent extends AbstractOnTurnEvent
{
	String condition;

	String equation;
	float remainder;

	@Override
	public boolean handle( Entity entity, float time )
	{
		HashMap<String, Integer> variableMap = entity.getVariableMap();

		if ( condition != null )
		{
			int conditionVal = EquationHelper.evaluate( condition, variableMap );
			if ( conditionVal == 0 ) { return false; }
		}

		int raw = EquationHelper.evaluate( equation, variableMap );

		int rounded = (int) Math.floor( raw * time + remainder );

		remainder = raw - rounded;

		entity.applyHealing( rounded );

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
		equation = xml.getText().toLowerCase();
	}

	@Override
	public Array<String> toString( HashMap<String, Integer> variableMap )
	{
		Array<String> lines = new Array<String>();

		int rounded = EquationHelper.evaluate( equation, variableMap );

		lines.add( "Heals " + rounded + " health" );

		return lines;
	}

}