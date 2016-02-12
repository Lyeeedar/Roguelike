package Roguelike.Save;

import Roguelike.Ability.AbilityTree;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.AssetManager;
import Roguelike.DungeonGeneration.DungeonFileParser.DFPRoom;
import Roguelike.DungeonGeneration.Room;
import Roguelike.DungeonGeneration.Room.RoomDoor;
import Roguelike.DungeonGeneration.Symbol;
import Roguelike.Entity.ActivationAction.*;
import Roguelike.GameEvent.AdditionalSprite;
import Roguelike.GameEvent.Constant.ConstantEvent;
import Roguelike.GameEvent.Damage.*;
import Roguelike.GameEvent.OnDeath.AbstractOnDeathEvent;
import Roguelike.GameEvent.OnDeath.FieldOnDeathEvent;
import Roguelike.GameEvent.OnDeath.HealOnDeathEvent;
import Roguelike.GameEvent.OnExpire.AbilityOnExpireEvent;
import Roguelike.GameEvent.OnExpire.KillOnExpireEvent;
import Roguelike.GameEvent.OnTask.CancelTaskEvent;
import Roguelike.GameEvent.OnTask.CostTaskEvent;
import Roguelike.GameEvent.OnTask.DamageTaskEvent;
import Roguelike.GameEvent.OnTask.StatusTaskEvent;
import Roguelike.GameEvent.OnTurn.DamageOverTimeEvent;
import Roguelike.GameEvent.OnTurn.HealOverTimeEvent;
import Roguelike.Global;
import Roguelike.Global.Direction;
import Roguelike.Global.Statistic;
import Roguelike.Items.Inventory;
import Roguelike.Items.Item;
import Roguelike.Items.Item.EquipmentSlot;
import Roguelike.Items.Item.ItemCategory;
import Roguelike.Levels.LevelManager;
import Roguelike.Lights.Light;
import Roguelike.Pathfinding.ShadowCastCache;
import Roguelike.Quests.Input.QuestInputFlagEquals;
import Roguelike.Quests.Input.QuestInputFlagPresent;
import Roguelike.Quests.Output.QuestOuputConditionDialogueValue;
import Roguelike.Quests.Output.QuestOutput;
import Roguelike.Quests.Output.QuestOutputConditionActionEnabled;
import Roguelike.Quests.Output.QuestOutputConditionEntityAlive;
import Roguelike.Quests.Quest;
import Roguelike.Save.SaveLevel.SaveLevelItem;
import Roguelike.Save.SaveLevel.SaveOrb;
import Roguelike.Sound.SoundInstance;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.Sprite.AnimationMode;
import Roguelike.Sprite.Sprite.AnimationState;
import Roguelike.Sprite.SpriteAnimation.AbstractSpriteAnimation;
import Roguelike.Sprite.SpriteAnimation.BumpAnimation;
import Roguelike.Sprite.SpriteAnimation.MoveAnimation;
import Roguelike.Sprite.SpriteAnimation.StretchAnimation;
import Roguelike.Sprite.TilingSprite;
import Roguelike.StatusEffect.StatusEffect;
import Roguelike.Tiles.GameTile;
import Roguelike.Tiles.Point;
import Roguelike.Util.EnumBitflag;
import Roguelike.Util.FastEnumMap;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import kryo.FastEnumMapSerializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class SaveFile
{
	private static Kryo kryo;

	public LevelManager levelManager;
	public ObjectSet<String> usedQuests;
	public ObjectMap<String, String> worldFlags;
	public ObjectMap<String, String> worldFlagsCopy;
	public ObjectMap<String, String> runFlags;
	public ObjectMap<String, String> deferredFlags;
	public boolean isDead;
	public int lives;

	public void save()
	{
		setupKryo();

		FileHandle attemptFile = Gdx.files.local( "attempt_save.dat" );

		Output output = null;
		try
		{
			output = new Output( new GZIPOutputStream( attemptFile.write( false ) ) );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}

		kryo.writeObject( output, levelManager );
		kryo.writeObject( output, usedQuests );
		kryo.writeObject( output, worldFlags );
		kryo.writeObject( output, worldFlagsCopy );
		kryo.writeObject( output, runFlags );
		kryo.writeObject( output, deferredFlags );
		output.writeBoolean( isDead );
		output.writeInt( lives );

		output.close();

		byte[] bytes = attemptFile.readBytes();
		FileHandle actualFile = Gdx.files.local( "save.dat" );
		actualFile.writeBytes( bytes, false );

		attemptFile.delete();

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

		levelManager = kryo.readObject( input, LevelManager.class );
		usedQuests = kryo.readObject( input, ObjectSet.class );
		worldFlags = kryo.readObject( input, ObjectMap.class );
		worldFlagsCopy = kryo.readObject( input, ObjectMap.class );
		runFlags = kryo.readObject( input, ObjectMap.class );
		deferredFlags = kryo.readObject( input, ObjectMap.class );
		isDead = input.readBoolean();
		lives = input.readInt();

		input.close();
	}

	private void setupKryo()
	{
		if ( kryo == null )
		{
			kryo = new Kryo();
			kryo.setRegistrationRequired( true );
			kryo.setAsmEnabled( true );

			registerSerializers( kryo );
			registerClasses( kryo );
		}
	}

	private void registerSerializers( Kryo kryo )
	{
		kryo.register( FastEnumMap.class, new FastEnumMapSerializer() );

		kryo.register( Array.class, new Serializer<Array>()
		{
			private Class genericType;

			{
				setAcceptsNull( true );
			}

			@Override
			public void write( Kryo kryo, Output output, Array array )
			{
				int length = array.size;
				output.writeInt( length, true );

				if ( length == 0 )
				{
					genericType = null;
					return;
				}

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
			public void setGenerics( Kryo kryo, Class[] generics )
			{
				if ( kryo.isFinal( generics[ 0 ] ) )
				{
					genericType = generics[ 0 ];
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
				float repeatDelay = input.readFloat();
				Color color = kryo.readObject( input, Color.class );
				int modeVal = input.readInt();
				AnimationMode mode = AnimationMode.values()[ modeVal ];
				float[] scale = input.readFloats( 2 );
				boolean drawActualSize = input.readBoolean();
				SoundInstance sound = kryo.readObjectOrNull( input, SoundInstance.class );
				AbstractSpriteAnimation anim = (AbstractSpriteAnimation)kryo.readClassAndObject( input );

				Sprite sprite = AssetManager.loadSprite( fileName, animDelay, color, mode, sound, drawActualSize );
				sprite.spriteAnimation = anim;
				sprite.baseScale = scale;
				sprite.repeatDelay = repeatDelay;
				return sprite;
			}

			@Override
			public void write( Kryo kryo, Output output, Sprite sprite )
			{
				output.writeString( sprite.fileName );
				output.writeFloat( sprite.animationDelay );
				output.writeFloat( sprite.repeatDelay );
				kryo.writeObject( output, sprite.colour );
				output.writeInt( sprite.animationState.mode.ordinal() );
				output.writeFloats( sprite.baseScale );
				output.writeBoolean( sprite.drawActualSize );
				kryo.writeObjectOrNull( output, sprite.sound, SoundInstance.class );
				kryo.writeClassAndObject( output, sprite.spriteAnimation );
			}
		} );

		kryo.register( SoundInstance.class, new Serializer<SoundInstance>()
		{
			@Override
			public SoundInstance read( Kryo kryo, Input input, Class<SoundInstance> type )
			{
				SoundInstance sound = new SoundInstance(  );

				sound.name = input.readString();
				sound.sound = AssetManager.loadSound( sound.name );

				sound.minPitch = input.readFloat();
				sound.maxPitch = input.readFloat();
				sound.volume = input.readFloat();

				sound.range = input.readInt();
				sound.falloffMin = input.readInt();

				return sound;
			}

			@Override
			public void write( Kryo kryo, Output output, SoundInstance sound )
			{
				output.writeString( sound.name );

				output.writeFloat( sound.minPitch );
				output.writeFloat( sound.maxPitch );
				output.writeFloat( sound.volume );

				output.writeInt( sound.range );
				output.writeInt( sound.falloffMin );
			}
		} );

		kryo.register( AbilityTree.class, new Serializer<AbilityTree> ()
		{
			@Override
			public void write( Kryo kryo, Output output, AbilityTree object )
			{
				SaveAbilityTree saveTree = new SaveAbilityTree();
				saveTree.store( object );

				kryo.writeObject( output, saveTree );
			}

			@Override
			public AbilityTree read( Kryo kryo, Input input, Class<AbilityTree> type )
			{
				SaveAbilityTree saveTree = kryo.readObject( input, SaveAbilityTree.class );

				return saveTree.create();
			}
		} );

		kryo.register( ActiveAbility.class, new Serializer<ActiveAbility>()
		{
			@Override
			public void write( Kryo kryo, Output output, ActiveAbility object )
			{
				output.writeString( object.creationPath );
				kryo.writeObjectOrNull( output, object.creationData, Element.class);
			}

			@Override
			public ActiveAbility read( Kryo kryo, Input input, Class<ActiveAbility> type )
			{
				String creationPath = input.readString();
				Element creationData = kryo.readObjectOrNull( input, Element.class );

				ActiveAbility ab;
				if (creationPath != null)
				{
					ab = ActiveAbility.load( creationPath );
				}
				else
				{
					ab = ActiveAbility.load( creationData );
				}

				return ab;
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

		kryo.register( ObjectMap.class, new Serializer<ObjectMap>()
		{
			@Override
			public void write( Kryo kryo, Output output, ObjectMap object )
			{
				Array<Object[]> data = new Array<Object[]>(  );
				for (Object key : object.keys())
				{
					Object value = object.get( key );
					data.add( new Object[]{key, value} );
				}
				kryo.writeObject( output, data );
			}

			@Override
			public ObjectMap read( Kryo kryo, Input input, Class<ObjectMap> type )
			{
				Array<Object[]> data = kryo.readObject( input, Array.class );

				ObjectMap map = new ObjectMap(  );
				for (Object[] pair : data)
				{
					map.put( pair[0], pair[1] );
				}
				return map;
			}
		} );

		kryo.register( IntMap.class, new Serializer<IntMap>()
		{
			@Override
			public void write( Kryo kryo, Output output, IntMap object )
			{
				Array<Object[]> data = new Array<Object[]>(  );

				for (Object entryObj : object.entries())
				{
					IntMap.Entry entry = (IntMap.Entry)entryObj;
					data.add( new Object[]{entry.key, entry.value} );
				}
				kryo.writeObject( output, data );
			}

			@Override
			public IntMap read( Kryo kryo, Input input, Class<IntMap> type )
			{
				Array<Object[]> data = kryo.readObject( input, Array.class );

				IntMap map = new IntMap(  );
				for (Object[] pair : data)
				{
					map.put( (Integer)pair[0], pair[1] );
				}
				return map;
			}
		} );
	}

	private void registerClasses( Kryo kryo )
	{
		kryo.register( SaveEnvironmentEntity.class );
		kryo.register( SaveField.class );
		kryo.register( SaveFile.class );
		kryo.register( SaveGameEntity.class );
		kryo.register( SaveLevel.class );
		kryo.register( SaveAbilityTree.class );
		kryo.register( SaveAbilityTree.SaveAbilityStage.class );
		kryo.register( SaveOrb.class );
		kryo.register( SaveLevelItem.class );

		kryo.register( Point.class );
		kryo.register( StatusEffect.class );
		kryo.register( StatusEffect.DurationType.class );
		kryo.register( Inventory.class );
		kryo.register( Element.class );
		kryo.register( DFPRoom.class );
		kryo.register( Item.class );
		kryo.register( Item.WeaponDefinition.class );
		kryo.register( Item.SpriteGroup.class );
		kryo.register( Light.class );
		kryo.register( ShadowCastCache.class );
		kryo.register( EnumBitflag.class );
		kryo.register( Symbol.class );
		kryo.register( Symbol[].class );
		kryo.register( Symbol[][].class );
		kryo.register( Room.class );
		kryo.register( RoomDoor.class );
		kryo.register( DFPRoom.Placement.class );
		kryo.register( AnimationState.class );
		kryo.register( AnimationMode.class );
		kryo.register( LevelManager.class );
		kryo.register( LevelManager.LevelData.class );
		kryo.register( LevelManager.BranchData.class );
		kryo.register( TilingSprite.class );

		kryo.register( HashMap.class );
		kryo.register( String[].class );
		kryo.register( int[].class );
		kryo.register( Object.class );
		kryo.register( Object[].class );
		kryo.register( char[].class );
		kryo.register( char[][].class );
		kryo.register( Float[].class );
		kryo.register( Float[][].class );
		kryo.register( boolean[].class );
		kryo.register( boolean[][].class );
		kryo.register( ObjectSet.class );

		kryo.register( BumpAnimation.class );
		kryo.register( MoveAnimation.class );
		kryo.register( StretchAnimation.class );
		kryo.register( MoveAnimation.MoveEquation.class );
		kryo.register( StretchAnimation.StretchEquation.class );

		kryo.register( EquipmentSlot.class );
		kryo.register( ItemCategory.class );
		kryo.register( Statistic.class );
		kryo.register( Direction.class );
		kryo.register( Global.Rarity.class );
		kryo.register( GameTile.OrbType.class );
		kryo.register( Item.WeaponDefinition.HitType.class );
		kryo.register( ActiveAbility.CooldownType.class );

		kryo.register( ConstantEvent.class );
		kryo.register( DamageEvent.class );
		kryo.register( FieldEvent.class );
		kryo.register( HealEvent.class );
		kryo.register( StatusEvent.class );
		kryo.register( BlockEvent.class );
		kryo.register( FieldOnDeathEvent.class );
		kryo.register( HealOnDeathEvent.class );
		kryo.register( AbstractOnDeathEvent.class );
		kryo.register( CancelTaskEvent.class );
		kryo.register( CostTaskEvent.class );
		kryo.register( DamageTaskEvent.class );
		kryo.register( StatusTaskEvent.class );
		kryo.register( DamageOverTimeEvent.class );
		kryo.register( HealOverTimeEvent.class );
		kryo.register( KillOnExpireEvent.class );
		kryo.register( AbilityOnExpireEvent.class );
		kryo.register( AdditionalSprite.class );

		kryo.register( Quest.class );
		kryo.register( QuestInputFlagPresent.class );
		kryo.register( QuestInputFlagEquals.class );
		kryo.register( QuestOutput.class );
		kryo.register( QuestOutputConditionEntityAlive.class );
		kryo.register( QuestOuputConditionDialogueValue.class );
		kryo.register( QuestOutputConditionActionEnabled.class );

		kryo.register( ActivationActionGroup.class );
		kryo.register( ActivationActionAddItem.class );
		kryo.register( ActivationActionChangeLevel.class );
		kryo.register( ActivationActionSetEnabled.class );
		kryo.register( ActivationActionSetPassable.class );
		kryo.register( ActivationActionSetSprite.class );
		kryo.register( ActivationActionSpawnEntity.class );
		kryo.register( ActivationActionActivate.class );
		kryo.register( ActivationActionSpawnField.class );
		kryo.register( ActivationActionKillThis.class );
		kryo.register( ActivationActionRemoveItem.class );
		kryo.register( ActivationActionAddStatus.class );
		kryo.register( ActivationActionDealDamage.class );
		kryo.register( ActivationConditionProximity.class );
		kryo.register( ActivationConditionHasItem.class );
	}
}
