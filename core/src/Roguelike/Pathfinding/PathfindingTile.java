package Roguelike.Pathfinding;

import java.util.HashSet;

import Roguelike.Global.Passability;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public interface PathfindingTile
{
	public boolean getPassable(Array<Passability> travelType);
	public int getInfluence();
}
