package Roguelike.Entity.Tasks;

import Roguelike.Entity.GameEntity;
import Roguelike.Tiles.GameTile;

public abstract class AbstractTask
{
	public boolean cancel = false;
	public float cost = 1;
	
	public abstract void processTask(GameEntity obj);
}
