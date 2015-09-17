package Roguelike.Save;

import Roguelike.Levels.Dungeon;
import Roguelike.Tiles.Point;

import com.badlogic.gdx.utils.Array;

public class SaveDungeon extends SaveableObject<Dungeon>
{
	public Array<SaveLevel> levels = new Array<SaveLevel>();
	public SaveLevel outsideLevel;
	public Point outsidePoint;

	public String UID;

	@Override
	public void store( Dungeon obj )
	{
		for ( SaveLevel level : obj.saveLevels.values() )
		{
			levels.add( level );
		}
		outsideLevel = obj.outsideLevel;
		outsidePoint = obj.outsidePoint;

		UID = obj.UID;
	}

	@Override
	public Dungeon create()
	{
		Dungeon dungeon = new Dungeon( outsideLevel, outsidePoint );
		for ( SaveLevel level : levels )
		{
			dungeon.addLevel( level );
		}

		dungeon.UID = UID;

		return dungeon;
	}

}
