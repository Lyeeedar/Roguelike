package Roguelike.Entity.AI.BehaviourTree.Actions;

import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;
import Roguelike.Entity.GameEntity;
import Roguelike.Entity.Tasks.TaskMove;
import Roguelike.Global;
import Roguelike.Global.Direction;
import Roguelike.Global.Passability;
import Roguelike.Global.Statistic;
import Roguelike.Items.Item;
import Roguelike.Items.Item.EquipmentSlot;
import Roguelike.Pathfinding.Pathfinder;
import Roguelike.Tiles.GameTile;
import Roguelike.Tiles.Point;
import Roguelike.Util.EnumBitflag;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

import java.util.Iterator;

public class ActionMoveToAttack extends AbstractAction
{
	// ----------------------------------------------------------------------
	public static final EnumBitflag<Passability> WeaponPassability = new EnumBitflag<Passability>( Passability.LEVITATE );

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

		Array<Point> possibleTiles = new Array<Point>();
		Item weapon = entity.getInventory().getEquip( EquipmentSlot.WEAPON );

		if (weapon != null && weapon.wepDef != null)
		{
			for (Direction dir : Direction.values())
			{
				// For each direction, find the attack points

				Matrix3 mat = new Matrix3();
				mat.setToRotation( dir.getAngle() );

				Vector3 vec = new Vector3();

				for (Point p : weapon.wepDef.hitPoints)
				{
					vec.set(p.x, p.y, 0);
					vec.mul( mat );

					int dx = Math.round( vec.x );
					int dy = Math.round( vec.y );

					Point newPos = Global.PointPool.obtain().set( target.x - dx, target.y - dy );
					GameTile tile = entity.tile[0][0].level.getGameTile( newPos );

					if ( tile != null && tile.getPassable( entity.getTravelType(), entity ) )
					{
						possibleTiles.add( newPos );
					}
				}
			}
		}
		else
		{
			for ( Direction dir : Direction.values() )
			{
				if ( dir == Direction.CENTER )
				{
					continue;
				}

				Point newPos = Global.PointPool.obtain().set( target.x + dir.getX(), target.y + dir.getY() );
				GameTile tile = entity.tile[0][0].level.getGameTile( newPos );

				if ( tile == null || !tile.getPassable( WeaponPassability, entity ) )
				{
					break;
				}
				if ( tile.getPassable( entity.getTravelType(), entity ) )
				{
					possibleTiles.add( newPos );
				}
			}
		}

		Array<Point> shadowCast = null;
		GameTile targetTile = entity.tile[0][0].level.getGameTile( target );
		if (targetTile.entity != null)
		{
			shadowCast = targetTile.entity.visibilityCache.getCurrentShadowCast();
		}
		else if (targetTile.environmentEntity != null)
		{
			targetTile.environmentEntity.updateShadowCast();
			shadowCast = targetTile.environmentEntity.visibilityCache.getCurrentShadowCast();
		}
		else
		{
			// Theres nothing there to attack, so give up
			State = BehaviourTreeState.FAILED;
			return State;
		}

		// minimise possible tiles
		Iterator<Point> itr = possibleTiles.iterator();
		while (itr.hasNext())
		{
			Point p = itr.next();

			boolean found = false;
			for (Point visibleP : shadowCast)
			{
				if (visibleP.x == p.x && visibleP.y == p.y)
				{
					found = true;
					break;
				}
			}

			if (!found)
			{
				itr.remove();
			}
		}

		int bestDist = Integer.MAX_VALUE;
		Array<Point> bestPath = null;

		for ( Point pos : possibleTiles )
		{
			if ( pos.x == entity.tile[0][0].x && pos.y == entity.tile[0][0].y )
			{
				Global.PointPool.freeAll( possibleTiles );
				State = BehaviourTreeState.SUCCEEDED;
				return State;
			}

			Pathfinder pathFinder = new Pathfinder( entity.tile[0][0].level.getGrid(), entity.tile[0][0].x, entity.tile[0][0].y, pos.x, pos.y, Global.CanMoveDiagonal, entity.size, entity );
			Array<Point> path = pathFinder.getPath( entity.getTravelType() );

			if ( path.size > 1 && path.size < bestDist )
			{
				if ( entity.tile[0][0].level.getGameTile( path.get( 1 ) ).getPassable( entity.getTravelType(), entity ) )
				{
					bestDist = path.size;

					if ( bestPath != null )
					{
						Global.PointPool.freeAll( bestPath );
					}
					bestPath = path;
				}
			}
		}

		Global.PointPool.freeAll( possibleTiles );

		if ( bestPath == null )
		{
			State = BehaviourTreeState.FAILED;
			return State;
		}

		int[] offset = new int[] { bestPath.get( 1 ).x - bestPath.get( 0 ).x, bestPath.get( 1 ).y - bestPath.get( 0 ).y };

		Global.PointPool.freeAll( bestPath );

		entity.tasks.add( new TaskMove( Direction.getDirection( offset ) ) );

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
		key = xmlElement.getAttribute( "Key" );
	}

}
