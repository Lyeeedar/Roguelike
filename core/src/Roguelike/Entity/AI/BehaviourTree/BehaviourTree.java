package Roguelike.Entity.AI.BehaviourTree;

import java.io.IOException;
import java.util.HashMap;

import Roguelike.Entity.GameEntity;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public class BehaviourTree
{
	public BehaviourTree(BehaviourTreeNode root)
	{
		this.root = root;
		root.Data = new HashMap<String, Object>();
	}
	
	//####################################################################//
	//region Public Methods
	
	//----------------------------------------------------------------------
	public void setData(String key, Object value)
	{
		root.setData(key, value);
	}
	
	//----------------------------------------------------------------------
	public void update(GameEntity entity)
	{
		root.evaluate(entity);
	}
	
	//----------------------------------------------------------------------
	public static BehaviourTree load(FileHandle file)
	{
		XmlReader xml = new XmlReader();
		Element xmlElement = null;
		
		try
		{
			xmlElement = xml.parse(file);
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		BehaviourTreeNode node = null;

		try
		{			
			Class<BehaviourTreeNode> c = BehaviourTreeNode.ClassMap.get(xmlElement.getName().toUpperCase());
			node = (BehaviourTreeNode)ClassReflection.newInstance(c);

			node.Data = new HashMap<String, Object>();
			
			node.parse(xmlElement);
		} 
		catch (ReflectionException e) { e.printStackTrace(); }
		
		return new BehaviourTree(node);
	}
	
	//endregion Public Methods
	//####################################################################//
	//region Data
	
	//----------------------------------------------------------------------
	private BehaviourTreeNode root;
	
	//----------------------------------------------------------------------
	public enum BehaviourTreeState
	{
		FAILED,
		SUCCEEDED,
		RUNNING
	}
	
	//endregion Data
	//####################################################################//
}