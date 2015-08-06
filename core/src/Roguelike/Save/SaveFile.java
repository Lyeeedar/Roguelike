package Roguelike.Save;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class SaveFile
{
	public SaveLevel level;
	
	public void save()
	{
		Kryo kryo = new Kryo();
		registerSerializers(kryo);
		Output output = null;
		try
		{
			output = new Output(new FileOutputStream("save.dat"));
		} 
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		
		kryo.writeObject(output, level);

		output.close();
	}
	
	public void load()
	{
		Kryo kryo = new Kryo();
		registerSerializers(kryo);
		Input input = null;
		try
		{
			input = new Input(new FileInputStream("save.dat"));
		} 
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		
		level = kryo.readObject(input, SaveLevel.class);

		input.close();
	}
	
	private void registerSerializers(Kryo kryo)
	{
		kryo.register(Array.class, new Serializer<Array>() 
		{
		    {
		        setAcceptsNull(true);
		    }

		    private Class genericType;

		    public void setGenerics (Kryo kryo, Class[] generics) 
		    {
		        if (kryo.isFinal(generics[0])) { genericType = generics[0]; }
		    }

		    public void write (Kryo kryo, Output output, Array array) 
		    {
		        int length = array.size;
		        output.writeInt(length, true);
		        
		        if (length == 0) { return; }
		        
		        if (genericType != null) 
		        {
		            Serializer serializer = kryo.getSerializer(genericType);
		            genericType = null;
		            for (Object element : array)
		            {
		            	kryo.writeObjectOrNull(output, element, serializer);
		            }
		        } 
		        else 
		        {
		            for (Object element : array)
		            {
		            	kryo.writeClassAndObject(output, element);
		            }
		        }
		    }

		    public Array read (Kryo kryo, Input input, Class<Array> type) 
		    {
		        Array array = new Array();
		        kryo.reference(array);
		        
		        int length = input.readInt(true);
		        array.ensureCapacity(length);
		        
		        if (genericType != null) 
		        {
		            Class elementClass = genericType;
		            Serializer serializer = kryo.getSerializer(genericType);
		            genericType = null;
		            
		            for (int i = 0; i < length; i++)
		            {
		            	array.add(kryo.readObjectOrNull(input, elementClass, serializer));
		            }
		        } 
		        else 
		        {
		            for (int i = 0; i < length; i++)
		            {
		            	array.add(kryo.readClassAndObject(input));
		            }
		        }
		        return array;
		    }
		});
		
		kryo.register(Color.class, new Serializer<Color>() 
		{
		    public Color read (Kryo kryo, Input input, Class<Color> type) 
		    {
		        Color color = new Color();
		        Color.rgba8888ToColor(color, input.readInt());
		        return color;
		    }

		    public void write (Kryo kryo, Output output, Color color) 
		    {
		        output.writeInt(Color.rgba8888(color));
		    }
		});
	}
}
