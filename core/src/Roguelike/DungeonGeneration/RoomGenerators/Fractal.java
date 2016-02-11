package Roguelike.DungeonGeneration.RoomGenerators;

import java.util.Random;

import Roguelike.DungeonGeneration.DungeonFileParser;
import Roguelike.DungeonGeneration.Symbol;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Fill the room with pools of terrain based on ranges within the output of
 * simplex noise
 *
 * @author Philip Collin
 *
 */
public class Fractal extends AbstractRoomGenerator
{
	private float frequency = 10;
	private float amplitude = 0.8f;
	private int octaves = 8;
	private float scale = 0.005f;
	private Array<FractalFeature> features = new Array<FractalFeature>();

	@Override
	public void process( Symbol[][] grid, Symbol floor, Symbol wall, Random ran, DungeonFileParser dfp )
	{
		int width = grid.length;
		int height = grid[0].length;

		for ( int x = 0; x < width; x++ )
		{
			for ( int y = 0; y < height; y++ )
			{
				grid[x][y] = wall;
			}
		}

		Chambers chambers = new Chambers();
		chambers.process( grid, floor, wall, ran, dfp );

		NoiseGenerator noise = new NoiseGenerator( ran.nextInt( 1000 ), frequency, amplitude, octaves, scale );
		float[][] simplexGrid = new float[width][height];

		float minVal = Float.MAX_VALUE;
		float maxVal = 0;

		for ( int x = 0; x < width; x++ )
		{
			for ( int y = 0; y < height; y++ )
			{
				float noiseVal = noise.generate( x, y );

				if ( noiseVal < minVal )
				{
					minVal = noiseVal;
				}
				if ( noiseVal > maxVal )
				{
					maxVal = noiseVal;
				}

				simplexGrid[x][y] = noiseVal;
			}
		}

		for ( int x = 0; x < width; x++ )
		{
			for ( int y = 0; y < height; y++ )
			{
				float val = simplexGrid[x][y];

				float zeroed = val - minVal;
				float alpha = zeroed / ( maxVal - minVal );

				simplexGrid[x][y] = alpha;
			}
		}

		for ( int x = 0; x < width; x++ )
		{
			for ( int y = 0; y < height; y++ )
			{
				float val = simplexGrid[x][y];
				for ( FractalFeature feature : features )
				{
					if ( val >= feature.minVal && val <= feature.maxVal )
					{
						grid[x][y] = feature.getAsSymbol( );
						break;
					}
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

		public NoiseGenerator( float offset, float frequency, float amplitude, int octaves, float scale )
		{
			this.offset = offset;
			this.frequency = frequency;
			this.amplitude = amplitude;
			this.octaves = octaves;
			this.scale = scale;
		}

		public float generate( float x, float y )
		{
			return FastSimplexNoise.noise( x + offset, 0, y + offset, frequency, amplitude, octaves, scale, true, null );
		}
	}

	// ----------------------------------------------------------------------
	public static class FractalFeature
	{
		public Symbol symbol;
		public float minVal;
		public float maxVal;

		public static FractalFeature load( Element xml )
		{
			FractalFeature feature = new FractalFeature();

			feature.symbol = Symbol.parse( xml );

			feature.minVal = xml.getFloat( "MinVal", 0 );
			feature.maxVal = xml.getFloat( "MaxVal", 1 );

			return feature;
		}

		public Symbol getAsSymbol( )
		{
			return symbol;
		}
	}

	@Override
	public void parse( Element xml )
	{
		frequency = xml.getFloat( "Frequency", 10 );
		amplitude = xml.getFloat( "Amplitude", 0.8f );
		octaves = xml.getInt( "Octaves", 8 );
		scale = xml.getFloat( "Scale", 0.005f );

		Element featuresElement = xml.getChildByName( "Features" );
		for ( int i = 0; i < featuresElement.getChildCount(); i++ )
		{
			Element featureElement = featuresElement.getChild( i );
			FractalFeature feature = FractalFeature.load( featureElement );
			features.add( feature );
		}
	}
}
