package Roguelike.Sprite;


public abstract class SpriteAnimation
{
	protected float duration;
	
	public SpriteAnimation(float duration)
	{
		this.duration = duration;
	}
		
	protected float time = 0;
	public boolean update(float delta)
	{
		time += delta;
		
		updateInternal(delta);
				
		return time > duration;
	}
	
	public abstract int[] getRenderOffset();
	
	protected abstract void updateInternal(float delta);
}
