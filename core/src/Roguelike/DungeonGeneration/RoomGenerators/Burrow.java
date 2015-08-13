package Roguelike.DungeonGeneration.RoomGenerators;

import java.util.Random;

import Roguelike.Global.Direction;
import Roguelike.DungeonGeneration.DungeonFileParser;
import Roguelike.DungeonGeneration.Symbol;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.XmlReader.Element;

/*
* Builds a 'burrow' - that is the organic looking patterns
* discovered by Kusigrosz and documented in this thread
* http://groups.google.com/group/rec.games.roguelike.development/browse_thread/thread/4c56271970c253bf
*/

/* Arguments:
*	 ngb_min, ngb_max: the minimum and maximum number of neighbouring
*		 floor cells that a wall cell must have to become a floor cell.
*		 1 <= ngb_min <= 3; ngb_min <= ngb_max <= 8;
*	 connchance: the chance (in percent) that a new connection is
*		 allowed; for ngb_max == 1 this has no effect as any
*		 connecting cell must have 2 neighbours anyway.
*	 cellnum: the maximum number of floor cells that will be generated.
* The default values of the arguments are defined below.
*
* Algorithm description:
* The algorithm operates on a rectangular grid. Each cell can be 'wall'
* or 'floor'. A (non-border) cell has 8 neigbours - diagonals count. 
* There is also a cell store with two operations: store a given cell on
* top, and pull a cell from the store. The cell to be pulled is selected
* randomly from the store if N_cells_in_store < 125, and from the top
* 25 * cube_root(N_cells_in_store) otherwise. There is no check for
* repetitions, so a given cell can be stored multiple times.
*
* The algorithm starts with most of the map filled with 'wall', with a
* "seed" of some floor cells; their neigbouring wall cells are in store.
* The main loop in delveon() is repeated until the desired number of 
* floor cells is achieved, or there is nothing in store:
*	 1) Get a cell from the store;
*	 Check the conditions: 
*	 a) the cell has between ngb_min and ngb_max floor neighbours, 
*	 b) making it a floor cell won't open new connections,
*		 or the RNG allows it with connchance/100 chance.
*	 if a) and b) are met, the cell becomes floor, and its wall
*	 neighbours are put in store in random order.
* There are many variants possible, for example:
* 1) picking the cell in rndpull() always from the whole store makes
*	 compact patterns;
* 2) storing the neighbours in digcell() clockwise starting from
*	 a random one, and picking the bottom cell in rndpull() creates
*	 meandering or spiral patterns.
*/
public class Burrow extends AbstractRoomGenerator
{
	private float floorCoverage;
	private int minNeighbours;
	private int maxNeighbours;
	private float connectionChance;
	
	private Array<int[]> tempArray = new Array<int[]>();
	
//	private boolean canPlace(Symbol[][] grid, Symbol floor, int x, int y)
//	{
//		
//	}
//	
//	private int[] placeNewSeed(Symbol[][] grid, Symbol floor, Symbol wall, Random ran)
//	{
//		Pools.freeAll(tempArray);
//		tempArray.clear();
//		
//		int width = grid.length;
//		int height = grid[0].length;
//		
//		for (int x = 0; x < width; x++)
//		{
//			for (int y = 0; y < height; y++)
//			{
//				if (grid[x][y] == wall)
//				{
//					int[] pos = Pools.obtain(int[].class);
//					pos[0] = x;
//					pos[1] = y;
//				}
//			}
//		}
//	}
	
	@Override
	public void process(Symbol[][] grid, Symbol floor, Symbol wall, Random ran, DungeonFileParser dfp)
	{
		Array<int[]> cellStore = new Array<int[]>();
		int placedCount = 0;
		
		int width = grid.length;
		int height = grid[0].length;
		
		int targetTileCount = (int)((float)(width*height)*floorCoverage);
		
		// Place seed tiles
		for (int i = 0; i < 8; i++)
		{
			int x = ran.nextInt(width);
			int y = ran.nextInt(height);
			
			grid[x][y] = floor;
			placedCount++;
			
			for (Direction dir : Direction.values())
			{
				int nx = x+dir.getX();
				int ny = y+dir.getY();
				
				cellStore.add(new int[]{nx, ny});
			}
		}
		
		// place tiles
		while (placedCount < targetTileCount)
		{
			
		}
	}

	@Override
	public void parse(Element xml)
	{
		floorCoverage = xml.getFloat("FloorCoverage", 0.6f);
		minNeighbours = xml.getInt("MinNeighbours", 1);
		maxNeighbours = xml.getInt("MaxNeighbours", 6);
		connectionChance = xml.getFloat("ConnectionChance", 0.2f);
	}

}
