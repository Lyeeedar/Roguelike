package Roguelike.GameEvent.OnTask;

import java.util.HashMap;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import Roguelike.Entity.Entity;
import Roguelike.Entity.GameEntity;
import Roguelike.Entity.Tasks.AbstractTask;
import Roguelike.GameEvent.IGameObject;

public abstract class AbstractOnTaskEvent
{
	public abstract boolean handle(Entity entity, AbstractTask task, IGameObject parent);
	public abstract void parse(Element xml);
	
	public static AbstractOnTaskEvent load(Element xml)
	{		
		Class<AbstractOnTaskEvent> c = ClassMap.get(xml.getName().toUpperCase());
		AbstractOnTaskEvent type = null;
		
		try
		{
			type = (AbstractOnTaskEvent)ClassReflection.newInstance(c);
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
		ClassMap.put("CANCEL", CancelTaskEvent.class);
		ClassMap.put("COST", CostTaskEvent.class);
		ClassMap.put("STATUS", StatusTaskEvent.class);
	}
}
