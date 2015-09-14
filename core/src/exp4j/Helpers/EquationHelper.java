package exp4j.Helpers;

import java.util.HashMap;
import java.util.Random;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
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
		ExpressionBuilder expB = new ExpressionBuilder( eqn );
		BooleanOperators.applyOperators( expB );
		expB.function( new RandomFunction() );
		MathUtilFunctions.applyFunctions( expB );

		return expB;
	}

	public static ExpressionBuilder createEquationBuilder( String eqn, Random ran )
	{
		ExpressionBuilder expB = new ExpressionBuilder( eqn );
		BooleanOperators.applyOperators( expB );
		expB.function( new RandomFunction( ran ) );
		MathUtilFunctions.applyFunctions( expB );

		return expB;
	}
}
