package Roguelike.Entity.Tasks;

import Roguelike.Global.Direction;
import Roguelike.RoguelikeGame;
import Roguelike.Entity.GameEntity;
import Roguelike.Sprite.BumpAnimation;
import Roguelike.Sprite.MoveAnimation;
import Roguelike.Sprite.SpriteAnimation;
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
		
		if (
			newX < 0 ||
			newY < 0 ||
			newX >= oldTile.level.width-1 ||
			newY >= oldTile.level.height-1
			)
		{
			return 0;
		}
		
		GameTile newTile = oldTile.level.getGameTile(newX, newY);		
		
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
			else
			{
				obj.attack(newTile.entity, dir);				
				obj.sprite.SpriteAnimation = new BumpAnimation(0.1f, dir, RoguelikeGame.TileSize);
			}
		}
		else if (newTile.environmentEntity != null && !newTile.environmentEntity.passable)
		{
			obj.attack(newTile.environmentEntity, dir);
			obj.sprite.SpriteAnimation = new BumpAnimation(0.1f, dir, RoguelikeGame.TileSize);
		}
		else if (obj.canMove && newTile.getPassable(obj.factions))
		{
			int[] diff = newTile.addObject(obj);
			
			obj.sprite.SpriteAnimation = new MoveAnimation(0.05f, diff, MoveEquation.SMOOTHSTEP);
		}
		
		return 1;
	}
}