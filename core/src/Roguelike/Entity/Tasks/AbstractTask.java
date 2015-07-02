package Roguelike.Entity.Tasks;

import Roguelike.Entity.Entity;
import Roguelike.Tiles.GameTile;

public abstract class AbstractTask
{
	public abstract float processTask(Entity obj);
}
