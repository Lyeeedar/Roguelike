package Roguelike.DungeonGeneration.RoomGenerators;

import java.util.Random;

import Roguelike.DungeonGeneration.DungeonFileParser;
import Roguelike.DungeonGeneration.Symbol;

import com.badlogic.gdx.utils.XmlReader.Element;

public class RandomPlace extends AbstractRoomGenerator
{

	@Override
	public void process( Symbol[][] grid, Symbol floor, Symbol wall, Random ran, DungeonFileParser dfp )
	{
		for ( int x = 0; x < grid.length; x++ )
		{
			for ( int y = 0; y < grid[0].length; y++ )
			{
				grid[x][y] = ran.nextBoolean() ? floor : wall;
			}
		}
	}

	@Override
	public void parse( Element xml )
	{
	}

}
