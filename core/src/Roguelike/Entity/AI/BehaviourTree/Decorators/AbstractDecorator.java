package Roguelike.Entity.AI.BehaviourTree.Decorators;

import Roguelike.Entity.AI.BehaviourTree.BehaviourTreeContainer;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTreeNode;
import Roguelike.Entity.AI.BehaviourTree.Selectors.AbstractSelector;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public abstract class AbstractDecorator extends BehaviourTreeContainer
{
	// ####################################################################//
	// region Public Methods

	// ----------------------------------------------------------------------
	public void setNode( BehaviourTreeNode node )
	{
		if ( node.Data == null )
		{
			node.Data = Data;
		}
		node.Parent = this;
		this.node = node;
	}

	// ----------------------------------------------------------------------
	@Override
	public void setData( String key, Object value )
	{
		if ( value == null )
		{
			Data.remove( key );
		}
		else
		{
			Data.put( key, value );
		}

		if ( node instanceof AbstractSelector || node instanceof AbstractDecorator )
		{
			node.setData( key, value );
		}
	}

	// ----------------------------------------------------------------------
	@Override
	public void parse( Element xmlElement )
	{
		Element xml = xmlElement.getChild( 0 );

		try
		{
			Class<BehaviourTreeNode> c = ClassMap.get( xml.getName().toUpperCase() );
			BehaviourTreeNode node = ClassReflection.newInstance( c );

			setNode( node );

			node.parse( xml );
		}
		catch ( Exception e )
		{
			System.err.println(xml.getName());
			e.printStackTrace();
		}
	}

	// ----------------------------------------------------------------------
	@Override
	public void cancel()
	{
		node.cancel();
	}

	// endregion Public Methods
	// ####################################################################//
	// region Data

	// ----------------------------------------------------------------------
	protected BehaviourTreeNode node;

	// endregion Data
	// ####################################################################//
}
