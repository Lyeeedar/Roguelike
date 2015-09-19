package Roguelike;

import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.RoguelikeGame.ScreenEnum;
import Roguelike.Ability.AbilityPool;
import Roguelike.Dialogue.DialogueManager;
import Roguelike.Entity.Entity;
import Roguelike.Entity.GameEntity;
import Roguelike.GameEvent.GameEventHandler;
import Roguelike.GameEvent.Damage.DamageObject;
import Roguelike.Levels.Dungeon;
import Roguelike.Levels.Level;
import Roguelike.Save.SaveAbilityPool;
import Roguelike.Save.SaveDungeon;
import Roguelike.Save.SaveFile;
import Roguelike.Save.SaveLevel;
import Roguelike.Screens.GameScreen;
import Roguelike.Screens.LoadingScreen;
import Roguelike.Screens.LoadingScreen.PostGenerateEvent;
import Roguelike.Sound.Mixer;
import Roguelike.Sound.RepeatingSoundEffect;
import Roguelike.Tiles.GameTile;
import Roguelike.Tiles.Point;
import Roguelike.UI.MessageStack.Line;
import Roguelike.UI.MessageStack.Message;
import Roguelike.Util.EnumBitflag;
import Roguelike.Util.FastEnumMap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

public class Global
{
	// ----------------------------------------------------------------------
	public static RoguelikeGame Game;

	// ----------------------------------------------------------------------
	public static AbstractApplicationChanger ApplicationChanger;

	// ----------------------------------------------------------------------
	public static boolean ANDROID = false;

	// ----------------------------------------------------------------------
	public static DialogueManager CurrentDialogue = null;

	// ----------------------------------------------------------------------
	public static float AnimationSpeed = 1;

	// ----------------------------------------------------------------------
	public static int FPS = 0;

	// ----------------------------------------------------------------------
	public static int[] ScreenSize = { 600, 400 };

	// ----------------------------------------------------------------------
	public static int[] Resolution = { 600, 400 };

	// ----------------------------------------------------------------------
	public static int[] TargetResolution = { 600, 400 };

	// ----------------------------------------------------------------------
	public static Skin skin;

	// ----------------------------------------------------------------------
	public static final int NUM_ABILITY_SLOTS = 8;

	// ----------------------------------------------------------------------
	public static int TileSize = 32;

	// ----------------------------------------------------------------------
	public static Level CurrentLevel;

	// ----------------------------------------------------------------------
	public static String PlayerName = "Jeff";

	// ----------------------------------------------------------------------
	public static String PlayerTitle = "Adventurer";

	// ----------------------------------------------------------------------
	public static HashMap<String, Dungeon> Dungeons = new HashMap<String, Dungeon>();

	// ----------------------------------------------------------------------
	public static HashMap<String, Integer> GlobalVariables = new HashMap<String, Integer>();

	// ----------------------------------------------------------------------
	public static HashMap<String, String> GlobalNames = new HashMap<String, String>();

	// ----------------------------------------------------------------------
	public static Mixer BGM;

	// ----------------------------------------------------------------------
	public static float AUT = 0;

	// ----------------------------------------------------------------------
	public static float DayNightFactor = 1;

	// ----------------------------------------------------------------------
	public static AbilityPool abilityPool;

	// ----------------------------------------------------------------------
	public enum Direction
	{
		CENTER( 0, 0 ), NORTH( 0, 1 ), SOUTH( 0, -1 ), EAST( 1, 0 ), WEST( -1, 0 ), NORTHEAST( 1, 1 ), NORTHWEST( -1, 1 ), SOUTHEAST( 1, -1 ), SOUTHWEST(
				-1,
				-1 );

		private final int x;
		private final int y;
		private final float angle;

		private Direction clockwise;
		private Direction anticlockwise;

		private boolean isCardinal = false;

