package Roguelike.DungeonGeneration;

import java.util.Random;

import Roguelike.DungeonGeneration.DungeonFileParser.Symbol;

public class CellularAutomata
{
	private static final int REGION = 25;
	private static final int GRID_WALL = 0;
	private static final int GRID_FLOOR = 1;

	/*
	 * The following 3 functions (floodfill, floodall, joinall) are creditted to Ray Dillinger.
	 * 
	 * These can be used to join disconnected regions.
	 */ 
	private static void floodfill(int[][] map, int size_y, int size_x, int y, int x, int mark, int[] miny, int[] minx)
	{ 
		int i; 
		int j; 
		for (i=-1;i<=1;i++) 
			for (j=-1;j<=1;j++) 
				if (i+x < size_x && i+x >= 0 && 
						j+y < size_y && j+y >= 0 && 
						map[j+y][i+x] != 0 && map[j+y][i+x] != mark)
				{ 
					map[j+y][i+x] = mark; 
					/* side effect of floodfilling is recording minimum x and y 
						for each region*/ 
					if (mark < REGION)
					{ 
						if (i+x < minx[mark]) minx[mark] = i+x; 
						if (j+y < miny[mark]) miny[mark] = j+y; 
					} 
					floodfill(map, size_y, size_x, j+y, i+x, mark, miny, minx); 
				}

	}

	/* find all regions, mark each open cell (by floodfill) with an integer 
		2 or greater indicating what region it's in. */ 
	private static int floodall(int[][] map, int size_y, int size_x, int[] miny,int[] minx)
	{ 
		int x; 
		int y; 
		int count = 2; 
		int retval = 0; 
		/* start by setting all floor tiles to 1. */ 
		/* wall spaces are marked 0. */ 
		for (y=0;y< size_y;y++)
		{ 
			for (x=0;x< size_x;x++)
			{ 
				if (map[y][x] != 0)
				{ 
					map[y][x] = 1; 
				} 
			} 
		}
		
		/* reset region extent marks to -1 invalid */ 
		for (x=0;x<REGION;x++)
		{ 
			minx[x] = -1; 
			miny[x] = -1; 
		}
		
		/* now mark regions starting with the number 2. */ 
		for (y=0;y< size_y;y++)
		{ 
			for (x=0;x< size_x;x++)
			{ 
				if (map[y][x] == 1)
				{ 
					if (count < REGION)
					{ 
						minx[count] = x; 
						miny[count] = y; 
					} 
					floodfill(map, size_y, size_x, y, x, count, miny, minx); 
					count++; 
					retval++; 
				} 
			} 
		} 
		/* return the number of floodfill regions found */ 
		return(retval); 

	} 



	/* joinall is an iterative step toward joining all map regions. 
		The output map is guaranteed to have the same number of 
		open spaces, or fewer, than the input. With enough 
		iterations, an output map having exactly one open space 
		will be produced. Further iterations will just copy that 
		map. 
	*/ 
	private static int joinall(int[][] mapin, int[][] mapout, int size_y, int size_x)
	{ 
		int[] minx = new int[REGION]; 
		int[] miny = new int[REGION]; 
		int x; 
		int y; 
		int count; 
		int retval; 
		retval = floodall(mapin, size_y, size_x, miny, minx); 
		/* if we have multiple unconnected regions */ 		
		if (retval > 1)
		{ 
			/* check to see if there are still regions that can be moved toward 
				0,0. */ 
			count = 0; 
			for (x = 2; x < REGION; x++) 
				if (minx[x] > 0 || miny[x] > 0) count = 1; 
			/* if no regions can still be moved toward (0,0) */ 
			if (count == 0)
			{ 
				/* the new map is the old map flipped about the diagonal. */ 
				for (y = 0; y < size_y; y++) 
					for (x = 0; x < size_x; x++) 
						mapout[size_y-y-1][size_x-x-1] = mapin[y][x]; 
				return(retval); 
			} 
			else
			{ /* there exist regions that can be moved toward 0,0 */ 
				/* write rock to every square of new map that is either 
					�rock or a region we can move; copy the map to all other squares. */ 
				for (y = 0; y < size_y; y++)
					for (x = 0; x < size_x; x++) 
					{ 
						if (mapin[y][x] >= REGION) mapout[y][x] = mapin[y][x]; 
						else mapout[y][x] = 0; 
					} 
				/* now copy regions we can move to new map, each with a shift 
					toward (0,0).*/ 
				for (y = 0; y < size_y; y++) 
					for (x = 0; x < size_x; x++) 
						if (mapin[y][x] != 0 && mapin[y][x] < REGION)
						{ 
							mapout[y-( miny[mapin[y][x]] > 0 ? 1 : 0)]
										 [x-(minx[mapin[y][x]] > 0 ? 1 : 0)] = 1; 
						} 
				return(retval); 	
			} 
		} 
		else
		{ /* there was only one connected region - the output map is the 
						input map. */ 
			for (y = 0; y < size_y; y++) 
				for (x = 0; x < size_x; x++) 
					if (mapin[y][x] == 0) mapout[y][x] = 0; 
					else mapout[y][x] = 1; 
			return(1); 
		}

	}


