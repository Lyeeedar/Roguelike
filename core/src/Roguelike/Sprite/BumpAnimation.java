package Roguelike.Sprite;

import Roguelike.Global.Direction;

import com.badlogic.gdx.math.MathUtils;

public class BumpAnimation extends SpriteAnimation
{
	private Direction direction;
	private float tileSize;
	
	private int[] offset = {0, 0};
	
	public BumpAnimation(float duration, Direction direction, float tileSize)
	{
		super(duration);
		
		this.direction = direction;
		this.tileSize = tileSize;
	}
		
	@Override
	protected void updateInternal(float delta)
	{
		float alpha = MathUtils.clamp(Math.abs( ( time - duration / 2 ) / ( duration / 2) ), 0, 1);
		
		offset[0] = (int)((tileSize/3) * alpha * direction.GetX());
		offset[1] = (int)((tileSize/3) * alpha * direction.GetY());
	}

	@Override
	public int[] getRenderOffset()
	{
		return offset;
	}
}
