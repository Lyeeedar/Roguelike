package Roguelike.Entity.AI.BehaviourTree.Selectors;

import com.badlogic.gdx.utils.XmlReader.Element;

import Roguelike.Entity.GameEntity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;

// A selector that will run through each node until the first node that does not finish is encountered
public class SelectorSequence extends AbstractSelector
{
	//####################################################################//
	//region Public Methods
	
	//----------------------------------------------------------------------
	@Override
	public BehaviourTreeState evaluate(GameEntity entity)
	{
		BehaviourTreeState state = BehaviourTreeState.SUCCEEDED;
		
		for (; i < nodes.size; i++)
		{
			BehaviourTreeState temp = nodes.get(i).evaluate(entity);
			if (temp != BehaviourTreeState.SUCCEEDED)
			{
				state = temp;
				break;
			}
		}
		
		if (state != BehaviourTreeState.RUNNING)
		{
			i = 0;
			for (; i < nodes.size; i++)
			{
				nodes.get(i).cancel();
			}
			i = 0;
		}
		
		if (reset)
		{
			i = 0;
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
		i = 0;
	}
	
	//----------------------------------------------------------------------
	@Override
	public void parse(Element xmlElement)
	{
		super.parse(xmlElement);

		reset = xmlElement.getBooleanAttribute("Reset", false);
	}
	
	//endregion Public Methods
	//####################################################################//
	//region Data
	
	//----------------------------------------------------------------------
	public boolean reset = false;
	
	//----------------------------------------------------------------------
	private int i = 0;
		
	//endregion Data
	//####################################################################//
}