package Roguelike.Entity.AI.BehaviourTree.Actions;

import Roguelike.Global;
import Roguelike.Entity.EnvironmentEntity.ActivationAction;
import Roguelike.Entity.GameEntity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;
import Roguelike.Entity.Tasks.TaskWait;
import Roguelike.Tiles.GameTile;
import Roguelike.Tiles.Point;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.XmlReader.Element;

public class ActionProcessInput extends AbstractAction
{

	@Override
	public BehaviourTreeState evaluate( GameEntity entity )
	{
		Point targetPos = (Point) getData( "ClickPos", null );

		if ( targetPos != null )
		{
			setData( "ClickPos", null );
		}
		else
		{
			boolean up = Gdx.input.isKeyPressed( Keys.UP );
			boolean down = Gdx.input.isKeyPressed( Keys.DOWN );
			boolean left = Gdx.input.isKeyPressed( Keys.LEFT );
			boolean right = Gdx.input.isKeyPressed( Keys.RIGHT );
			boolean space = Gdx.input.isKeyPressed( Keys.SPACE );

			int x = 0;
			int y = 0;

			if ( up )
			{
				y = 1;
			}
			else if ( down )
			{
				y = -1;
			}

			if ( left )
			{
				x = -1;
			}
			else if ( right )
			{
				x = 1;
			}

			if ( x != 0 || y != 0 || space )
			{
				targetPos = Pools.obtain( Point.class ).set( entity.tile.x + x, entity.tile.y + y );
			}
		}

		if ( targetPos != null )
		{
			Point oldPos = (Point) getData( "Pos", null );
			if ( oldPos != null )
			{
				Pools.free( oldPos );
			}

			GameTile tile = entity.tile.level.getGameTile( targetPos );

			boolean entityWithinRange = false;
			if ( tile != null
					&& tile.environmentEntity != null
					&& !tile.environmentEntity.canTakeDamage
					&& !tile.environmentEntity.passableBy.intersect( Global.CurrentLevel.player.getTravelType() ) )
			{
				for ( ActivationAction action : tile.environmentEntity.actions )
				{
					if ( action.visible )
					{
						entityWithinRange = Math.abs( Global.CurrentLevel.player.tile.x - tile.x ) <= 1
								&& Math.abs( Global.CurrentLevel.player.tile.y - tile.y ) <= 1;
						break;
					}
				}
			}

			if ( targetPos.x == entity.tile.x && targetPos.y == entity.tile.y )
			{
				entity.tasks.add( new TaskWait() );

				setData( "Pos", null );
			}
			else if ( entityWithinRange )
			{
				for ( ActivationAction action : tile.environmentEntity.actions )
				{
					if ( action.visible )
					{
						action.activate( tile.environmentEntity );
						Global.CurrentLevel.player.tasks.add( new TaskWait() );
						break;
					}
				}
			}
			else
			{
				setData( "Pos", targetPos );
			}

			setData( "Rest", null );
		}

		return targetPos != null ? BehaviourTreeState.SUCCEEDED : BehaviourTreeState.FAILED;
	}

	@Override
	public void cancel()
	{
	}

	@Override
	public void parse( Element xmlElement )
	{
	}

}
