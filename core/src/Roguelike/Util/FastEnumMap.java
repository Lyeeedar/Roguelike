package Roguelike.Util;

public class FastEnumMap<T extends Enum<T>, V>
{
	private V[] items;

	@SuppressWarnings( "unchecked" )
	public FastEnumMap( Class<T> enumClass )
	{
		items = (V[]) new Object[enumClass.getEnumConstants().length];
	}

	public void put( T key, V value )
	{
		items[key.ordinal()] = value;
	}

	public void remove( T key )
	{
		items[key.ordinal()] = null;
	}

	public V get( T key )
	{
		return items[key.ordinal()];
	}

	public boolean containsKey( T key )
	{
		return items[key.ordinal()] != null;
	}
}
