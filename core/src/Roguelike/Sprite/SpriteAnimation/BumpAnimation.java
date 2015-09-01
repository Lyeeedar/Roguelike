package Roguelike.Sprite.SpriteAnimation;

import Roguelike.Global;
import Roguelike.Global.Direction;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.XmlReader.Element;

public class BumpAnimation extends AbstractSpriteAnimation
{
	private Direction direction;
	private float duration;

	private float time;
	private int[] offset = { 0, 0 };

	public BumpAnimation()
	{

	}

	public BumpAnimation( float duration, Direction direction )
	{
		duration *= Global.AnimationSpeed;

		this.duration = duration;
		this.direction = direction;
	}

	@Override
	public boolean update( float delta )
	{
		time += delta;

		float alpha = MathUtils.clamp( Math.abs( ( time - duration / 2 ) / ( duration / 2 ) ), 0, 1 );

		offset[0] = (int) ( ( Global.TileSize / 3 ) * alpha * direction.getX() );
		offset[1] = (int) ( ( Global.TileSize / 3 ) * alpha * direction.getY() );

		return time > duration;
	}

	@Override
	public int[] getRenderOffset()
	{
		return offset;
	}

	@Override
	public float[] getRenderScale()
	{
		return null;
	}

	@Override
	public void set( float duration, int[] diff )
	{
		this.duration = duration;
		this.direction = Direction.getDirection( diff );
		this.time = 0;
	}

	@Override
	public void parse( Element xml )
	{
	}

	@Override
	public AbstractSpriteAnimation copy()
	{
		BumpAnimation anim = new BumpAnimation();
		anim.direction = direction;
		anim.duration = duration;

		return anim;
	}
}
