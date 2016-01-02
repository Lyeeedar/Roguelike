package Roguelike.Ability.ActiveAbility.TargetingType;

import java.util.HashMap;

import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public abstract class AbstractTargetingType
{
	public abstract AbstractTargetingType copy();
	public abstract void parse(Element xml);

	public abstract boolean isTargetValid(ActiveAbility ab, GameTile tile);

	public static AbstractTargetingType load(Element xml)
	{
		Class<AbstractTargetingType> c = ClassMap.get(xml.getName().toUpperCase());
		AbstractTargetingType type = null;

		try
		{
			type = (AbstractTargetingType)ClassReflection.newInstance(c);
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
		ClassMap.put("ENTITY", TargetingTypeEntity.class);
		ClassMap.put("TILE", TargetingTypeTile.class);
		ClassMap.put("DIRECTION", TargetingTypeDirection.class);
	}
}
