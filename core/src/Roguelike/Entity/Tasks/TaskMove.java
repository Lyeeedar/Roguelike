package Roguelike.Entity.Tasks;

import java.util.Iterator;

import com.badlogic.gdx.utils.Array;

import Roguelike.Global.Direction;
import Roguelike.Items.Item;
import Roguelike.Items.Item.EquipmentSlot;
import Roguelike.Items.Item.WeaponType;
import Roguelike.RoguelikeGame;
import Roguelike.Entity.GameEntity;
import Roguelike.Shadows.ShadowCaster;
import Roguelike.Sprite.BumpAnimation;
import Roguelike.Sprite.MoveAnimation;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.SpriteAnimation;
import Roguelike.Sprite.SpriteEffect;
import Roguelike.Sprite.MoveAnimation.MoveEquation;
import Roguelike.Tiles.GameTile;

public class TaskMove extends AbstractTask
{
	Direction dir;		
	public TaskMove(Direction dir)
	{
		this.dir = dir;
	}
	
	@Override
	public float processTask(GameEntity obj)
	{		
		GameTile oldTile = obj.tile;
		
		int newX = oldTile.x+dir.GetX();
		int newY = oldTile.y+dir.GetY();
		
		GameTile newTile = oldTile.level.getGameTile(newX, newY);
		
		Item wep = obj.getInventory().getEquip(EquipmentSlot.MAINWEAPON);
		WeaponType type = WeaponType.NONE;
		
		if (wep != null)
		{
			type = wep.weaponType;
		}
		
		Array<GameTile> hitTiles = new Array<GameTile>();
		hitTiles.add(oldTile.level.getGameTile(newX, newY));
		
		if (type == WeaponType.SPEAR)
		{
			hitTiles.add(oldTile.level.getGameTile(
					oldTile.x+dir.GetX()*2, 
					oldTile.y+dir.GetY()*2
					));
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
		else if (type == WeaponType.BOW)
		{
			Direction anticlockwise = dir.GetAnticlockwise();
			Direction clockwise = dir.GetClockwise();
			
			int[] acwOffset = { dir.GetX() - anticlockwise.GetX(), dir.GetY() - anticlockwise.GetY() };
			int[] cwOffset = { dir.GetX() - clockwise.GetX(), dir.GetY() - clockwise.GetY() };
			
			hitTiles.add(oldTile.level.getGameTile(
					oldTile.x+anticlockwise.GetX(), 
					oldTile.y+anticlockwise.GetY()
					));
			
			hitTiles.add(oldTile.level.getGameTile(
					oldTile.x+clockwise.GetX(), 
					oldTile.y+clockwise.GetY()
					));
			
			for (int i = 2; i <= 4; i++)
			{
				int acx = oldTile.x + anticlockwise.GetX()*i;
				int acy = oldTile.y + anticlockwise.GetY()*i;
				
				int nx = oldTile.x + dir.GetX()*i;
				int ny = oldTile.y + dir.GetY()*i;
				
				int cx = oldTile.x + clockwise.GetX()*i;
				int cy = oldTile.y + clockwise.GetY()*i;
				
				// add base tiles
				hitTiles.add(oldTile.level.getGameTile(acx, acy));
				hitTiles.add(oldTile.level.getGameTile(nx, ny));
				hitTiles.add(oldTile.level.getGameTile(cx, cy));
				
				// add anticlockwise - mid
				int acwdiff = Math.max(Math.abs(acx - nx), Math.abs(acy - ny));
				for (int ii = 1; ii < acwdiff; ii++)
				{
					int px = nx + acwOffset[0] * ii;
					int py = ny + acwOffset[1] * ii;
					
					hitTiles.add(oldTile.level.getGameTile(px, py));
				}
				
				// add mid - clockwise
				int cwdiff = Math.max(Math.abs(cx - nx), Math.abs(cy - ny));
				for (int ii = 1; ii < cwdiff; ii++)
				{
					int px = nx + cwOffset[0] * ii;
					int py = ny + cwOffset[1] * ii;
					
					hitTiles.add(oldTile.level.getGameTile(px, py));
				}
			}
		}
		
		// Remove invisible tiles
		Array<int[]> visibleTiles = new Array<int[]>();
		ShadowCaster shadowCaster = new ShadowCaster(obj.tile.level.getGrid(), 4);
		shadowCaster.ComputeFOV(obj.tile.x, obj.tile.y, visibleTiles);
		
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
		
		boolean hitSomething = false;
		for (GameTile tile : hitTiles)
		{
			if (tile.entity != null && !tile.entity.isAllies(obj))
			{
				hitSomething = true;
				break;
			}
		}
		
		if (newTile.environmentEntity != null && !newTile.environmentEntity.passable)
		{
			hitSomething = true;
		}
		
		if (!hitSomething)
		{
			if (newTile.entity != null)
			{
				if (obj.isAllies(newTile.entity))
				{
					if (obj.canSwap && obj.canMove && newTile.entity.canMove)
					{
						int[] diff1 = oldTile.addObject(newTile.entity);
						int[] diff2 = newTile.addObject(obj);
						
						newTile.entity.sprite.SpriteAnimation = new MoveAnimation(0.05f, diff1, MoveEquation.SMOOTHSTEP);
						obj.sprite.SpriteAnimation = new MoveAnimation(0.05f, diff2, MoveEquation.SMOOTHSTEP);
					}
				}
			}
			else if (obj.canMove && newTile.getPassable(obj.factions))
			{
				int[] diff = newTile.addObject(obj);
				
				obj.sprite.SpriteAnimation = new MoveAnimation(0.05f, diff, MoveEquation.SMOOTHSTEP);
			}
		}
		else
		{
			// do damage
			
			if (type == WeaponType.BOW)
			{
				// if bow only hit closest
				
				GameTile bestTarget = null;
				
				// Find closest game entity first
				int closest = Integer.MAX_VALUE;
				for (GameTile tile : hitTiles)
				{
					if (tile.entity != null && !tile.entity.isAllies(obj))
					{
						int dist = Math.abs(tile.x - obj.tile.x) + Math.abs(tile.y - obj.tile.y);
						
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
					if (bestTarget.entity != null && !bestTarget.entity.isAllies(obj))
					{
						obj.attack(bestTarget.entity, dir);
					}
					else if (bestTarget.environmentEntity != null && !bestTarget.environmentEntity.passable)
					{
						obj.attack(bestTarget.environmentEntity, dir);
					}
					
					Item weapon = obj.getInventory().getEquip(EquipmentSlot.MAINWEAPON);
					Sprite hitEffect = weapon != null ? weapon.HitEffect : obj.defaultHitEffect;
					
					if (bestTarget.entity != null)
					{
						SpriteEffect e = new SpriteEffect(hitEffect.copy(), Direction.CENTER, null);
						e.Sprite.rotation = dir.GetAngle();

						bestTarget.entity.spriteEffects.add(e);
					}
					
					if (bestTarget.environmentEntity != null)
					{
						SpriteEffect e = new SpriteEffect(hitEffect.copy(), Direction.CENTER, null);
						e.Sprite.rotation = dir.GetAngle();

						bestTarget.environmentEntity.spriteEffects.add(e);
					}
					
					SpriteEffect e = new SpriteEffect(hitEffect.copy(), Direction.CENTER, null);
					e.Sprite.rotation = dir.GetAngle();

					bestTarget.spriteEffects.add(e);
				}
			}
			else
			{
				for (GameTile tile : hitTiles)
				{
					if (tile.entity != null && !tile.entity.isAllies(obj))
					{
						obj.attack(tile.entity, dir);
					}
					else if (tile.environmentEntity != null && !tile.environmentEntity.passable)
					{
						obj.attack(tile.environmentEntity, dir);
					}
				}
				
				Item weapon = obj.getInventory().getEquip(EquipmentSlot.MAINWEAPON);
				Sprite hitEffect = weapon != null ? weapon.HitEffect : obj.defaultHitEffect;
				
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
				}
			}
			
			// do graphics stuff
			obj.sprite.SpriteAnimation = new BumpAnimation(0.1f, dir, RoguelikeGame.TileSize);
		}
		
		return 1;
	}
}