package Roguelike.Entity.Tasks;

import Roguelike.Global.Direction;
import Roguelike.RoguelikeGame;
import Roguelike.Entity.GameEntity;
import Roguelike.Sprite.BumpAnimation;
import Roguelike.Sprite.SpriteAnimation;
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
			newX >= oldTile.Level.width-1 ||
			newY >= oldTile.Level.height-1
			)
		{
			return 0;
		}
		
		GameTile newTile = oldTile.Level.getGameTile(newX, newY);		
		
		if (newTile.Entity != null)
		{
			if (obj.isAllies(newTile.Entity))
			{
				if (obj.canSwap && obj.canMove && newTile.Entity.canMove)
				{
					oldTile.addObject(newTile.Entity);
					newTile.addObject(obj);
				}
			}
			else
			{
				obj.attack(newTile.Entity, dir);				
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
			newTile.addObject(obj);
		}
		
		return 1;
	}
}