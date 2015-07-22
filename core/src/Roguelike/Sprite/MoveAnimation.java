package Roguelike.Sprite;

import com.badlogic.gdx.math.MathUtils;

public class MoveAnimation extends SpriteAnimation
{	
	private int[] diff;
	
	private int[] offset = {0, 0};
	
	public MoveAnimation(float duration, int[] diff)
	{
		super(duration);
		
		this.diff = diff;
	}
		
	@Override
	protected void updateInternal(float delta)
	{
		float alpha = MathUtils.clamp(( duration - time ) / duration, 0, 1);
		
		alpha = alpha * alpha * (3 - 2 * alpha); // smoothstep
		
		offset[0] = (int)(diff[0] * alpha);
		offset[1] = (int)(diff[1] * alpha);
	}

	@Override
	public int[] getRenderOffset()
	{
		return offset;
	}
}
