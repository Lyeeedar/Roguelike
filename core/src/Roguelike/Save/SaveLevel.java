package Roguelike.Save;

import Roguelike.DungeonGeneration.DungeonFileParser.DFPRoom;
import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Entity.GameEntity;
import Roguelike.Fields.Field;
import Roguelike.Items.Item;
import Roguelike.Levels.Level;
import Roguelike.Tiles.GameTile;
import Roguelike.Tiles.Point;
import Roguelike.Util.FastEnumMap;
import com.badlogic.gdx.utils.Array;

public final class SaveLevel extends SaveableObject<Level>
{
	public String fileName;
	public int depth;
	public long seed;
	public String UID;
	public boolean created = false;
	public boolean isBossLevel = false;

	public Array<DFPRoom> requiredRooms = new Array<DFPRoom>();

	public Array<SaveGameEntity> gameEntities = new Array<SaveGameEntity>();
	public Array<SaveLevelItem> items = new Array<SaveLevelItem>();
	public Array<SaveEnvironmentEntity> environmentEntities = new Array<SaveEnvironmentEntity>();
	public Array<SaveField> fields = new Array<SaveField>();
	public Array<SaveOrb> orbs = new Array<SaveOrb>();

	public boolean[][] seenState;

	public SaveLevel()
	{

	}

	public SaveLevel( String UID )
	{
		this.UID = UID;
	}

	public SaveLevel( String fileName, int depth, Array<DFPRoom> requiredRooms, long seed )
	{
		this.fileName = fileName;
		this.depth = depth;
		if ( requiredRooms != null )
		{
			this.requiredRooms = requiredRooms;
		}
		this.seed = seed;

		createUID();
	}

	public void createUID()
	{
		UID = "Level " + fileName + ": Depth " + depth + ": ID " + this.hashCode();
	}

	@Override
	public void store( Level obj )
	{
		created = true;
		fileName = obj.fileName;
		depth = obj.depth;
		seed = obj.seed;
		UID = obj.UID;

		requiredRooms.clear();
		if ( obj.requiredRooms != null )
		{
			requiredRooms.addAll( obj.requiredRooms );
		}

		gameEntities.clear();
		Array<GameEntity> tempGameEntities = new Array<GameEntity>( false, 16 );
		obj.getAllEntities( tempGameEntities );
		for ( GameEntity entity : tempGameEntities )
		{
			SaveGameEntity saveObj = new SaveGameEntity();
			saveObj.store( entity );
			gameEntities.add( saveObj );

			if ( entity == obj.player )
			{
				saveObj.isPlayer = true;
			}
		}

		orbs.clear();
		items.clear();
		for ( int x = 0; x < obj.width; x++ )
		{
			for ( int y = 0; y < obj.height; y++ )
			{
				GameTile tile = obj.getGameTile( x, y );
				for ( Item item : tile.items )
				{
					items.add( new SaveLevelItem( tile, item ) );
				}

				if ( tile.orbs.size > 0 )
				{
					orbs.add( new SaveOrb( tile ) );
				}
			}
		}

		fields.clear();
		Array<Field> tempFields = new Array<Field>( false, 16 );
		obj.getAllFields( tempFields );
		for ( Field field : tempFields )
		{
			SaveField save = new SaveField();
			save.store( field );
			fields.add( save );
		}

		environmentEntities.clear();
		Array<EnvironmentEntity> tempEnvironmentEntities = new Array<EnvironmentEntity>( false, 16 );
		obj.getAllEnvironmentEntities( tempEnvironmentEntities );
		for ( EnvironmentEntity entity : tempEnvironmentEntities )
		{
			SaveEnvironmentEntity saveObj = new SaveEnvironmentEntity();
			saveObj.store( entity );
			environmentEntities.add( saveObj );
		}

		seenState = new boolean[obj.Grid.length][obj.Grid[0].length];
		for ( int x = 0; x < seenState.length; x++ )
		{
			for ( int y = 0; y < seenState[0].length; y++ )
			{
				seenState[x][y] = obj.Grid[x][y].seen;
			}
		}
	}

	@Override
	public Level create()
	{
		return null;
	}

	public GameEntity getPlayer()
	{
		for ( SaveGameEntity entity : gameEntities )
		{
			if ( entity.isPlayer )
			{
				GameEntity ge = entity.create();
				return ge;
			}
		}

		return null;
	}

	public void addSavedLevelContents( Level level )
	{
		for ( SaveGameEntity entity : gameEntities )
		{
			GameTile tile = level.getGameTile( entity.pos );
			GameEntity ge = entity.create();
			tile.addGameEntity( ge );

			if ( entity.isPlayer )
			{
				level.player = ge;
			}
		}

		for ( SaveField field : fields )
		{
			GameTile tile = level.getGameTile( field.pos );
			Field f = field.create();
			tile.addField( f );
		}

		for ( SaveLevelItem item : items )
		{
			GameTile tile = level.getGameTile( item.pos );
			tile.items.add( item.item );
		}

		for ( SaveEnvironmentEntity entity : environmentEntities )
		{
			GameTile tile = level.getGameTile( entity.pos );
			tile.addEnvironmentEntity( entity.create() );
		}

		for ( SaveOrb orb : orbs )
		{
			GameTile tile = level.getGameTile( orb.pos );
			tile.orbs = orb.orbs.copy();
		}

		if (seenState != null)
		{
			for ( int x = 0; x < seenState.length; x++ )
			{
				for ( int y = 0; y < seenState[0].length; y++ )
				{
					level.Grid[x][y].seen = seenState[x][y];
				}
			}

			for ( int x = 0; x < seenState.length; x++ )
			{
				for ( int y = 0; y < seenState[ 0 ].length; y++ )
				{
					level.updateSeenBitflag( x, y );
					level.updateUnseenBitflag( x, y );
				}
			}
		}
	}

	public static final class SaveOrb
	{
		public FastEnumMap<GameTile.OrbType, Integer> orbs;
		public Point pos = new Point();

		public SaveOrb()
		{

		}

		public SaveOrb( GameTile tile )
		{
			orbs = tile.orbs.copy();
			pos.x = tile.x;
			pos.y = tile.y;
		}
	}

	public static final class SaveLevelItem
	{
		public Point pos = new Point();
		public Item item;

		public SaveLevelItem()
		{

		}

		public SaveLevelItem( GameTile tile, Item item )
		{
			pos.set( tile.x, tile.y );
			this.item = item;
		}
	}
}
