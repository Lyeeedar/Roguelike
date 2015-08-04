package Roguelike.Sprite;

import Roguelike.Global;
import Roguelike.RoguelikeGame;

import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector;

public class MoveAnimation extends SpriteAnimation
{	
	public enum MoveEquation
	{
		LINEAR,
		SMOOTHSTEP,
		EXPONENTIAL,
		LEAP
	}
	
	private int[] diff;
	private MoveEquation eqn;
	
	public int leapHeight = 3;
	
	private int[] offset = {0, 0};
	
	public MoveAnimation(float duration, int[] diff, MoveEquation eqn)
	{
		super(duration);
		
		this.diff = diff;
		this.eqn = eqn;
	}
		
	@Override
	protected void updateInternal(float delta)
	{
		float alpha = MathUtils.clamp(( duration - time ) / duration, 0, 1);
		
		if (eqn == MoveEquation.SMOOTHSTEP)
		{
			alpha = alpha * alpha * (3 - 2 * alpha); // smoothstep
		}
		else if (eqn == MoveEquation.EXPONENTIAL)
		{
			alpha = 1 - (1 - alpha) * (1 - alpha) * (1 - alpha) * (1 - alpha);
		}
		
		offset[0] = (int)(diff[0] * alpha);
		offset[1] = (int)(diff[1] * alpha);
		
		if (eqn == MoveEquation.LEAP)
		{
			//B2(t) = (1 - t) * (1 - t) * p0 + 2 * (1-t) * t * p1 + t*t*p2
			alpha = (1 - alpha) * (1 - alpha) * 0 + 2 * (1 - alpha) * alpha * 1 + alpha * alpha * 0;
			offset[1] += (Global.TileSize*leapHeight) * alpha;
		}
	}

	@Override
	public int[] getRenderOffset()
	{
		return offset;
	}
}
