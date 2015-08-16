package Roguelike.Entity.Tasks;

import Roguelike.Global;
import Roguelike.Global.Direction;
import Roguelike.Global.Passability;
import Roguelike.Global.Statistic;
import Roguelike.Entity.GameEntity;
import Roguelike.Items.Item;
import Roguelike.Items.Item.EquipmentSlot;
import Roguelike.Sound.SoundInstance;
import Roguelike.Sprite.BumpAnimation;
import Roguelike.Sprite.MoveAnimation;
import Roguelike.Sprite.MoveAnimation.MoveEquation;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.SpriteEffect;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class TaskAttack extends AbstractTask
{
	private static final Array<Passability> WeaponPassability = new Array<Passability>(new Passability[]{Passability.LIGHT});
	
	public Direction dir;

	public TaskAttack(Direction dir)
	{
		this.dir = dir;
	}

	public boolean checkHitSomething(GameEntity obj)
	{
		// Collect data
		GameTile oldTile = obj.tile;

		int newX = oldTile.x+dir.getX();
		int newY = oldTile.y+dir.getY();

		GameTile newTile = oldTile.level.getGameTile(newX, newY);

		Item wep = obj.getInventory().getEquip(EquipmentSlot.MAINWEAPON);

		Array<GameTile> hitTiles = getHitTiles(oldTile, newTile, obj, wep);

		// Check if should attack something
		boolean hitSomething = false;
		for (GameTile tile : hitTiles)
		{
			if (tile.entity != null && !tile.entity.isAllies(obj))
			{
				hitSomething = true;
				break;
			}
		}

//		if (newTile.environmentEntity != null && newTile.environmentEntity.canTakeDamage && !Passability.isPassable(newTile.environmentEntity.passableBy, obj.getTravelType()))
//		{
//			hitSomething = true;
//		}

		return hitSomething;
	}
	
	public boolean canAttackTile(GameEntity obj, GameTile tile)
	{
		// Collect data
		GameTile oldTile = obj.tile;

		int newX = oldTile.x+dir.getX();
		int newY = oldTile.y+dir.getY();

		GameTile newTile = oldTile.level.getGameTile(newX, newY);

		Item wep = obj.getInventory().getEquip(EquipmentSlot.MAINWEAPON);

		Array<GameTile> hitTiles = getHitTiles(oldTile, newTile, obj, wep);

		for (GameTile t : hitTiles)
		{
			if (t == tile) { return true; }
		}
		
		return false;
	}

	private void doNormalAttack(Array<GameTile> hitTiles, GameEntity entity, Item weapon)
	{
		// hit all tiles
		for (GameTile tile : hitTiles)
		{
			if (tile.entity != null && !tile.entity.isAllies(entity))
			{
				entity.attack(tile.entity, dir);
			}
			else if (tile.environmentEntity != null && !Passability.isPassable(tile.environmentEntity.passableBy, entity.getTravelType()))
			{
				entity.attack(tile.environmentEntity, dir);
			}
		}

		Sprite hitEffect = null;
		if (weapon == null)
		{
			hitEffect = entity.defaultHitEffect;
		}
		else
		{
			hitEffect = weapon.getWeaponHitEffect();
		}

		// add hit effects
		for (GameTile tile : hitTiles)
		{
			if (tile.entity != null)
			{
				SpriteEffect e = new SpriteEffect(hitEffect.copy(), Direction.CENTER, null);
				e.Sprite.rotation = dir.getAngle();

				tile.entity.spriteEffects.add(e);
			}

			if (tile.environmentEntity != null)
			{
				SpriteEffect e = new SpriteEffect(hitEffect.copy(), Direction.CENTER, null);
				e.Sprite.rotation = dir.getAngle();

				tile.environmentEntity.spriteEffects.add(e);
			}

			SpriteEffect e = new SpriteEffect(hitEffect.copy(), Direction.CENTER, null);
			e.Sprite.rotation = dir.getAngle();

			tile.spriteEffects.add(e);
			
			SoundInstance sound = hitEffect.sound;
			if (sound != null) { sound.play(tile); }
		}
	}

	private void doRangedAttack(Array<GameTile> hitTiles, GameTile newTile, GameEntity entity, Item weapon)
	{
		// if bow only hit closest

		GameTile bestTarget = null;

		// Find closest game entity first
		int closest = Integer.MAX_VALUE;
		for (GameTile tile : hitTiles)
		{
			if (tile.entity != null && !tile.entity.isAllies(entity))
			{
				int dist = Math.abs(tile.x - entity.tile.x) + Math.abs(tile.y - entity.tile.y);

				if (dist < closest)
				{
					closest = dist;
					bestTarget = tile;
				}
			}
			else if (tile.environmentEntity != null && tile.environmentEntity.canTakeDamage)
			{
				int dist = Math.abs(tile.x - entity.tile.x) + Math.abs(tile.y - entity.tile.y);

				if (dist < closest)
				{
					closest = dist;
					bestTarget = tile;
				}
			}
		}

		if (bestTarget == null)
		{
			bestTarget = newTile;
		}

		if (bestTarget != null)
		{
			if (bestTarget.entity != null && !bestTarget.entity.isAllies(entity))
			{
				entity.attack(bestTarget.entity, dir);
			}
			else if (bestTarget.environmentEntity != null && !Passability.isPassable(bestTarget.environmentEntity.passableBy, entity.getTravelType()))
			{
				entity.attack(bestTarget.environmentEntity, dir);
			}

			Sprite hitEffect = weapon.getWeaponHitEffect();

			Sprite sprite = hitEffect.copy();

			int[] diff = bestTarget.getPosDiff(entity.tile);
			float distMoved = (float)Math.sqrt(diff[0]*diff[0] + diff[1]*diff[1]) / (float)Global.TileSize;

			sprite.spriteAnimation = new MoveAnimation(0.025f * distMoved, diff, MoveEquation.LINEAR);
			SpriteEffect e = new SpriteEffect(sprite, Direction.CENTER, null);

			// basis vector = 0, 1
			Vector2 direction = new Vector2(diff[0]*-1, diff[1]*-1);
			direction.nor();
			double dot = 0*direction.x + 1*direction.y; // dot product
			double det = 0*direction.y - 1*direction.x; // determinant
			e.Sprite.rotation = (float) Math.atan2(det, dot) * MathUtils.radiansToDegrees;

			bestTarget.spriteEffects.add(e);
			
			SoundInstance sound = hitEffect.sound;
			if (sound != null) { sound.play(bestTarget); }
		}
	}

	private Array<GameTile> getHitTiles(GameTile oldTile, GameTile newTile, GameEntity entity, Item weapon)
	{		
		String type = "none";
		int range = 1;

		if (weapon != null)
		{
			type = weapon.type;
			range = weapon.getRange(entity);
		}

		Array<GameTile> hitTiles = new Array<GameTile>();
		hitTiles.add(newTile);

		if (type.equals("axe"))
		{
			Direction anticlockwise = dir.getAnticlockwise();
			Direction clockwise = dir.getClockwise();

			hitTiles.add(oldTile.level.getGameTile(
					oldTile.x+anticlockwise.getX(), 
					oldTile.y+anticlockwise.getY()
					));

			hitTiles.add(oldTile.level.getGameTile(
					oldTile.x+clockwise.getX(), 
					oldTile.y+clockwise.getY()
					));
		}
		else if (type.equals("spear") || type.equals("bow") || type.equals("wand"))
		{
			for (int i = 2; i <= range; i++)
			{			
				int nx = oldTile.x + dir.getX()*i;
				int ny = oldTile.y + dir.getY()*i;

				GameTile tile = oldTile.level.getGameTile(nx, ny);
				
				hitTiles.add(tile);
				
				if (!tile.getPassable(WeaponPassability)) { break; }
			}
		}

		return hitTiles;
	}

	@Override
	public void processTask(GameEntity obj)
	{
		// Collect data
		GameTile oldTile = obj.tile;

		int newX = oldTile.x+dir.getX();
		int newY = oldTile.y+dir.getY();

		GameTile newTile = oldTile.level.getGameTile(newX, newY);

		Item wep = obj.getInventory().getEquip(EquipmentSlot.MAINWEAPON);

		Array<GameTile> hitTiles = getHitTiles(oldTile, newTile, obj, wep);

		// Check if should attack something
		boolean hitSomething = false;
		for (GameTile tile : hitTiles)
		{
			if (tile.entity != null && !tile.entity.isAllies(obj))
			{
				hitSomething = true;
				break;
			}
			
			if (tile.environmentEntity != null && tile.environmentEntity.canTakeDamage && !Passability.isPassable(tile.environmentEntity.passableBy, obj.getTravelType()))
			{
				hitSomething = true;
			}
		}

		// Do attack
		if (hitSomething)
		{			
			if (wep != null && (wep.type.equals("bow") || wep.type.equals("wand")))
			{
				doRangedAttack(hitTiles, newTile, obj, wep);
			}
			else
			{
				doNormalAttack(hitTiles, obj, wep);
			}

			// do graphics stuff
			obj.sprite.spriteAnimation = new BumpAnimation(0.1f, dir, Global.TileSize);
		}
	}
}
