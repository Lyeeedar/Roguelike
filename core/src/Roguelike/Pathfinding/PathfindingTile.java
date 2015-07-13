package Roguelike.Pathfinding;

import java.util.HashSet;

public interface PathfindingTile
{
	public boolean GetPassable(HashSet<String> factions);
}
