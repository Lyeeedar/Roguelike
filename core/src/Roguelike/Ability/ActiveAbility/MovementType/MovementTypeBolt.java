package Roguelike.Ability.ActiveAbility.MovementType;

import Roguelike.Global;
import Roguelike.Global.Direction;
import Roguelike.Global.Passability;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Pathfinding.BresenhamLine;
import Roguelike.Tiles.GameTile;
import Roguelike.Tiles.Point;
import Roguelike.Util.EnumBitflag;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public class MovementTypeBolt extends AbstractMovementType
{
	private static final EnumBitflag<Passability> BoltPassability = new EnumBitflag<Passability>( Passability.LEVITATE );

	private float accumulator = 1;
	private int speed;

	@Override
	public void parse( Element xml )
	{
		speed = xml.getInt( "Speed", 1 );
	}

	Array<Point> path;
	int i;

	@Override
	public void init( ActiveAbility ab, int endx, int endy )
	{
		Array<Point> fullpath = BresenhamLine.line( ab.source.x, ab.source.y, endx, endy, ab.source.level.getGrid(), false, ab.getRange() + 1, BoltPassability, ab.getCaster() );

		Array<Point> actualpath = new Array<Point>( fullpath.size );
		for ( int i = 1; i < ab.getRange() + 1 && i < fullpath.size; i++ )
		{
			actualpath.add( fullpath.get( i ).copy() );
		}
		path = actualpath;

		Global.PointPool.freeAll( fullpath );

		ab.AffectedTiles.clear();
		ab.AffectedTiles.add( ab.source.level.getGameTile( path.get( 0 ) ) );
	}

	@Override
	public boolean update( ActiveAbility ab )
	{
		if ( ab.AffectedTiles.peek().entity != null )
		{
			i = path.size;
			return true;
		}

		float step = 1.0f / speed;
		if ( accumulator >= 0 )
		{
			if ( i < path.size - 1 )
			{
				GameTile nextTile = ab.AffectedTiles.peek().level.getGameTile( path.get( i + 1 ) );
				direction = Direction.getDirection( ab.AffectedTiles.peek(), nextTile );

				if ( !nextTile.tileData.passableBy.intersect( BoltPassability ) )
				{
					i = path.size;
					return true;
				}

				ab.AffectedTiles.clear();
				ab.AffectedTiles.add( nextTile );

				if ( nextTile.entity != null )
				{
					i = path.size;
					return true;
				}
				else
				{
					i++;

					if ( i == path.size - 1 ) { return true; }
				}
			}
			else
			{
				return true;
			}

			accumulator -= step;
		}

		return false;
	}

	@Override
	public AbstractMovementType copy()
	{
		MovementTypeBolt t = new MovementTypeBolt();
		t.speed = speed;
		t.accumulator = accumulator;
		t.path = path;
		t.i = i;

		return t;
	}

	@Override
	public void updateAccumulators( float cost )
	{
		accumulator += cost;
	}

	@Override
	public boolean needsUpdate()
	{
		return accumulator > 0;
	}
}
