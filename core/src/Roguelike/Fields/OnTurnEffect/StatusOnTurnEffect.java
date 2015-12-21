package Roguelike.Fields.OnTurnEffect;

import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Global;
import Roguelike.Entity.Entity;
import Roguelike.Fields.Field;
import Roguelike.StatusEffect.StatusEffect;

import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

public class StatusOnTurnEffect extends AbstractOnTurnEffect
{
	private String condition;
	private String[] reliesOn;
	public String stacksEqn;
	public Element status;

	@Override
	public void parse( Element xml )
	{
		condition = xml.getAttribute( "Condition", null );
		if ( condition != null )
		{
			condition = condition.toLowerCase();
		}

		reliesOn = xml.getAttribute( "ReliesOn", "" ).split( "," );

		status = xml;
		stacksEqn = xml.getAttribute( "Stacks", null );
		if ( stacksEqn != null )
		{
			stacksEqn = stacksEqn.toLowerCase();
		}
	}

	@Override
	public void process( Field field, Entity entity, float cost )
	{
		HashMap<String, Integer> variableMap = new HashMap<String, Integer>();
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

				Expression exp = EquationHelper.tryBuild( expB );
				if ( exp != null )
				{
					EquationHelper.setVariableValues( exp, variableMap, "" );

					stacks = (int) Math.ceil( exp.evaluate() * cost );
				}
			}
		}

		for ( int i = 0; i < stacks; i++ )
		{
			entity.addStatusEffect( StatusEffect.load( status, field ) );
		}
	}
}
