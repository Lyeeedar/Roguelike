package Roguelike.Entity.Tasks;

import Roguelike.Entity.Entity;
import Roguelike.Entity.GameEntity;
import Roguelike.Global;
import Roguelike.Global.Direction;
import Roguelike.Global.Passability;
import Roguelike.Items.Item;
import Roguelike.Items.Item.EquipmentSlot;
import Roguelike.Sound.SoundInstance;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.SpriteAnimation.BumpAnimation;
import Roguelike.Sprite.SpriteAnimation.MoveAnimation;
import Roguelike.Sprite.SpriteAnimation.MoveAnimation.MoveEquation;
import Roguelike.Sprite.SpriteEffect;
import Roguelike.Tiles.GameTile;
import Roguelike.Tiles.Point;
import Roguelike.Util.EnumBitflag;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;

public class TaskAttack extends AbstractTask
{
	private static final EnumBitflag<Passability> WeaponPassability = new EnumBitflag<Passability>( Passability.LIGHT );

	public Direction dir;

	public TaskAttack( Direction dir )
	{
		if ( !Global.CanMoveDiagonal && !dir.isCardinal() )
		{
			throw new RuntimeException( "Invalid attack direction: " + dir.toString() );
		}

		this.dir = dir;
	}

	public static Array<Point> buildAllDirectionHitTiles( GameEntity entity )
	{
		Array<Point> points = new Array<Point>(  );

		Item weapon = entity.getInventory().getEquip( EquipmentSlot.WEAPON );

		for ( Direction dir : Direction.values() )
		{
			if ( Global.CanMoveDiagonal || dir.isCardinal() )
			{
				int xstep = 0;
				int ystep = 0;

				int sx = 0;
				int sy = 0;

				if ( dir == Direction.NORTH )
				{
					sx = 0;
					sy = entity.size - 1;

					xstep = 1;
					ystep = 0;
				}
				else if ( dir == Direction.SOUTH )
				{
					sx = 0;
					sy = 0;

					xstep = 1;
					ystep = 0;
				}
				else if ( dir == Direction.EAST )
				{
					sx = entity.size - 1;
					sy = 0;

					xstep = 0;
					ystep = 1;
				}
				else if ( dir == Direction.WEST )
				{
					sx = 0;
					sy = 0;

					xstep = 0;
					ystep = 1;
				}

				for (int i = 0; i < entity.size; i++)
				{
					GameTile attackerTile = entity.tile[sx + xstep * i][sy + ystep * i];

					if (weapon != null && weapon.wepDef != null)
					{
						Matrix3 mat = new Matrix3(  );
						mat.setToRotation( dir.getAngle() );
						Vector3 vec = new Vector3();

						for (Point point : weapon.wepDef.hitPoints)
						{
							vec.set( point.x, point.y, 0 );
							vec.mul( mat );

							int dx = Math.round( vec.x );
							int dy = Math.round( vec.y );

							Point pos = Global.PointPool.obtain().set( attackerTile.x + dx, attackerTile.y + dy );
							points.add( pos );
						}
					}
					else
					{
						Point pos = Global.PointPool.obtain().set( attackerTile.x + dir.getX(), attackerTile.y + dir.getY() );
						points.add( pos );
					}
				}
			}
		}

		// restrict by visibility and remove duplicates
		Array<Point> visibleTiles = entity.visibilityCache.getCurrentShadowCast();

		Iterator<Point> itr = points.iterator();
		while (itr.hasNext())
		{
			Point pos = itr.next();

			boolean matchFound = false;

			// Remove not visible
			for (Point point : visibleTiles)
			{
				if (point.x == pos.x && point.y == pos.y)
				{
					matchFound = true;
					break;
				}
			}

			// Remove duplicates
			for (int i = 0; i < points.size; i++)
			{
				Point opos = points.get( i );
				if (opos != pos && opos.x == pos.x && opos.y == pos.y)
				{
					matchFound = false;
					break;
				}
			}

			if (!matchFound)
			{
				itr.remove();

				Global.PointPool.free( pos );
			}
		}

		return points;
	}

