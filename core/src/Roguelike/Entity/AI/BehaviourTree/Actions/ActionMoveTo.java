package Roguelike.Entity.AI.BehaviourTree.Actions;

import Roguelike.Global.Direction;
import Roguelike.Entity.GameEntity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;
import Roguelike.Entity.Tasks.TaskMove;
import Roguelike.Pathfinding.Pathfinder;
import Roguelike.Tiles.GameTile;
import Roguelike.Tiles.Point;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.XmlReader.Element;

public class ActionMoveTo extends AbstractAction
{
	public int dst;
	public boolean towards;
	public String key;

	@Override
	public BehaviourTreeState evaluate( GameEntity entity )
	{
		Point target = (Point) getData( key, null );

		// if no target, fail
		if ( target == null )
		{
			State = BehaviourTreeState.FAILED;
			return State;
		}

		// if we arrived at our target, succeed
		if ( entity.tile[0][0].x == target.x && entity.tile[0][0].y == target.y )
		{
			State = BehaviourTreeState.SUCCEEDED;
			return State;
		}

		Pathfinder pathFinder = new Pathfinder( entity.tile[0][0].level.getGrid(), entity.tile[0][0].x, entity.tile[0][0].y, target.x, target.y, true, entity.size );
		Array<Point> path = pathFinder.getPath( entity.getTravelType() );

		// if couldnt find a valid path, fail
		if ( path.size < 2 )
		{
			Pools.freeAll( path );
			State = BehaviourTreeState.FAILED;
			return State;
		}

		GameTile nextTile = entity.tile[0][0].level.getGameTile( path.get( 1 ) );
		// if next step is impassable then fail
		if ( !nextTile.getPassable( entity.getTravelType() ) )
		{
			Pools.freeAll( path );
			State = BehaviourTreeState.FAILED;
			return State;
		}

		int[] offset = new int[] { path.get( 1 ).x - path.get( 0 ).x, path.get( 1 ).y - path.get( 0 ).y };

		// if moving towards path to the object
		if ( towards )
		{
			if ( path.size - 1 <= dst )
			{
				Pools.freeAll( path );
				State = BehaviourTreeState.SUCCEEDED;
				return State;
			}

			entity.tasks.add( new TaskMove( Direction.getDirection( offset ) ) );
		}
		// if moving away then just run directly away
		else
		{
			if ( path.size - 1 >= dst )
			{
				Pools.freeAll( path );
				State = BehaviourTreeState.SUCCEEDED;
				return State;
			}

			entity.tasks.add( new TaskMove( Direction.getDirection( offset[0] * -1, offset[1] * -1 ) ) );
		}

		Pools.freeAll( path );
		State = BehaviourTreeState.RUNNING;
		return State;
	}

	@Override
	public void cancel()
	{

	}

	@Override
	public void parse( Element xmlElement )
	{
		dst = Integer.parseInt( xmlElement.getAttribute( "Distance", "0" ) );
		towards = Boolean.parseBoolean( xmlElement.getAttribute( "Towards", "true" ) );
		key = xmlElement.getAttribute( "Key" );
	}
}
