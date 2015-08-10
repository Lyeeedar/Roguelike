package Roguelike.DungeonGeneration;

public class Burrow
{
//	#define	BURROW_TEMP	1	/* Limit burrows building to temporary flags */
//	#define BURROW_CARD	2	/* Cardinal directions NSEW only */
//
//	/*
//	 * Builds a 'burrow' - that is the organic looking patterns
//	 * discovered by Kusigrosz and documented in this thread
//	 * http://groups.google.com/group/rec.games.roguelike.development/browse_thread/thread/4c56271970c253bf
//	 */
//
//	/* Arguments:
//	 *	 ngb_min, ngb_max: the minimum and maximum number of neighbouring
//	 *		 floor cells that a wall cell must have to become a floor cell.
//	 *		 1 <= ngb_min <= 3; ngb_min <= ngb_max <= 8;
//	 *	 connchance: the chance (in percent) that a new connection is
//	 *		 allowed; for ngb_max == 1 this has no effect as any
//	 *		 connecting cell must have 2 neighbours anyway.
//	 *	 cellnum: the maximum number of floor cells that will be generated.
//	 * The default values of the arguments are defined below.
//	 *
//	 * Algorithm description:
//	 * The algorithm operates on a rectangular grid. Each cell can be 'wall'
//	 * or 'floor'. A (non-border) cell has 8 neigbours - diagonals count. 
//	 * There is also a cell store with two operations: store a given cell on
//	 * top, and pull a cell from the store. The cell to be pulled is selected
//	 * randomly from the store if N_cells_in_store < 125, and from the top
//	 * 25 * cube_root(N_cells_in_store) otherwise. There is no check for
//	 * repetitions, so a given cell can be stored multiple times.
//	 *
//	 * The algorithm starts with most of the map filled with 'wall', with a
//	 * "seed" of some floor cells; their neigbouring wall cells are in store.
//	 * The main loop in delveon() is repeated until the desired number of 
//	 * floor cells is achieved, or there is nothing in store:
//	 *	 1) Get a cell from the store;
//	 *	 Check the conditions: 
//	 *	 a) the cell has between ngb_min and ngb_max floor neighbours, 
//	 *	 b) making it a floor cell won't open new connections,
//	 *		 or the RNG allows it with connchance/100 chance.
//	 *	 if a) and b) are met, the cell becomes floor, and its wall
//	 *	 neighbours are put in store in random order.
//	 * There are many variants possible, for example:
//	 * 1) picking the cell in rndpull() always from the whole store makes
//	 *	 compact patterns;
//	 * 2) storing the neighbours in digcell() clockwise starting from
//	 *	 a random one, and picking the bottom cell in rndpull() creates
//	 *	 meandering or spiral patterns.
//	 */
//	#define TRN_XSIZE 400
//	#define TRN_YSIZE 400
//
//	struct cellstore
//	{
//		int* xs;
//		int* ys;
//		int index;
//		int size;
//	};
//
//	/* Globals: */
//	int Xoff[8] = {1,  1,  0, -1, -1, -1,  0,  1};
//	int Yoff[8] = {0,  1,  1,  1,  0, -1, -1, -1};
//
//	/* Functions */
//	void rnd_perm(int *tab, int nelem)
//	{
//		int i;
//		int rind;
//		int tmp;
//
//		assert(tab && (nelem >= 0));
//		
//
//		for (i = 0; i < nelem; i++)
//		{
//			rind = rand_int(i + 1);
//			
//			tmp = tab[rind];
//			tab[rind] = tab[i];
//			tab[i] = tmp;
//		}
//	}
//
//	struct cellstore mkstore(int size)
//	{
//		struct cellstore ret;
//		
//		assert(size > 0);
//
//		ret.xs = C_ZNEW(size, int);
//		assert(ret.xs);
//		ret.ys = C_ZNEW(size, int);
//		assert(ret.ys);
//		
//		ret.size = size;
//		ret.index = 0;
//
//		return (ret);
//	}
//
//	void delstore(struct cellstore* cstore)
//	{
//		assert(cstore);
//
//		FREE(cstore->xs);
//		cstore->xs = 0;
//
//		FREE(cstore->ys);
//		cstore->ys = 0;
//
//		cstore->size = 0;
//		cstore->index = 0;
//	}
//
//	int storecell(struct cellstore* cstore, int y, int x)
//	{
//		int rind;
//
//		assert(cstore);
//		
//		if (cstore->index < cstore->size)
//		{
//			cstore->xs[cstore->index] = x;
//			cstore->ys[cstore->index] = y;
//			(cstore->index)++;
//			return (1); /* new cell stored */
//		}
//		else /* Replace another cell. Should not happen if lossless storage */
//		{
//			rind = rand_int(cstore->index);
//			cstore->xs[rind] = x;
//			cstore->ys[rind] = y;
//			return (0); /* old cell gone, new cell stored */
//		}
//	}
//
//	/* Remove a cell from the store and put its coords into x, y.
//	 * Note that pulling any cell except the topmost puts the topmost one in
//	 * its place. 
//	 */
//	int rndpull(struct cellstore* cstore, int* y, int* x, bool compact)
//	{
//		int rind;
//
//		assert(cstore && x && y);
//		if (cstore->index <= 0)
//		{
//			return(0); /* no cells */
//		}
//
//		/* compact patterns */
//		if (compact)
//		{
//			rind = rand_int(cstore->index);
//		}
//		else
//		{
//			/* fluffy patterns */
//			rind = (cstore->index < 125) ?
//				rand_int(cstore->index) :
//				cstore->index - rand_int(25 * uti_icbrt(cstore->index)) - 1;
//		}
//
//		*x = cstore->xs[rind];
//		*y = cstore->ys[rind];
//		
//		if (cstore->index - 1 != rind) /* not the topmost cell - overwrite */
//		{
//			cstore->xs[rind] = cstore->xs[cstore->index - 1];
//			cstore->ys[rind] = cstore->ys[cstore->index - 1];
//		}
//
//		cstore->index -= 1;
//
//		return(1);
//	}
//
//	/* Count neighbours of the given cells that contain terrain feat.
//	 */
//	int ngbcount(int y, int x, s16b feat)
//	{
//		int i;
//		int count = 0;
//		
//		for (i = 0; i < 8; i++)
//		{
//			if (in_bounds_fully(y + Yoff[i],x + Xoff[i]) && 
//				(cave_feat[y + Yoff[i]][x + Xoff[i]] == feat))
//			{
//				count++;
//			}
//		}
//		return (count);
//	}
//
//	/* Number of groups of '1's in the 8 neighbours around a central cell.
//	 * The encoding is binary, lsb is to the right, then clockwise.
//	 */
//	const int ngb_grouptab[256] = 
//	{
//	/**********  0  1  2  3  4  5  6  7  8  9  */
//	/* 000 */	0, 1, 1, 1, 1, 1, 1, 1, 1, 2,
//	/* 010 */	2, 2, 1, 1, 1, 1, 1, 2, 2, 2,
//	/* 020 */	1, 1, 1, 1, 1, 2, 2, 2, 1, 1,
//	/* 030 */	1, 1, 1, 2, 2, 2, 2, 2, 2, 2,
//	/* 040 */	2, 3, 3, 3, 2, 2, 2, 2, 1, 2,
//	/* 050 */	2, 2, 1, 1, 1, 1, 1, 2, 2, 2,
//	/* 060 */	1, 1, 1, 1, 1, 1, 2, 1, 2, 1,
//	/* 070 */	2, 1, 2, 2, 3, 2, 2, 1, 2, 1,
//	/* 080 */	1, 1, 2, 1, 1, 1, 1, 1, 1, 1,
//	/* 090 */	2, 1, 1, 1, 1, 1, 1, 1, 2, 1,
//	/* 000 */	2, 1, 2, 1, 2, 2, 3, 2, 2, 1,
//	/* 110 */	2, 1, 1, 1, 2, 1, 1, 1, 1, 1,
//	/* 120 */	1, 1, 2, 1, 1, 1, 1, 1, 1, 1,
//	/* 130 */	2, 1, 2, 1, 2, 1, 2, 2, 3, 2,
//	/* 140 */	2, 1, 2, 1, 2, 2, 3, 2, 2, 1,
//	/* 150 */	2, 1, 2, 2, 3, 2, 2, 1, 2, 1,
//	/* 160 */	2, 2, 3, 2, 3, 2, 3, 2, 3, 3,
//	/* 170 */	4, 3, 3, 2, 3, 2, 2, 2, 3, 2,
//	/* 180 */	2, 1, 2, 1, 2, 2, 3, 2, 2, 1,
//	/* 190 */	2, 1, 1, 1, 2, 1, 2, 1, 2, 1,
//	/* 200 */	2, 2, 3, 2, 2, 1, 2, 1, 1, 1,
//	/* 210 */	2, 1, 1, 1, 1, 1, 1, 1, 2, 1,
//	/* 220 */	1, 1, 1, 1, 1, 1, 2, 1, 2, 1,
//	/* 230 */	2, 1, 2, 2, 3, 2, 2, 1, 2, 1,
//	/* 240 */	1, 1, 2, 1, 1, 1, 1, 1, 1, 1,
//	/* 250 */	2, 1, 1, 1, 1, 1
//	};
//
//	/* Examine the 8 neigbours of the given cell, and count the number
//	 * of separate groups of terrain cells. A groups contains cells that are
//	 * of the same type (feat) and are adjacent (diagonally, too!)
//	 */
//	int ngbgroups(int y, int x, s16b feat)
//	{
//		int bitmap = 0; /* lowest bit is the cell to the right, then clockwise */
//		int i;
//		
//		for (i = 0; i < 8; i++)
//		{
//			bitmap >>= 1;
//
//			if (in_bounds_fully(y + Yoff[i], x + Xoff[i]) &&
//				cave_feat[y + Yoff[i]][x + Xoff[i]] == feat)
//			{
//				bitmap |= 0x80;
//			}
//		}
//
//		return (ngb_grouptab[bitmap]);
//	}
//
//	/* Dig out an available cell to floor and store its available neighbours in
//	 * random order.
//	 */
//	int digcell(struct cellstore* cstore,
//		int y, int x, s16b floor, s16b available, byte cave_flag, byte burrows_flag)
//	{
//		int order[8] = {0, 1, 2, 3, 4, 5, 6, 7};
//		int i, j;
//
//		assert(cstore);
//
//		if ((!in_bounds_fully(y, x)) || ((available) && (cave_feat[y][x] != available)) ||
//				(((burrows_flag & (BURROW_TEMP)) != 0) && ((play_info[y][x] & (PLAY_TEMP)) == 0)))
//		{
//			return (0); /* did nothing */
//		}
//
//		/* Dig the cell */
//		cave_alter_feat(y, x, FS_TUNNEL);
//		
//		/* Fill it with terrain */
//		build_terrain(y, x, floor);
//		
//		/* Update the room flags */
//		cave_info[y][x] |= (cave_flag);
//		
//		/* Clear the temp flag */
//		if ((burrows_flag & (BURROW_TEMP)) != 0) play_info[y][x] &= ~(PLAY_TEMP);
//
//		rnd_perm(order, 8);
//
//		for (i = 0; i < 8; i += ((burrows_flag & (BURROW_CARD)) != 0) ? 2 : 1) 
//		{
//			j = order[i];
//			if (in_bounds_fully(y + Yoff[j], x + Xoff[j]) &&
//				((!available) || (cave_feat[y + Yoff[j]][x + Xoff[j]] == available)) &&
//				(((burrows_flag & (BURROW_TEMP)) == 0) ||
//						((play_info[y + Yoff[j]][x + Xoff[j]] & (PLAY_TEMP)) != 0)))
//			{
//				storecell(cstore, y + Yoff[j], x + Xoff[j]);
//			}
//			cave_info[y + Yoff[j]][x + Xoff[j]] |= (cave_flag);
//		}
//
//		return (1); /* dug 1 cell */
//	}
//
//	/* Continue digging until cellnum or no more cells in store. Digging is
//	 * allowed if the terrain in the cell is 'ava'ilable, cell has from
//	 * ngb_min to ngb_max flo neighbours, and digging won't open new
//	 * connections; the last condition is ignored with percent chance
//	 * connchance.
//	 */
//	int delveon(struct cellstore* cstore,
//		int ngb_min, int ngb_max, int connchance, int cellnum,
//		s16b floor, s16b available, bool compact, byte cave_flag, byte burrows_flag)
//	{
//		int count = 0;
//		int ngb_count;
//		int ngb_groups;
//		int x, y;
//
//		assert(cstore);
//		assert((cellnum >= 0)/* && (cellnum < mpc.xsize * mpc.ysize)*/);
//		assert((connchance >= 0) && (connchance <= 100));
//		assert((ngb_min >= 0) && (ngb_min <= 3) && 
//			(ngb_min <= ngb_max) && (ngb_max <= 8));
//		assert(floor != available);
//
//		while ((count < cellnum) && rndpull(cstore, &y, &x, compact))
//		{
//			ngb_count = ngbcount(y, x, floor);
//			ngb_groups = ngbgroups(y, x, floor);
//
//			if ( in_bounds_fully(y, x) && ((!available) || (cave_feat[y][x] == available)) &&
//				(((burrows_flag & (BURROW_TEMP)) == 0) || ((play_info[y][x] & (PLAY_TEMP)) != 0)) &&
//				(ngb_count >= ngb_min) && (ngb_count <= ngb_max) && 
//				((ngb_groups <= 1) || (rand_int(100) < connchance)) )
//			{
//					count += digcell(cstore, y, x, floor, available, cave_flag, burrows_flag);
//			}
//		}
//		
//		return (count);
//	}
//
//	/* Estimate a sensible number of cells for given ngb_min, ngb_max.
//	 */
//	int cellnum_est(int totalcells, int ngb_min, int ngb_max)
//	{
//		int denom[12] = {8, 8, 8, 7, 6, 5, 5, 4, 4, 4, 3, 3};
//		/* two first entries are not used */
//
//		assert(totalcells > 0);
//		assert((ngb_min + ngb_max >= 2) && (ngb_min + ngb_max < 12));
//		
//		return (totalcells / denom[ngb_min + ngb_max]);
//	}
//
//
//	/*
//	 * Generate a random 'burrow-like' cavern of cellnum cells.
//	 */
//	bool generate_burrows(int y1, int x1, int y2, int x2,
//		int ngb_min, int ngb_max, int connchance, int cellnum, 
//		s16b floor, s16b available, bool compact, byte cave_flag, byte burrows_flag)
//	{
//		struct cellstore cstore;
//		int count = 0;
//		int ngb_count;
//		int ngb_groups;
//		int x, y;
//		
//		int xorig = (x1 + x2)/2;
//		int yorig = (y1 + y2)/2;
//		
//		int xsize = x2 - x1 + 1;
//		int ysize = y2 - y1 + 1;
//
//		assert((cellnum >= 0) && (cellnum < xsize * ysize));
//		assert((connchance >= 0) && (connchance <= 100));
//		assert((ngb_min >= 0) && (ngb_min <= 3) && 
//			(ngb_min <= ngb_max) && (ngb_max <= 8));
//		assert(floor != available);
//
//		cstore = mkstore(8 * xsize * ysize); /* overkill */
//		storecell(&cstore, yorig, xorig);
//
//		while ((count < 2 * ngb_min) && (count < cellnum) && 
//			rndpull(&cstore, &y, &x, compact))
//		{
//			ngb_count = ngbcount(y, x, floor);
//			ngb_groups = ngbgroups(y, x, floor);
//
//			/* stay close to origin, ignore ngb_min */
//			if ( in_bounds_fully(y, x) && ((!available) || (cave_feat[y][x] == available)) &&
//				(((burrows_flag & (BURROW_TEMP)) == 0) || ((play_info[y][x] & (PLAY_TEMP)) != 0)) &&
//				(abs(x - xorig) < 2) && (abs(y - yorig) < 2) &&
//				(ngb_count <= ngb_max) && 
//				((ngb_groups <= 1) || (rand_int(100) < connchance)) )
//			{
//				count += digcell(&cstore, y, x, floor, available, cave_flag, burrows_flag);
//			}
//		}
//
//		if (count < cellnum)
//			{
//			count += delveon(&cstore, ngb_min, ngb_max, connchance, 
//				cellnum - count, floor, available, compact, cave_flag, burrows_flag);
//			}
//
//		delstore(&cstore);
//		return (count > 0);
//	}
}
