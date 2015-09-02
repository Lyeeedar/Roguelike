package Roguelike.Save;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;

import kryo.FastEnumMapSerializer;
import Roguelike.AssetManager;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.Sprite.AnimationMode;
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

public class SaveFile
{
	public String currentLevel;
	public HashMap<String, SaveLevel> allLevels;
	public SaveAbilityPool abilityPool;

	public void save()
	{
		Kryo kryo = new Kryo();
		registerSerializers( kryo );
		Output output = new Output( Gdx.files.local( "save.dat" ).write( false ) );

		output.writeString( currentLevel );
		kryo.writeObject( output, allLevels );
		kryo.writeObject( output, abilityPool );

		output.close();
	}

	public void load()
	{
		Kryo kryo = new Kryo();
		registerSerializers( kryo );
		Input input = null;
		try
		{
			input = new Input( new FileInputStream( "save.dat" ) );
		}
		catch ( FileNotFoundException e )
		{
			e.printStackTrace();
		}

		currentLevel = input.readString();
		allLevels = kryo.readObject( input, HashMap.class );
		abilityPool = kryo.readObject( input, SaveAbilityPool.class );

		input.close();
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
}