		static
		{
			// Setup neighbours
			Direction.CENTER.clockwise = Direction.CENTER;
			Direction.CENTER.anticlockwise = Direction.CENTER;

			Direction.NORTH.anticlockwise = Direction.NORTHWEST;
			Direction.NORTH.clockwise = Direction.NORTHEAST;

			Direction.NORTHEAST.anticlockwise = Direction.NORTH;
			Direction.NORTHEAST.clockwise = Direction.EAST;

			Direction.EAST.anticlockwise = Direction.NORTHEAST;
			Direction.EAST.clockwise = Direction.SOUTHEAST;

			Direction.SOUTHEAST.anticlockwise = Direction.EAST;
			Direction.SOUTHEAST.clockwise = Direction.SOUTH;

			Direction.SOUTH.anticlockwise = Direction.SOUTHEAST;
			Direction.SOUTH.clockwise = Direction.SOUTHWEST;

			Direction.SOUTHWEST.anticlockwise = Direction.SOUTH;
			Direction.SOUTHWEST.clockwise = Direction.WEST;

			Direction.WEST.anticlockwise = Direction.SOUTHWEST;
			Direction.WEST.clockwise = Direction.NORTHWEST;

			Direction.NORTHWEST.anticlockwise = Direction.WEST;
			Direction.NORTHWEST.clockwise = Direction.NORTH;

			// Setup is cardinal
			Direction.NORTH.isCardinal = true;
			Direction.SOUTH.isCardinal = true;
			Direction.EAST.isCardinal = true;
			Direction.WEST.isCardinal = true;
		}

		Direction( int x, int y )
		{
			this.x = x;
			this.y = y;

			// basis vector = 0, 1
			double dot = 0 * x + 1 * y; // dot product
			double det = 0 * y - 1 * x; // determinant
			angle = (float) Math.atan2( det, dot ) * MathUtils.radiansToDegrees;
		}

		public boolean isCardinal()
		{
			return isCardinal;
		}

		public int getX()
		{
			return x;
		}

		public int getY()
		{
			return y;
		}

		public float getAngle()
		{
			return angle;
		}

		public Direction getOpposite()
		{
			return getDirection( x * -1, y * -1 );
		}

		public Direction getClockwise()
		{
			return clockwise;
		}

		public Direction getAnticlockwise()
		{
			return anticlockwise;
		}

		public static Direction getDirection( int x, int y )
		{
			x = MathUtils.clamp( x, -1, 1 );
			y = MathUtils.clamp( y, -1, 1 );

			Direction d = Direction.CENTER;

			for ( Direction dir : Direction.values() )
			{
				if ( dir.getX() == x && dir.getY() == y )
				{
					d = dir;
					break;
				}
			}

			return d;
		}

		public static Direction getDirection( int[] dir )
		{
			return getDirection( dir[0], dir[1] );
		}

		public static Direction getDirection( GameTile t1, GameTile t2 )
		{
			return getDirection( t2.x - t1.x, t2.y - t1.y );
		}

		public static Array<Point> buildCone( Direction dir, Point start, int range )
		{
			Array<Point> hitTiles = new Array<Point>();

			Direction anticlockwise = dir.getAnticlockwise();
			Direction clockwise = dir.getClockwise();

			Point acwOffset = Pools.obtain( Point.class ).set( dir.getX() - anticlockwise.getX(), dir.getY() - anticlockwise.getY() );
			Point cwOffset = Pools.obtain( Point.class ).set( dir.getX() - clockwise.getX(), dir.getY() - clockwise.getY() );

			hitTiles.add( Pools.obtain( Point.class ).set( start.x + anticlockwise.getX(), start.y + anticlockwise.getY() ) );

			hitTiles.add( Pools.obtain( Point.class ).set( start.x + dir.getX(), start.y + dir.getY() ) );

			hitTiles.add( Pools.obtain( Point.class ).set( start.x + clockwise.getX(), start.y + clockwise.getY() ) );

			for ( int i = 2; i <= range; i++ )
			{
				int acx = start.x + anticlockwise.getX() * i;
				int acy = start.y + anticlockwise.getY() * i;

				int nx = start.x + dir.getX() * i;
				int ny = start.y + dir.getY() * i;

				int cx = start.x + clockwise.getX() * i;
				int cy = start.y + clockwise.getY() * i;

				// add base tiles
				hitTiles.add( Pools.obtain( Point.class ).set( acx, acy ) );
				hitTiles.add( Pools.obtain( Point.class ).set( nx, ny ) );
				hitTiles.add( Pools.obtain( Point.class ).set( cx, cy ) );

				// add anticlockwise - mid
				for ( int ii = 1; ii <= range; ii++ )
				{
					int px = acx + acwOffset.x * ii;
					int py = acy + acwOffset.y * ii;

					hitTiles.add( Pools.obtain( Point.class ).set( px, py ) );
				}

				// add mid - clockwise
				for ( int ii = 1; ii <= range; ii++ )
				{
					int px = cx + cwOffset.x * ii;
					int py = cy + cwOffset.y * ii;

					hitTiles.add( Pools.obtain( Point.class ).set( px, py ) );
				}
			}

			Pools.free( acwOffset );
			Pools.free( cwOffset );

			return hitTiles;
		}
	}

