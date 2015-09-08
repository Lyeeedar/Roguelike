package Roguelike.Entity.AI.BehaviourTree.Actions;

import Roguelike.Entity.GameEntity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public class ActionExclaim extends AbstractAction
{
	private String tilesKey;

	@Override
	public BehaviourTreeState evaluate( GameEntity entity )
	{
		Object obj = getData( tilesKey, null );

		if ( obj == null || !( obj instanceof Array ) ) { return BehaviourTreeState.FAILED; }

		Array<GameTile> tiles = (Array<GameTile>) obj;

		if ( entity.dialogue != null && entity.dialogue.exclamationManager != null )
		{
			entity.dialogue.exclamationManager.process( tiles, entity );
		}

		return BehaviourTreeState.SUCCEEDED;
	}

	@Override
	public void cancel()
	{
	}

	@Override
	public void parse( Element xmlElement )
	{
		tilesKey = xmlElement.getAttribute( "TilesKey" );
	}

}
