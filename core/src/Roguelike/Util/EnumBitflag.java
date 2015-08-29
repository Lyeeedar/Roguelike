package Roguelike.Util;

public class EnumBitflag<T extends Enum<T>>
{
	private int bitflag = 0;

	public EnumBitflag( T... vals )
	{
		for ( T val : vals )
		{
			setBit( val );
		}
	}

	public void setAll( EnumBitflag<T> other )
	{
		bitflag |= other.bitflag;
	}

	public void setBit( T val )
	{
		bitflag |= ( 1 << ( val.ordinal() + 1 ) );
	}

	public void clearBit( T val )
	{
		bitflag &= ~( 1 << ( val.ordinal() + 1 ) );
	}

	public boolean contains( T val )
	{
		return ( ( 1 << ( val.ordinal() + 1 ) ) & bitflag ) != 0;
	}

	public boolean intersect( EnumBitflag<T> other )
	{
		return ( other.bitflag & bitflag ) != 0;
	}
}
