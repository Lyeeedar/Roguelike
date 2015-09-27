package Roguelike.Ability.ActiveAbility.EffectType;

import Roguelike.Global;
import Roguelike.Global.Direction;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Entity.GameEntity;
import Roguelike.Pathfinding.BresenhamLine;
import Roguelike.Sprite.SpriteAnimation.MoveAnimation;
import Roguelike.Sprite.SpriteAnimation.MoveAnimation.MoveEquation;
import Roguelike.Tiles.GameTile;
import Roguelike.Tiles.Point;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public class EffectTypeTeleport extends AbstractEffectType
{
	public enum ToMove
	{
		CASTER, TARGET
	}

	public enum MoveType
	{
		TOWARDS, AWAY
	}

	public enum DestinationType
	{
		EXACT, LINE, CONE
	}

	public enum Style
	{
		BLINK, CHARGE, LEAP
	}

	private ToMove toMove;
	private MoveType moveType;
	private DestinationType destinationType;
	private Style style;
	private int distance = 1;

	@Override
	public void update( ActiveAbility aa, float time, GameTile tile )
	{
		GameTile destination = null;
		GameEntity entityToMove = null;

		if ( toMove == ToMove.CASTER )
		{
			entityToMove = aa.caster;
			destination = getDestination( aa.source, tile, entityToMove );
		}
		else if ( toMove == ToMove.TARGET )
		{
			entityToMove = tile.entity;
			destination = getDestination( tile, aa.source, entityToMove );
		}

		if ( destination != null && entityToMove != null && entityToMove.tile[0][0] != destination )
		{
			int[] diff = destination.addGameEntity( entityToMove );
			entityToMove.actionDelayAccumulator = 0;

			if ( style == Style.CHARGE )
			{
				int distMoved = ( Math.abs( diff[0] ) + Math.abs( diff[1] ) ) / Global.TileSize;
				entityToMove.sprite.spriteAnimation = new MoveAnimation( 0.05f * distMoved, diff, MoveEquation.EXPONENTIAL );
			}
			else if ( style == Style.LEAP )
			{
				float distMoved = ( ( Math.abs( diff[0] ) + Math.abs( diff[1] ) ) / Global.TileSize ) / 3;
				entityToMove.sprite.spriteAnimation = new MoveAnimation( 0.25f, diff, MoveEquation.LEAP );
			}
		}
	}

	private GameTile getDestination( GameTile src, GameTile target, GameEntity entity )
	{
		if ( entity == null ) { return null; }

		Direction dir = null;

		if ( moveType == MoveType.TOWARDS )
		{
			dir = Direction.getDirection( src, target );
		}
		else
		{
			dir = Direction.getDirection( target, src );
		}

		GameTile destination = null;

		if ( destinationType == DestinationType.EXACT )
		{
			destination = target;
		}
		else if ( destinationType == DestinationType.LINE )
		{
			destination = src.level.getGameTile( src.x + dir.getX() * distance, src.y + dir.getY() * distance );
		}
		else
		{
			Array<Point> possibleTiles = Direction.buildCone( dir, Global.PointPool.obtain().set( src.x, src.y ), distance );
			Point pos = possibleTiles.random();

			destination = src.level.getGameTile( pos );

			Global.PointPool.freeAll( possibleTiles );
		}

		if ( destination != null && style != Style.CHARGE )
		{
			if ( destination.getPassable( entity.getTravelType(), entity ) && destination.entity == null )
			{
				return destination;
			}
			else
			{
				for ( Direction d : Direction.values() )
				{
					GameTile tile = getTile( destination, d );

					if ( tile.getPassable( entity.getTravelType(), entity ) && tile.entity == null ) { return tile; }
				}
			}
		}

		Array<Point> possibleTiles = BresenhamLine.lineNoDiag( src.x, src.y, destination.x, destination.y );

		for ( int i = possibleTiles.size - 1; i >= 0; i-- )
		{
			GameTile tile = src.level.getGameTile( possibleTiles.get( i ) );

			if ( tile.getPassable( entity.getTravelType(), entity ) && tile.entity == null )
			{
				Global.PointPool.freeAll( possibleTiles );
				return tile;
			}
		}

		Global.PointPool.freeAll( possibleTiles );

		return src;
	}

	private GameTile getTile( GameTile current, Direction dir )
	{
		int nx = current.x + dir.getX();
		int ny = current.y + dir.getY();

		return current.level.getGameTile( nx, ny );
	}

	@Override
	public void parse( Element xml )
	{
		toMove = ToMove.valueOf( xml.get( "Move", "Caster" ).toUpperCase() );
		moveType = MoveType.valueOf( xml.get( "Direction", "Towards" ).toUpperCase() );
		destinationType = DestinationType.valueOf( xml.get( "Type", "Exact" ).toUpperCase() );
		distance = xml.getInt( "Distance", 1 );

		style = Style.valueOf( xml.get( "Style", "Blink" ).toUpperCase() );
	}

	@Override
	public AbstractEffectType copy()
	{
		EffectTypeTeleport effect = new EffectTypeTeleport();
		effect.toMove = toMove;
		effect.moveType = moveType;
		effect.destinationType = destinationType;

		effect.distance = distance;

		effect.style = style;

		return effect;
	}

	@Override
	public String toString( ActiveAbility aa )
	{
		String line = "";

		line += Global.capitalizeString( style.toString() ) + "s the ";

		line += toMove.toString().toLowerCase() + " ";
		line += moveType.toString().toLowerCase() + " ";

		line += "to a point within " + distance;

		return line;
	}
}
