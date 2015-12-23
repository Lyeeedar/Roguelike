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

				tiles.add( attackerTile.level.getGameTile( attackerTile.x + (int)vec.x, attackerTile.y + (int)vec.y ) );
			}
		}
		else
		{
			// TODO: attacks for large creatures
			GameTile attackerTile = attacker.tile[0][0];
			tiles.add( attackerTile.level.getGameTile( attackerTile.x + dir.getX(), attackerTile.y + dir.getY() ) );
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
			if ( wep != null && ( wep.type.equals( "bow" ) || wep.type.equals( "wand" ) ) )
			{
				doRangedAttack( hitTiles, newTile, obj, wep );
			}
			else
			{
				doNormalAttack( hitTiles, obj, wep );
			}

			// do graphics stuff
			obj.sprite.spriteAnimation = new BumpAnimation( 0.1f, dir );
		}
	}

	private void doNormalAttack( Array<GameTile> hitTiles, GameEntity entity, Item weapon )
	{
		// hit all tiles
		for ( GameTile tile : hitTiles )
		{
			if ( tile.entity != null && !tile.entity.isAllies( entity ) )
			{
				entity.attack( tile.entity, dir );
			}
			else if ( tile.environmentEntity != null && !tile.environmentEntity.passableBy.intersect( entity.getTravelType() ) )
			{
				entity.attack( tile.environmentEntity, dir );
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

		// add hit effects
		for ( GameTile tile : hitTiles )
		{
			if ( tile.entity != null )
			{
				SpriteEffect e = new SpriteEffect( hitEffect.copy(), Direction.CENTER, null );
				e.Sprite.rotation = dir.getAngle();
			}

			if ( tile.environmentEntity != null )
			{
				SpriteEffect e = new SpriteEffect( hitEffect.copy(), Direction.CENTER, null );
				e.Sprite.rotation = dir.getAngle();
			}

			SpriteEffect e = new SpriteEffect( hitEffect.copy(), Direction.CENTER, null );
			e.Sprite.rotation = dir.getAngle();

			tile.spriteEffects.add( e );

			SoundInstance sound = hitEffect.sound;
			if ( sound != null )
			{
				sound.play( tile );
			}
		}
	}

	private void doRangedAttack( Array<GameTile> hitTiles, GameTile newTile, GameEntity entity, Item weapon )
	{
		// if bow only hit closest

		int cx = 0;
		int cy = 0;
		int dst = Integer.MAX_VALUE;

		for ( int x = 0; x < entity.size; x++ )
		{
			for ( int y = 0; y < entity.size; y++ )
			{
				int tmpdst = Math.abs( newTile.x - ( entity.tile[0][0].x + x ) ) + Math.abs( newTile.y - ( entity.tile[0][0].y + y ) );

				if ( tmpdst < dst )
				{
					dst = tmpdst;
					cx = x;
					cy = y;
				}
			}
		}

		GameTile source = entity.tile[cx][cy];

		GameTile bestTarget = null;

		// Find closest game entity first
		int closest = Integer.MAX_VALUE;
		for ( GameTile tile : hitTiles )
		{
			if ( tile.entity != null && !tile.entity.isAllies( entity ) )
			{
				int dist = Math.abs( tile.x - source.x ) + Math.abs( tile.y - source.y );

				if ( dist < closest )
				{
					closest = dist;
					bestTarget = tile;
				}
			}
			else if ( tile.environmentEntity != null && tile.environmentEntity.canTakeDamage )
			{
				int dist = Math.abs( tile.x - source.x ) + Math.abs( tile.y - source.y );

				if ( dist < closest )
				{
					closest = dist;
					bestTarget = tile;
				}
			}
		}

		if ( bestTarget == null )
		{
			bestTarget = newTile;
		}

		if ( bestTarget != null )
		{
			if ( bestTarget.entity != null && !bestTarget.entity.isAllies( entity ) )
			{
				entity.attack( bestTarget.entity, dir );
			}
			else if ( bestTarget.environmentEntity != null && !bestTarget.environmentEntity.passableBy.intersect( entity.getTravelType() ) )
			{
				entity.attack( bestTarget.environmentEntity, dir );
			}

			Sprite hitEffect = weapon.getWeaponHitEffect();

			Sprite sprite = hitEffect.copy();

			int[] diff = bestTarget.getPosDiff( source );
			float distMoved = (float) Math.sqrt( diff[0] * diff[0] + diff[1] * diff[1] ) / Global.TileSize;

			sprite.spriteAnimation = new MoveAnimation( 0.025f * distMoved, diff, MoveEquation.LINEAR );
			SpriteEffect e = new SpriteEffect( sprite, Direction.CENTER, null );

			// basis vector = 0, 1
			Vector2 direction = new Vector2( diff[0] * -1, diff[1] * -1 );
			direction.nor();
			double dot = 0 * direction.x + 1 * direction.y; // dot product
			double det = 0 * direction.y - 1 * direction.x; // determinant
			e.Sprite.rotation = (float) Math.atan2( det, dot ) * MathUtils.radiansToDegrees;

			bestTarget.spriteEffects.add( e );

			SoundInstance sound = hitEffect.sound;
			if ( sound != null )
			{
				sound.play( bestTarget );
			}
		}
	}
}
