package Roguelike.Entity.AI.BehaviourTree.Selectors;

import Roguelike.Entity.AI.BehaviourTree.BehaviourTreeContainer;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTreeNode;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public abstract class AbstractSelector extends BehaviourTreeContainer
{
	//####################################################################//
	//region Public Methods
	
	//----------------------------------------------------------------------
	public void addNode(BehaviourTreeNode node)
	{
		if (node.Data == null) { node.Data = Data; }
		node.Parent = this;
		nodes.add(node);
	}
	
	//----------------------------------------------------------------------
	@Override
	public void setData(String key, Object value)
	{
		super.setData(key, value);
		for (int i = 0; i < nodes.size; i++)
		{
			nodes.get(i).setData(key, value);
		}
	}
	
	//----------------------------------------------------------------------
	@Override
	public void parse(Element xmlElement)
	{
		for (int i = 0; i < xmlElement.getChildCount(); i++)
		{
			Element xml = xmlElement.getChild(i);

			try
			{
				Class<BehaviourTreeNode> c = ClassMap.get(xml.getName().toUpperCase());
				BehaviourTreeNode node = (BehaviourTreeNode)ClassReflection.newInstance(c);
				
				addNode(node);
				
				node.parse(xml);
			} 
			catch (ReflectionException e) { e.printStackTrace(); }			
		}
	}
	
	//endregion Public Methods
	//####################################################################//
	//region Data
	
	//----------------------------------------------------------------------
	protected final Array<BehaviourTreeNode> nodes = new Array<BehaviourTreeNode>();
	
	//endregion Data
	//####################################################################//
}
