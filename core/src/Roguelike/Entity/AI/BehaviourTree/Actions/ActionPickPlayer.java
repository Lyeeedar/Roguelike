package Roguelike.Entity.AI.BehaviourTree.Actions;

import Roguelike.Entity.AI.BehaviourTree.BehaviourTree;
import Roguelike.Entity.GameEntity;
import Roguelike.Global;
import com.badlogic.gdx.utils.XmlReader;

/**
 * Created by Philip on 23-Feb-16.
 */
public class ActionPickPlayer extends AbstractAction
{
	public String outputKey;

	@Override
	public BehaviourTree.BehaviourTreeState evaluate( GameEntity entity )
	{
		GameEntity player = Global.CurrentLevel.player;
		setData( outputKey, player );

		return BehaviourTree.BehaviourTreeState.SUCCEEDED;
	}

	@Override
	public void cancel()
	{

	}

	@Override
	public void parse( XmlReader.Element xmlElement )
	{
		outputKey = xmlElement.getAttribute( "OutputKey" );
	}
}