	// ----------------------------------------------------------------------
	public enum Passability
	{
		WALK( Statistic.WALK ), LEVITATE( Statistic.LEVITATE ), LIGHT( Statistic.LIGHT ), ENTITY( Statistic.ENTITY );

		public final Statistic stat;

		private Passability( Statistic stat )
		{
			this.stat = stat;
		}

		public static EnumBitflag<Passability> variableMapToTravelType( HashMap<String, Integer> stats )
		{
			EnumBitflag<Passability> travelType = new EnumBitflag<Passability>();

			for ( Passability p : Passability.values() )
			{
				String checkString = p.stat.toString().toLowerCase();
				if ( stats.containsKey( checkString ) )
				{
					int val = stats.get( checkString );

					if ( val > 0 )
					{
						travelType.setBit( p );
					}
				}
			}

			return travelType;
		}

		public static EnumBitflag<Passability> parse( String passable )
		{
			EnumBitflag<Passability> passableBy = new EnumBitflag<Passability>();

			if ( passable != null )
			{
				if ( passable.equalsIgnoreCase( "true" ) )
				{
					// all
					for ( Passability p : Passability.values() )
					{
						passableBy.setBit( p );
					}
				}
				else if ( passable.equalsIgnoreCase( "false" ) )
				{
					// none
				}
				else
				{
					String[] split = passable.split( "," );
					for ( String p : split )
					{
						passableBy.setBit( Passability.valueOf( p.toUpperCase() ) );
					}
				}
			}

			return passableBy;
		}

		public static EnumBitflag<Passability> parseArray( String passable )
		{
			EnumBitflag<Passability> passableBy = new EnumBitflag<Passability>();

			if ( passable != null )
			{
				if ( passable.equalsIgnoreCase( "true" ) )
				{
					// all
					for ( Passability p : Passability.values() )
					{
						passableBy.setBit( p );
					}
				}
				else if ( passable.equalsIgnoreCase( "false" ) )
				{
					// none
				}
				else
				{
					String[] split = passable.split( "," );
					for ( String p : split )
					{
						passableBy.setBit( Passability.valueOf( p.toUpperCase() ) );
					}
				}
			}

			return passableBy;
		}

	}

	// ----------------------------------------------------------------------
	public enum Statistic
	{
		// Basic stats
		MAXHP,
		RANGE,

		// Passability
		WALK,
		LEVITATE,
		LIGHT,
		ENTITY,

		// Elemental stats
		AETHER_ATK,
		AETHER_PIERCE,
		AETHER_DEF,
		AETHER_HARDINESS,

		VOID_ATK,
		VOID_PIERCE,
		VOID_DEF,
		VOID_HARDINESS,

		METAL_ATK,
		METAL_PIERCE,
		METAL_DEF,
		METAL_HARDINESS,

		WOOD_ATK,
		WOOD_PIERCE,
		WOOD_DEF,
		WOOD_HARDINESS,

		AIR_ATK,
		AIR_PIERCE,
		AIR_DEF,
		AIR_HARDINESS,

		WATER_ATK,
		WATER_PIERCE,
		WATER_DEF,
		WATER_HARDINESS,

		FIRE_ATK,
		FIRE_PIERCE,
		FIRE_DEF,
		FIRE_HARDINESS;

		public static HashMap<String, Integer> emptyMap = new HashMap<String, Integer>();
		static
		{
			for ( Statistic s : Statistic.values() )
			{
				emptyMap.put( s.toString().toLowerCase(), 0 );
			}
		}

