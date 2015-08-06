package Roguelike.Entity.Tasks;

import java.util.Iterator;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import Roguelike.Global;
import Roguelike.Global.Direction;
import Roguelike.Global.Statistic;
import Roguelike.Items.Item;
import Roguelike.Items.Item.EquipmentSlot;
import Roguelike.Items.Item.WeaponType;
import Roguelike.RoguelikeGame;
import Roguelike.Entity.GameEntity;
import Roguelike.Pathfinding.ShadowCaster;
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
	public void processTask(GameEntity obj)
	{		
		// Collect data
		GameTile oldTile = obj.tile;
		
		int newX = oldTile.x+dir.GetX();
		int newY = oldTile.y+dir.GetY();
		
		GameTile newTile = oldTile.level.getGameTile(newX, newY);
		
		if (newTile.entity != null)
		{
			// Swap positions if possible
			if (obj.isAllies(newTile.entity))
			{
				if (obj.canSwap && obj.canMove && newTile.entity.canMove)
				{
					int[] diff1 = oldTile.addGameEntity(newTile.entity);
					int[] diff2 = newTile.addGameEntity(obj);
					
					newTile.entity.sprite.spriteAnimation = new MoveAnimation(0.1f, diff1, MoveEquation.SMOOTHSTEP);
					obj.sprite.spriteAnimation = new MoveAnimation(0.1f, diff2, MoveEquation.SMOOTHSTEP);
				}
			}
		}
		else if (obj.canMove && newTile.getPassable(obj.getTravelType()))
		{
			int[] diff = newTile.addGameEntity(obj);
			
			obj.sprite.spriteAnimation = new MoveAnimation(0.1f, diff, MoveEquation.SMOOTHSTEP);
		}
	}
}