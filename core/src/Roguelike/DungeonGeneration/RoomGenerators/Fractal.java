package Roguelike.DungeonGeneration.RoomGenerators;

import java.util.Random;

import Roguelike.DungeonGeneration.DungeonFileParser;
import Roguelike.DungeonGeneration.Symbol;

import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Fill the room with pools of terrain based on ranges within the output of simplex noise
 * @author Philip Collin
 *
 */
public class Fractal extends AbstractRoomGenerator
{
	private boolean isPointValid(float[][] grid, int x, int y, float minval, float maxval)
	{
		int width = grid.length;
		int height = grid[0].length;
		
		boolean center = grid[x][y] <= maxval && grid[x][y] >= minval;
		boolean top = y+1 < height && grid[x][y+1] <= maxval && grid[x][y+1] >= minval;
		boolean bottom = y-1 >= 0 && grid[x][y-1] <= maxval && grid[x][y-1] >= minval;
		boolean left = x-1 >= 0 && grid[x-1][y] <= maxval && grid[x-1][y] >= minval;
		boolean right = x+1 < width && grid[x+1][y] <= maxval && grid[x+1][y] >= minval;
		
		return center && (top || bottom || left || right);
	}
	
	public void process(Symbol[][] grid, Symbol floor, Symbol wall, Random ran, DungeonFileParser dfp)
	{
		int width = grid.length;
		int height = grid[0].length;
		
		NoiseGenerator noise = new NoiseGenerator(ran.nextInt(1000), 20, 0.8f, 8, 0.005f);
		float[][] simplexGrid = new float[width][height];
		
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				float noiseVal = noise.generate(x, y);
				noiseVal = (noiseVal + 1) / 2; // scale between 0 - 1;
				simplexGrid[x][y] = noiseVal;
			}
		}
		
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{				
				if (isPointValid(simplexGrid, x, y, 0, 0.6f))
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

	private static final class NoiseGenerator
	{
		float offset;
		float frequency;
		float amplitude;
		int octaves;
		float scale;

		public NoiseGenerator(float offset, float frequency, float amplitude, int octaves, float scale)
		{
			this.offset = offset;
			this.frequency = frequency;
			this.amplitude = amplitude;
			this.octaves = octaves;
			this.scale = scale;
		}

		public float generate(float x, float y)
		{
			return FastSimplexNoise.noise(x+offset, 0, y+offset, frequency, amplitude, octaves, scale, true, null);
		}
	}

	@Override
	public void parse(Element xml)
	{
	}
}
