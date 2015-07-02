package Roguelike.Entity.AI.BehaviourTree.Actions;

import Roguelike.Entity.Entity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;
import Roguelike.Tiles.GameTile;

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
	public BehaviourTreeState evaluate(Entity entity)
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
				if (o instanceof GameTile)
				{
					GameTile gt = (GameTile)o;					
					setData(OutputKey, new int[]{gt.x, gt.y});					
					State = BehaviourTreeState.SUCCEEDED;
				}
				else if (o instanceof Entity)
				{
					Entity e = (Entity)o;					
					setData(OutputKey, new int[]{e.Tile.x, e.Tile.y});					
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