		public static HashMap<String, Integer> statsBlockToVariableBlock( FastEnumMap<Statistic, Integer> stats )
		{
			HashMap<String, Integer> variableMap = new HashMap<String, Integer>();

			for ( Statistic key : Statistic.values() )
			{
				Integer val = stats.get( key );
				if ( val != null )
				{
					variableMap.put( key.toString().toLowerCase(), val );
				}
			}

			return variableMap;
		}

		public static FastEnumMap<Statistic, Integer> getStatisticsBlock()
		{
			FastEnumMap<Statistic, Integer> stats = new FastEnumMap<Statistic, Integer>( Statistic.class );

			for ( Statistic stat : Statistic.values() )
			{
				stats.put( stat, 0 );
			}

			return stats;
		}

		public static FastEnumMap<Statistic, Integer> load( Element xml, FastEnumMap<Statistic, Integer> values )
		{
			for ( int i = 0; i < xml.getChildCount(); i++ )
			{

				Element el = xml.getChild( i );

				Statistic stat = Statistic.valueOf( el.getName().toUpperCase() );
				String eqn = el.getText();
				int newVal = values.get( stat );

				ExpressionBuilder expB = EquationHelper.createEquationBuilder( eqn );
				expB.variable( "Value" );

				Expression exp = EquationHelper.tryBuild( expB );
				if ( exp != null )
				{
					exp.setVariable( "Value", newVal );
					newVal = (int) exp.evaluate();
				}

				values.put( stat, newVal );
			}

			return values;
		}

		public static FastEnumMap<Statistic, Integer> copy( FastEnumMap<Statistic, Integer> map )
		{
			FastEnumMap<Statistic, Integer> newMap = new FastEnumMap<Statistic, Integer>( Statistic.class );
			for ( Statistic e : Statistic.values() )
			{
				newMap.put( e, map.get( e ) );
			}
			return newMap;
		}

		public static String formatString( String input )
		{
			String[] words = input.split( "_" );

			String output = "";

			for ( String word : words )
			{
				word = word.toLowerCase();
				output += word + " ";
			}

			return output.trim();
		}
	}

	// ----------------------------------------------------------------------
	public enum Tier1Element
	{
		AETHER( Color.RED ), VOID( Color.DARK_GRAY ), METAL( Color.LIGHT_GRAY ), WOOD( Color.GREEN ), AIR( Color.CYAN ), WATER( Color.BLUE ), FIRE(
				Color.ORANGE );

		public Tier1Element Weakness;
		public Tier1Element Strength;

		public Statistic Attack;
		public Statistic Pierce;
		public Statistic Defense;
		public Statistic Hardiness;

		public Color Colour;

		static
		{
			Tier1Element.METAL.Weakness = Tier1Element.FIRE;
			Tier1Element.METAL.Strength = Tier1Element.WOOD;

			Tier1Element.WOOD.Weakness = Tier1Element.METAL;
			Tier1Element.WOOD.Strength = Tier1Element.AIR;

			Tier1Element.AIR.Weakness = Tier1Element.WOOD;
			Tier1Element.AIR.Strength = Tier1Element.WATER;

			Tier1Element.WATER.Weakness = Tier1Element.AIR;
			Tier1Element.WATER.Strength = Tier1Element.FIRE;

			Tier1Element.FIRE.Weakness = Tier1Element.WATER;
			Tier1Element.FIRE.Strength = Tier1Element.METAL;
		}

		Tier1Element( Color colour )
		{
			this.Colour = colour;

			Attack = Statistic.valueOf( toString() + "_ATK" );
			Pierce = Statistic.valueOf( toString() + "_PIERCE" );
			Defense = Statistic.valueOf( toString() + "_DEF" );
			Hardiness = Statistic.valueOf( toString() + "_HARDINESS" );

			Colors.put( this.toString(), this.Colour );
		}

		public static FastEnumMap<Tier1Element, Integer> getElementBlock()
		{
			FastEnumMap<Tier1Element, Integer> map = new FastEnumMap<Tier1Element, Integer>( Tier1Element.class );

			for ( Tier1Element el : Tier1Element.values() )
			{
				map.put( el, 0 );
			}

			return map;
		}

