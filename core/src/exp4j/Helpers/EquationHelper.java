package exp4j.Helpers;

import java.util.HashMap;
import java.util.Random;

import com.badlogic.gdx.math.MathUtils;
import exp4j.Functions.ChanceFunction;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Global;
import exp4j.Functions.MathUtilFunctions;
import exp4j.Functions.RandomFunction;
import exp4j.Operators.BooleanOperators;

public class EquationHelper
{
	public static Expression tryBuild( ExpressionBuilder expB )
	{
		Expression exp = null;

		// try
		// {
		exp = expB.build();
		// }
		// catch (Exception e) { }

		return exp;
	}

	public static void setVariableNames( ExpressionBuilder expB, HashMap<String, Integer> variableMap, String prefix )
	{
		for ( String key : variableMap.keySet() )
		{
			expB.variable( prefix + key );
		}
	}

	public static void setVariableValues( Expression exp, HashMap<String, Integer> variableMap, String prefix )
	{
		for ( String key : variableMap.keySet() )
		{
			exp.setVariable( prefix + key, variableMap.get( key ) );
		}
	}

	public static ExpressionBuilder createEquationBuilder( String eqn )
	{
		return createEquationBuilder( eqn, MathUtils.random );
	}

	public static ExpressionBuilder createEquationBuilder( String eqn, Random ran )
	{
		ExpressionBuilder expB = new ExpressionBuilder( eqn );
		BooleanOperators.applyOperators( expB );
		expB.function( new RandomFunction( ran ) );
		expB.function( new ChanceFunction( ran ) );
		MathUtilFunctions.applyFunctions( expB );

		return expB;
	}

	public static int evaluate( String eqn, HashMap<String, Integer> variableMap )
	{
		return evaluate( eqn, variableMap, MathUtils.random );
	}

	public static int evaluate( String eqn, HashMap<String, Integer> variableMap, Random ran )
	{
		if ( Global.isNumber( eqn ) )
		{
			return Integer.parseInt( eqn );
		}
		else
		{
			ExpressionBuilder expB = createEquationBuilder( eqn, ran );
			setVariableNames( expB, variableMap, "" );
			Expression exp = tryBuild( expB );

			if ( exp == null )
			{
				return 0;
			}
			else
			{
				setVariableValues( exp, variableMap, "" );
				double rawVal = exp.evaluate();
				int val = Math.round( (float)rawVal );

				return val;
			}
		}
	}
}
