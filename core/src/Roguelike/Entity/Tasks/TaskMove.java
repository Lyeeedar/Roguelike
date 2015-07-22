package Roguelike.Entity.Tasks;

import Roguelike.Global.Direction;
import Roguelike.RoguelikeGame;
import Roguelike.Entity.GameEntity;
import Roguelike.Sprite.BumpAnimation;
import Roguelike.Sprite.SpriteAnimation;
import Roguelike.Tiles.GameTile;

public class TaskMove extends AbstractTask
{
	Direction Dir;		
	public TaskMove(Direction dir)
	{
		this.Dir = dir;
	}
	
	@Override
	public float processTask(GameEntity obj)
	{
		obj.Channeling = null;
		
		GameTile oldTile = obj.Tile;
		
		int newX = oldTile.x+Dir.GetX();
		int newY = oldTile.y+Dir.GetY();
		
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
				if (obj.CanSwap)
				{
					oldTile.addObject(newTile.Entity);
					newTile.addObject(obj);
				}
			}
			else
			{
				obj.attack(newTile.Entity, Dir);				
				obj.Sprite.SpriteAnimation = new BumpAnimation(0.1f, Dir, RoguelikeGame.TileSize);
			}
		}
		else if (newTile.getPassable(obj.m_factions))
		{
			newTile.addObject(obj);
		}
		
		return 1;
	}
}