		public static FastEnumMap<Tier1Element, Integer> load( Element xml, FastEnumMap<Tier1Element, Integer> values )
		{
			for ( int i = 0; i < xml.getChildCount(); i++ )
			{
				Element el = xml.getChild( i );

				Tier1Element elem = Tier1Element.valueOf( el.getName().toUpperCase() );
				String eqn = el.getText();
				int newVal = values.get( elem );

				ExpressionBuilder expB = EquationHelper.createEquationBuilder( eqn );
				expB.variable( "Value" );

				Expression exp = EquationHelper.tryBuild( expB );
				if ( exp != null )
				{
					exp.setVariable( "Value", newVal );
					newVal = (int) exp.evaluate();
				}

				values.put( elem, newVal );
			}

			return values;
		}

		public static FastEnumMap<Tier1Element, Integer> copy( FastEnumMap<Tier1Element, Integer> map )
		{
			FastEnumMap<Tier1Element, Integer> newMap = new FastEnumMap<Tier1Element, Integer>( Tier1Element.class );
			for ( Tier1Element e : Tier1Element.values() )
			{
				newMap.put( e, map.get( e ) );
			}
			return newMap;
		}
	}

	// ----------------------------------------------------------------------
	public enum Tier1ComboHarmful
	{
		POISON( Tier1Element.METAL, Tier1Element.WOOD ),
		PARALYZE( Tier1Element.METAL, Tier1Element.AIR ),
		TORPOR( Tier1Element.METAL, Tier1Element.WATER ),
		IMMOLATE( Tier1Element.METAL, Tier1Element.FIRE ),
		MINDSHOCK( Tier1Element.WOOD, Tier1Element.AIR ),
		CORROSION( Tier1Element.WOOD, Tier1Element.WATER ),
		ACID( Tier1Element.WOOD, Tier1Element.FIRE ),
		ICE( Tier1Element.WATER, Tier1Element.AIR ),
		PLASMA( Tier1Element.WATER, Tier1Element.FIRE ),
		LIGHTNING( Tier1Element.AIR, Tier1Element.FIRE );

		public final Tier1Element[] Tier1Elements;

		Tier1ComboHarmful( Tier1Element e1, Tier1Element e2 )
		{
			Tier1Elements = new Tier1Element[] { e1, e2 };
		}
	}

	// ----------------------------------------------------------------------
	public enum Tier2ElementHelpful
	{
		REGENERATION( Tier1Element.METAL, Tier1Element.WOOD ),
		DODGE( Tier1Element.METAL, Tier1Element.AIR ),
		HASTE( Tier1Element.METAL, Tier1Element.WATER ),
		POWER( Tier1Element.METAL, Tier1Element.FIRE ),
		STABILITY( Tier1Element.WOOD, Tier1Element.AIR ),
		PROTECTION( Tier1Element.WOOD, Tier1Element.WATER ),
		RETALIATION( Tier1Element.WOOD, Tier1Element.FIRE ),
		ICECHARGE( Tier1Element.WATER, Tier1Element.AIR ),
		PLASMACHARGE( Tier1Element.WATER, Tier1Element.FIRE ),
		LIGHTNINGCHARGE( Tier1Element.AIR, Tier1Element.FIRE );

		public final Tier1Element[] Tier1Elements;

		Tier2ElementHelpful( Tier1Element e1, Tier1Element e2 )
		{
			Tier1Elements = new Tier1Element[] { e1, e2 };
		}
	}

	// ----------------------------------------------------------------------
	public static void save()
	{
		SaveFile save = new SaveFile();

		CurrentLevel.dungeon.getSaveLevel( CurrentLevel.UID ).store( CurrentLevel );

		for ( Dungeon dungeon : Dungeons.values() )
		{
			SaveDungeon saveDungeon = new SaveDungeon();
			saveDungeon.store( dungeon );
			save.dungeons.add( saveDungeon );
		}

		save.currentDungeon = Global.CurrentLevel.dungeon.UID;
		save.currentLevel = Global.CurrentLevel.UID;
		save.globalVariables = (HashMap<String, Integer>) GlobalVariables.clone();
		save.globalNames = (HashMap<String, String>) GlobalNames.clone();

		save.abilityPool = new SaveAbilityPool();
		save.abilityPool.store( Global.abilityPool );

		save.AUT = AUT;

		save.save();
	}

