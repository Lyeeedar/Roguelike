package Roguelike.DungeonGeneration.RoomGenerators;

import Roguelike.DungeonGeneration.DungeonFileParser;
import Roguelike.DungeonGeneration.Symbol;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;

import java.util.Random;

/**
 * Created by Philip on 11-Feb-16.
 */
public class Island extends AbstractRoomGenerator
{
	private Array<Fractal.FractalFeature> features = new Array<Fractal.FractalFeature>();

	@Override
	public void process( Symbol[][] grid, Symbol floor, Symbol wall, Random ran, DungeonFileParser dfp )
	{
		int width = grid.length;
		int height = grid[0].length;

		MidpointDisplacement midpointDisplacement = new MidpointDisplacement( ran, width, height );

		float[][] map = midpointDisplacement.getMap();

		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				float val = map[x][y];
				Symbol symbol = null;
				for ( Fractal.FractalFeature feature : features )
				{
					if (feature.minVal <= val && feature.maxVal >= val)
					{
						symbol = feature.getAsSymbol( );
						break;
					}
				}

				if (symbol != null)
				{
					symbol.resolveExtends( dfp.sharedSymbolMap );
					grid[x][y] = symbol;
				}
			}
		}
	}

	@Override
	public void parse( XmlReader.Element xml )
	{
		for ( int i = 0; i < xml.getChildCount(); i++ )
		{
			XmlReader.Element featureElement = xml.getChild( i );
			Fractal.FractalFeature feature = Fractal.FractalFeature.load( featureElement );
			features.add( feature );
		}
	}
}
