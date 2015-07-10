package exp4j.Operators;

import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.operator.Operator;

public class BooleanOperators
{
	public static class LessThanOperator extends Operator
	{
		public LessThanOperator()
		{
			super("<", 2, true,  Operator.PRECEDENCE_ADDITION - 1);
		}

		@Override
		public double apply(double... args)
		{
			return args[0] < args[1] ? 1 : 0;
		}
	}
	
	public static class LessThanOrEqualOperator extends Operator
	{
		public LessThanOrEqualOperator()
		{
			super("<=", 2, true,  Operator.PRECEDENCE_ADDITION - 1);
		}

		@Override
		public double apply(double... args)
		{
			return args[0] <= args[1] ? 1 : 0;
		}
	}
	
	public static class GreaterThanOperator extends Operator
	{
		public GreaterThanOperator()
		{
			super(">", 2, true,  Operator.PRECEDENCE_ADDITION - 1);
		}

		@Override
		public double apply(double... args)
		{
			return args[0] > args[1] ? 1 : 0;
		}
	}
	
	public static class GreaterThanOrEqualOperator extends Operator
	{
		public GreaterThanOrEqualOperator()
		{
			super(">=", 2, true,  Operator.PRECEDENCE_ADDITION - 1);
		}

		@Override
		public double apply(double... args)
		{
			return args[0] >= args[1] ? 1 : 0;
		}
	}
	
	public static class EqualsOperator extends Operator
	{
		public EqualsOperator()
		{
			super("==", 2, true,  Operator.PRECEDENCE_ADDITION - 1);
		}

		@Override
		public double apply(double... args)
		{
			return args[0] == args[1] ? 1 : 0;
		}
	}
	
	public static class NotEqualsOperator extends Operator
	{
		public NotEqualsOperator()
		{
			super("!=", 2, true,  Operator.PRECEDENCE_ADDITION - 1);
		}

		@Override
		public double apply(double... args)
		{
			return args[0] != args[1] ? 1 : 0;
		}
	}
	
	public static class AndOperator extends Operator
	{
		public AndOperator()
		{
			super("&&", 2, true,  Operator.PRECEDENCE_ADDITION - 2);
		}

		@Override
		public double apply(double... args)
		{
			boolean b1 = args[0] != 0;
			boolean b2 = args[1] != 0;
			
			return b1 && b2 ? 1 : 0;
		}
	}
	
	public static class OrOperator extends Operator
	{
		public OrOperator()
		{
			super("||", 2, true,  Operator.PRECEDENCE_ADDITION - 2);
		}

		@Override
		public double apply(double... args)
		{
			boolean b1 = args[0] != 0;
			boolean b2 = args[1] != 0;
			
			return b1 || b2 ? 1 : 0;
		}
	}

	public static void applyOperators(ExpressionBuilder expB)
	{
		expB.operator(new LessThanOperator());
		expB.operator(new LessThanOrEqualOperator());
		
		expB.operator(new GreaterThanOperator());
		expB.operator(new GreaterThanOrEqualOperator());
		
		expB.operator(new EqualsOperator());
		expB.operator(new NotEqualsOperator());
		
		expB.operator(new AndOperator());
		expB.operator(new OrOperator());
	}
}
