package Roguelike.DungeonGeneration;

import java.util.PriorityQueue;
import java.util.Random;

import Roguelike.Global;
import Roguelike.Global.Direction;
import Roguelike.Global.Passability;
import Roguelike.DungeonGeneration.DungeonFileParser.DFPRoom;
import Roguelike.DungeonGeneration.FactionParser.Creature;
import Roguelike.DungeonGeneration.FactionParser.Feature;
import Roguelike.DungeonGeneration.FactionParser.FeaturePlacementType;
import Roguelike.DungeonGeneration.RoomGenerators.AbstractRoomGenerator;
import Roguelike.Pathfinding.AStarPathfind;
import Roguelike.Pathfinding.BresenhamLine;
import Roguelike.Tiles.Point;
import Roguelike.Util.EnumBitflag;

import com.badlogic.gdx.utils.Array;

// ----------------------------------------------------------------------
public final class Room
{
	// ----------------------------------------------------------------------
	public static final EnumBitflag<Passability> GeneratorPassability = new EnumBitflag<Passability>( Passability.WALK );

	// ----------------------------------------------------------------------
	public Direction orientation = Direction.CENTER;

	// ----------------------------------------------------------------------
	public DFPRoom roomData;

	// ----------------------------------------------------------------------
	public boolean fromEmptySpace = false;

	// ----------------------------------------------------------------------
	public int width;
	public int height;

	// ----------------------------------------------------------------------
	public int x;
	public int y;

	// ----------------------------------------------------------------------
	public Array<RoomDoor> doors = new Array<RoomDoor>();

	// ----------------------------------------------------------------------
	public Symbol[][] roomContents;

	// ----------------------------------------------------------------------
	public String faction;

	// ----------------------------------------------------------------------
	private String comparisonString;

	// ----------------------------------------------------------------------
	public void revertChanges( Random ran, DungeonFileParser dfp )
	{
		roomData.fillRoom( this, ran, dfp );
		comparisonString = null;
		doors.clear();
	}

	// ----------------------------------------------------------------------
	public String comparisonString()
	{
		if ( comparisonString == null )
		{
			comparisonString = "";

			for ( int x = 0; x < width; x++ )
			{
				for ( int y = 0; y < height; y++ )
				{
					comparisonString += roomContents[x][y].character;
				}
			}
		}

		return comparisonString;
	}

	// ----------------------------------------------------------------------
	public void rotate()
	{
		Symbol[][] newContents = new Symbol[height][width];

		for ( int x = 0; x < width; x++ )
		{
			for ( int y = 0; y < height; y++ )
			{
				newContents[y][x] = roomContents[x][y];
			}
		}

		roomContents = newContents;

		int temp = height;
		height = width;
		width = temp;
	}

	// ----------------------------------------------------------------------
	public void flipVertical()
	{
		for ( int x = 0; x < width; x++ )
		{
			for ( int y = 0; y < height / 2; y++ )
			{
				Symbol temp = roomContents[x][y];
				roomContents[x][y] = roomContents[x][height - y - 1];
				roomContents[x][height - y - 1] = temp;
			}
		}
	}

	// ----------------------------------------------------------------------
	public void flipHorizontal()
	{
		for ( int x = 0; x < width / 2; x++ )
		{
			for ( int y = 0; y < height; y++ )
			{
				Symbol temp = roomContents[x][y];
				roomContents[x][y] = roomContents[width - x - 1][y];
				roomContents[width - x - 1][y] = temp;
			}
		}
	}

	// ----------------------------------------------------------------------
	public void enclose( DungeonFileParser dfp )
	{
		Symbol wall = dfp.getSymbol( '#' );
		wall.resolveExtends( dfp.sharedSymbolMap );

		// Ensure solid outer wall
		for ( int x = 0; x < width; x++ )
		{
			for ( int y = 0; y < height; y++ )
			{
				if ( x == 0 || x == width - 1 || y == 0 || y == height - 1 )
				{
					if ( isPosDoor( x, y) )
					{

					}
					else
					{
						roomContents[x][y] = wall;
					}
				}

				if ( roomContents[x][y] == null )
				{
					roomContents[x][y] = wall;
				}
			}
		}
	}

