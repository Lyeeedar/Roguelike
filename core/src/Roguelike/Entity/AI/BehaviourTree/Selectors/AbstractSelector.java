package Roguelike.Entity.AI.BehaviourTree.Selectors;

import Roguelike.Entity.AI.BehaviourTree.BehaviourTreeContainer;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTreeNode;
import Roguelike.Entity.AI.BehaviourTree.Decorators.AbstractDecorator;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public abstract class AbstractSelector extends BehaviourTreeContainer
{
	// ####################################################################//
	// region Public Methods

	// ----------------------------------------------------------------------
	public void addNode( BehaviourTreeNode node )
	{
		if ( node.Data == null )
		{
			node.Data = Data;
		}
		node.Parent = this;
		nodes.add( node );
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

		for ( int i = 0; i < nodes.size; i++ )
		{
			if ( nodes.get( i ) instanceof AbstractSelector || nodes.get( i ) instanceof AbstractDecorator )
			{
				nodes.get( i ).setData( key, value );
			}
		}
	}

	// ----------------------------------------------------------------------
	@Override
	public void parse( Element xmlElement )
	{
		for ( int i = 0; i < xmlElement.getChildCount(); i++ )
		{
			Element xml = xmlElement.getChild( i );

			try
			{
				Class<BehaviourTreeNode> c = ClassMap.get( xml.getName().toUpperCase() );
				BehaviourTreeNode node = ClassReflection.newInstance( c );

				addNode( node );

				node.parse( xml );
			}
			catch ( ReflectionException e )
			{
				e.printStackTrace();
			}
		}
	}

	// endregion Public Methods
	// ####################################################################//
	// region Data

	// ----------------------------------------------------------------------
	protected final Array<BehaviourTreeNode> nodes = new Array<BehaviourTreeNode>();

	// endregion Data
	// ####################################################################//
}
