package Roguelike.DungeonGeneration.RoomGenerators;

import java.util.HashMap;
import java.util.Random;

import Roguelike.DungeonGeneration.DungeonFileParser;
import Roguelike.DungeonGeneration.Symbol;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public abstract class AbstractRoomGenerator
{
	// ----------------------------------------------------------------------
	public boolean ensuresConnectivity = false;

	// ----------------------------------------------------------------------
	public abstract void process( Symbol[][] grid, Symbol floor, Symbol wall, Random ran, DungeonFileParser dfp );

	// ----------------------------------------------------------------------
	public abstract void parse( Element xml );

	// ----------------------------------------------------------------------
	public static AbstractRoomGenerator load( Element xml )
	{
		Class<AbstractRoomGenerator> c = ClassMap.get( xml.getName().toUpperCase() );
		AbstractRoomGenerator type = null;

		try
		{
			type = ClassReflection.newInstance( c );
		}
		catch ( ReflectionException e )
		{
			e.printStackTrace();
		}

		type.parse( xml );

		return type;
	}

	// ----------------------------------------------------------------------
	protected static HashMap<String, Class> ClassMap = new HashMap<String, Class>();

	// ----------------------------------------------------------------------
	static
	{
		ClassMap.put( "BASIC", Basic.class );
		ClassMap.put( "CELLULARAUTOMATA", CellularAutomata.class );
		ClassMap.put( "CHAMBERS", Chambers.class );
		ClassMap.put( "FRACTAL", Fractal.class );
		ClassMap.put( "OVERLAPPINGRECTS", OverlappingRects.class );
		ClassMap.put( "POLYGON", Polygon.class );
		ClassMap.put( "STARBURST", Starburst.class );
		ClassMap.put( "RANDOM", RandomPlace.class );
	}
}
