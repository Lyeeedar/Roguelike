package Roguelike.Entity.AI.BehaviourTree.Actions;

import Roguelike.Entity.ActivationAction.ActivationActionGroup;
import Roguelike.Global;
import Roguelike.Entity.GameEntity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;
import Roguelike.Entity.Tasks.TaskWait;
import Roguelike.Screens.GameScreen;
import Roguelike.Tiles.GameTile;
import Roguelike.Tiles.Point;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
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
		else if ( ! Global.MovementTypePathfind && GameScreen.Instance.preparedAbility == null )
		{
			if ( Gdx.input.isTouched( 0 ) && ! Gdx.input.isTouched( 1 ) && ! Gdx.input.isTouched( 2 ) && ! Gdx.input.isTouched( 3 ) && ! Gdx.input.isTouched( 4 ) )
			{
				int touchX = Gdx.input.getX();
				int touchY = Gdx.input.getY();

				if ( !GameScreen.Instance.pointOverUI( touchX, touchY ) )
				{
					Vector3 mousePos = GameScreen.Instance.camera.unproject( new Vector3( touchX, touchY, 0 ) );

					int mousePosX = (int) mousePos.x;
					int mousePosY = (int) mousePos.y;

					int offsetx = Global.Resolution[ 0 ] / 2 - Global.CurrentLevel.player.tile[ 0 ][ 0 ].x * Global.TileSize;
					int offsety = Global.Resolution[ 1 ] / 2 - Global.CurrentLevel.player.tile[ 0 ][ 0 ].y * Global.TileSize;

					int x = ( mousePosX - offsetx ) / Global.TileSize;
					int y = ( mousePosY - offsety ) / Global.TileSize;

					int dx = x - Global.CurrentLevel.player.tile[0][0].x;
					int dy = y - Global.CurrentLevel.player.tile[0][0].y;

					if ( dx == 0 && dy == 0 )
					{
						if ( Gdx.input.justTouched() )
						{
							targetPos = Global.PointPool.obtain().set( entity.tile[0][0].x, entity.tile[0][0].y );
						}
					}
					else if ( Math.abs( dx ) > Math.abs( dy ) )
					{
						if ( dx < 0 )
						{
							targetPos = Global.PointPool.obtain().set( entity.tile[0][0].x - 1, entity.tile[0][0].y );
						}
						else
						{
							targetPos = Global.PointPool.obtain().set( entity.tile[0][0].x + 1, entity.tile[0][0].y );
						}
					}
					else
					{
						if ( dy < 0 )
						{
							targetPos = Global.PointPool.obtain().set( entity.tile[0][0].x, entity.tile[0][0].y - 1 );
						}
						else
						{
							targetPos = Global.PointPool.obtain().set( entity.tile[0][0].x, entity.tile[0][0].y + 1 );
						}
					}
				}
			}
		}
		else if ( !GameScreen.Instance.lockContextMenu )
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
				targetPos = Global.PointPool.obtain().set( entity.tile[0][0].x + x, entity.tile[0][0].y + y );
			}
		}

		if ( targetPos != null )
		{
			boolean isWait = targetPos.x == entity.tile[0][0].x && targetPos.y == entity.tile[0][0].y;

			Point oldPos = (Point) getData( "Pos", null );
			if ( oldPos != null && oldPos.obtained )
			{
				Global.PointPool.free( oldPos );
			}

			GameTile tile = entity.tile[0][0].level.getGameTile( targetPos );

			boolean entityWithinRange = false;
			boolean dialogueWithinRange = false;

			if ( tile != null )
			{
				if (isWait)
				{
					if ( tile.environmentEntity != null )
					{
						for ( ActivationActionGroup action : tile.environmentEntity.onActivateActions )
						{
							if ( action.enabled )
							{
								entityWithinRange = true;
								break;
							}
						}
					}
				}
				else
				{
					if ( tile.environmentEntity != null
						 && !tile.environmentEntity.canTakeDamage
						 && !tile.environmentEntity.passableBy.intersect( Global.CurrentLevel.player.getTravelType() ) )
					{
						for ( ActivationActionGroup action : tile.environmentEntity.onActivateActions )
						{
							if ( action.enabled )
							{
								entityWithinRange = Math.abs( Global.CurrentLevel.player.tile[0][0].x - tile.x ) <= 1
													&& Math.abs( Global.CurrentLevel.player.tile[0][0].y - tile.y ) <= 1;
								break;
							}
						}
					}
					else if ( tile.entity != null )
					{
						if (!tile.entity.isAllies( entity ))
						{
							entity.weaponSheathed = false;
						}
						else if ( tile.entity.dialogue != null )
						{
							dialogueWithinRange = Math.abs( Global.CurrentLevel.player.tile[0][0].x - tile.x ) <= 1
												  && Math.abs( Global.CurrentLevel.player.tile[0][0].y - tile.y ) <= 1;
						}
					}
				}
			}

			if ( entityWithinRange )
			{
				Array<ActivationActionGroup> valid = new Array<ActivationActionGroup>(  );
				for ( ActivationActionGroup action : tile.environmentEntity.onActivateActions )
				{
					if ( action.enabled && action.checkCondition( tile.environmentEntity, 1 ) )
					{
						valid.add( action );
						break;
					}
				}

				if (valid.size > 0)
				{
					GameScreen.Instance.displayActionOptions(valid, tile.environmentEntity);
				}
			}
			else if ( isWait )
			{
				entity.tasks.add( new TaskWait() );

				setData( "Pos", null );
			}
			else if ( dialogueWithinRange )
			{
				if (tile.entity.inCombat())
				{
					tile.entity.dialogue.exclamationManager.inCombat.process( tile.entity, null, null );
				}
				else if (tile.entity.dialogue.popupText == null)
				{
					tile.entity.dialogue.initialiseDialogue( tile.entity );
					tile.entity.dialogue.advance( tile.entity );
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
