package Roguelike.DungeonGeneration.RoomGenerators;

import java.util.Random;

import Roguelike.DungeonGeneration.DungeonFileParser;
import Roguelike.DungeonGeneration.Symbol;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Seperates the room via Binary Space partitioning, then places doors to
 * connect the branches of the tree.
 * 
 * @author Philip Collin
 *
 */
public class Chambers extends AbstractRoomGenerator
{
	public Chambers()
	{
		this.ensuresConnectivity = true;
	}

	public static class BSPTree
	{
		public int x;
		public int y;
		public int width;
		public int height;

		public BSPTree( int x, int y, int width, int height )
		{
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}

		public BSPTree child1;
		public BSPTree child2;
		public boolean splitVertically;

		private static final int minSize = 5;
		private static final int maxSize = 20;

		public void partition( Random ran )
		{
			if ( ( width < minSize * 2 && height < minSize * 2 ) || ( width < maxSize && height < maxSize && ran.nextInt( 5 ) == 0 ) )
			{

			}
			else if ( width < minSize * 2 )
			{
				float split = 0.3f + ran.nextFloat() * 0.4f;
				int splitheight = (int) ( height * split );

				child1 = new BSPTree( x, y, width, splitheight );
				child2 = new BSPTree( x, y + splitheight, width, height - splitheight );

				splitVertically = true;

				child1.partition( ran );
				child2.partition( ran );

			}
			else if ( height < minSize * 2 )
			{
				float split = 0.3f + ran.nextFloat() * 0.4f;
				int splitwidth = (int) ( width * split );

				child1 = new BSPTree( x, y, splitwidth, height );
				child2 = new BSPTree( x + splitwidth, y, width - splitwidth, height );

				splitVertically = false;

				child1.partition( ran );
				child2.partition( ran );
			}
			else
			{
				boolean vertical = ran.nextBoolean();
				if ( vertical )
				{
					float split = 0.3f + ran.nextFloat() * 0.4f;
					int splitwidth = (int) ( width * split );

					child1 = new BSPTree( x, y, splitwidth, height );
					child2 = new BSPTree( x + splitwidth, y, width - splitwidth, height );

					splitVertically = false;

					child1.partition( ran );
					child2.partition( ran );
				}
				else
				{
					float split = 0.3f + ran.nextFloat() * 0.4f;
					int splitheight = (int) ( height * split );

					child1 = new BSPTree( x, y, width, splitheight );
					child2 = new BSPTree( x, y + splitheight, width, height - splitheight );

					splitVertically = true;

					child1.partition( ran );
					child2.partition( ran );
				}
			}
		}

		private void placeDoor( Symbol[][] grid, Symbol floor, Symbol wall, Symbol door, Random ran )
		{
			int gridWidth = grid.length;
			int gridHeight = grid[0].length;
			Array<int[]> possibleDoorTiles = new Array<int[]>();

			if ( splitVertically )
			{
				for ( int ix = 1; ix < width - 2; ix++ )
				{
					int tx = x + ix;
					int ty = child2.y;

					boolean valid = true;
					if ( valid )
					{
						int ttx = tx;
						int tty = ty - 1;
						if ( tty >= 0 && grid[ttx][tty] != floor )
						{
							valid = false;
						}
					}

					if ( valid )
					{
						int ttx = tx;
						int tty = ty + 1;
						if ( tty < gridHeight && grid[ttx][tty] != floor )
						{
							valid = false;
						}
					}

					if ( valid )
					{
						possibleDoorTiles.add( new int[] { tx, ty } );
					}
				}
			}
			else
			{
				for ( int iy = 1; iy < height - 2; iy++ )
				{
					int tx = child2.x;
					int ty = y + iy;

					boolean valid = true;
					if ( valid )
					{
						int ttx = tx - 1;
						int tty = ty;
						if ( ttx >= 0 && grid[ttx][tty] != floor )
						{
							valid = false;
						}
					}

					if ( valid )
					{
						int ttx = tx + 1;
						int tty = ty;
						if ( ttx < gridWidth && grid[ttx][tty] != floor )
						{
							valid = false;
						}
					}

					if ( valid )
					{
						possibleDoorTiles.add( new int[] { tx, ty } );
					}
				}
			}

			int[] doorPos = possibleDoorTiles.size > 0 ? possibleDoorTiles.removeIndex( ran.nextInt( possibleDoorTiles.size ) ) : null;

			if ( doorPos != null )
			{
				grid[doorPos[0]][doorPos[1]] = door;
			}
		}

		public void dig( Symbol[][] grid, Symbol floor, Symbol wall, Symbol door, Random ran )
		{
			if ( child1 != null )
			{
				child1.dig( grid, floor, wall, door, ran );
				child2.dig( grid, floor, wall, door, ran );
				placeDoor( grid, floor, wall, door, ran );
			}
			else
			{
				for ( int ix = 1; ix < width; ix++ )
				{
					for ( int iy = 1; iy < height; iy++ )
					{
						grid[x + ix][y + iy] = floor;
					}
				}
			}

		}
	}

	@Override
	public void process( Symbol[][] grid, Symbol floor, Symbol wall, Random ran, DungeonFileParser dfp )
	{
		BSPTree tree = new BSPTree( 0, 0, grid.length, grid[0].length );
		tree.partition( ran );
		tree.dig( grid, floor, wall, floor, ran );
	}

	@Override
	public void parse( Element xml )
	{
	}
}