	public static Array<GameTile> buildHitTileArray( GameEntity attacker, Direction dir )
	{
		Array<GameTile> tiles = new Array<GameTile>(  );

		Item weapon = attacker.getInventory().getEquip( EquipmentSlot.WEAPON );

		int xstep = 0;
		int ystep = 0;

		int sx = 0;
		int sy = 0;

		if ( dir == Direction.NORTH )
		{
			sx = 0;
			sy = attacker.size - 1;

			xstep = 1;
			ystep = 0;
		}
		else if ( dir == Direction.SOUTH )
		{
			sx = 0;
			sy = 0;

			xstep = 1;
			ystep = 0;
		}
		else if ( dir == Direction.EAST )
		{
			sx = attacker.size - 1;
			sy = 0;

			xstep = 0;
			ystep = 1;
		}
		else if ( dir == Direction.WEST )
		{
			sx = 0;
			sy = 0;

			xstep = 0;
			ystep = 1;
		}

		for (int i = 0; i < attacker.size; i++)
		{
			GameTile attackerTile = attacker.tile[sx + xstep * i][sy + ystep * i];

			if (weapon != null && weapon.wepDef != null)
			{
				Matrix3 mat = new Matrix3(  );
				mat.setToRotation( dir.getAngle() );
				Vector3 vec = new Vector3();

				for (Point point : weapon.wepDef.hitPoints)
				{
					vec.set( point.x, point.y, 0 );
					vec.mul( mat );

					int dx = Math.round( vec.x );
					int dy = Math.round( vec.y );

					GameTile tile = attackerTile.level.getGameTile( attackerTile.x + dx, attackerTile.y + dy );

					if (tile != null)
					{
						tiles.add( tile );
					}
				}
			}
			else
			{
				tiles.add( attackerTile.level.getGameTile( attackerTile.x + dir.getX(), attackerTile.y + dir.getY() ) );
			}
		}

		// restrict by visibility and remove duplicates
		Array<Point> visibleTiles = attacker.visibilityCache.getCurrentShadowCast();

		Iterator<GameTile> itr = tiles.iterator();
		while (itr.hasNext())
		{
			GameTile tile = itr.next();

			boolean matchFound = false;

			// Remove not visible
			for (Point point : visibleTiles)
			{
				if (point.x == tile.x && point.y == tile.y)
				{
					matchFound = true;
					break;
				}
			}

			// Remove duplicates
			for (int i = 0; i < tiles.size; i++)
			{
				GameTile otile = tiles.get( i );
				if (otile != tile && otile.x == tile.x && otile.y == tile.y)
				{
					matchFound = false;
					break;
				}
			}

			if (!matchFound)
			{
				itr.remove();
			}
		}

		return tiles;
	}

	public boolean checkHitSomething( GameEntity obj )
	{
		Array<GameTile> hitTiles = buildHitTileArray( obj, dir );

		// Check if should attack something
		boolean hitSomething = false;
		for ( GameTile tile : hitTiles )
		{
			if ( tile.entity != null && !tile.entity.isAllies( obj ) )
			{
				hitSomething = true;
				break;
			}
		}

		return hitSomething;
	}

	public boolean canAttackTile( GameEntity obj, GameTile tile )
	{
		Array<GameTile> hitTiles = buildHitTileArray( obj, dir );

		for ( GameTile t : hitTiles )
		{
			if ( t == tile ) { return true; }
		}

		return false;
	}

	@Override
	public void processTask( GameEntity obj )
	{
		// Collect data
		GameTile oldTile = obj.tile[ 0 ][ 0 ];

		int newX = oldTile.x + dir.getX();
		int newY = oldTile.y + dir.getY();

		GameTile newTile = oldTile.level.getGameTile( newX, newY );

		Item wep = obj.getInventory().getEquip( EquipmentSlot.WEAPON );

		Array<GameTile> hitTiles = buildHitTileArray( obj, dir );

		// Check if should attack something
		boolean hitSomething = false;
		for ( GameTile tile : hitTiles )
		{
			if ( tile.entity != null && !tile.entity.isAllies( obj ) )
			{
				hitSomething = true;
				break;
			}

			if ( tile.environmentEntity != null && tile.environmentEntity.canTakeDamage && !tile.environmentEntity.passableBy.intersect( obj.getTravelType() ) )
			{
				hitSomething = true;
			}
		}

		// Do attack
		if ( hitSomething )
		{
			doAttack( hitTiles, obj, wep );

			// do graphics stuff
			obj.sprite.spriteAnimation = new BumpAnimation( 0.1f, dir );
		}
	}

