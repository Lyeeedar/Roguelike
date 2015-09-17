package Roguelike.Save;

import java.io.IOException;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import kryo.FastEnumMapSerializer;
import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.Global.Direction;
import Roguelike.Global.Statistic;
import Roguelike.Global.Tier1Element;
import Roguelike.DungeonGeneration.DungeonFileParser.DFPRoom;
import Roguelike.DungeonGeneration.DungeonFileParser.DFPRoom.Orientation;
import Roguelike.DungeonGeneration.Room;
import Roguelike.DungeonGeneration.Room.RoomDoor;
import Roguelike.DungeonGeneration.Symbol;
import Roguelike.GameEvent.Constant.ConstantEvent;
import Roguelike.GameEvent.Damage.DamageEvent;
import Roguelike.GameEvent.Damage.FieldEvent;
import Roguelike.GameEvent.Damage.HealEvent;
import Roguelike.GameEvent.Damage.StatusEvent;
import Roguelike.GameEvent.OnDeath.FieldOnDeathEvent;
import Roguelike.GameEvent.OnDeath.HealOnDeathEvent;
import Roguelike.GameEvent.OnTask.CancelTaskEvent;
import Roguelike.GameEvent.OnTask.CostTaskEvent;
import Roguelike.GameEvent.OnTask.DamageTaskEvent;
import Roguelike.GameEvent.OnTask.StatusTaskEvent;
import Roguelike.GameEvent.OnTurn.DamageOverTimeEvent;
import Roguelike.GameEvent.OnTurn.HealOverTimeEvent;
import Roguelike.Items.Inventory;
import Roguelike.Items.Item;
import Roguelike.Items.Item.EquipmentSlot;
import Roguelike.Items.Item.ItemCategory;
import Roguelike.Lights.Light;
import Roguelike.Pathfinding.ShadowCastCache;
import Roguelike.Save.SaveAbilityPool.SaveAbilityLine;
import Roguelike.Save.SaveAbilityPool.SaveAbilityPoolItem;
import Roguelike.Save.SaveGameEntity.CooldownWrapper;
import Roguelike.Save.SaveLevel.SaveEssence;
import Roguelike.Save.SaveLevel.SaveLevelItem;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.Sprite.AnimationMode;
import Roguelike.Sprite.Sprite.AnimationState;
import Roguelike.StatusEffect.StatusEffect;
import Roguelike.Tiles.Point;
import Roguelike.Tiles.SeenTile.SeenHistoryItem;
import Roguelike.Util.EnumBitflag;
import Roguelike.Util.FastEnumMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public final class SaveFile
{
	private static Kryo kryo;

	public String currentDungeon;
	public String currentLevel;
	public Array<SaveDungeon> dungeons = new Array<SaveDungeon>();
	public SaveAbilityPool abilityPool;
	public float AUT;
	public HashMap<String, Integer> globalVariables;
	public HashMap<String, String> globalNames;

	public void save()
	{
		setupKryo();

		Output output = null;
		try
		{
			output = new Output( new GZIPOutputStream( Gdx.files.local( "save.dat" ).write( false ) ) );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}

		output.writeString( currentDungeon );
		output.writeString( currentLevel );
		kryo.writeObject( output, dungeons );
		kryo.writeObject( output, abilityPool );
		output.writeFloat( AUT );
		kryo.writeObject( output, globalVariables );
		kryo.writeObject( output, globalNames );

		output.close();

		System.out.println( "Saved" );
	}

	public void load()
	{
		setupKryo();

		Input input = null;
		try
		{
			input = new Input( new GZIPInputStream( Gdx.files.local( "save.dat" ).read() ) );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}

		currentDungeon = input.readString();
		currentLevel = input.readString();
		dungeons = kryo.readObject( input, Array.class );
		abilityPool = kryo.readObject( input, SaveAbilityPool.class );
		AUT = input.readFloat();
		globalVariables = kryo.readObject( input, HashMap.class );
		globalNames = kryo.readObject( input, HashMap.class );

		input.close();
	}

	private void setupKryo()
	{
		if ( kryo == null )
		{
			kryo = new Kryo();
			kryo.setRegistrationRequired( true );
			kryo.setAsmEnabled( Global.ANDROID );

			registerSerializers( kryo );
			registerClasses( kryo );
		}
	}

	private void registerSerializers( Kryo kryo )
	{
		kryo.register( FastEnumMap.class, new FastEnumMapSerializer() );

		kryo.register( Array.class, new Serializer<Array>()
				{
			{
				setAcceptsNull( true );
			}

			private Class genericType;

			@Override
			public void setGenerics( Kryo kryo, Class[] generics )
			{
				if ( kryo.isFinal( generics[0] ) )
				{
					genericType = generics[0];
				}
			}

			@Override
			public void write( Kryo kryo, Output output, Array array )
			{
				int length = array.size;
				output.writeInt( length, true );

				if ( length == 0 ) { return; }

				if ( genericType != null )
				{
					Serializer serializer = kryo.getSerializer( genericType );
					genericType = null;
					for ( Object element : array )
					{
						kryo.writeObjectOrNull( output, element, serializer );
					}
				}
				else
				{
					for ( Object element : array )
					{
						kryo.writeClassAndObject( output, element );
					}
				}
			}

			@Override
			public Array read( Kryo kryo, Input input, Class<Array> type )
			{
				Array array = new Array();
				kryo.reference( array );

				int length = input.readInt( true );
				array.ensureCapacity( length );

				if ( genericType != null )
				{
					Class elementClass = genericType;
					Serializer serializer = kryo.getSerializer( genericType );
					genericType = null;

					for ( int i = 0; i < length; i++ )
					{
						array.add( kryo.readObjectOrNull( input, elementClass, serializer ) );
					}
				}
				else
				{
					for ( int i = 0; i < length; i++ )
					{
						array.add( kryo.readClassAndObject( input ) );
					}
				}
				return array;
			}
		} );

		kryo.register( Color.class, new Serializer<Color>()
				{
			@Override
			public Color read( Kryo kryo, Input input, Class<Color> type )
			{
				Color color = new Color();
				Color.rgba8888ToColor( color, input.readInt() );
				return color;
			}

			@Override
			public void write( Kryo kryo, Output output, Color color )
			{
				output.writeInt( Color.rgba8888( color ) );
			}
		} );

		kryo.register( Sprite.class, new Serializer<Sprite>()
				{
			@Override
			public Sprite read( Kryo kryo, Input input, Class<Sprite> type )
			{
				String fileName = input.readString();
				float animDelay = input.readFloat();
				Color color = kryo.readObject( input, Color.class );
				int modeVal = input.readInt();
				AnimationMode mode = AnimationMode.values()[modeVal];
				float[] scale = input.readFloats( 2 );

				Sprite sprite = AssetManager.loadSprite( fileName, animDelay, color, mode, null );
				sprite.baseScale = scale;
				return sprite;
			}

			@Override
			public void write( Kryo kryo, Output output, Sprite sprite )
			{
				output.writeString( sprite.fileName );
				output.writeFloat( sprite.animationDelay );
				kryo.writeObject( output, sprite.colour );
				output.writeInt( sprite.animationState.mode.ordinal() );
				output.writeFloats( sprite.baseScale );
			}
		} );

		kryo.register( Element.class, new Serializer<Element>()
				{
			@Override
			public Element read( Kryo kryo, Input input, Class<Element> type )
			{
				String xml = input.readString();

				XmlReader reader = new XmlReader();
				Element element = reader.parse( xml );
				return element;
			}

			@Override
			public void write( Kryo kryo, Output output, Element element )
			{
				output.writeString( element.toString() );
			}
		} );
	}

	private void registerClasses( Kryo kryo )
	{
		kryo.register( SaveAbilityPool.class );
		kryo.register( SaveEnvironmentEntity.class );
		kryo.register( SaveField.class );
		kryo.register( SaveFile.class );
		kryo.register( SaveGameEntity.class );
		kryo.register( SaveDungeon.class );
		kryo.register( SaveLevel.class );
		kryo.register( SaveSeenTile.class );
		kryo.register( SaveSeenTile[].class );
		kryo.register( SaveSeenTile[][].class );

		kryo.register( SaveAbilityLine.class );
		kryo.register( SaveAbilityPoolItem.class );
		kryo.register( SaveAbilityPoolItem[].class );
		kryo.register( CooldownWrapper.class );
		kryo.register( SaveEssence.class );
		kryo.register( SaveLevelItem.class );

		kryo.register( Point.class );
		kryo.register( StatusEffect.class );
		kryo.register( Inventory.class );
		kryo.register( Element.class );
		kryo.register( DFPRoom.class );
		kryo.register( SeenHistoryItem.class );
		kryo.register( Item.class );
		kryo.register( Light.class );
		kryo.register( ShadowCastCache.class );
		kryo.register( EnumBitflag.class );
		kryo.register( Symbol.class );
		kryo.register( Symbol[].class );
		kryo.register( Symbol[][].class );
		kryo.register( Room.class );
		kryo.register( RoomDoor.class );
		kryo.register( Orientation.class );
		kryo.register( AnimationState.class );
		kryo.register( AnimationMode.class );

		kryo.register( HashMap.class );
		kryo.register( String[].class );
		kryo.register( int[].class );
		kryo.register( Object.class );
		kryo.register( char[].class );
		kryo.register( char[][].class );
		kryo.register( Float[].class );
		kryo.register( Float[][].class );

		kryo.register( EquipmentSlot.class );
		kryo.register( ItemCategory.class );
		kryo.register( Statistic.class );
		kryo.register( Direction.class );
		kryo.register( Tier1Element.class );

		kryo.register( ConstantEvent.class );
		kryo.register( DamageEvent.class );
		kryo.register( FieldEvent.class );
		kryo.register( HealEvent.class );
		kryo.register( StatusEvent.class );
		kryo.register( FieldOnDeathEvent.class );
		kryo.register( HealOnDeathEvent.class );
		kryo.register( CancelTaskEvent.class );
		kryo.register( CostTaskEvent.class );
		kryo.register( DamageTaskEvent.class );
		kryo.register( StatusTaskEvent.class );
		kryo.register( DamageOverTimeEvent.class );
		kryo.register( HealOverTimeEvent.class );
	}
}
