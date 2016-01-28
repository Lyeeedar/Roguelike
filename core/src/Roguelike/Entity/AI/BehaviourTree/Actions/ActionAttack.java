package Roguelike.Entity.AI.BehaviourTree.Actions;

import Roguelike.Global;
import Roguelike.Global.Direction;
import Roguelike.Entity.GameEntity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;
import Roguelike.Entity.Tasks.TaskAttack;
import Roguelike.Tiles.GameTile;
import Roguelike.Tiles.Point;

import com.badlogic.gdx.utils.Array;
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

		// Find out preferred attack direction
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

		GameTile targetTile = entity.tile[0][0].level.getGameTile( target );
		Direction preferredDir = Direction.getCardinalDirection( target.x - cx, target.y - cy );

		Direction bestDir = preferredDir;
		int bestCount = 0;

		// Check for best direction
		for ( Direction dir : Direction.values() )
		{
			if ( dir != Direction.CENTER && (Global.CanMoveDiagonal || dir.isCardinal()) )
			{
				Array<GameTile> hitTiles = TaskAttack.buildHitTileArray( entity, dir );
				int targetCount = 0;
				boolean valid = false;

				for (GameTile tile : hitTiles)
				{
					if (tile == targetTile)
					{
						valid = true;
					}

					if (tile.entity != null && !tile.entity.isAllies( entity ))
					{
						targetCount++;
					}
				}

				if (valid)
				{
					if (dir == preferredDir)
					{
						bestDir = preferredDir;
						break;
					}

					if (targetCount > bestCount)
					{
						bestDir = dir;
						bestCount = targetCount;
					}
				}
			}
		}

		if (bestDir != Direction.CENTER)
		{
			TaskAttack task = new TaskAttack( bestDir );

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
