package Roguelike.DungeonGeneration;

import java.util.Random;

import Roguelike.Global.Direction;
import Roguelike.DungeonGeneration.RoomGenerators.MidpointDisplacement;
import Roguelike.Save.SaveLevel;
import Roguelike.Tiles.Point;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;

public class WorldMapGenerator extends AbstractDungeonGenerator
{
	// ####################################################################//
	// region Constructor

	// ----------------------------------------------------------------------
	public WorldMapGenerator()
	{

	}

	// endregion Constructor
	// ####################################################################//
	// region Public Methods

	// ----------------------------------------------------------------------
	@Override
	public boolean generate()
	{
		if ( generationIndex == 0 )
		{
			selectRooms();

			toBePlaced.clear();
			placedRooms.clear();

			toBePlaced.addAll( requiredRooms );

			generationIndex++;
			generationText = "Generating Grid";
		}
		else if ( generationIndex == 1 )
		{
			// Fill with values
			generateGrid();

			if ( isConnected() )
			{
				generationIndex++;
				generationText = "Filling with symbols";
			}
		}
		else if ( generationIndex == 2 )
		{
			// Convert to symbols
			generateSymbolGrid();

			generationIndex++;
			generationText = "Place rooms";
		}
		else if ( generationIndex == 3 )
		{
			// Place the rooms
			placeRooms();

			generationIndex++;
			generationText = "Creating level";
		}
		else if ( generationIndex == 4 )
		{
			// Create Level
			level = createLevel( symbolGrid, dfp.getSymbol( '0' ) );
			level.isVisionRestricted = false;

			generationIndex++;
			generationText = "Completed";
		}

		percent = (int) ( ( 100.0f / 5.0f ) * generationIndex );

		if ( generationIndex < 5 )
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	// ----------------------------------------------------------------------
	@Override
	public void setup( SaveLevel level, DungeonFileParser dfp )
	{
		this.saveLevel = level;
		this.dfp = dfp;

		ran = new Random( level.seed );
	}

	// endregion Public Methods
	// ####################################################################//
	// region Private Methods

	// ----------------------------------------------------------------------
	private void generateGrid()
	{
		MidpointDisplacement md = new MidpointDisplacement( ran );
		grid = md.getMap();

		width = grid.length;
		height = grid[0].length;
	}

	// ----------------------------------------------------------------------
	private boolean isConnected()
	{
		boolean[][] reached = new boolean[width][height];

		int x = 0;
		int y = 0;

		outer:
		for ( x = 0; x < width; x++ )
		{
			for ( y = 0; y < height; y++ )
			{
				if ( grid[x][y] >= 1 )
				{
					break outer;
				}
			}
		}

		Array<int[]> toBeProcessed = new Array<int[]>();
		toBeProcessed.add( new int[] { x, y } );

		while ( toBeProcessed.size > 0 )
		{
			int[] point = toBeProcessed.pop();
			x = point[0];
			y = point[1];

			if ( reached[x][y] )
			{
				continue;
			}

			reached[x][y] = true;

			for ( Direction dir : Direction.values() )
			{
				int nx = x + dir.getX();
				int ny = y + dir.getY();

				if ( grid[nx][ny] >= 1 )
				{
					toBeProcessed.add( new int[] { nx, ny } );
				}
			}
		}

		for ( x = 0; x < width; x++ )
		{
			for ( y = 0; y < height; y++ )
			{
				if ( grid[x][y] >= 1 && !reached[x][y] ) { return false; }
			}
		}

		return true;
	}

	// ----------------------------------------------------------------------
	private void generateSymbolGrid()
	{
		symbolGrid = new Symbol[width][height];
		for ( int x = 0; x < width; x++ )
		{
			for ( int y = 0; y < height; y++ )
			{
				symbolGrid[x][y] = dfp.getSymbol( Integer.toString( grid[x][y] ).charAt( 0 ) );

				if ( grid[x][y] == 4 )
				{
					symbolGrid[x][y].metaValue = "PlayerSpawn";
				}
			}
		}
	}

	// ----------------------------------------------------------------------
	private void placeRooms()
	{
		for ( Room room : toBePlaced )
		{
			int tile = room.roomData.placementHint != null ? Integer.parseInt( room.roomData.placementHint ) : 4;

			Array<Point> validTiles = new Array<Point>();

			for ( int x = 0; x < width; x++ )
			{
				for ( int y = 0; y < height; y++ )
				{
					if ( grid[x][y] == tile )
					{
						validTiles.add( Pools.obtain( Point.class ).set( x, y ) );
					}
				}
			}

			Point chosen = validTiles.get( ran.nextInt( validTiles.size ) );

			for ( int x = 0; x < room.width; x++ )
			{
				for ( int y = 0; y < room.height; y++ )
				{
					symbolGrid[x + chosen.x][y + chosen.y] = room.roomContents[x][y];
					symbolGrid[x + chosen.x][y + chosen.y].containingRoom = room;
				}
			}

			Pools.freeAll( validTiles );

			placedRooms.add( room );
		}

		toBePlaced.clear();
	}

	// endregion Private Methods
	// ####################################################################//
	// region Data

	private int[][] grid;
	private Symbol[][] symbolGrid;

	// endregion Data
	// ####################################################################//
}
