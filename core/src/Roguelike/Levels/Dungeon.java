package Roguelike.Levels;

import java.util.HashMap;

import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Entity.GameEntity;
import Roguelike.Save.SaveLevel;
import Roguelike.Tiles.Point;

import com.badlogic.gdx.utils.Array;

public class Dungeon
{
	public boolean isDisposed = false;

	public String UID;

	public HashMap<String, SaveLevel> saveLevels = new HashMap<String, SaveLevel>();
	public HashMap<String, Level> loadedLevels = new HashMap<String, Level>();

	public SaveLevel outsideLevel;
	public Point outsidePoint;

	public boolean isCleared = false;

	public Dungeon()
	{
		UID = "Dungeon :" + this.hashCode();
	}

	public Dungeon( SaveLevel outside, Point point )
	{
		outsideLevel = outside;
		outsidePoint = point;

		UID = "Dungeon :" + this.hashCode();
	}

	public void addLevel( SaveLevel level )
	{
		saveLevels.put( level.UID, level );
	}

	public void addLevel( Level level )
	{
		loadedLevels.put( level.UID, level );
	}

	public SaveLevel getSaveLevel( SaveLevel level )
	{
		if ( !saveLevels.containsKey( level.UID ) )
		{
			addLevel( level );
		}

		return saveLevels.get( level.UID );
	}

	public SaveLevel getSaveLevel( String UID )
	{
		return saveLevels.get( UID );
	}

	private boolean isLevelDeepest( Level level )
	{
		Array<EnvironmentEntity> entities = new Array<EnvironmentEntity>();
		level.getAllEnvironmentEntities( entities );
		for ( EnvironmentEntity entity : entities )
		{
			if ( entity.data.containsKey( "Destination" ) && ( (SaveLevel) entity.data.get( "Destination" ) ).depth > level.depth ) { return false; }
		}

		return true;
	}

	private boolean hasLivingBosses( Level level )
	{
		Array<GameEntity> entities = new Array<GameEntity>();
		level.getAllEntities( entities );
		for ( GameEntity entity : entities )
		{
			if ( entity.isBoss ) { return true; }
		}

		return false;
	}

	public boolean isCleared( Level level )
	{
		if ( !isCleared )
		{
			isCleared = isLevelDeepest( level ) && !hasLivingBosses( level );
		}

		return isCleared;
	}

}

// in level:
// when enemy dies, and was boss, check is dungeon is cleared
// if true spawn new Dungeon exit portal

// Create new DungeonEntrance environent entity, which has cleared tex (if
// appropriate)
