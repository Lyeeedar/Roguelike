package Roguelike.Entity.Tasks;

import Roguelike.Global.Direction;
import Roguelike.Entity.Entity;
import Roguelike.Sprite.SpriteAnimation;
import Roguelike.Tiles.GameTile;

public class TaskAttack extends AbstractTask
{
	Direction Dir;		
	public TaskAttack(Direction dir)
	{
		this.Dir = dir;
	}

	@Override
	public float processTask(Entity obj)
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
			obj.attack(newTile.Entity, Dir);
			
			obj.Sprite.SpriteAnimation = new SpriteAnimation(Dir, 0.1f);
		}
		
		return 1.5f;
	}

}
