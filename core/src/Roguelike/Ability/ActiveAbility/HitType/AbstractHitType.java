package Roguelike.Ability.ActiveAbility.HitType;

import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Entity.Entity;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import java.util.HashMap;

/**
 * Created by Philip on 02-Jan-16.
 */
public abstract class AbstractHitType
{
	public abstract AbstractHitType copy();
	public abstract void parse(XmlReader.Element xml );

	public abstract boolean isTargetValid( ActiveAbility aa, Entity entity );

	public static AbstractHitType load(XmlReader.Element xml )
	{
		Class<AbstractHitType> c = ClassMap.get(xml.getName().toUpperCase());
		AbstractHitType type = null;

		try
		{
			type = (AbstractHitType) ClassReflection.newInstance( c );
		}
		catch (ReflectionException e)
		{
			e.printStackTrace();
		}

		type.parse(xml);

		return type;
	}

	public static final HashMap<String, Class> ClassMap = new HashMap<String, Class>();
	static
	{
		ClassMap.put( "SELF", HitTypeSelf.class );
		ClassMap.put( "NOTSELF", HitTypeNotSelf.class );
		ClassMap.put( "ANY", HitTypeAny.class );
		ClassMap.put( "ENEMY", HitTypeEnemy.class );
		ClassMap.put( "ALLY", HitTypeAlly.class );
	}
}
