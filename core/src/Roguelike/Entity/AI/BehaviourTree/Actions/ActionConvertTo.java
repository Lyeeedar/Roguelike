package Roguelike.Entity.AI.BehaviourTree.Actions;

import Roguelike.Entity.GameEntity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;
import Roguelike.Tiles.GameTile;
import Roguelike.Tiles.Point;

import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.XmlReader.Element;

public class ActionConvertTo extends AbstractAction
{
	private enum ConvertType
	{
		POSITION
	}
	
	ConvertType Type;
	String InputKey;
	String OutputKey;

	@Override
	public BehaviourTreeState evaluate(GameEntity entity)
	{
		Object o = getData(InputKey, null);
		
		if (o == null)
		{
			State = BehaviourTreeState.FAILED;
		}
		else
		{
			if (Type == ConvertType.POSITION)
			{
				Object storedVal = getData(OutputKey, null);
				if (storedVal != null && storedVal instanceof Point) 
				{ 
					Pools.free((Point)storedVal); 
					setData(OutputKey, null); 
				}
				
				if (o instanceof GameTile)
				{
					GameTile gt = (GameTile)o;					
					setData(OutputKey, Pools.obtain(Point.class).set(gt.x, gt.y));					
					State = BehaviourTreeState.SUCCEEDED;
				}
				else if (o instanceof GameEntity)
				{
					GameEntity e = (GameEntity)o;					
					setData(OutputKey, Pools.obtain(Point.class).set(e.tile.x, e.tile.y));					
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
		Type = ConvertType.valueOf(xmlElement.getAttribute("Type").toUpperCase());
		InputKey = xmlElement.getAttribute("InputKey");
		OutputKey = xmlElement.getAttribute("OutputKey");
	}

}
