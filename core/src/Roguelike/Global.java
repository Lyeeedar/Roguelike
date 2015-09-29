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
import Roguelike.Lights.Light;
import Roguelike.Save.SaveAbilityPool;
import Roguelike.Save.SaveDungeon;
import Roguelike.Save.SaveFile;
import Roguelike.Save.SaveLevel;
import Roguelike.Screens.LoadingScreen;
import Roguelike.Screens.LoadingScreen.PostGenerateEvent;
import Roguelike.Sound.Mixer;
import Roguelike.Sound.RepeatingSoundEffect;
import Roguelike.Tiles.GameTile;
import Roguelike.Tiles.GameTile.LightData;
import Roguelike.Tiles.Point;
import Roguelike.Tiles.SeenTile.SeenHistoryItem;
import Roguelike.UI.Tooltip.TooltipStyle;
import Roguelike.Util.EnumBitflag;
import Roguelike.Util.FastEnumMap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar.ProgressBarStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
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
	public static Pool<Point> PointPool = Pools.get( Point.class );
	public static Pool<Light> LightPool = Pools.get( Light.class );
	public static Pool<LightData> LightDataPool = Pools.get( LightData.class );
	public static Pool<SeenHistoryItem> SeenHistoryItemPool = Pools.get( SeenHistoryItem.class );

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

			Point acwOffset = PointPool.obtain().set( dir.getX() - anticlockwise.getX(), dir.getY() - anticlockwise.getY() );
			Point cwOffset = PointPool.obtain().set( dir.getX() - clockwise.getX(), dir.getY() - clockwise.getY() );

			hitTiles.add( PointPool.obtain().set( start.x + anticlockwise.getX(), start.y + anticlockwise.getY() ) );

			hitTiles.add( PointPool.obtain().set( start.x + dir.getX(), start.y + dir.getY() ) );

			hitTiles.add( PointPool.obtain().set( start.x + clockwise.getX(), start.y + clockwise.getY() ) );

			for ( int i = 2; i <= range; i++ )
			{
				int acx = start.x + anticlockwise.getX() * i;
				int acy = start.y + anticlockwise.getY() * i;

				int nx = start.x + dir.getX() * i;
				int ny = start.y + dir.getY() * i;

				int cx = start.x + clockwise.getX() * i;
				int cy = start.y + clockwise.getY() * i;

				// add base tiles
				hitTiles.add( PointPool.obtain().set( acx, acy ) );
				hitTiles.add( PointPool.obtain().set( nx, ny ) );
				hitTiles.add( PointPool.obtain().set( cx, cy ) );

				// add anticlockwise - mid
				for ( int ii = 1; ii <= range; ii++ )
				{
					int px = acx + acwOffset.x * ii;
					int py = acy + acwOffset.y * ii;

					hitTiles.add( PointPool.obtain().set( px, py ) );
				}

				// add mid - clockwise
				for ( int ii = 1; ii <= range; ii++ )
				{
					int px = cx + cwOffset.x * ii;
					int py = cy + cwOffset.y * ii;

					hitTiles.add( PointPool.obtain().set( px, py ) );
				}
			}

			PointPool.free( acwOffset );
			PointPool.free( cwOffset );

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
		BASE_ATK,
		BASE_DEF,

		AETHER_ATK,
		AETHER_DEF,

		VOID_ATK,
		VOID_DEF,

		METAL_ATK,
		METAL_DEF,

		WOOD_ATK,
		WOOD_DEF,

		AIR_ATK,
		AIR_DEF,

		WATER_ATK,
		WATER_DEF,

		FIRE_ATK,
		FIRE_DEF;

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
	public enum ElementType
	{
		BASE( Color.GRAY ),
		AETHER( Color.RED ),
		VOID( Color.WHITE ),
		METAL( Color.LIGHT_GRAY ),
		WOOD( Color.GREEN ),
		AIR( Color.CYAN ),
		WATER( Color.BLUE ),
		FIRE( Color.ORANGE );

		public ElementType Weakness;
		public ElementType Strength;

		public Statistic Attack;
		public Statistic Defense;

		public Color Colour;

		static
		{
			ElementType.METAL.Weakness = ElementType.FIRE;
			ElementType.METAL.Strength = ElementType.WOOD;

			ElementType.WOOD.Weakness = ElementType.METAL;
			ElementType.WOOD.Strength = ElementType.AIR;

			ElementType.AIR.Weakness = ElementType.WOOD;
			ElementType.AIR.Strength = ElementType.WATER;

			ElementType.WATER.Weakness = ElementType.AIR;
			ElementType.WATER.Strength = ElementType.FIRE;

			ElementType.FIRE.Weakness = ElementType.WATER;
			ElementType.FIRE.Strength = ElementType.METAL;
		}

		ElementType( Color colour )
		{
			this.Colour = colour;

			Attack = Statistic.valueOf( toString() + "_ATK" );
			Defense = Statistic.valueOf( toString() + "_DEF" );

			Colors.put( this.toString(), this.Colour );
		}

		public static FastEnumMap<ElementType, Integer> getElementBlock()
		{
			FastEnumMap<ElementType, Integer> map = new FastEnumMap<ElementType, Integer>( ElementType.class );

			for ( ElementType el : ElementType.values() )
			{
				map.put( el, 0 );
			}

			return map;
		}

		public static FastEnumMap<ElementType, Integer> load( Element xml, FastEnumMap<ElementType, Integer> values )
		{
			for ( int i = 0; i < xml.getChildCount(); i++ )
			{
				Element el = xml.getChild( i );

				ElementType elem = ElementType.valueOf( el.getName().toUpperCase() );
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

		public static FastEnumMap<ElementType, Integer> copy( FastEnumMap<ElementType, Integer> map )
		{
			FastEnumMap<ElementType, Integer> newMap = new FastEnumMap<ElementType, Integer>( ElementType.class );
			for ( ElementType e : ElementType.values() )
			{
				newMap.put( e, map.get( e ) );
			}
			return newMap;
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

		for ( ElementType el : ElementType.values() )
		{
			int atk = damObj.attackerVariableMap.get( Statistic.BASE_ATK.toString().toLowerCase() );
			int def = damObj.defenderVariableMap.get( Statistic.BASE_DEF.toString().toLowerCase() );

			if ( el != ElementType.BASE )
			{
				atk = damObj.attackerVariableMap.get( el.Attack.toString().toLowerCase() );
				def = (int) Math.floor( def / 3.0f ) + damObj.defenderVariableMap.get( el.Defense.toString().toLowerCase() );
			}

			int dam = 0;

			if ( atk == 0 )
			{

			}
			else if ( atk == def )
			{
				dam = 10;
			}
			else if ( atk < def )
			{
				dam = (int) ( 10.0f / ( ( def - atk ) * 0.5f ) );
			}
			else if ( atk > def )
			{
				dam = 10 + (int) ( 5.0f * ( ( atk - def ) / 10.0f ) );
			}

			damObj.damageMap.put( el, dam );
		}

		damObj.setDamageVariables();

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

		defender.applyDamage( damObj.getTotalDamage(), attacker );
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

	// ----------------------------------------------------------------------
	public static Skin loadSkin()
	{
		Skin skin = new Skin();

		BitmapFont font = AssetManager.loadFont( "Sprites/GUI/stan0755.ttf", 12, new Color( 0.97f, 0.87f, 0.7f, 1 ), 1, Color.BLACK, false );
		skin.add( "default", font );

		BitmapFont titlefont = AssetManager.loadFont( "Sprites/GUI/stan0755.ttf", 20, new Color( 1f, 0.9f, 0.8f, 1 ), 1, Color.BLACK, true );
		skin.add( "title", titlefont );

		Pixmap pixmap = new Pixmap( 1, 1, Format.RGBA8888 );
		pixmap.setColor( Color.WHITE );
		pixmap.fill();
		skin.add( "white", new Texture( pixmap ) );

		TextFieldStyle textField = new TextFieldStyle();
		textField.fontColor = Color.WHITE;
		textField.font = skin.getFont( "default" );
		textField.background = new NinePatchDrawable( new NinePatch( AssetManager.loadTextureRegion( "Sprites/GUI/TextField.png" ), 6, 6, 6, 6 ) );
		textField.cursor = skin.newDrawable( "white", Color.WHITE );
		textField.selection = skin.newDrawable( "white", Color.LIGHT_GRAY );
		skin.add( "default", textField );

		LabelStyle label = new LabelStyle();
		label.font = skin.getFont( "default" );
		skin.add( "default", label );

		LabelStyle titleLabel = new LabelStyle();
		titleLabel.font = skin.getFont( "title" );
		skin.add( "title", titleLabel );

		CheckBoxStyle checkButton = new CheckBoxStyle();
		checkButton.checkboxOff = new TextureRegionDrawable( AssetManager.loadTextureRegion( "Sprites/GUI/Unchecked.png" ) );
		checkButton.checkboxOn = new TextureRegionDrawable( AssetManager.loadTextureRegion( "Sprites/GUI/Checked.png" ) );
		checkButton.font = skin.getFont( "default" );
		skin.add( "default", checkButton );

		TextButtonStyle textButton = new TextButtonStyle();
		textButton.up = new NinePatchDrawable( new NinePatch( AssetManager.loadTextureRegion( "Sprites/GUI/Button.png" ), 12, 12, 12, 12 ) );
		textButton.font = skin.getFont( "default" );
		textButton.fontColor = Color.LIGHT_GRAY;
		textButton.overFontColor = Color.WHITE;
		skin.add( "default", textButton );

		TextButtonStyle bigTextButton = new TextButtonStyle();
		bigTextButton.up = new NinePatchDrawable( new NinePatch( AssetManager.loadTextureRegion( "Sprites/GUI/Button.png" ), 12, 12, 12, 12 ) );
		bigTextButton.font = skin.getFont( "title" );
		bigTextButton.fontColor = Color.LIGHT_GRAY;
		bigTextButton.overFontColor = Color.WHITE;
		skin.add( "big", bigTextButton );

		TooltipStyle toolTip = new TooltipStyle();
		toolTip.background = new NinePatchDrawable( new NinePatch( AssetManager.loadTextureRegion( "Sprites/GUI/Tooltip.png" ), 21, 21, 21, 21 ) );
		skin.add( "default", toolTip );

		ProgressBarStyle progressBar = new ProgressBarStyle();
		progressBar.background = new NinePatchDrawable( new NinePatch( AssetManager.loadTextureRegion( "Sprites/GUI/TextField.png" ), 6, 6, 6, 6 ) );
		progressBar.knobBefore = new NinePatchDrawable( new NinePatch( AssetManager.loadTextureRegion( "Sprites/GUI/ProgressIndicator.png" ), 8, 8, 8, 8 ) );
		skin.add( "default-horizontal", progressBar );

		return skin;
	}
}
