package Roguelike.GameEvent.OnDeath;

import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Global;
import Roguelike.Entity.Entity;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

public final class HealOnDeathEvent extends AbstractOnDeathEvent
{
	private String condition;
	private String[] reliesOn;
	private String amountEqn;

	@Override
	public boolean handle( Entity entity, Entity killer )
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
			int conditionVal = EquationHelper.evaluate( condition, variableMap );
			if ( conditionVal == 0 ) { return false; }
		}

		int healVal = EquationHelper.evaluate( amountEqn, variableMap );

		entity.applyHealing( healVal );

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
		amountEqn = xml.getText();
	}

	@Override
	public Array<String> toString( HashMap<String, Integer> variableMap )
	{
		Array<String> lines = new Array<String>();

		for ( String name : reliesOn )
		{
			if ( !variableMap.containsKey( name.toLowerCase() ) )
			{
				variableMap.put( name.toLowerCase(), 0 );
			}
		}

		int healVal = EquationHelper.evaluate( amountEqn, variableMap );

		lines.add( "Heal for " + healVal );

		return lines;
	}

}