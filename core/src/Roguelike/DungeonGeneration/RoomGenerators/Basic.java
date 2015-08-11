package Roguelike.DungeonGeneration.RoomGenerators;

import java.util.Random;

import Roguelike.DungeonGeneration.DungeonFileParser;
import Roguelike.DungeonGeneration.Symbol;

import com.badlogic.gdx.utils.XmlReader.Element;

public class Basic extends AbstractRoomGenerator
{
	@Override
	public void process(Symbol[][] grid, Symbol floor, Symbol wall, Random ran, DungeonFileParser dfp)
	{
		int width = grid.length;
		int height = grid[0].length;
		
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				if (x == 0 || x == width-1 || y == 0 || y == height-1)
				{
					grid[x][y] = wall;
				}
				else
				{
					grid[x][y] = floor;
				}
			}
		}
	}

	@Override
	public void parse(Element xml)
	{
	}

}