	// ----------------------------------------------------------------------
	public static void load()
	{
		SaveFile save = new SaveFile();
		save.load();

		for ( SaveDungeon saveDungeon : save.dungeons )
		{
			Dungeon dungeon = saveDungeon.create();
			Dungeons.put( dungeon.UID, dungeon );
		}

		Global.abilityPool = save.abilityPool.create();
		Global.GlobalVariables = (HashMap<String, Integer>) save.globalVariables.clone();
		Global.GlobalNames = (HashMap<String, String>) save.globalNames.clone();

		Dungeon dungeon = Dungeons.get( save.currentDungeon );
		SaveLevel level = dungeon.getSaveLevel( save.currentLevel );

		LoadingScreen.Instance.set( dungeon, level, null, null, null );
		RoguelikeGame.Instance.switchScreen( ScreenEnum.LOADING );

		AUT = save.AUT;
		DayNightFactor = (float) ( 0.1f + ( ( ( Math.sin( AUT / 100.0f ) + 1.0f ) / 2.0f ) * 0.9f ) );
	}

	// ----------------------------------------------------------------------
	public static void newGame( GameEntity player, final String lines )
	{
		AUT = 0;
		DayNightFactor = (float) ( 0.1f + ( ( ( Math.sin( AUT / 100.0f ) + 1.0f ) / 2.0f ) * 0.9f ) );

		Dungeons.clear();
		GlobalVariables.clear();
		GlobalNames.clear();

		GlobalNames.put( "player", PlayerName );

		// player.size = 2;
		// player.sprite.size = 2;
		// player.tile = new GameTile[2][2];

		player.setPopupText( "Urgh... Where am I... Who am I...", 2 );

		Dungeon dungeon = new Dungeon( null, null, "Village", -1 );
		SaveLevel firstLevel = new SaveLevel( "Town", 0, null, MathUtils.random( Long.MAX_VALUE - 1 ) );
		Dungeons.put( dungeon.UID, dungeon );

		LoadingScreen.Instance.set( dungeon, firstLevel, player, "PlayerSpawn", new PostGenerateEvent()
		{
			@Override
			public void execute( Level level )
			{
				abilityPool = AbilityPool.createAbilityPool( lines );
			}
		} );

		RoguelikeGame.Instance.switchScreen( ScreenEnum.LOADING );
	}

	// ----------------------------------------------------------------------
	public static void ChangeLevel( Level level, GameEntity player, Object travelData )
	{
		if ( CurrentLevel != null )
		{
			for ( RepeatingSoundEffect sound : CurrentLevel.ambientSounds )
			{
				sound.stop();
			}

			CurrentLevel.player.tile[0][0].entity = null;
			// CurrentLevel.player = null;

			// Save
			SaveLevel save = CurrentLevel.dungeon.getSaveLevel( CurrentLevel.UID );
			save.store( CurrentLevel );

			CurrentLevel.player.tile[0][0].entity = CurrentLevel.player;
		}

		CurrentLevel = level;

		if ( BGM != null )
		{
			BGM.mix( level.bgmName, 1 );
		}
		else
		{
			BGM = new Mixer( level.bgmName, 0.1f );
		}

		if ( player != null )
		{
			level.player = player;

			if ( travelData instanceof String )
			{
				String travelKey = (String) travelData;

				outer:
				for ( int x = 0; x < level.width; x++ )
				{
					for ( int y = 0; y < level.height; y++ )
					{
						GameTile tile = level.getGameTile( x, y );
						if ( tile.metaValue != null )
						{
							if ( tile.metaValue.equals( travelKey ) )
							{
								tile.addGameEntity( player );
								break outer;
							}
						}

						if ( tile.environmentEntity != null && tile.environmentEntity.data.size() > 0 )
						{
							if ( tile.environmentEntity.data.containsKey( travelKey ) )
							{
								tile.addGameEntity( player );
								break outer;
							}
						}
					}
				}
			}
			else if ( travelData instanceof Point )
			{
				GameTile tile = level.getGameTile( (Point) travelData );
				tile.addGameEntity( player );
			}
		}

		CurrentLevel.updateVisibleTiles();

		save();
	}

