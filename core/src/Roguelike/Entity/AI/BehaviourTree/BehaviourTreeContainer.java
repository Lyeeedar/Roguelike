package Roguelike.Entity.AI.BehaviourTree;

public abstract class BehaviourTreeContainer extends BehaviourTreeNode
{
	//----------------------------------------------------------------------
	public void setDataTree(String key, Object value)
	{
		if (Parent == null)
		{
			setData(key, value);
		}
		else
		{
			Parent.setDataTree(key, value);
		}
	}
}
