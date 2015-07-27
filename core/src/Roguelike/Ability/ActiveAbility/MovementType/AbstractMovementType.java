package Roguelike.Ability.ActiveAbility.MovementType;

import java.util.HashMap;

import Roguelike.Global.Direction;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Ability.ActiveAbility.AbilityType.AbstractAbilityType;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public abstract class AbstractMovementType
{
	public Direction direction = Direction.CENTER;
	
	public abstract AbstractMovementType copy();
	public abstract void parse(Element xml);
	
	public abstract boolean needsUpdate();
	public abstract void updateAccumulators(float cost);
	public abstract void init(ActiveAbility ab, int endx, int endy);
	public abstract boolean update(ActiveAbility ab);
	
	public static AbstractMovementType load(Element xml)
	{
		Class<AbstractMovementType> c = ClassMap.get(xml.getName().toUpperCase());
		AbstractMovementType type = null;
		
		try
		{
			type = (AbstractMovementType)ClassReflection.newInstance(c);
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
		ClassMap.put("SMITE", MovementTypeSmite.class);
		ClassMap.put("BOLT", MovementTypeBolt.class);
		ClassMap.put("RAY", MovementTypeRay.class);
	}
}
