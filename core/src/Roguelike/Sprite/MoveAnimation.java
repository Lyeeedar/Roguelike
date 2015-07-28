package Roguelike.Sprite;

import Roguelike.Global;
import Roguelike.RoguelikeGame;

import com.badlogic.gdx.math.MathUtils;

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
			offset[1] += (Global.TileSize*3) * (0.5f - Math.abs(alpha - 0.5f));
		}
	}

	@Override
	public int[] getRenderOffset()
	{
		return offset;
	}
}
