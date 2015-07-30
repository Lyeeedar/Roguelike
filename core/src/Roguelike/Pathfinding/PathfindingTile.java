package Roguelike.Pathfinding;

import java.util.HashSet;

public interface PathfindingTile
{
	public boolean getPassable();
	public int getInfluence();
}
