package Roguelike.Pathfinding;

import java.util.HashSet;

public interface PathfindingTile
{
	public boolean getPassable(HashSet<String> factions);
	public int getInfluence();
}