	// ----------------------------------------------------------------------
	public static void calculateDamage( Entity attacker, Entity defender, HashMap<String, Integer> attackerVariableMap, boolean doEvents )
	{
		DamageObject damObj = new DamageObject( attacker, defender, attackerVariableMap );
		runEquations( damObj );

		if ( doEvents )
		{
			for ( GameEventHandler handler : attacker.getAllHandlers() )
			{
				handler.onDealDamage( damObj );
			}

			for ( GameEventHandler handler : defender.getAllHandlers() )
			{
				handler.onReceiveDamage( damObj );
			}
		}

		printMessages( damObj );

		defender.applyDamage( damObj.getTotalDamage(), attacker );
	}

	// ----------------------------------------------------------------------
	private static void runEquations( DamageObject damObj )
	{
		for ( Tier1Element el : Tier1Element.values() )
		{
			float attack = damObj.attackerVariableMap.get( el.Attack.toString().toLowerCase() );
			float pierce = damObj.attackerVariableMap.get( el.Pierce.toString().toLowerCase() );

			float accumulatedReduction = 0;
			{
				float defense = damObj.defender.statistics.get( el.Defense );
				float hardiness = 1.0f - (float) damObj.defender.statistics.get( el.Hardiness ) / 100.0f;

				float maxReduction = attack * hardiness;

				accumulatedReduction += Math.max( 0, Math.min( defense, maxReduction ) - pierce );
			}

			HashMap<String, Integer> defenderVariableMap = damObj.defender.getBaseVariableMap();
			for ( GameEventHandler handler : damObj.defender.getAllHandlers() )
			{
				float defense = handler.getStatistic( defenderVariableMap, el.Defense );
				float hardiness = 1.0f - handler.getStatistic( defenderVariableMap, el.Hardiness ) / 100.0f;

				float maxReduction = attack * hardiness;

				accumulatedReduction += Math.max( 0, Math.min( defense, maxReduction ) - pierce );
			}

			attack -= accumulatedReduction;
			attack = Math.max( attack, 0 );

			damObj.damageMap.put( el, MathUtils.ceil( attack ) );
		}

		damObj.setDamageVariables();
	}

	// ----------------------------------------------------------------------
	private static void printMessages( DamageObject damObj )
	{
		Line line = new Line( new Message( " (" ) );

		boolean first = true;
		for ( Tier1Element el : Tier1Element.values() )
		{
			int dam = damObj.damageMap.get( el );

			if ( dam != 0 )
			{
				if ( first )
				{
					first = false;
					line.messages.add( new Message( "" + dam, el.Colour ) );
				}
				else
				{
					line.messages.add( new Message( ", " + dam, el.Colour ) );
				}
			}
		}

		line.messages.add( new Message( ")" ) );

		line.messages.insert( 0, new Message( damObj.attacker.name + " hits " + damObj.defender.name + " for " + damObj.getTotalDamage() + " damage!" ) );

		if ( damObj.getTotalDamage() > 0 )
		{
			GameScreen.Instance.addConsoleMessage( line );
		}
	}

	// ----------------------------------------------------------------------
	public static boolean isNumber( String string )
	{
		if ( string == null || string.isEmpty() ) { return false; }
		int i = 0;
		if ( string.charAt( 0 ) == '-' )
		{
			if ( string.length() > 1 )
			{
				i++;
			}
			else
			{
				return false;
			}
		}
		for ( ; i < string.length(); i++ )
		{
			if ( !Character.isDigit( string.charAt( i ) ) ) { return false; }
		}
		return true;
	}

	// ----------------------------------------------------------------------
	public static int calculateScaleBonusDam( int baseDam, int scaleLevel, int stat )
	{
		if ( stat < 100 ) { return 0; }

		float scaleRange = baseDam * scaleLevel;
		float alpha = MathUtils.log2( stat / 100 );

		float val = scaleRange * alpha;

		return (int) Math.floor( val );
	}

	// ----------------------------------------------------------------------
	public static String expandNames( String input )
	{
		String[] split = input.split( "\\$" );

		boolean skip = !input.startsWith( "\\$" );

		String output = "";

		for ( int i = 0; i < split.length; i++ )
		{
			if ( skip )
			{
				output += split[i];
			}
			else
			{
				output += GlobalNames.get( split[i].toLowerCase() );
			}

			skip = !skip;
		}

		return output;
	}

	// ----------------------------------------------------------------------
	public static String capitalizeString( String s )
	{
		return s.substring( 0, 1 ).toUpperCase() + s.substring( 1 ).toLowerCase();
	}
}
