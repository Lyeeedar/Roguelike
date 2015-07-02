package Roguelike.Sprite;

import Roguelike.Global.Direction;

import com.badlogic.gdx.math.MathUtils;

public class SpriteAnimation
{
	public Direction Direction;
	float duration;
	
	public SpriteAnimation(Direction direction, float duration)
	{
		this.Direction = direction;
		this.duration = duration;
	}
	
	public float Alpha = 0;
	
	float time = 0;
	public boolean update(float delta)
	{
		time += delta;
		
		Alpha = MathUtils.clamp(Math.abs( ( time - duration / 2 ) / ( duration / 2) ), 0, 1);
		
		return time > duration;
	}
}
