package Roguelike.StatusEffect.OnTurn;

import java.util.HashMap;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import Roguelike.StatusEffect.StatusEffect;

public abstract class AbstractOnTurnEffect
{
	public abstract void evaluate(StatusEffect effect, float time);
	public abstract void parse(Element xml);
	
	public static AbstractOnTurnEffect load(Element xml)
	{		
		Class<AbstractOnTurnEffect> c = ClassMap.get(xml.getName());
		AbstractOnTurnEffect type = null;
		
		try
		{
			type = (AbstractOnTurnEffect)ClassReflection.newInstance(c);
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
		ClassMap.put("DOT", DamageOverTimeEffect.class);
	}
}
