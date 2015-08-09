package Roguelike.Ability.ActiveAbility.CostType;

import java.util.HashMap;

import Roguelike.Ability.ActiveAbility.ActiveAbility;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public abstract class AbstractCostType
{
	public abstract boolean isCostAvailable(ActiveAbility aa);
	public abstract void spendCost(ActiveAbility aa);
	public abstract void parse(Element xml);
	public abstract AbstractCostType copy();
	
	public static AbstractCostType load(Element xml)
	{		
		Class<AbstractCostType> c = ClassMap.get(xml.getName().toUpperCase());
		AbstractCostType type = null;
		
		try
		{
			type = (AbstractCostType)ClassReflection.newInstance(c);
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
		ClassMap.put("STATUS", CostTypeStatus.class);
		ClassMap.put("HEALTH", CostTypeHP.class);
		ClassMap.put("CONDITION", CostTypeCondition.class);
	}
}