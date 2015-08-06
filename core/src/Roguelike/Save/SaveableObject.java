package Roguelike.Save;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public abstract class SaveableObject<T>
{
	public abstract void store(T obj);
	public abstract T create();
}
