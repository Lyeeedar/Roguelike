package exp4j.Helpers;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class EquationHelper
{
	public static Expression tryBuild(ExpressionBuilder expB)
	{
		Expression exp = null;
		
		try
		{
			exp = expB.build();
		}
		catch (Exception e)
		{
			
		}
		
		return exp;
	}
}
