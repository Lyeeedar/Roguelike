package Roguelike.Entity.AI.BehaviourTree.Actions;

import Roguelike.Entity.Entity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public class ActionPickClosest extends AbstractAction
{
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
				float best = Float.MAX_VALUE;
				Object bestObj = null;
				
				for (Object o : array)
				{
					if (o instanceof int[])
					{
						int[] pos = (int[])o;
						float tmp = Vector2.dst2(entity.Tile.x, entity.Tile.y, pos[0], pos[1]);
						
						if (tmp < best)
						{
							best = tmp;
							bestObj = o;
						}
					}
					else if (o instanceof Entity)
					{
						Entity pos = (Entity)o;
						float tmp = Vector2.dst2(entity.Tile.x, entity.Tile.y, pos.Tile.x, pos.Tile.y);
						
						if (tmp < best)
						{
							best = tmp;
							bestObj = o;
						}
					}
					else if (o instanceof GameTile)
					{
						GameTile pos = (GameTile)o;
						float tmp = Vector2.dst2(entity.Tile.x, entity.Tile.y, pos.x, pos.y);
						
						if (tmp < best)
						{
							best = tmp;
							bestObj = o;
						}
					}
				}
				
				if (bestObj != null)
				{
					setData(outputKey, bestObj);				
					State = BehaviourTreeState.SUCCEEDED;
				}
				else
				{
					State = BehaviourTreeState.FAILED;
				}
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
