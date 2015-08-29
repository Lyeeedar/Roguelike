package Roguelike.Lights;

import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.XmlReader.Element;

public class AmbientShadow
{
	public float factor;
	public int range;

	public void apply( GameTile[][] grid, int px, int py )
	{
		for ( int x = px - range; x < px + range; x++ )
		{
			for ( int y = py - range; y < py + range; y++ )
			{
				if ( x < 0 || y < 0 || x >= grid.length || y >= grid[0].length )
				{
					continue;
				}

				GameTile tile = grid[x][y];

				float dst = 1 - Vector2.dst2( px, py, tile.x, tile.y ) / ( range * range );
				if ( dst < 0 )
				{
					dst = 0;
				}

				float scaleVal = 1.0f - factor * dst;

				tile.ambientColour.mul( scaleVal );
			}
		}
	}

	public void parse( Element xml )
	{
		factor = xml.getFloat( "Factor", 0.5f );
		range = xml.getInt( "Range", 2 );
	}

	public static AmbientShadow load( Element xml )
	{
		AmbientShadow shadow = new AmbientShadow();

		shadow.parse( xml );

		return shadow;
	}
}