	/*
	 * This builds a cave-like region using cellular automata identical to that outlined in the
	 * algorithm at http://roguebasin.roguelikedevelopment.org/index.php?title=Cellular_Automata_Method_for_Generating_Random_Cave-Like_Levels
	 * 
	 * The floor and wall grid define which features are generated.
	 * Wall_prob defines the chance of being initialised as a wall grid.
	 * 
	 * R1 defines the minimum number of walls that must be within 1 grid to make the new grid a wall
	 * R2 defines the maximum number of walls that must be within 2 grids to make the new grid a wall
	 * else the new grid is a floor
	 * gen gives the number of generations. gen2 gives the number of generations from which the r2 parameter
	 * is ignored
	 * 
	 * Examples given in the article are:
	 * wall_prob = 45, r1 = 5, r2 = N/A, gen = 5, gen2 = 0
	 * wall_prob = 45, r1 = 5, r2 = 0, gen = 5, gen2 = 5
	 * wall_prob = 40, r1 = 5, r2 = 2, gen = 7, gen2 = 4
	 * 
	 * We can define a combination of wall, floor and edge to allow e.g. a series of islands rising out of lava
	 * or some other mix of terrain.
	 */
	public static void process(Symbol[][] grid, Symbol floor, Symbol wall, Random ran)
	{
		int wall_prob = 45, r1 = 5, r2 = 0, gen = 5, gen2 = 5;
		
		int xi, yi;
		
		int size_y = grid[0].length;
		int size_x = grid.length;
		
		int ii, jj;
		
		int count;
		
		int[][] igrid  = new int[size_y][size_x];
		int[][] igrid2  = new int[size_y][size_x];
		
		/* Initialise the starting grids randomly */
		for(yi=1; yi<size_y-1; yi++)
		for(xi=1; xi<size_x-1; xi++)
			igrid[yi][xi] = ran.nextInt(100) < wall_prob ? GRID_WALL : GRID_FLOOR;
		
		/* Initialise the destination grids - for paranoia */
		for(yi=0; yi<size_y; yi++)
		for(xi=0; xi<size_x; xi++)
			igrid2[yi][xi] = GRID_WALL;
		
		/* Surround the starting grids in walls */
		for(yi=0; yi<size_y; yi++)
			igrid[yi][0] = igrid[yi][size_x-1] = GRID_WALL;
		for(xi=0; xi<size_x; xi++)
			igrid[0][xi] = igrid[size_y-1][xi] = GRID_WALL;
		
		/* Run through generations */
		for(; gen > 0; gen--, gen2--)
		{
			for(yi=1; yi<size_y-1; yi++)
			for(xi=1; xi<size_x-1; xi++)
		 	{
		 		int adjcount_r1 = 0,
		 		    adjcount_r2 = 0;
		 		
		 		for(ii=-1; ii<=1; ii++)
				for(jj=-1; jj<=1; jj++)
		 		{
		 			if(igrid[yi+ii][xi+jj] != GRID_FLOOR)
		 				adjcount_r1++;
		 		}
		 		for(ii=yi-2; ii<=yi+2; ii++)
		 		for(jj=xi-2; jj<=xi+2; jj++)
		 		{
		 			if(Math.abs(ii-yi)==2 && Math.abs(jj-xi)==2)
		 				continue;
		 			if(ii<0 || jj<0 || ii>=size_y || jj>=size_x)
		 				continue;
		 			if(igrid[ii][jj] != GRID_FLOOR)
		 				adjcount_r2++;
		 		}
		 		if(adjcount_r1 >= r1 || ((gen2 > 0) && (adjcount_r2 <= r2)))
		 			igrid2[yi][xi] = GRID_WALL;
		 		else
		 			igrid2[yi][xi] = GRID_FLOOR;
		 	}
		 	for(yi=1; yi<size_y-1; yi++)
		 	for(xi=1; xi<size_x-1; xi++)
		 		igrid[yi][xi] = igrid2[yi][xi];
		}
		
		/* Join all regions */
		do
		{
			joinall(igrid,igrid2, size_y, size_x);
			count = joinall(igrid2,igrid, size_y, size_x); 
		} while (count > 1);
		
		/* Write final grids out to map */
	 	for(yi=0; yi<size_y; yi++)
	 	for(xi=0; xi<size_x; xi++)
	 	{
	 		if (igrid[yi][xi] == GRID_FLOOR)
	 		{
	 			grid[xi][yi] = floor;
	 		}
	 		else
	 		{
	 			grid[xi][yi] = wall;
	 		}
	 	}
	}
}
