package Roguelike.Entity.AI.BehaviourTree.Decorators;

import java.io.IOException;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import Roguelike.Entity.Entity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTreeNode;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;

public class DecoratorImport extends AbstractDecorator
{

	@Override
	public BehaviourTreeState evaluate(Entity entity)
	{
		return node.evaluate(entity);
	}

	@Override
	public void cancel()
	{
		node.cancel();
	}
	
	@Override
	public void parse(Element xmlElement)
	{
		String path = xmlElement.getAttribute("Path");
		
		XmlReader importXml = new XmlReader();
		Element importXmlElement = null;
		
		try
		{
			importXmlElement = importXml.parse(Gdx.files.internal("AI/"+path));
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		try
		{
			Class<BehaviourTreeNode> c = ClassMap.get(importXmlElement.getName());
			BehaviourTreeNode node = (BehaviourTreeNode)ClassReflection.newInstance(c);

			setNode(node);
			
			node.parse(importXmlElement);
		} 
		catch (ReflectionException e) { e.printStackTrace(); }	
	}

}
