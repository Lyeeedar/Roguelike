package Roguelike.Entity.AI.BehaviourTree.Selectors;

import Roguelike.Entity.GameEntity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;

import com.badlogic.gdx.utils.Array;

// A selector that will keep randomly selecting a node until the first non-failed node is found
public class SelectorRandom extends AbstractSelector
{
	//####################################################################//
	//region Public Methods
	
	//----------------------------------------------------------------------
	@Override
	public BehaviourTreeState evaluate(GameEntity entity)
	{
		BehaviourTreeState state = BehaviourTreeState.FAILED;
		
		if (i == -1) 
		{
			numList.clear();
			for (int i = 0; i < nodes.size; i++) { numList.add(i); }
			
			while (state == BehaviourTreeState.FAILED && numList.size > 0)
			{
				int ti = ran.nextInt(numList.size);
				i = numList.get(ti);
				numList.removeIndex(ti);
				
				state = nodes.get(i).evaluate(entity);
			}
		}
						
		if (state != BehaviourTreeState.RUNNING)
		{
			i = -1;
		}
		
		this.State = state;
		return state;
	}
	
	//----------------------------------------------------------------------
	@Override
	public void cancel()
	{
		i = -1;
		
		for (int i = 0; i < nodes.size; i++)
		{
			nodes.get(i).cancel();
		}
	}
		
	//endregion Public Methods
	//####################################################################//
	//region Data
	
	//----------------------------------------------------------------------
	private final java.util.Random ran = new java.util.Random();
	
	//----------------------------------------------------------------------
	private final Array<Integer> numList = new Array<Integer>(false, 16);
	
	//----------------------------------------------------------------------
	private int i = -1;
		
	//endregion Data
	//####################################################################//
}