	private void doAttack( Array<GameTile> hitTiles, GameEntity entity, Item weapon )
	{
		final GameTile source = entity.tile[0][0];

		// Get all the attacked tiles
		Array<GameTile> attackedTiles = new Array<GameTile>(  );

		if (weapon == null || weapon.wepDef == null || weapon.wepDef.hitType == Item.WeaponDefinition.HitType.ALL)
		{
			attackedTiles.addAll( hitTiles );
		}
		else if (weapon.wepDef.hitType == Item.WeaponDefinition.HitType.CLOSEST)
		{
			int num = weapon.wepDef.hitData != null ? Integer.parseInt( weapon.wepDef.hitData ) : 1;

			Array<GameTile> validEntityTiles = new Array<GameTile>(  );
			Array<GameTile> validEnvironmentTiles = new Array<GameTile>(  );

			// Get tiles valid to hit
			for ( GameTile tile : hitTiles )
			{
				if ( tile.entity != null && !tile.entity.isAllies( entity ) )
				{
					validEntityTiles.add( tile );
				}
				else if ( tile.environmentEntity != null && tile.environmentEntity.canTakeDamage )
				{
					validEnvironmentTiles.add( tile );
				}
			}

			Comparator<GameTile> comp = new Comparator<GameTile>()
			{
				@Override
				public int compare( GameTile o1, GameTile o2 )
				{
					int dist1 = Math.abs( o1.x - source.x ) + Math.abs( o1.y - source.y );
					int dist2 = Math.abs( o2.x - source.x ) + Math.abs( o2.y - source.y );

					return dist1 - dist2;
				}
			};

			// sort by distance
			validEntityTiles.sort( comp );
			validEnvironmentTiles.sort( comp );

			for ( int i = 0; i < num && i < validEntityTiles.size; i++ )
			{
				attackedTiles.add( validEntityTiles.get( i ) );
			}

			for ( int i = 0; i < num - validEntityTiles.size && i < validEnvironmentTiles.size; i++ )
			{
				attackedTiles.add( validEnvironmentTiles.get( i ) );
			}
		}
		else if (weapon.wepDef.hitType == Item.WeaponDefinition.HitType.RANDOM)
		{
			int num = weapon.wepDef.hitData != null ? Integer.parseInt( weapon.wepDef.hitData ) : 1;

			Array<GameTile> validEntityTiles = new Array<GameTile>(  );
			Array<GameTile> validEnvironmentTiles = new Array<GameTile>(  );

			// Get tiles valid to hit
			for ( GameTile tile : hitTiles )
			{
				if ( tile.entity != null && !tile.entity.isAllies( entity ) )
				{
					validEntityTiles.add( tile );
				}
				else if ( tile.environmentEntity != null && tile.environmentEntity.canTakeDamage )
				{
					validEnvironmentTiles.add( tile );
				}
			}

			if (validEntityTiles.size > 0)
			{
				for (int i = 0; i < num; i++)
				{
					attackedTiles.add( validEntityTiles.random() );
				}
			}
			else if (validEnvironmentTiles.size > 0)
			{
				for (int i = 0; i < num; i++)
				{
					attackedTiles.add( validEnvironmentTiles.random() );
				}
			}
		}

		Sprite hitEffect = null;
		if ( weapon == null )
		{
			hitEffect = entity.defaultHitEffect;
		}
		else
		{
			hitEffect = weapon.getWeaponHitEffect();
		}

		Point minPoint = Global.PointPool.obtain().set( Integer.MAX_VALUE, Integer.MAX_VALUE );
		Point maxPoint = Global.PointPool.obtain().set( 0, 0 );

		HashSet<Entity> hitEntities = new HashSet<Entity>(  );

		// Do the attack
		for ( GameTile tile : attackedTiles )
		{
			if ( tile.entity != null && !tile.entity.isAllies( entity ) )
			{
				if (!hitEntities.contains( tile.entity ))
				{
					entity.attack( tile.entity, dir );
					hitEntities.add( tile.entity );
				}
			}
			else if ( tile.environmentEntity != null && !tile.environmentEntity.passableBy.intersect( entity.getTravelType() ) )
			{
				if (!hitEntities.contains( tile.environmentEntity ))
				{
					entity.attack( tile.environmentEntity, dir );
					hitEntities.add( tile.environmentEntity );
				}
			}

			if ( weapon == null || weapon.wepDef == null || weapon.wepDef.hitType != Item.WeaponDefinition.HitType.ALL )
			{
				int[] diff = tile.getPosDiff( source );

				Sprite sprite = hitEffect.copy();

				if ( sprite.spriteAnimation != null )
				{
					int distMoved = ( Math.abs( diff[ 0 ] ) + Math.abs( diff[ 1 ] ) ) / Global.TileSize;
					sprite.spriteAnimation.set( 0.05f * distMoved, diff );
				}

				Vector2 vec = new Vector2( diff[ 0 ] * -1, diff[ 1 ] * -1 );
				vec.nor();
				float x = vec.x;
				float y = vec.y;
				double dot = 0 * x + 1 * y; // dot product
				double det = 0 * y - 1 * x; // determinant
				float angle = (float) Math.atan2( det, dot ) * MathUtils.radiansToDegrees;
				sprite.rotation = angle;

				if ( attackedTiles.size > 1 )
				{
					sprite.renderDelay = sprite.animationDelay * tile.getDist( source ) + sprite.animationDelay;
				}

				SpriteEffect effect = new SpriteEffect( sprite, Direction.CENTER, weapon != null && weapon.light != null ? weapon.light.copyNoFlag() : null );

				tile.spriteEffects.add( effect );

				SoundInstance sound = hitEffect.sound;
				if ( sound != null )
				{
					sound.play( tile );
				}
			}
			else
			{
				if (tile.x < minPoint.x)
				{
					minPoint.x = tile.x;
				}
				if (tile.x > maxPoint.x)
				{
					maxPoint.x = tile.x;
				}
				if (tile.y < minPoint.y)
				{
					minPoint.y = tile.y;
				}
				if (tile.y > maxPoint.y)
				{
					maxPoint.y = tile.y;
				}

			}
		}

		if ( weapon != null && weapon.wepDef != null && weapon.wepDef.hitType == Item.WeaponDefinition.HitType.ALL )
		{
			// Use a joined sprite

			Sprite sprite = hitEffect.copy();

			sprite.rotation = dir.getAngle();
			sprite.baseScale[0] = ( maxPoint.x - minPoint.x ) + 1;
			sprite.baseScale[1] = ( maxPoint.y - minPoint.y ) + 1;


			if (dir == Direction.WEST || dir == Direction.EAST)
			{
				float temp = sprite.baseScale[0];
				sprite.baseScale[0] = sprite.baseScale[1];
				sprite.baseScale[1] = temp;
			}

			SpriteEffect effect = new SpriteEffect( sprite, Direction.CENTER, weapon != null && weapon.light != null ? weapon.light.copyNoFlag() : null );

			int px = minPoint.x;
			int py = minPoint.y;

			float dx = ( maxPoint.x - minPoint.x ) / 2.0f;
			float dy = ( maxPoint.y - minPoint.y ) / 2.0f;

			px += dir.getX() < 0 ? Math.ceil( dx ) : Math.floor( dx );
			py += dir.getY() < 0 ? Math.ceil( dy ) : Math.floor( dy );

			GameTile tile = attackedTiles.first().level.getGameTile( px, py );

			tile.spriteEffects.add( effect );

			SoundInstance sound = hitEffect.sound;
			if ( sound != null )
			{
				sound.play( tile );
			}
		}
	}
}
