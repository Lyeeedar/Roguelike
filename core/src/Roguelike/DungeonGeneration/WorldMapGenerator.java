package Roguelike.DungeonGeneration;

import java.util.Random;

import Roguelike.AssetManager;
import Roguelike.Global.Passability;
import Roguelike.DungeonGeneration.RoomGenerators.MidpointDisplacement;
import Roguelike.Levels.Level;
import Roguelike.Save.SaveLevel;
import Roguelike.Tiles.GameTile;
import Roguelike.Tiles.TileData;

import com.badlogic.gdx.graphics.Color;

public class WorldMapGenerator extends AbstractDungeonGenerator
{
	// ####################################################################//
	// region Constructor

	// ----------------------------------------------------------------------
	public WorldMapGenerator( SaveLevel level )
	{
		this.saveLevel = level;

		ran = new Random( level.seed );
	}

	// endregion Constructor
	// ####################################################################//
	// region Public Methods

	@Override
	public boolean generate()
	{
		if ( generationIndex == 0 )
		{
			// Fill with values
			generateGrid();

			generationIndex++;
			generationText = "Creating Level";
		}
		else if ( generationIndex == 1 )
		{
			// Create Level
			createLevel();

			generationIndex++;
			generationText = "Completed";
		}

		percent = (int) ( ( 100.0f / 2.0f ) * generationIndex );

		if ( generationIndex < 2 )
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
	public Level getLevel()
	{
		return level;
	}

	// endregion Public Methods
	// ####################################################################//
	// region Private Methods

	// ----------------------------------------------------------------------
	private static int[][] minimiseGrid( int[][] grid )
	{
		int width = grid.length;
		int height = grid[0].length;

		int minx = -1;
		int miny = -1;
		int maxx = -1;
		int maxy = -1;

		boolean complete = false;

		// find min x
		for ( int x = 0; x < width; x++ )
		{
			for ( int y = 0; y < height; y++ )
			{
				int s = grid[x][y];
				if ( s != 0 )
				{
					minx = x - 1;

					complete = true;
					break;
				}
			}
			if ( complete )
			{
				break;
			}
		}
		if ( minx == -1 ) { return grid; }

		// find min y
		complete = false;
		for ( int y = 0; y < height; y++ )
		{
			for ( int x = minx; x < width; x++ )
			{
				int s = grid[x][y];
				if ( s != 0 )
				{
					miny = y - 1;

					complete = true;
					break;
				}
			}
			if ( complete )
			{
				break;
			}
		}
		if ( miny == -1 ) { return grid; }

		// find max x
		complete = false;
		for ( int x = width - 1; x >= minx; x-- )
		{
			for ( int y = miny; y < height; y++ )
			{
				int s = grid[x][y];
				if ( s != 0 )
				{
					maxx = x + 2;

					complete = true;
					break;
				}
			}
			if ( complete )
			{
				break;
			}
		}
		if ( maxx == -1 ) { return grid; }

		// find max y
		complete = false;
		for ( int y = height - 1; y >= miny; y-- )
		{
			for ( int x = minx; x < maxx; x++ )
			{
				int s = grid[x][y];
				if ( s != 0 )
				{
					maxy = y + 2;

					complete = true;
					break;
				}
			}
			if ( complete )
			{
				break;
			}
		}
		if ( maxy == -1 ) { return grid; }

		// minimise room
		int newwidth = maxx - minx;
		int newheight = maxy - miny;

		int[][] newgrid = new int[newwidth][newheight];

		for ( int x = 0; x < newwidth; x++ )
		{
			for ( int y = 0; y < newheight; y++ )
			{
				newgrid[x][y] = grid[minx + x][miny + y];
			}
		}

		return newgrid;
	}

	// ----------------------------------------------------------------------
	private void generateGrid()
	{
		MidpointDisplacement md = new MidpointDisplacement( ran );
		grid = md.getMap();
		grid = minimiseGrid( grid );
	}

	// ----------------------------------------------------------------------
	private void createLevel()
	{
		// minimise
		int width = grid.length;
		int height = grid[0].length;

		GameTile[][] actualTiles = new GameTile[width][height];
		level = new Level( actualTiles );
		level.Ambient = new Color( 1, 1, 1, 1 );
		level.affectedByDayNight = false;
		level.bgmName = "Heroic Age";

		level.depth = saveLevel.depth;
		level.fileName = saveLevel.fileName;
		level.seed = saveLevel.seed;

		level.isVisionRestricted = false;

		TileData[] tiles = {
				new TileData( Passability.parse( "light" ), AssetManager.loadSprite( "Level/WorldMap/DeepSea" ) ),
				new TileData( Passability.parse( "light" ), AssetManager.loadSprite( "Level/WorldMap/ShallowSea" ) ),
				new TileData( Passability.parse( "true" ), AssetManager.loadSprite( "Level/WorldMap/Desert" ) ),
				new TileData( Passability.parse( "true" ), AssetManager.loadSprite( "Level/WorldMap/Plains" ) ),
				new TileData( Passability.parse( "true" ), AssetManager.loadSprite( "Level/WorldMap/Grassland" ) ),
				new TileData( Passability.parse( "true" ), AssetManager.loadSprite( "Level/WorldMap/Forest" ) ),
				new TileData( Passability.parse( "true" ), AssetManager.loadSprite( "Level/WorldMap/Hills" ) ),
				new TileData( Passability.parse( "light" ), AssetManager.loadSprite( "Level/WorldMap/mountains" ) ),
				new TileData( Passability.parse( "light" ), AssetManager.loadSprite( "Level/WorldMap/mountains" ) ) };

		for ( int x = 0; x < width; x++ )
		{
			for ( int y = 0; y < height; y++ )
			{
				int val = grid[x][y];

				GameTile newTile = new GameTile( x, y, level, tiles[val] );

				if ( val == 3 )
				{
					newTile.metaValue = "PlayerSpawn";
				}

				actualTiles[x][y] = newTile;
			}
		}

		saveLevel.addSavedLevelContents( level );

		level.depth = saveLevel.depth;
		level.UID = saveLevel.UID;

		level.calculateAmbient();
	}

	// endregion Private Methods
	// ####################################################################//
	// region Data

	private Level level;
	private SaveLevel saveLevel;
	private Random ran;
	private int[][] grid;

	// endregion Data
	// ####################################################################//
}