	// ----------------------------------------------------------------------
	public boolean isPosDoor(int x, int y)
	{
		for (RoomDoor door : doors)
		{
			if (door.pos.x == x && door.pos.y == y)
			{
				return true;
			}
		}

		return false;
	}

	// ----------------------------------------------------------------------
	public void print()
	{
		System.out.println(this.toString());
	}

	// ----------------------------------------------------------------------
	@Override
	public String toString()
	{
		String s = "";
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				s += roomContents[x][y].character;
			}
			s += '\n';
		}
		return s;
	}

	// ----------------------------------------------------------------------
	public void generateRoomContents( Random ran, DungeonFileParser dfp, Symbol floor, Symbol wall, AbstractRoomGenerator generator )
	{
		if ( width < dfp.corridorStyle.width+2 && height < dfp.corridorStyle.width+2 )
		{
			throw new RuntimeException( "Room too small to attach a corridor to!" );
		}

		Symbol[][] best = null;
		int bestFill = 0;

		roomContents = new Symbol[width][height];

		for ( int i = 0; i < 20; i++ )
		{
			if ( generator != null )
			{
				generator.process( roomContents, floor, wall, ran, dfp );
			}
			else
			{
				for ( int x = 0; x < width; x++ )
				{
					for ( int y = 0; y < height; y++ )
					{
						roomContents[x][y] = floor;
					}
				}
			}

			// Ensure solid outer wall
			for ( int x = 0; x < width; x++ )
			{
				for ( int y = 0; y < height; y++ )
				{
					if ( x == 0 || x == width - 1 || y == 0 || y == height - 1 )
					{
						roomContents[x][y] = wall;
					}

					if ( roomContents[x][y] == null )
					{
						roomContents[x][y] = wall;
					}
				}
			}

			// minimise room size
			Symbol[][] newRoomContents = RecursiveDockGenerator.minimiseGrid( roomContents, wall );
			int nwidth = newRoomContents.length;
			int nheight = newRoomContents[0].length;

			if (nwidth*nheight > bestFill)
			{
				bestFill = nwidth*nheight;
				best = newRoomContents;
			}

			if ( nwidth < dfp.corridorStyle.width+2 && nheight < dfp.corridorStyle.width+2 )
			{
				continue;
			}
			else if ( nwidth*nheight > ( width * height ) / 2 )
			{
				break;
			}
		}

		roomContents = best;
		width = roomContents.length;
		height = roomContents[0].length;

		carveDoors( dfp, ran, floor, !generator.ensuresConnectivity );

		System.out.println("Generate:");
		print();
	}

	// ----------------------------------------------------------------------
	public void carveDoors(DungeonFileParser dfp, Random ran, Symbol floor, boolean digToCenter)
	{
		boolean canAttachCorridorVertically = width >= dfp.corridorStyle.width + 2;
		boolean canAttachCorridorHorizontally = height >= dfp.corridorStyle.width + 2;

		// Place corridor connections
		// Sides
		// 1
		// 0 2
		// 3

		int numDoors = (int) ( Math.max( 0, ran.nextFloat() ) * 3 ) + 1;
		for ( int i = 0; i < numDoors; i++ )
		{
			int doorSide = 0;

			if (canAttachCorridorHorizontally && canAttachCorridorVertically) { doorSide = ran.nextInt( 4 ); }
			else if (canAttachCorridorHorizontally) { doorSide = ran.nextBoolean() ? 0 : 2; }
			else if (canAttachCorridorVertically) { doorSide = ran.nextBoolean() ? 1 : 3; }

			int x = 0;
			int y = 0;

			if ( doorSide == 0 )
			{
				x = 0;
				y = 1 + ran.nextInt( height - ( 1 + dfp.corridorStyle.width ) );

				for ( int c = 0; c < dfp.corridorStyle.width; c++ )
				{
					roomContents[x][y + c] = floor;
				}
			}
			else if ( doorSide == 1 )
			{
				x = 1 + ran.nextInt( width - ( 1 + dfp.corridorStyle.width ) );
				y = 0;

				for ( int c = 0; c < dfp.corridorStyle.width; c++ )
				{
					roomContents[x + c][y] = floor;
				}
			}
			else if ( doorSide == 2 )
			{
				x = width - 1;
				y = 1 + ran.nextInt( height - ( 1 + dfp.corridorStyle.width ) );

				for ( int c = 0; c < dfp.corridorStyle.width; c++ )
				{
					roomContents[x][y + c] = floor;
				}
			}
			else if ( doorSide == 3 )
			{
				x = 1 + ran.nextInt( width - ( 1 + dfp.corridorStyle.width ) );
				y = height - 1;

				for ( int c = 0; c < dfp.corridorStyle.width; c++ )
				{
					roomContents[x + c][y] = floor;
				}
			}

			Array<Point> path = BresenhamLine.lineNoDiag( x, y, width / 2, height / 2 );
			for ( Point pos : path )
			{
				boolean done = false;
				if ( roomContents[pos.x][pos.y] == floor )
				{
					done = true;
				}

				for ( int ix = 0; ix < dfp.corridorStyle.width; ix++ )
				{
					for ( int iy = 0; iy < dfp.corridorStyle.width; iy++ )
					{
						int nx = pos.x + ix;
						int ny = pos.y + iy;

						if ( nx < width && ny < height )
						{
							roomContents[nx][ny] = floor;
						}
					}
				}

				if ( !digToCenter && done )
				{
					break;
				}
			}

			Global.PointPool.freeAll( path );
		}
	}

	// ----------------------------------------------------------------------
	public void generateRoomContents( Random ran, DungeonFileParser dfp )
	{
		Symbol floor = roomData != null ? roomData.getSymbol( '.' ) : dfp.sharedSymbolMap.get( '.' );
		Symbol wall = roomData != null ? roomData.getSymbol( '#' ) : dfp.sharedSymbolMap.get( '#' );

		floor.resolveExtends( dfp.sharedSymbolMap );
		wall.resolveExtends( dfp.sharedSymbolMap );

		AbstractRoomGenerator generator = dfp.getRoomGenerator( ran );

		generateRoomContents( ran, dfp, floor, wall, generator );
	}

	// ----------------------------------------------------------------------
	public void resolveExtends( DungeonFileParser dfp )
	{
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				roomContents[x][y].resolveExtends( dfp.sharedSymbolMap );
			}
		}
	}

	// ----------------------------------------------------------------------
	private boolean isPosEnclosed( int x, int y )
	{
		EnumBitflag<Direction> solid = new EnumBitflag<Direction>();
		for ( Direction dir : Direction.values() )
		{
			int x1 = x + dir.getX();
			int y1 = y + dir.getY();

			boolean collide = x1 >= 0 && y1 >= 0 && x1 < width && y1 < height && !roomContents[x1][y1].isPassable( GeneratorPassability );

			if ( collide )
			{
				solid.setBit( dir );
			}
		}

		// Identify open paths through this pos

		// Vertical path
		if ( !solid.contains( Direction.NORTH ) && !solid.contains( Direction.SOUTH ) )
		{
			boolean side1 = solid.contains( Direction.EAST ) || ( solid.contains( Direction.NORTHEAST ) || solid.contains( Direction.SOUTHEAST ) );
			boolean side2 = solid.contains( Direction.WEST ) || ( solid.contains( Direction.NORTHWEST ) || solid.contains( Direction.SOUTHWEST ) );

			if ( side1 && side2 ) { return true; }
		}

		// Horizontal path
		if ( !solid.contains( Direction.EAST ) && !solid.contains( Direction.WEST ) )
		{
			boolean side1 = solid.contains( Direction.NORTH ) || ( solid.contains( Direction.NORTHEAST ) || solid.contains( Direction.NORTHWEST ) );
			boolean side2 = solid.contains( Direction.SOUTH ) || ( solid.contains( Direction.SOUTHEAST ) || solid.contains( Direction.SOUTHWEST ) );

			if ( side1 && side2 ) { return true; }
		}

		return false;
	}

	// ----------------------------------------------------------------------
	public void addFeatures( Random ran, DungeonFileParser dfp, FactionParser faction, int influence, boolean spawnMiniBoss )
	{
		if ( faction == null ) { return; }

		Symbol[][] roomCopy = new Symbol[width][height];
		for ( int x = 0; x < width; x++ )
		{
			for ( int y = 0; y < height; y++ )
			{
				roomCopy[x][y] = roomContents[x][y];
			}
		}

		print();

		// build the any list
		Array<Point> validList = new Array<Point>();
		Array<Point> fullList = new Array<Point>();
		for ( int x = 0; x < width; x++ )
		{
			for ( int y = 0; y < height; y++ )
			{
				if ( roomContents[x][y] != null && roomContents[x][y].isPassable( GeneratorPassability ) && roomContents[x][y].getEnvironmentEntityPassable( GeneratorPassability ) )
				{
					if (  x > 0 && x < width - 1 && y > 0 && y < height - 1 )
					{
						Point point = Global.PointPool.obtain().set( x, y );
						if (!isPosEnclosed( x, y ))
						{
							validList.add( point );
						}
						fullList.add( point );
					}
				}
			}
		}

		// start placing features

		// Do furthest
		{
			Array<Feature> features = faction.features.get( FeaturePlacementType.FURTHEST );

			for ( Feature f : features )
			{
				// Skip if out of range
				if ( influence < f.minRange || influence > f.maxRange )
				{
					continue;
				}

				// Build list
				PriorityQueue<FeatureTile> furthestList = new PriorityQueue<FeatureTile>();
				for ( Point tile : validList )
				{
					int x = tile.x;
					int y = tile.y;

					if ( roomContents[x][y].getTileData().canFeature
							&& roomContents[x][y].isPassable( GeneratorPassability )
							&& ( f.environmentData == null || !roomContents[x][y].hasEnvironmentEntity() ) )
					{
						furthestList.add( new FeatureTile( tile, doors ) );
					}
				}

				// Skip if no valid tiles
				if ( furthestList.size() == 0 )
				{
					break;
				}

				// calculate num features to place
				int numTilesToPlace = f.getNumTilesToPlace( influence, furthestList.size() );

				// Place the features
				for ( int i = 0; i < numTilesToPlace; i++ )
				{
					Point pos = furthestList.poll().pos;

					roomContents[pos.x][pos.y] = f.getAsSymbol( roomContents[pos.x][pos.y] );

					if ( furthestList.size() == 0 )
					{
						break;
					}
				}
			}
		}

		// Do wall
		{
			Array<Feature> features = faction.features.get( FeaturePlacementType.WALL );

			for ( Feature f : features )
			{
				if ( influence < f.minRange || influence > f.maxRange )
				{
					continue;
				}

				// Build list
				Array<Point> wallList = new Array<Point>();
				for ( Point tile : validList )
				{
					int x = tile.x;
					int y = tile.y;

					if ( roomContents[x][y].getTileData().canFeature
							&& roomContents[x][y].isPassable( GeneratorPassability )
							&& ( f.environmentData == null || !roomContents[x][y].hasEnvironmentEntity() ) )
					{
						boolean isWall = false;
						for ( Direction d : Direction.values() )
						{
							int nx = x + d.getX();
							int ny = y + d.getY();

							if ( nx >= 0 && nx < width && ny >= 0 && ny < height )
							{
								if ( roomContents[nx][ny] != null && !roomContents[nx][ny].isPassable( GeneratorPassability ) )
								{
									isWall = true;
									break;
								}
							}
						}

						if ( isWall )
						{
							wallList.add( tile );
						}
					}
				}

				if ( wallList.size == 0 )
				{
					break;
				}

				int numTilesToPlace = f.getNumTilesToPlace( influence, wallList.size );

				for ( int i = 0; i < numTilesToPlace; i++ )
				{
					Point pos = wallList.removeIndex( ran.nextInt( wallList.size ) );

					roomContents[pos.x][pos.y] = f.getAsSymbol( roomContents[pos.x][pos.y] );

					if ( wallList.size == 0 )
					{
						break;
					}
				}
			}
		}

		// Do centre
		{
			Array<Feature> features = faction.features.get( FeaturePlacementType.CENTRE );

			for ( Feature f : features )
			{
				if ( influence < f.minRange || influence > f.maxRange )
				{
					continue;
				}

				// Build list
				Array<Point> centreList = new Array<Point>();
				for ( Point tile : validList )
				{
					int x = tile.x;
					int y = tile.y;

					if ( roomContents[x][y].getTileData().canFeature
							&& roomContents[x][y].isPassable( GeneratorPassability )
							&& ( f.environmentData == null || !roomContents[x][y].hasEnvironmentEntity() ) )
					{
						boolean isWall = false;
						for ( Direction d : Direction.values() )
						{
							int nx = x + d.getX();
							int ny = y + d.getY();

							if ( nx >= 0 && nx < width && ny >= 0 && ny < height )
							{
								if ( roomContents[nx][ny] != null && !roomContents[nx][ny].isPassable( GeneratorPassability ) )
								{
									isWall = true;
									break;
								}
							}
						}

						if ( !isWall )
						{
							centreList.add( tile );
						}
					}
				}

				if ( centreList.size == 0 )
				{
					break;
				}

				int numTilesToPlace = f.getNumTilesToPlace( influence, centreList.size );

				for ( int i = 0; i < numTilesToPlace; i++ )
				{
					Point pos = centreList.removeIndex( ran.nextInt( centreList.size ) );

					roomContents[pos.x][pos.y] = f.getAsSymbol( roomContents[pos.x][pos.y] );

					if ( centreList.size == 0 )
					{
						break;
					}
				}
			}
		}

		// Do any
		{
			Array<Feature> features = faction.features.get( FeaturePlacementType.ANY );

			for ( Feature f : features )
			{
				if ( influence < f.minRange || influence > f.maxRange )
				{
					continue;
				}

				Array<Point> anyList = new Array<Point>();
				for ( Point tile : validList )
				{
					int x = tile.x;
					int y = tile.y;

					if ( roomContents[x][y].getTileData().canFeature
							&& roomContents[x][y].isPassable( GeneratorPassability )
							&& ( f.environmentData == null || !roomContents[x][y].hasEnvironmentEntity() ) )
					{
						anyList.add( tile );
					}
				}

				if ( anyList.size == 0 )
				{
					break;
				}

				int numTilesToPlace = f.getNumTilesToPlace( influence, anyList.size );

				for ( int i = 0; i < numTilesToPlace; i++ )
				{
					Point pos = anyList.removeIndex( ran.nextInt( anyList.size ) );

					roomContents[pos.x][pos.y] = f.getAsSymbol( roomContents[pos.x][pos.y] );

					if ( anyList.size == 0 )
					{
						break;
					}
				}
			}
		}

		// Ensure connectivity
		for ( int i = 0; i < doors.size; i++ )
		{
			RoomDoor door = doors.get( i );

			for ( int ii = i+1; ii < doors.size; ii++ )
			{
				RoomDoor otherDoor = doors.get( ii );

				AStarPathfind pathfind = new AStarPathfind( roomContents, door.pos.x, door.pos.y, otherDoor.pos.x, otherDoor.pos.y, Global.CanMoveDiagonal, false, 1, GeneratorPassability, null );
				Array<Point> path = pathfind.getPath();

				if (path == null)
				{
					print();

					continue;
				}

				//if (path != null)
				{
					for ( Point point : path )
					{
						Symbol s = roomContents[point.x][point.y];

						if ( dfp.corridorStyle != null && dfp.corridorStyle.width == 1 && dfp.corridorStyle.centralConstant != null )
						{
							s = dfp.corridorStyle.centralConstant.getAsSymbol( s, dfp );
						}
						else if ( !s.isPassable( GeneratorPassability ) )
						{
							s.tileData = roomCopy[point.x][point.y].tileData;
						}

						if ( s.hasEnvironmentEntity()
							 && !s.environmentData.get( "Type", "" ).equals( "Door" )
							 && !s.getEnvironmentEntityPassable( GeneratorPassability ) )
						{
							s.environmentData = roomCopy[point.x][point.y].environmentData;
							s.environmentEntityData = roomCopy[point.x][point.y].environmentEntityData;
						}

						roomContents[point.x][point.y] = s;
					}

					Global.PointPool.freeAll( path );
				}
			}
		}

		// Do spawn
		{
			// get valid spawn tiles
			Array<Point> spawnList = new Array<Point>();
			Array<Point> blockedList = new Array<Point>(  );
			for ( Point tile : fullList )
			{
				int x = tile.x;
				int y = tile.y;

				if ( roomContents[x][y].getTileData().canSpawn &&
					 roomContents[x][y].isPassable( GeneratorPassability ) &&
					 !roomContents[x][y].hasGameEntity())
				{
					if ( roomContents[x][y].getEnvironmentEntityPassable( GeneratorPassability ) )
					{
						spawnList.add( tile );
					}
					else
					{
						blockedList.add( tile );
					}
				}
			}

			if (spawnMiniBoss)
			{
				if ( spawnList.size == 0 )
				{
					if ( blockedList.size > 0 )
					{
						Point p = blockedList.removeIndex( 0 );
						roomContents[p.x][p.y].environmentEntityData = roomCopy[p.x][p.y].environmentEntityData;
						roomContents[p.x][p.y].environmentData = roomCopy[p.x][p.y].environmentData;

						spawnList.add( p );
					}
				}

				if ( spawnList.size > 0 )
				{
					String entityName = null;

					if ( faction.minibosses.size > 0 )
					{
						faction.minibosses.get( ran.nextInt( faction.minibosses.size ) );
					}
					else
					{
						entityName = faction.creatures.get( faction.creatures.size - 1 ).entityName;
					}

					Point pos = spawnList.removeIndex( ran.nextInt( spawnList.size ) );

					roomContents[pos.x][pos.y] = roomContents[pos.x][pos.y].copy();
					roomContents[pos.x][pos.y].entityData = entityName;
					roomContents[pos.x][pos.y].isBoss = true;
				}
			}

			double difficulty = spawnList.size > 0 ? Math.log( spawnList.size ) : 0;
			difficulty /= 2;
			difficulty = Math.pow( difficulty, 2 );
			difficulty = Math.max( 1, difficulty );

			// place mobs
			Array<Creature> creatures = faction.getCreatures( ran, (float) difficulty, influence );

			for ( Creature creature : creatures )
			{
				if ( spawnList.size == 0 )
				{
					if ( blockedList.size == 0 )
					{
						break;
					}
					else
					{
						Point p = blockedList.removeIndex( 0 );
						roomContents[p.x][p.y].environmentEntityData = roomCopy[p.x][p.y].environmentEntityData;
						roomContents[p.x][p.y].environmentData = roomCopy[p.x][p.y].environmentData;

						spawnList.add( p );
					}
				}

				Point pos = spawnList.removeIndex( ran.nextInt( spawnList.size ) );

				roomContents[pos.x][pos.y] = roomContents[pos.x][pos.y].copy();
				roomContents[pos.x][pos.y].entityData = creature.entityName;
				roomContents[pos.x][pos.y].character = 'E';
			}
		}

		print();

		Global.PointPool.freeAll( fullList );
	}

	// ----------------------------------------------------------------------
	private void addDoor( int pos, int space, Direction dir, Random ran, DungeonFileParser dfp )
	{
		if ( space >= dfp.corridorStyle.width )
		{
			int offset = space > dfp.corridorStyle.width ? ran.nextInt( space - dfp.corridorStyle.width ) : 0;

			if ( dir == Direction.WEST )
			{
				doors.add( new RoomDoor( Direction.WEST, new Point( 0, pos + offset ) ) );
			}
			else if ( dir == Direction.EAST )
			{
				doors.add( new RoomDoor( Direction.EAST, new Point( width - 1, pos + offset ) ) );
			}
			else if ( dir == Direction.NORTH )
			{
				doors.add( new RoomDoor( Direction.NORTH, new Point( pos + offset, 0 ) ) );
			}
			else if ( dir == Direction.SOUTH )
			{
				doors.add( new RoomDoor( Direction.SOUTH, new Point( pos + offset, height - 1 ) ) );
			}
		}
	}

	// ----------------------------------------------------------------------
	private void processSide( Direction dir, Random ran, DungeonFileParser dfp )
	{
		int range = ( dir == Direction.WEST || dir == Direction.EAST ) ? height : width;

		int blockStart = -1;
		for ( int pos = 1; pos < range - 1; pos++ )
		{
			int x = 0;
			int y = 0;

			if ( dir == Direction.WEST )
			{
				x = 0;
				y = pos;
			}
			else if ( dir == Direction.EAST )
			{
				x = width - 1;
				y = pos;
			}
			else if ( dir == Direction.NORTH )
			{
				x = pos;
				y = 0;
			}
			else
			{
				x = pos;
				y = height - 1;
			}

			if ( blockStart >= 0 )
			{
				if ( !roomContents[x][y].isPassable( GeneratorPassability ) )
				{
					addDoor( blockStart, pos - blockStart, dir, ran, dfp );
					blockStart = -1;
				}
			}
			else
			{
				if ( roomContents[x][y].isPassable( GeneratorPassability ) )
				{
					blockStart = pos;
				}
			}
		}

		if ( blockStart >= 0 )
		{
			int pos = range - 1;
			addDoor( blockStart, pos - blockStart, dir, ran, dfp );
		}
	}

	// ----------------------------------------------------------------------
	public void findDoors( Random ran, DungeonFileParser dfp )
	{
		// Sides
		// 1
		// 0 2
		// 3

		// Side 0
		processSide( Direction.WEST, ran, dfp );

		// Side 2
		processSide( Direction.EAST, ran, dfp );

		// Side 1
		// Side 1
		processSide( Direction.NORTH, ran, dfp );

		// Side 3
		processSide( Direction.SOUTH, ran, dfp );
	}

	// ----------------------------------------------------------------------
	private class FeatureTile implements Comparable<FeatureTile>
	{
		public Point pos;
		public int dist;

		public FeatureTile( Point pos, Array<RoomDoor> doors )
		{
			this.pos = pos;

			int d = 0;
			for ( RoomDoor door : doors )
			{
				d += Math.abs( pos.x - door.pos.x ) + Math.abs( pos.y - door.pos.y );
			}

			d /= doors.size;

			dist = d;
		}

		@Override
		public int compareTo( FeatureTile o )
		{
			return ( (Integer) dist ).compareTo( o.dist );
		}

	}

	// ----------------------------------------------------------------------
	public static final class RoomDoor
	{
		public Direction side;
		public Point pos;

		public RoomDoor()
		{

		}

		public RoomDoor( Direction side, Point pos )
		{
			this.side = side;
			this.pos = pos;
		}
	}
}
