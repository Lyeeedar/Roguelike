package Roguelike.Entity.AI.BehaviourTree.Actions;

import Roguelike.Global.Statistic;
import Roguelike.Entity.GameEntity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;
import Roguelike.Pathfinding.ShadowCastCache;
import Roguelike.Tiles.GameTile;
import Roguelike.Tiles.Point;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public class ActionGetAllVisible extends AbstractAction
{
	private enum FindType
	{
		TILES, ENTITIES, ALLIES, ENEMIES
	}

	private FindType Type;
	private String Key;
	private String srcKey;

	@Override
	public BehaviourTreeState evaluate( GameEntity entity )
	{
		Array<GameTile> visibleTiles = null;

		if ( srcKey != null )
		{
			visibleTiles = (Array<GameTile>) getData( srcKey, null );
		}
		else
		{
			Array<Point> output = entity.visibilityCache.getCurrentShadowCast();
			visibleTiles = new Array<GameTile>();
			for ( Point tile : output )
			{
				visibleTiles.add( entity.tile[0][0].level.getGameTile( tile ) );
			}
		}

		if ( Type == FindType.TILES )
		{
			setData( Key, visibleTiles );
			State = visibleTiles.size > 0 ? BehaviourTreeState.SUCCEEDED : BehaviourTreeState.FAILED;
		}
		else
		{
			Array<GameEntity> entities = new Array<GameEntity>();

			for ( GameTile tile : visibleTiles )
			{
				GameEntity e = tile.entity;

				if ( e == null )
				{

				}
				else if ( Type == FindType.ENTITIES )
				{
					entities.add( e );
				}
				else if ( Type == FindType.ALLIES )
				{
					if ( entity.isAllies( e ) )
					{
						entities.add( e );
					}
				}
				else if ( Type == FindType.ENEMIES )
				{
					if ( !entity.isAllies( e ) )
					{
						entities.add( e );
					}
				}
			}

			setData( Key, entities );
			State = entities.size > 0 ? BehaviourTreeState.SUCCEEDED : BehaviourTreeState.FAILED;
		}

		return State;
	}

	@Override
	public void cancel()
	{
	}

	@Override
	public void parse( Element xmlElement )
	{
		Type = FindType.valueOf( xmlElement.getAttribute( "Type" ).toUpperCase() );
		Key = xmlElement.getAttribute( "Key" );
		srcKey = xmlElement.getAttribute( "SrcKey", null );
	}

}
