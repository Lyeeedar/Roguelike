package exp4j.Functions;

import com.badlogic.gdx.math.MathUtils;

import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;

public class MathUtilFunctions
{
	public static class CeilFunction extends Function
	{
		public CeilFunction()
		{
			super("CEIL", 1);
		}

		@Override
		public double apply(double... arg0)
		{
			return MathUtils.ceil((float) arg0[0]);
		}
	}
	
	public static class FloorFunction extends Function
	{
		public FloorFunction()
		{
			super("FLOOR", 1);
		}

		@Override
		public double apply(double... arg0)
		{
			return MathUtils.floor((float) arg0[0]);
		}
	}
	
	public static class RoundFunction extends Function
	{
		public RoundFunction()
		{
			super("ROUND", 1);
		}

		@Override
		public double apply(double... arg0)
		{
			return MathUtils.round((float) arg0[0]);
		}
	}
	
	public static class MinFunction extends Function
	{
		public MinFunction()
		{
			super("MIN", 2);
		}

		@Override
		public double apply(double... arg0)
		{
			return Math.min(arg0[0], arg0[1]);
		}
	}
	
	public static class MaxFunction extends Function
	{
		public MaxFunction()
		{
			super("MAX", 2);
		}

		@Override
		public double apply(double... arg0)
		{
			return Math.max(arg0[0], arg0[1]);
		}
	}
	
	public static class ClampFunction extends Function
	{
		public ClampFunction()
		{
			super("CLAMP", 3);
		}

		@Override
		public double apply(double... arg0)
		{
			return MathUtils.clamp(arg0[0], arg0[1], arg0[2]);
		}
	}
	
	public static void applyFunctions(ExpressionBuilder expB)
	{
		expB.function(new CeilFunction());
		expB.function(new FloorFunction());
		expB.function(new RoundFunction());
		expB.function(new MinFunction());
		expB.function(new MaxFunction());
		expB.function(new ClampFunction());
	}
}
