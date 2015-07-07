package Roguelike.Entity.AI.BehaviourTree.Decorators;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import Roguelike.Entity.AI.BehaviourTree.BehaviourTreeContainer;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTreeNode;

public abstract class AbstractDecorator extends BehaviourTreeContainer
{
	//####################################################################//
	//region Public Methods
	
	//----------------------------------------------------------------------
	public void setNode(BehaviourTreeNode node)
	{
		if (node.Data == null) { node.Data = Data; }
		node.Parent = this;
		this.node = node;
	}
	
	//----------------------------------------------------------------------
	@Override
	public void setData(String key, Object value)
	{
		super.setData(key, value);
		node.setData(key, value);
	}
	
	//----------------------------------------------------------------------
	@Override
	public void parse(Element xmlElement)
	{		
		Element xml = xmlElement.getChild(0);

		try
		{
			Class<BehaviourTreeNode> c = ClassMap.get(xml.getName().toUpperCase());
			BehaviourTreeNode node = (BehaviourTreeNode)ClassReflection.newInstance(c);

			setNode(node);
			
			node.parse(xml);
		} 
		catch (ReflectionException e) { e.printStackTrace(); }	
	}
	
	//----------------------------------------------------------------------
	@Override
	public void cancel()
	{
		node.cancel();
	}
	
	//endregion Public Methods
	//####################################################################//
	//region Data
	
	//----------------------------------------------------------------------
	protected BehaviourTreeNode node;
	
	//endregion Data
	//####################################################################//
}
