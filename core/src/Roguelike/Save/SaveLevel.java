package Roguelike.Save;

import Roguelike.DungeonGeneration.DungeonFileParser.DFPRoom;
import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Entity.GameEntity;
import Roguelike.Fields.Field;
import Roguelike.Items.Item;
import Roguelike.Levels.Level;
import Roguelike.Tiles.GameTile;
import Roguelike.Tiles.Point;

import com.badlogic.gdx.utils.Array;

public final class SaveLevel extends SaveableObject<Level>
{
	public String fileName;
	public int depth;
	public long seed;
	public String UID;
	public boolean created = false;

	public Array<DFPRoom> requiredRooms = new Array<DFPRoom>();

	public Array<SaveGameEntity> gameEntities = new Array<SaveGameEntity>();
	public Array<SaveLevelItem> items = new Array<SaveLevelItem>();
	public Array<SaveEnvironmentEntity> environmentEntities = new Array<SaveEnvironmentEntity>();
	public Array<SaveField> fields = new Array<SaveField>();
	public Array<SaveEssence> essences = new Array<SaveEssence>();

	public SaveSeenTile[][] seenTiles;

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

	@Override
	public void store( Level obj )
	{
		created = true;
		fileName = obj.fileName;
		depth = obj.depth;
		seed = obj.seed;
		UID = obj.UID;

		requiredRooms.clear();
		requiredRooms.addAll( obj.requiredRooms );

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

		essences.clear();
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

				if ( tile.essence > 0 )
				{
					essences.add( new SaveEssence( tile ) );
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
			if ( entity.canTakeDamage )
			{
				SaveEnvironmentEntity saveObj = new SaveEnvironmentEntity();
				saveObj.store( entity );
				environmentEntities.add( saveObj );
			}
		}

		seenTiles = new SaveSeenTile[obj.width][obj.height];
		for ( int x = 0; x < obj.width; x++ )
		{
			for ( int y = 0; y < obj.height; y++ )
			{
				SaveSeenTile save = new SaveSeenTile();
				save.store( obj.getSeenTile( x, y ) );

				seenTiles[x][y] = save;
			}
		}
	}

	@Override
	public Level create()
	{
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

		for ( SaveEssence essence : essences )
		{
			GameTile tile = level.getGameTile( essence.pos );
			tile.essence = essence.essence;
		}

		if ( seenTiles != null )
		{
			for ( int x = 0; x < level.width; x++ )
			{
				for ( int y = 0; y < level.height; y++ )
				{
					level.SeenGrid[x][y] = seenTiles[x][y].create();
				}
			}
		}
	}

	public void createUID()
	{
		UID = "Level " + fileName + ": Depth " + depth + ": ID " + this.hashCode();
	}

	public static final class SaveEssence
	{
		public int essence;
		public Point pos = new Point();

		public SaveEssence()
		{

		}

		public SaveEssence( GameTile tile )
		{
			essence = tile.essence;
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
