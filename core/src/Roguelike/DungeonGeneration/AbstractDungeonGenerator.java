package Roguelike.DungeonGeneration;

import Roguelike.Levels.Level;

public abstract class AbstractDungeonGenerator
{
	public int percent;
	public String generationText = "Selecting Rooms";
	protected int generationIndex = 0;

	public abstract boolean generate();

	public abstract Level getLevel();
}
