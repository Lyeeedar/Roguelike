package Roguelike.Entity.AI.BehaviourTree.Actions;

import Roguelike.Global;
import Roguelike.Global.Direction;
import Roguelike.Entity.GameEntity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;
import Roguelike.Entity.Tasks.TaskAttack;
import Roguelike.Tiles.GameTile;
import Roguelike.Tiles.Point;

import com.badlogic.gdx.utils.XmlReader.Element;

public class ActionAttack extends AbstractAction
{
	public String key;

	@Override
	public BehaviourTreeState evaluate( GameEntity entity )
	{
		if ( entity.weaponSheathed )
		{
			State = BehaviourTreeState.FAILED;
			return State;
		}

		Point target = (Point) getData( key, null );

		// if no target, fail
		if ( target == null )
		{
			State = BehaviourTreeState.FAILED;
			return State;
		}

		int cx = 0;
		int cy = 0;
		int dst = Integer.MAX_VALUE;

		for ( int x = 0; x < entity.size; x++ )
		{
			for ( int y = 0; y < entity.size; y++ )
			{
				int tmpdst = Math.abs( target.x - ( entity.tile[0][0].x + x ) ) + Math.abs( target.y - ( entity.tile[0][0].y + y ) );

				if ( tmpdst < dst )
				{
					dst = tmpdst;
					cx = entity.tile[0][0].x + x;
					cy = entity.tile[0][0].y + y;
				}
			}
		}

		Direction dir = Direction.getDirection( target.x - cx, target.y - cy );

		if ( dir != Direction.CENTER && ( Global.CanMoveDiagonal || dir.isCardinal() ) )
		{
			TaskAttack task = new TaskAttack( dir );
			GameTile targetTile = entity.tile[0][0].level.getGameTile( target );
			if ( targetTile != null
						 && targetTile.environmentEntity != null
						 && targetTile.environmentEntity.canTakeDamage
						 && task.canAttackTile( entity, targetTile ) )
			{
				entity.tasks.add( task );

				State = BehaviourTreeState.SUCCEEDED;
				return State;
			}
			else if ( task.checkHitSomething( entity ) )
			{
				entity.tasks.add( task );

				State = BehaviourTreeState.SUCCEEDED;
				return State;
			}
		}

		State = BehaviourTreeState.FAILED;
		return State;
	}

	@Override
	public void cancel()
	{
	}

	@Override
	public void parse( Element xmlElement )
	{
		key = xmlElement.getAttribute( "Key" );
	}
}
