package Roguelike.Entity.Tasks;

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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import java.util.Comparator;
import java.util.Iterator;

public class TaskAttack extends AbstractTask
{
	private static final EnumBitflag<Passability> WeaponPassability = new EnumBitflag<Passability>( Passability.LIGHT );

	public Direction dir;

	public TaskAttack( Direction dir )
	{
		this.dir = dir;
	}

	public Array<GameTile> buildHitTileArray( GameEntity attacker, Direction dir )
	{
		Array<GameTile> tiles = new Array<GameTile>(  );

		Item weapon = attacker.getInventory().getEquip( EquipmentSlot.WEAPON );
		if (weapon != null && weapon.wepDef != null)
		{
			GameTile attackerTile = attacker.tile[0][0];

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

			// TODO: attacks for large creatures
			GameTile attackerTile = attacker.tile[0][0];
			tiles.add( attackerTile.level.getGameTile( attackerTile.x + dir.getX(), attackerTile.y + dir.getY() ) );
		}

		// restrict by visibility
		Array<Point> visibleTiles = attacker.visibilityCache.getCurrentShadowCast();

		Iterator<GameTile> itr = tiles.iterator();
		while (itr.hasNext())
		{
			GameTile tile = itr.next();

			boolean matchFound = false;

			for (Point point : visibleTiles)
			{
				if (point.x == tile.x && point.y == tile.y)
				{
					matchFound = true;
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

			Array<GameTile> validTiles = new Array<GameTile>(  );

			// Get tiles valid to hit
			for ( GameTile tile : hitTiles )
			{
				if ( tile.entity != null && !tile.entity.isAllies( entity ) )
				{
					validTiles.add( tile );
				}
				else if ( tile.environmentEntity != null && tile.environmentEntity.canTakeDamage )
				{
					validTiles.add( tile );
				}
			}

			// sort by distance
			validTiles.sort( new Comparator<GameTile>()
			{
				@Override
				public int compare( GameTile o1, GameTile o2 )
				{
					int dist1 = Math.abs( o1.x - source.x ) + Math.abs( o1.y - source.y );
					int dist2 = Math.abs( o2.x - source.x ) + Math.abs( o2.y - source.y );

					return dist1 - dist2;
				}
			} );

			for (int i = 0; i < num; i++)
			{
				attackedTiles.add( validTiles.get( i ) );
			}
		}
		else if (weapon.wepDef.hitType == Item.WeaponDefinition.HitType.RANDOM)
		{
			int num = weapon.wepDef.hitData != null ? Integer.parseInt( weapon.wepDef.hitData ) : 1;

			Array<GameTile> validTiles = new Array<GameTile>(  );

			// Get tiles valid to hit
			for ( GameTile tile : hitTiles )
			{
				if ( tile.entity != null && !tile.entity.isAllies( entity ) )
				{
					validTiles.add( tile );
				}
				else if ( tile.environmentEntity != null && tile.environmentEntity.canTakeDamage )
				{
					validTiles.add( tile );
				}
			}

			for (int i = 0; i < num; i++)
			{
				attackedTiles.add( validTiles.random() );
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

		// Do the attack
		for ( GameTile tile : attackedTiles )
		{
			if ( tile.entity != null && !tile.entity.isAllies( entity ) )
			{
				entity.attack( tile.entity, dir );
			}
			else if ( tile.environmentEntity != null && !tile.environmentEntity.passableBy.intersect( entity.getTravelType() ) )
			{
				entity.attack( tile.environmentEntity, dir );
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
			sprite.size[0] = ( maxPoint.x - minPoint.x ) + 1;
			sprite.size[1] = ( maxPoint.y - minPoint.y ) + 1;

			SpriteEffect effect = new SpriteEffect( sprite, Direction.CENTER, weapon != null && weapon.light != null ? weapon.light.copyNoFlag() : null );

			int px = minPoint.x;// + ( maxPoint.x - minPoint.x ) / 2;
			int py = minPoint.y;// + ( maxPoint.y - minPoint.y ) / 2;

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
