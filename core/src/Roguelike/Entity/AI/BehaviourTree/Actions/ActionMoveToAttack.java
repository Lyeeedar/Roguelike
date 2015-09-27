package Roguelike.Entity.AI.BehaviourTree.Actions;

import Roguelike.Global;
import Roguelike.Global.Direction;
import Roguelike.Global.Passability;
import Roguelike.Global.Statistic;
import Roguelike.Entity.GameEntity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;
import Roguelike.Entity.Tasks.TaskMove;
import Roguelike.Items.Item;
import Roguelike.Items.Item.EquipmentSlot;
import Roguelike.Pathfinding.Pathfinder;
import Roguelike.Tiles.GameTile;
import Roguelike.Tiles.Point;
import Roguelike.Util.EnumBitflag;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

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

		Item wep = entity.getInventory().getEquip( EquipmentSlot.MAINWEAPON );
		int range = 1;

		if ( wep != null )
		{
			String type = wep.type;

			range = wep.getStatistic( entity.getBaseVariableMap(), Statistic.RANGE );
			if ( range == 0 )
			{
				if ( type.equals( "spear" ) )
				{
					range = 2;
				}
				else if ( type.equals( "bow" ) || type.equals( "wand" ) )
				{
					range = 4;
				}
				else
				{
					range = 1;
				}
			}
		}

		Array<Point> possibleTiles = new Array<Point>();

		for ( Direction dir : Direction.values() )
		{
			if ( dir == Direction.CENTER )
			{
				continue;
			}

			for ( int i = 0; i < range; i++ )
			{
				Point newPos = Global.PointPool.obtain().set( target.x + dir.getX() * ( i + 1 ), target.y + dir.getY() * ( i + 1 ) );
				GameTile tile = entity.tile[0][0].level.getGameTile( newPos );

				if ( !tile.getPassable( WeaponPassability, entity ) )
				{
					break;
				}
				if ( tile.getPassable( entity.getTravelType(), entity ) )
				{
					possibleTiles.add( newPos );
				}
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

			Pathfinder pathFinder = new Pathfinder( entity.tile[0][0].level.getGrid(), entity.tile[0][0].x, entity.tile[0][0].y, pos.x, pos.y, true, entity.size, entity );
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
