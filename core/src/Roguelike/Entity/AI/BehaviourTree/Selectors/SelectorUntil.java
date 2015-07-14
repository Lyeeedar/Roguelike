package Roguelike.Entity.AI.BehaviourTree.Selectors;

import com.badlogic.gdx.utils.XmlReader.Element;

import Roguelike.Entity.GameEntity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;

// A selector that will run through the nodes until the first node that returns the state desired is found
public class SelectorUntil extends AbstractSelector
{		
	//####################################################################//
	//region Public Methods
	
	//----------------------------------------------------------------------
	@Override
	public BehaviourTreeState evaluate(GameEntity entity)
	{
		BehaviourTreeState state = BehaviourTreeState.FAILED;
		
		int i = 0;
		for (; i < nodes.size; i++)
		{
			BehaviourTreeState temp = nodes.get(i).evaluate(entity);
			if (temp == Until)
			{
				state = BehaviourTreeState.SUCCEEDED;
				break;
			}
		}
		i++;
		for (; i < nodes.size; i++)
		{
			nodes.get(i).cancel();
		}
		
		this.State = state;
		return state;
	}
	
	//----------------------------------------------------------------------
	@Override
	public void cancel()
	{
		for (int i = 0; i < nodes.size; i++)
		{
			nodes.get(i).cancel();
		}
	}
	
	//----------------------------------------------------------------------
	@Override
	public void parse(Element xmlElement)
	{
		super.parse(xmlElement);
		
		Until = BehaviourTreeState.valueOf(xmlElement.getAttribute("State").toUpperCase());
	}
		
	//endregion Public Methods
	//####################################################################//
	//region Data
	
	private BehaviourTreeState Until;
	
	//endregion Data
	//####################################################################//
}
