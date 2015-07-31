package Roguelike.Ability.ActiveAbility.EffectType;

import java.util.HashMap;

import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public abstract class AbstractEffectType
{
	public abstract void update(ActiveAbility aa, float time, GameTile tile, GameTile epicenter);
	public abstract void parse(Element xml);
	public abstract AbstractEffectType copy();
	
	public static AbstractEffectType load(Element xml)
	{		
		Class<AbstractEffectType> c = ClassMap.get(xml.getName().toUpperCase());
		AbstractEffectType type = null;
		
		try
		{
			type = (AbstractEffectType)ClassReflection.newInstance(c);
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
		ClassMap.put("DAMAGE", EffectTypeDamage.class);
		ClassMap.put("HEAL", EffectTypeHeal.class);
		ClassMap.put("STATUS", EffectTypeStatus.class);
		ClassMap.put("TELEPORT", EffectTypeTeleport.class);
	}
}
