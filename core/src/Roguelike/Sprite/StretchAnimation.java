package Roguelike.Sprite;

import Roguelike.Global;

import com.badlogic.gdx.math.MathUtils;

public class StretchAnimation extends SpriteAnimation
{	
	public enum StretchEquation
	{
		EXTEND,
		REVERSEEXTEND
	}
	
	private int[] diff;
	private float finalScale;
	private StretchEquation eqn;
	private float trueDuration;
			
	private int[] offset = {0, 0};
	private float[] scale = {1, 1};
	
	public StretchAnimation(float duration, int[] diff, float padDuration, StretchEquation eqn)
	{
		super(duration+padDuration);
		
		this.diff = diff;
		this.eqn = eqn;
		this.trueDuration = duration;
		
		float dist = (float)Math.sqrt(diff[0]*diff[0] + diff[1]*diff[1]) + Global.TileSize*2;
		finalScale = (dist/(float)Global.TileSize) / 2.0f;
	}
		
	@Override
	protected void updateInternal(float delta)
	{
		float alpha = MathUtils.clamp(( trueDuration - time ) / trueDuration, 0, 1);
		
		if (eqn == StretchEquation.EXTEND)
		{
			offset[0] = (int)(diff[0]/2 + (diff[0]/2) * alpha);
			offset[1] = (int)(diff[1]/2 + (diff[1]/2) * alpha);
			
			scale[1] = 1 + finalScale * (1-alpha);
		}
		else if (eqn == StretchEquation.REVERSEEXTEND)
		{
			offset[0] = (int)((diff[0]/2) * (1-alpha));
			offset[1] = (int)((diff[1]/2) * (1-alpha));
			
			scale[1] = 1 + finalScale * (1-alpha);
		}
	}

	@Override
	public int[] getRenderOffset()
	{
		return offset;
	}

	@Override
	public float[] getRenderScale()
	{
		return scale;
	}
}