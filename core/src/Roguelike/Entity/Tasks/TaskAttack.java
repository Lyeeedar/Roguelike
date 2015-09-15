package Roguelike.Entity.Tasks;

import Roguelike.Global;
import Roguelike.Global.Direction;
import Roguelike.Global.Passability;
import Roguelike.Entity.GameEntity;
import Roguelike.Items.Item;
import Roguelike.Items.Item.EquipmentSlot;
import Roguelike.Sound.SoundInstance;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.SpriteEffect;
import Roguelike.Sprite.SpriteAnimation.BumpAnimation;
import Roguelike.Sprite.SpriteAnimation.MoveAnimation;
import Roguelike.Sprite.SpriteAnimation.MoveAnimation.MoveEquation;
import Roguelike.Tiles.GameTile;
import Roguelike.Util.EnumBitflag;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
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
		if ( dir.isCardinal() )
		{
			Item wep = attacker.getInventory().getEquip( EquipmentSlot.MAINWEAPON );

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

			Array<GameTile> hitTiles = new Array<GameTile>();

			for ( int i = 0; i < attacker.size; i++ )
			{
				GameTile oldTile = attacker.tile[sx + xstep * i][sy + ystep * i];

				int newX = oldTile.x + dir.getX();
				int newY = oldTile.y + dir.getY();

				GameTile newTile = oldTile.level.getGameTile( newX, newY );

				hitTiles.addAll( getHitTiles( oldTile, newTile, attacker, wep ) );
			}

			return hitTiles;
		}
		else
		{
			// Collect data
			GameTile oldTile = attacker.tile[Math.max( 0, dir.getX() ) * ( attacker.size - 1 )][Math.max( 0, dir.getY() ) * ( attacker.size - 1 )];

			int newX = oldTile.x + dir.getX();
			int newY = oldTile.y + dir.getY();

			GameTile newTile = oldTile.level.getGameTile( newX, newY );

			Item wep = attacker.getInventory().getEquip( EquipmentSlot.MAINWEAPON );

			Array<GameTile> hitTiles = getHitTiles( oldTile, newTile, attacker, wep );
			return hitTiles;
		}
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
					cx = entity.tile[0][0].x + x;
					cy = entity.tile[0][0].y + y;
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

	private Array<GameTile> getHitTiles( GameTile oldTile, GameTile newTile, GameEntity entity, Item weapon )
	{
		String type = "none";
		int range = 1;

		if ( weapon != null )
		{
			type = weapon.type;
			range = weapon.getRange( entity );
		}

		Array<GameTile> hitTiles = new Array<GameTile>();
		hitTiles.add( newTile );

		if ( type.equals( "axe" ) )
		{
			Direction anticlockwise = dir.getAnticlockwise();
			Direction clockwise = dir.getClockwise();

			hitTiles.add( oldTile.level.getGameTile( oldTile.x + anticlockwise.getX(), oldTile.y + anticlockwise.getY() ) );

			hitTiles.add( oldTile.level.getGameTile( oldTile.x + clockwise.getX(), oldTile.y + clockwise.getY() ) );
		}
		else if ( type.equals( "spear" ) || type.equals( "bow" ) || type.equals( "wand" ) )
		{
			for ( int i = 2; i <= range; i++ )
			{
				int nx = oldTile.x + dir.getX() * i;
				int ny = oldTile.y + dir.getY() * i;

				GameTile tile = oldTile.level.getGameTile( nx, ny );

				hitTiles.add( tile );

				if ( !tile.getPassable( WeaponPassability, entity ) )
				{
					break;
				}
			}
		}

		return hitTiles;
	}

	@Override
	public void processTask( GameEntity obj )
	{
		// Collect data
		GameTile oldTile = obj.tile[0][0];

		int newX = oldTile.x + dir.getX();
		int newY = oldTile.y + dir.getY();

		GameTile newTile = oldTile.level.getGameTile( newX, newY );

		Item wep = obj.getInventory().getEquip( EquipmentSlot.MAINWEAPON );

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
}
