package Roguelike.Fields.OnDeathEffect;

import java.util.HashMap;

import Roguelike.Fields.Field;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public abstract class AbstractOnDeathEffect
{
	public abstract void process(Field field, GameTile tile);
	public abstract void parse(Element xml);

	//----------------------------------------------------------------------
	public static AbstractOnDeathEffect load(Element xml)
	{
		Class<AbstractOnDeathEffect> c = ClassMap.get(xml.getName().toUpperCase());
		AbstractOnDeathEffect type = null;

		try
		{
			type = (AbstractOnDeathEffect)ClassReflection.newInstance(c);
		}
		catch (ReflectionException e)
		{
			e.printStackTrace();
		}

		type.parse(xml);

		return type;
	}

	//----------------------------------------------------------------------
	protected static HashMap<String, Class> ClassMap = new HashMap<String, Class>();

	//----------------------------------------------------------------------
	static
	{
		ClassMap.put("SPAWN", SpawnOnDeathEffect.class);
		ClassMap.put("SPAWNENTITY", SpawnEntityOnDeathEffect.class);
		ClassMap.put("DAMAGE", DamageOnDeathEffect.class);
		ClassMap.put("STATUS", StatusOnDeathEffect.class);
		ClassMap.put("SPRITE", SpriteOnDeathEffect.class);
	}
}
