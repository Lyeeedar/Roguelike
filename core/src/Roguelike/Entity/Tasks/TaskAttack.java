package Roguelike.Entity.Tasks;

import java.util.Iterator;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import Roguelike.Global;
import Roguelike.Entity.GameEntity;
import Roguelike.Global.Direction;
import Roguelike.Global.Passability;
import Roguelike.Global.Statistic;
import Roguelike.Items.Item;
import Roguelike.Items.Item.EquipmentSlot;
import Roguelike.Items.Item.WeaponType;
import Roguelike.Shadows.ShadowCaster;
import Roguelike.Sound.SoundInstance;
import Roguelike.Sprite.BumpAnimation;
import Roguelike.Sprite.MoveAnimation;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.SpriteEffect;
import Roguelike.Sprite.MoveAnimation.MoveEquation;
import Roguelike.Tiles.GameTile;

public class TaskAttack extends AbstractTask
{
	public Direction dir;

	public TaskAttack(Direction dir)
	{
		this.dir = dir;
	}

	public boolean checkHitSomething(GameEntity obj)
	{
		// Collect data
		GameTile oldTile = obj.tile;

		int newX = oldTile.x+dir.GetX();
		int newY = oldTile.y+dir.GetY();

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

		if (newTile.environmentEntity != null && !Passability.isPassable(newTile.environmentEntity.passableBy, obj.getTravelType()))
		{
			hitSomething = true;
		}

		return hitSomething;
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
			hitEffect = WeaponType.NONE.hitSprite;
		}
		else
		{
			hitEffect = weapon.hitEffect != null ? weapon.hitEffect : weapon.weaponType.hitSprite;
		}

		// add hit effects
		for (GameTile tile : hitTiles)
		{
			if (tile.entity != null)
			{
				SpriteEffect e = new SpriteEffect(hitEffect.copy(), Direction.CENTER, null);
				e.Sprite.rotation = dir.GetAngle();

				tile.entity.spriteEffects.add(e);
			}

			if (tile.environmentEntity != null)
			{
				SpriteEffect e = new SpriteEffect(hitEffect.copy(), Direction.CENTER, null);
				e.Sprite.rotation = dir.GetAngle();

				tile.environmentEntity.spriteEffects.add(e);
			}

			SpriteEffect e = new SpriteEffect(hitEffect.copy(), Direction.CENTER, null);
			e.Sprite.rotation = dir.GetAngle();

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

			Sprite hitEffect = weapon.hitEffect != null ? weapon.hitEffect : weapon.weaponType.hitSprite;

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
		WeaponType type = WeaponType.NONE;
		int range = 1;

		if (weapon != null)
		{
			type = weapon.weaponType;

			range = weapon.getStatistic(entity.getBaseVariableMap(), Statistic.RANGE);
			if (range == 0) 
			{ 
				if (type == WeaponType.SPEAR)
				{
					range = 2;
				}
				else if (type == WeaponType.BOW || type == WeaponType.WAND)
				{
					range = 4;
				}
				else
				{
					range = 1;
				}
			}
		}

		Array<GameTile> hitTiles = new Array<GameTile>();
		hitTiles.add(newTile);

		if (type == WeaponType.SPEAR)
		{
			for (int i = 1; i < range; i++)
			{
				hitTiles.add(oldTile.level.getGameTile(
						oldTile.x+dir.GetX()*(i+1), 
						oldTile.y+dir.GetY()*(i+1)
						));
			}
		}
		else if (type == WeaponType.AXE)
		{
			Direction anticlockwise = dir.GetAnticlockwise();
			Direction clockwise = dir.GetClockwise();

			hitTiles.add(oldTile.level.getGameTile(
					oldTile.x+anticlockwise.GetX(), 
					oldTile.y+anticlockwise.GetY()
					));

			hitTiles.add(oldTile.level.getGameTile(
					oldTile.x+clockwise.GetX(), 
					oldTile.y+clockwise.GetY()
					));
		}
		else if (type == WeaponType.BOW || type == WeaponType.WAND)
		{
			//			Direction anticlockwise = dir.GetAnticlockwise();
			//			Direction clockwise = dir.GetClockwise();
			//						
			//			hitTiles.add(oldTile.level.getGameTile(
			//					oldTile.x+anticlockwise.GetX(), 
			//					oldTile.y+anticlockwise.GetY()
			//					));
			//			
			//			hitTiles.add(oldTile.level.getGameTile(
			//					oldTile.x+clockwise.GetX(), 
			//					oldTile.y+clockwise.GetY()
			//					));

			for (int i = 2; i <= range; i++)
			{
				//				int acx = oldTile.x + anticlockwise.GetX()*i;
				//				int acy = oldTile.y + anticlockwise.GetY()*i;
				//				
				int nx = oldTile.x + dir.GetX()*i;
				int ny = oldTile.y + dir.GetY()*i;
				//				
				//				int cx = oldTile.x + clockwise.GetX()*i;
				//				int cy = oldTile.y + clockwise.GetY()*i;

				//hitTiles.add(oldTile.level.getGameTile(acx, acy));
				hitTiles.add(oldTile.level.getGameTile(nx, ny));
				//hitTiles.add(oldTile.level.getGameTile(cx, cy));
			}
		}

		// Remove invisible tiles
		Array<int[]> visibleTiles = new Array<int[]>();
		ShadowCaster shadowCaster = new ShadowCaster(entity.tile.level.getGrid(), range);
		shadowCaster.ComputeFOV(entity.tile.x, entity.tile.y, visibleTiles);

		Iterator<GameTile> itr = hitTiles.iterator();
		while (itr.hasNext())
		{
			GameTile tile = itr.next();

			if (tile != null)
			{
				boolean visible = false;
				for (int[] pos : visibleTiles)
				{
					if (pos[0] == tile.x && pos[1] == tile.y)
					{
						visible = true;
						break;
					}
				}

				if (!visible)
				{
					itr.remove();
				}
			}
			else
			{
				itr.remove();
			}
		}

		return hitTiles;
	}

	@Override
	public void processTask(GameEntity obj)
	{
		// Collect data
		GameTile oldTile = obj.tile;

		int newX = oldTile.x+dir.GetX();
		int newY = oldTile.y+dir.GetY();

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

		if (newTile.environmentEntity != null && !Passability.isPassable(newTile.environmentEntity.passableBy, obj.getTravelType()))
		{
			hitSomething = true;
		}

		// Do attack
		if (hitSomething)
		{			
			if (wep != null && (wep.weaponType == WeaponType.BOW || wep.weaponType == WeaponType.WAND))
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
