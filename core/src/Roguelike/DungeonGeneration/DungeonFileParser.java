package Roguelike.DungeonGeneration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.DungeonGeneration.RoomGenerators.AbstractRoomGenerator;
import Roguelike.Sound.RepeatingSoundEffect;
import Roguelike.Sprite.Sprite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

public class DungeonFileParser
{
	// ----------------------------------------------------------------------
	public static final class CorridorStyle
	{
		public enum PathStyle
		{
			STRAIGHT, WANDERING
		}

		public PathStyle pathStyle = PathStyle.STRAIGHT;

		public int width = 1;

		public CorridorFeature centralConstant;
		public CorridorFeature centralRecurring;
		public CorridorFeature sideRecurring;

		public void parse( Element xml )
		{
			pathStyle = PathStyle.valueOf( xml.get( "PathStyle", "Straight" ).toUpperCase() );

			width = xml.getInt( "Width", 1 );

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
	public static final class CorridorFeature
	{
		public enum PlacementMode
		{
			BOTH, TOP, BOTTOM, ALTERNATE
		}

		public Symbol symbol;

		public int interval;

		public PlacementMode placementMode;

		public static CorridorFeature load( Element xml )
		{
			CorridorFeature feature = new CorridorFeature();

			feature.interval = xml.getInt( "Interval", 0 );
			feature.placementMode = PlacementMode.valueOf( xml.get( "PlacementMode", "Both" ).toUpperCase() );

			feature.symbol = Symbol.parse( xml );

			return feature;
		}

		public Symbol getAsSymbol( Symbol current, DungeonFileParser dfp )
		{
			symbol.resolveExtends( dfp.sharedSymbolMap );

			Symbol nsymbol = current.copy();
			nsymbol.character = 'C';
			nsymbol.tileData = symbol.tileData != null ? symbol.tileData : current.tileData;
			nsymbol.environmentData = symbol.environmentData != null ? symbol.environmentData : current.environmentData;

			return nsymbol;
		}
	}

	// ----------------------------------------------------------------------
	public static final class RoomGenerator
	{
		public AbstractRoomGenerator generator;
		public int weight;
	}

	// ----------------------------------------------------------------------
	public static final class Faction
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
	public static final class DFPRoom
	{
		// ----------------------------------------------------------------------
		public enum Placement
		{
			NORTH, SOUTH, EAST, WEST, CENTRE
		}

		public Placement placement;

		public boolean isTransition;

		public String spawnEquation;

		public String width;
		public String height;
		public HashMap<Character, Symbol> symbolMap = new HashMap<Character, Symbol>();
		public char[][] roomDef;
		public String faction;
		public boolean addFactionFeatures = true;
		public AbstractRoomGenerator generator;
		public String placementHint;

		public boolean lockRotation = false;

		private boolean symbolsResolved = false;

		public static DFPRoom parse( Element xml )
		{
			DFPRoom room = new DFPRoom();

			room.spawnEquation = xml.getAttribute( "Condition", "1" ).toLowerCase();

			room.placementHint = xml.get( "PlacementHint", null );

			room.faction = xml.get( "Faction", null );

			room.placement = Placement.valueOf( xml.get( "Placement", "Centre" ).toUpperCase() );

			room.lockRotation = xml.getBoolean( "LockRotation", false );

			room.addFactionFeatures = xml.getBoolean( "AddFeatures", true );

			Element rowsElement = xml.getChildByName( "Rows" );

			if ( rowsElement != null )
			{
				if ( rowsElement.getChildCount() > 0 )
				{
					// Rows defined here
					int width = 0;
					int height = rowsElement.getChildCount();

					for ( int i = 0; i < height; i++ )
					{
						if ( rowsElement.getChild( i ).getText().length() > width )
						{
							width = rowsElement.getChild( i ).getText().length();
						}
					}

					room.height = "" + height;
					room.width = "" + width;

					room.roomDef = new char[width][height];
					for ( int x = 0; x < width; x++ )
					{
						for ( int y = 0; y < height; y++ )
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

					int height = 0;
					int width = lines.length;

					String[][] rows = new String[lines.length][];
					for ( int i = 0; i < lines.length; i++ )
					{
						rows[i] = lines[i].split( " " );

						height = rows[i].length;
					}

					room.width = "" + width;
					room.height = "" + height;

					room.roomDef = new char[width][height];
					for ( int x = 0; x < width; x++ )
					{
						for ( int y = 0; y < height; y++ )
						{
							room.roomDef[x][y] = rows[x][y].charAt( 0 );
						}
					}
				}
			}
			else
			{
				Element generatorElement = xml.getChildByName( "Generator" );
				if (generatorElement != null)
				{
					generatorElement = generatorElement.getChild( 0 );
					room.generator = AbstractRoomGenerator.load( generatorElement );
				}

				room.width = xml.get( "Width", "rnd(6)+4" );
				room.height = xml.get( "Height", "rnd(6)+4" );
			}

			Element symbolsElement = xml.getChildByName( "Symbols" );
			if ( symbolsElement != null )
			{
				for ( int i = 0; i < symbolsElement.getChildCount(); i++ )
				{
					Symbol symbol = Symbol.parse( symbolsElement.getChild( i ) );
					room.symbolMap.put( symbol.character, symbol );

					if ( symbol.environmentData != null
							&& ( symbol.environmentData.get( "Type", "" ).equals( "Transition" ) || symbol.environmentData.get( "Type", "" ).equals( "Dungeon" ) ) )
					{
						room.isTransition = true;
					}
				}
			}

			return room;
		}

		public void resolveSymbols(HashMap<Character, Symbol> sharedSymbolMap)
		{
			for (Map.Entry<Character, Symbol> pair : sharedSymbolMap.entrySet())
			{
				symbolMap.put( pair.getKey(), pair.getValue().copy() );
			}

			for (Symbol s : symbolMap.values())
			{
				s.resolveExtends( symbolMap );
			}

			symbolsResolved = true;
		}

		public DFPRoom copy()
		{
			DFPRoom room = new DFPRoom();

			room.placement = placement;
			room.isTransition = isTransition;
			room.spawnEquation = spawnEquation;
			room.width = width;
			room.height = height;
			room.roomDef = roomDef;
			room.faction = faction;
			room.generator = generator;
			room.placementHint = placementHint;

			for ( Character key : symbolMap.keySet() )
			{
				room.symbolMap.put( key, symbolMap.get( key ).copy() );
			}

			return room;
		}

		public Symbol getSymbol( char c )
		{
			Symbol s = symbolMap.get( c );

			if ( s == null )
			{
				throw new RuntimeException( "Attempted to use undefined symbol: " + c );
			}

			return s;
		}

		public int processCondition( int depth, Random ran, boolean isBoss )
		{
			if ( Global.isNumber( spawnEquation ) )
			{
				return Integer.parseInt( spawnEquation );
			}
			else
			{
				ExpressionBuilder expB = EquationHelper.createEquationBuilder( spawnEquation, ran );
				expB.variable( "depth" );
				expB.variable( "boss" );

				Expression exp = EquationHelper.tryBuild( expB );
				if ( exp == null ) { return 0; }

				exp.setVariable( "depth", depth );
				exp.setVariable( "boss", isBoss ? 1 : 0 );

				int val = (int) exp.evaluate();

				return val;
			}
		}

		public void fillRoom( Room room, Random ran, DungeonFileParser dfp )
		{
			if (!symbolsResolved)
			{
				resolveSymbols( dfp.sharedSymbolMap );
			}

			room.roomData = this;
			room.width = EquationHelper.evaluate( width, Global.Statistic.emptyMap, ran );
			room.height = EquationHelper.evaluate( height, Global.Statistic.emptyMap, ran );
			room.roomContents = new Symbol[room.width][room.height];
			room.faction = faction;

			if ( generator != null )
			{
				Symbol floor = getSymbol( '.' );
				Symbol wall = getSymbol( '#' );

				floor.resolveExtends( dfp.sharedSymbolMap );
				wall.resolveExtends( dfp.sharedSymbolMap );

				room.generateRoomContents( ran, dfp, floor, wall, generator );
			}
			else if ( roomDef == null )
			{
				Symbol floor = getSymbol( '.' );
				Symbol wall = getSymbol( '#' );

				floor.resolveExtends( dfp.sharedSymbolMap );
				wall.resolveExtends( dfp.sharedSymbolMap );

				AbstractRoomGenerator generator = dfp.getRoomGenerator( ran );

				room.generateRoomContents( ran, dfp, floor, wall, generator );
			}
			else
			{
				for ( int x = 0; x < room.width; x++ )
				{
					for ( int y = 0; y < room.height; y++ )
					{
						room.roomContents[x][y] = getSymbol( roomDef[x][y] );
					}
				}
			}
		}
	}

	// ----------------------------------------------------------------------
	public String generator;

	// ----------------------------------------------------------------------
	public HashMap<Character, Symbol> sharedSymbolMap = new HashMap<Character, Symbol>();

	// ----------------------------------------------------------------------
	public Array<DFPRoom> rooms = new Array<DFPRoom>();

	// ----------------------------------------------------------------------
	public Array<Faction> majorFactions = new Array<Faction>();

	// ----------------------------------------------------------------------
	public Array<Faction> minorFactions = new Array<Faction>();

	// ----------------------------------------------------------------------
	public Color ambient;

	// ----------------------------------------------------------------------
	public Sprite background;

	// ----------------------------------------------------------------------
	public boolean affectedByDayNight = false;

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
	public int minWidth;
	public int minHeight;

	// ----------------------------------------------------------------------
	public HashMap<String, DFPRoom[]> entranceRooms = new HashMap<String, DFPRoom[]>(  );

	// ----------------------------------------------------------------------
	public Symbol getSymbol( char c )
	{
		Symbol s = sharedSymbolMap.get( c );

		if ( s == null )
		{
			System.out.println( "Failed to find symbol for character '" + c + "'! Falling back to using '.'" );
			s = sharedSymbolMap.get( '.' );
		}

		return s;
	}

	// ----------------------------------------------------------------------
	public void addDefaultSymbols()
	{
		for (int i = 0; i < 10; i++)
		{
			char c = (""+i).charAt( 0 );
			if (!sharedSymbolMap.containsKey( c ))
			{
				Symbol symbol = new Symbol();
				symbol.character = c;
				symbol.entityData = ""+c;
				symbol.extendsSymbol = '.';

				sharedSymbolMap.put( c, symbol );
			}
		}

		if (!sharedSymbolMap.containsKey( 'B' ))
		{
			Symbol symbol = new Symbol();
			symbol.character = 'B';
			symbol.entityData = "Boss";
			symbol.extendsSymbol = '.';

			sharedSymbolMap.put( 'B', symbol );
		}
	}

	// ----------------------------------------------------------------------
	public Array<DFPRoom> getRooms( int depth, Random ran, boolean isBoss, FactionParser majorFaction, Array<FactionParser> minorFactions )
	{
		Array<DFPRoom> outRooms = new Array<DFPRoom>();

		for ( DFPRoom room : rooms )
		{
			int count = room.processCondition( depth, ran, isBoss );
			for ( int i = 0; i < count; i++ )
			{
				outRooms.add( room.copy() );
			}
		}

		for (DFPRoom room : majorFaction.rooms)
		{
			int count = room.processCondition( depth, ran, isBoss );
			for ( int i = 0; i < count; i++ )
			{
				DFPRoom cpy = room.copy();
				cpy.faction = majorFaction.name;
				outRooms.add( cpy );
			}
		}

		for (FactionParser faction : minorFactions)
		{
			if (ran.nextInt( 3 ) == 0)
			{
				Array<DFPRoom> validRooms = new Array<DFPRoom>(  );

				for (DFPRoom room : faction.rooms)
				{
					int count = room.processCondition( depth, ran, false );
					for ( int i = 0; i < count; i++ )
					{
						DFPRoom cpy = room.copy();
						cpy.faction = faction.name;
						validRooms.add( cpy );
					}
				}

				if (validRooms.size > 0)
				{
					outRooms.add( validRooms.get( ran.nextInt( validRooms.size ) ) );
				}
			}
		}

		return outRooms;
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

		generator = xmlElement.get( "Generator", "RecursiveDock" );

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

		Element backgroundElement = xmlElement.getChildByName( "Background" );
		if ( backgroundElement != null )
		{
			background = AssetManager.loadSprite( backgroundElement );
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
		if ( factionsElement != null )
		{
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
		}

		Element symbolsElement = xmlElement.getChildByName( "Symbols" );
		for ( int i = 0; i < symbolsElement.getChildCount(); i++ )
		{
			Symbol symbol = Symbol.parse( symbolsElement.getChild( i ) );
			sharedSymbolMap.put( symbol.character, symbol );
		}
		addDefaultSymbols();

		for (Element el : xmlElement.getChildrenByName( "Entrance" ))
		{
			String key = el.getAttribute( "Key", "all" ).toLowerCase();

			DFPRoom prevRoom = DFPRoom.parse( el.getChildByName( "Prev" ) );
			DFPRoom thisRoom = DFPRoom.parse( el.getChildByName( "This" ) );

			entranceRooms.put( key, new DFPRoom[]{ prevRoom, thisRoom } );
		}

		Element roomsElement = xmlElement.getChildByName( "Rooms" );
		if ( roomsElement != null )
		{
			for ( int i = 0; i < roomsElement.getChildCount(); i++ )
			{
				DFPRoom room = DFPRoom.parse( roomsElement.getChild( i ) );
				rooms.add( room );
			}
		}

		minWidth = xmlElement.getInt( "MinWidth", 10 );
		minHeight = xmlElement.getInt( "MinHeight", 10 );

		Element ae = xmlElement.getChildByName( "Ambient" );
		ambient = new Color( ae.getFloat( "Red", 1 ), ae.getFloat( "Blue", 1 ), ae.getFloat( "Green", 1 ), ae.getFloat( "Alpha", 1 ) );
		affectedByDayNight = ae.getBoolean( "AffectedByDayNight", false );

		Element soundElement = xmlElement.getChildByName( "Sound" );
		BGM = soundElement.get( "BGM" );

		Element ambientElement = soundElement.getChildByName( "Ambient" );
		if (ambientElement != null)
		{
			for ( Element ambientSound : ambientElement.getChildrenByName( "Sound" ) )
			{
				ambientSounds.add( RepeatingSoundEffect.parse( ambientSound ) );
			}
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
