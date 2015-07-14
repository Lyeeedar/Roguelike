package exp4j.Functions;

import com.badlogic.gdx.math.MathUtils;

import net.objecthunter.exp4j.function.Function;

public class RandomFunction extends Function
{

	public RandomFunction()
	{
		super("RND", 1);
	}

	@Override
	public double apply(double... arg0)
	{
		return MathUtils.random() * arg0[0];
	}


}
