package Roguelike.Entity.Tasks;

import Roguelike.Entity.GameEntity;
import Roguelike.Tiles.GameTile;

public abstract class AbstractTask
{
	public abstract float processTask(GameEntity obj);
}
