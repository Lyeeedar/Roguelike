package Roguelike.Util;

public class FastEnumMap<T extends Enum<T>, V>
{
	private Class<T> keyType;
	private V[] items;

	@SuppressWarnings( "unchecked" )
	public FastEnumMap( Class<T> keyType )
	{
		this.keyType = keyType;
		items = (V[]) new Object[keyType.getEnumConstants().length];
	}

	public FastEnumMap( FastEnumMap<?, ?> other )
	{
		this.keyType = (Class<T>) other.keyType;
		items = (V[]) new Object[keyType.getEnumConstants().length];
	}

	public int numItems()
	{
		return items.length;
	}

	public int size()
	{
		int count = 0;

		for ( int i = 0; i < items.length; i++ )
		{
			if ( items[i] != null )
			{
				count++;
			}
		}

		return count;
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

	public void put( int index, V value )
	{
		items[index] = value;
	}

	public void remove( int index )
	{
		items[index] = null;
	}

	public V get( int index )
	{
		return items[index];
	}

	public boolean containsKey( int index )
	{
		return items[index] != null;
	}
}
