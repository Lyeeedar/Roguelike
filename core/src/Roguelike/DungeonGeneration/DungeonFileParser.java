package Roguelike.DungeonGeneration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import Roguelike.DungeonGeneration.RecursiveDockGenerator.Room;
import Roguelike.DungeonGeneration.RoomGenerators.AbstractRoomGenerator;
import Roguelike.DungeonGeneration.RoomGenerators.OverlappingRects;
import Roguelike.Sound.RepeatingSoundEffect;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class DungeonFileParser
{
	// ----------------------------------------------------------------------
	public static class CorridorStyle
	{
		public enum PathStyle
		{
			STRAIGHT, WANDERING
		}

		public PathStyle pathStyle = PathStyle.STRAIGHT;

		public int minWidth;
		public int maxWidth;

		public CorridorFeature centralConstant;
		public CorridorFeature centralRecurring;
		public CorridorFeature sideRecurring;

		public void parse( Element xml )
		{
			pathStyle = PathStyle.valueOf( xml.get( "PathStyle", "Straight" ).toUpperCase() );

			if ( xml.getInt( "Width", -1 ) != -1 )
			{
				minWidth = maxWidth = xml.getInt( "Width" );
			}
			minWidth = xml.getInt( "MinWidth", minWidth );
			maxWidth = xml.getInt( "MaxWidth", maxWidth );

			Element centralConstantElement = xml.getChildByName( "CentralConstant" );
			if ( centralConstantElement != null )
			{
				centralConstant = CorridorFeature.load( centralConstantElement );
			}

			Element centralRecurringElement = xml.getChildByName( "CentralRecurring" );
			if ( centralRecurringElement != null )
			{
				centralRecurring = CorridorFeature.load( centralRecurringElement );
			}

			Element sideRecurringElement = xml.getChildByName( "SideRecurring" );
			if ( sideRecurringElement != null )
			{
				sideRecurring = CorridorFeature.load( sideRecurringElement );
			}
		}
	}

	// ----------------------------------------------------------------------
	public static class CorridorFeature
	{
		public enum PlacementMode
		{
			BOTH, TOP, BOTTOM, ALTERNATE
		}

		public Element tileData;
		public Element environmentData;

		public int interval;

		public PlacementMode placementMode;

		public static CorridorFeature load( Element xml )
		{
			CorridorFeature feature = new CorridorFeature();

			feature.interval = xml.getInt( "Interval", 0 );
			feature.placementMode = PlacementMode.valueOf( xml.get( "PlacementMode", "Both" ).toUpperCase() );
			feature.tileData = xml.getChildByName( "TileData" );
			feature.environmentData = xml.getChildByName( "EnvironmentData" );

			return feature;
		}

		public Symbol getAsSymbol( Symbol current )
		{
			Symbol symbol = current.copy();
			symbol.character = 'F';
			symbol.tileData = tileData != null ? tileData : current.tileData;
			symbol.environmentData = environmentData != null ? environmentData : current.environmentData;

			return symbol;
		}
	}

	// ----------------------------------------------------------------------
	public static class RoomGenerator
	{
		public AbstractRoomGenerator generator;
		public int weight;
	}

	// ----------------------------------------------------------------------
	public static class Faction
	{
		public String name;
		public int weight;

		public Faction( String name, int weight )
		{
			this.name = name;
			this.weight = weight;
		}
	}

	// ----------------------------------------------------------------------
	public static class DFPRoom
	{
		// ----------------------------------------------------------------------
		public enum Orientation
		{
			EDGE, CENTRE, RANDOM, FIXED
		}

		public Orientation orientation;

		public boolean isTransition;

		public int minDepth = 0;
		public int maxDepth = Integer.MAX_VALUE;

		public int width;
		public int height;
		public HashMap<Character, Symbol> localSymbolMap = new HashMap<Character, Symbol>();
		public HashMap<Character, Symbol> sharedSymbolMap;
		public char[][] roomDef;
		public String faction;
		public AbstractRoomGenerator generator;

		public static DFPRoom parse( Element xml, HashMap<Character, Symbol> sharedSymbolMap )
		{
			DFPRoom room = new DFPRoom();
			room.sharedSymbolMap = sharedSymbolMap;

			room.minDepth = xml.getIntAttribute( "Min", room.minDepth );
			room.maxDepth = xml.getIntAttribute( "Max", room.maxDepth );

			room.faction = xml.get( "Faction", null );

			room.orientation = Orientation.valueOf( xml.get( "Orientation", "Random" ).toUpperCase() );

			Element rowsElement = xml.getChildByName( "Rows" );

			if ( rowsElement != null )
			{
				if ( rowsElement.getChildCount() > 0 )
				{
					// Rows defined here
					room.height = rowsElement.getChildCount();
					for ( int i = 0; i < room.height; i++ )
					{
						if ( rowsElement.getChild( i ).getText().length() > room.width )
						{
							room.width = rowsElement.getChild( i ).getText().length();
						}
					}

					room.roomDef = new char[room.width][room.height];
					for ( int x = 0; x < room.width; x++ )
					{
						for ( int y = 0; y < room.height; y++ )
						{
							room.roomDef[x][y] = rowsElement.getChild( y ).getText().charAt( x );
						}
					}
				}
				else
				{
					// Rows in seperate csv file
					String fileName = rowsElement.getText();
					FileHandle handle = Gdx.files.internal( fileName + ".csv" );
					String content = handle.readString();

					String[] lines = content.split( System.getProperty( "line.separator" ) );
					room.width = lines.length;

					String[][] rows = new String[lines.length][];
					for ( int i = 0; i < lines.length; i++ )
					{
						rows[i] = lines[i].split( " " );

						room.height = rows[i].length;
					}

					room.roomDef = new char[room.width][room.height];
					for ( int x = 0; x < room.width; x++ )
					{
						for ( int y = 0; y < room.height; y++ )
						{
							room.roomDef[x][y] = rows[x][y].charAt( 0 );
						}
					}
				}
			}
			else
			{
				Element generatorElement = xml.getChildByName( "Generator" ).getChild( 0 );
				room.generator = AbstractRoomGenerator.load( generatorElement );

				room.width = xml.getInt( "Width" );
				room.height = xml.getInt( "Height" );
			}

			Element symbolsElement = xml.getChildByName( "Symbols" );
			if ( symbolsElement != null )
			{
				for ( int i = 0; i < symbolsElement.getChildCount(); i++ )
				{
					Symbol symbol = Symbol.parse( symbolsElement.getChild( i ), sharedSymbolMap, room.localSymbolMap );
					room.localSymbolMap.put( symbol.character, symbol );

					if ( symbol.environmentData != null && symbol.environmentData.get( "Type", "" ).equals( "Transition" ) )
					{
						room.isTransition = true;
					}
				}
			}

			return room;
		}

		public Symbol getSymbol( char c )
		{
			Symbol s = localSymbolMap.get( c );
			if ( s == null )
			{
				s = sharedSymbolMap.get( c );
			}
			if ( s == null )
			{
				System.out.println( "Failed to find symbol for character '" + c + "'! Falling back to using '.'" );
				s = sharedSymbolMap.get( '.' );
			}

			if ( s == null )
			{
				s = sharedSymbolMap.get( '.' );
			}

			return s;
		}

		public void fillRoom( Room room, Random ran, DungeonFileParser dfp )
		{
			room.roomData = this;
			room.width = width;
			room.height = height;
			room.roomContents = new Symbol[width][height];
			room.faction = faction;

			if ( generator != null )
			{
				Symbol floor = getSymbol( '.' );
				Symbol wall = getSymbol( '#' );

				room.generateRoomContents( ran, dfp, floor, wall, generator );
			}
			else
			{
				for ( int x = 0; x < width; x++ )
				{
					for ( int y = 0; y < height; y++ )
					{
						room.roomContents[x][y] = getSymbol( roomDef[x][y] );
					}
				}
			}
		}
	}

	// ----------------------------------------------------------------------
	public HashMap<Character, Symbol> sharedSymbolMap = new HashMap<Character, Symbol>();

	// ----------------------------------------------------------------------
	public Array<DFPRoom> requiredRooms = new Array<DFPRoom>();

	// ----------------------------------------------------------------------
	public Array<DFPRoom> optionalRooms = new Array<DFPRoom>();

	// ----------------------------------------------------------------------
	public Array<Faction> majorFactions = new Array<Faction>();

	// ----------------------------------------------------------------------
	public Array<Faction> minorFactions = new Array<Faction>();

	// ----------------------------------------------------------------------
	public Color ambient;

	// ----------------------------------------------------------------------
	public String BGM;

	// ----------------------------------------------------------------------
	public Array<RepeatingSoundEffect> ambientSounds = new Array<RepeatingSoundEffect>();

	// ----------------------------------------------------------------------
	public Array<RoomGenerator> roomGenerators = new Array<RoomGenerator>();

	// ----------------------------------------------------------------------
	public CorridorStyle corridorStyle = new CorridorStyle();

	// ----------------------------------------------------------------------
	public RoomGenerator preprocessor;

	// ----------------------------------------------------------------------
	public Array<DFPRoom> getRequiredRooms( int depth )
	{
		Array<DFPRoom> rooms = new Array<DFPRoom>();

		for ( DFPRoom room : requiredRooms )
		{
			if ( room.minDepth <= depth && room.maxDepth >= depth )
			{
				rooms.add( room );
			}
		}

		return rooms;
	}

	// ----------------------------------------------------------------------
	public String getMajorFaction( Random ran )
	{
		int totalWeight = 0;
		for ( Faction fac : majorFactions )
		{
			totalWeight += fac.weight;
		}

		int ranVal = ran.nextInt( totalWeight );

		int currentWeight = 0;
		for ( Faction fac : majorFactions )
		{
			currentWeight += fac.weight;

			if ( currentWeight >= ranVal ) { return fac.name; }
		}

		return null;
	}

	// ----------------------------------------------------------------------
	public String getMinorFaction( Random ran )
	{
		int totalWeight = 0;
		for ( Faction fac : minorFactions )
		{
			totalWeight += fac.weight;
		}

		int ranVal = ran.nextInt( totalWeight );

		int currentWeight = 0;
		for ( Faction fac : minorFactions )
		{
			currentWeight += fac.weight;

			if ( currentWeight >= ranVal ) { return fac.name; }
		}

		return null;
	}

	// ----------------------------------------------------------------------
	public AbstractRoomGenerator getRoomGenerator( Random ran )
	{
		int totalWeight = 0;
		for ( RoomGenerator rg : roomGenerators )
		{
			totalWeight += rg.weight;
		}

		int target = ran.nextInt( totalWeight );
		int current = 0;

		for ( RoomGenerator rg : roomGenerators )
		{
			current += rg.weight;

			if ( current >= target ) { return rg.generator; }
		}

		return null;
	}

	// ----------------------------------------------------------------------
	private void internalLoad( String name )
	{
		XmlReader xml = new XmlReader();
		Element xmlElement = null;

		try
		{
			xmlElement = xml.parse( Gdx.files.internal( "Levels/" + name + ".xml" ) );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}

		Element roomGenElement = xmlElement.getChildByName( "RoomGenerators" );
		if ( roomGenElement != null )
		{
			for ( int i = 0; i < roomGenElement.getChildCount(); i++ )
			{
				Element roomGen = roomGenElement.getChild( i );

				RoomGenerator gen = new RoomGenerator();
				gen.generator = AbstractRoomGenerator.load( roomGen );
				gen.weight = roomGen.getInt( "Weight" );

				roomGenerators.add( gen );
			}
		}
		else
		{
			RoomGenerator gen = new RoomGenerator();
			gen.generator = new OverlappingRects();
			gen.weight = 1;

			roomGenerators.add( gen );
		}

		Element corridorElement = xmlElement.getChildByName( "CorridorStyle" );
		if ( corridorElement != null )
		{
			corridorStyle.parse( corridorElement );
		}

		Element preprocessorElement = xmlElement.getChildByName( "Preprocessor" );
		if ( preprocessorElement != null )
		{
			preprocessor = new RoomGenerator();
			preprocessor.generator = AbstractRoomGenerator.load( preprocessorElement.getChild( 0 ) );
		}

		Element factionsElement = xmlElement.getChildByName( "Factions" );

		Element majorFacElement = factionsElement.getChildByName( "Major" );
		for ( int i = 0; i < majorFacElement.getChildCount(); i++ )
		{
			Element facElement = majorFacElement.getChild( i );

			String facname = facElement.getName();
			int weight = Integer.parseInt( facElement.getText() );

			majorFactions.add( new Faction( facname, weight ) );
		}

		Element minorFacElement = factionsElement.getChildByName( "Minor" );
		for ( int i = 0; i < minorFacElement.getChildCount(); i++ )
		{
			Element facElement = minorFacElement.getChild( i );

			String facname = facElement.getName();
			int weight = Integer.parseInt( facElement.getText() );

			minorFactions.add( new Faction( facname, weight ) );
		}

		Element symbolsElement = xmlElement.getChildByName( "Symbols" );
		for ( int i = 0; i < symbolsElement.getChildCount(); i++ )
		{
			Symbol symbol = Symbol.parse( symbolsElement.getChild( i ), sharedSymbolMap, null );
			sharedSymbolMap.put( symbol.character, symbol );
		}

		Element requiredElement = xmlElement.getChildByName( "Required" );
		if ( requiredElement != null )
		{
			for ( int i = 0; i < requiredElement.getChildCount(); i++ )
			{
				DFPRoom room = DFPRoom.parse( requiredElement.getChild( i ), sharedSymbolMap );
				requiredRooms.add( room );
			}
		}

		Element optionalElement = xmlElement.getChildByName( "Optional" );
		if ( optionalElement != null )
		{
			for ( int i = 0; i < optionalElement.getChildCount(); i++ )
			{
				DFPRoom room = DFPRoom.parse( optionalElement.getChild( i ), sharedSymbolMap );
				optionalRooms.add( room );
			}
		}

		Element ae = xmlElement.getChildByName( "Ambient" );
		ambient = new Color( ae.getFloat( "Red" ), ae.getFloat( "Blue" ), ae.getFloat( "Green" ), ae.getFloat( "Alpha" ) );

		Element soundElement = xmlElement.getChildByName( "Sound" );
		BGM = soundElement.get( "BGM" );

		for ( Element ambientSound : soundElement.getChildByName( "Ambient" ).getChildrenByName( "Sound" ) )
		{
			ambientSounds.add( RepeatingSoundEffect.parse( ambientSound ) );
		}
	}

	// ----------------------------------------------------------------------
	public static DungeonFileParser load( String name )
	{
		DungeonFileParser dfp = new DungeonFileParser();

		dfp.internalLoad( name );

		return dfp;
	}
}
