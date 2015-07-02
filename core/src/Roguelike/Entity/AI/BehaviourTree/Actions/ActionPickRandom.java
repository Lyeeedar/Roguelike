package Roguelike.Entity.AI.BehaviourTree.Actions;

import java.util.Iterator;
import java.util.Random;

import Roguelike.Entity.Entity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public class ActionPickRandom extends AbstractAction
{
	Random ran = new Random();
	String inputKey;
	String outputKey;

	@Override
	public BehaviourTreeState evaluate(Entity entity)
	{
		Object obj = getData(inputKey, null);
		
		if (obj == null || !(obj instanceof Iterable))
		{
			State = BehaviourTreeState.FAILED;
		}
		else
		{
			Array<Object> array = new Array<Object>();
			for (Object o : (Iterable)obj)
			{
				array.add(o);
			}

			if (array.size == 0)
			{
				State = BehaviourTreeState.FAILED;
			}
			else
			{
				int index = ran.nextInt(array.size);
				setData(outputKey, array.get(index));				
				State = BehaviourTreeState.SUCCEEDED;
			}
			
		}
		
		
		return State;
	}

	@Override
	public void cancel()
	{
	}

	@Override
	public void parse(Element xmlElement)
	{
		inputKey = xmlElement.getAttribute("InputKey");
		outputKey = xmlElement.getAttribute("OutputKey");
	}

}
