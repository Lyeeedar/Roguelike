package Roguelike.Sprite.SpriteAnimation;

import java.util.HashMap;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public abstract class AbstractSpriteAnimation
{
	public abstract void set( float duration, int[] diff );

	public abstract int[] getRenderOffset();

	public abstract float[] getRenderScale();

	public abstract boolean update( float delta );

	public abstract void parse( Element xml );

	public abstract AbstractSpriteAnimation copy();

	public static AbstractSpriteAnimation load( Element xml )
	{
		Class<AbstractSpriteAnimation> c = ClassMap.get( xml.getName().toUpperCase() );
		AbstractSpriteAnimation type = null;

		try
		{
			type = ClassReflection.newInstance( c );
		}
		catch ( ReflectionException e )
		{
			e.printStackTrace();
		}

		type.parse( xml );

		return type;
	}

	public static final HashMap<String, Class> ClassMap = new HashMap<String, Class>();
	static
	{
		ClassMap.put( "BUMP", BumpAnimation.class );
		ClassMap.put( "MOVE", MoveAnimation.class );
		ClassMap.put( "STRETCH", StretchAnimation.class );
	}
}
