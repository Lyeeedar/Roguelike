package Roguelike.DungeonGeneration;

import java.util.HashMap;

import Roguelike.Global.Direction;
import Roguelike.Global.Passability;
import Roguelike.DungeonGeneration.RecursiveDockGenerator.Room;
import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Entity.GameEntity;
import Roguelike.Pathfinding.PathfindingTile;
import Roguelike.Tiles.TileData;
import Roguelike.Util.EnumBitflag;

import com.badlogic.gdx.utils.XmlReader.Element;

//----------------------------------------------------------------------
public final class Symbol implements PathfindingTile
{
	public Room containingRoom;

	public char character;

	public Element tileData;

	public Element environmentData;
	public HashMap<String, Object> environmentEntityData;
	public Direction attachLocation;

	public String entityData;

	public String metaValue;

	// ----------------------------------------------------------------------
	private long processedTileData;
	private EnumBitflag<Passability> tilePassable;

	// ----------------------------------------------------------------------

	public Symbol copy()
	{
		Symbol s = new Symbol();
		s.character = character;
		s.tileData = tileData;
		s.environmentData = environmentData;
		s.environmentEntityData = environmentEntityData;
		s.entityData = entityData;
		s.metaValue = metaValue;

		return s;
	}

	public boolean hasEnvironmentEntity()
	{
		return environmentData != null;
	}

	public EnvironmentEntity getEnvironmentEntity( String levelUID )
	{
		if ( environmentData != null )
		{
			EnvironmentEntity ee = EnvironmentEntity.load( environmentData, levelUID );

			if ( ee.creationData.get( "Type", "" ).equals( "Transition" ) )
			{
				if ( environmentEntityData != null )
				{
					ee.data = environmentEntityData;
				}
				else
				{
					environmentEntityData = ee.data;
				}

			}

			return ee;
		}

		return null;
	}

	public boolean getEnvironmentEntityPassable( EnumBitflag<Passability> travelType )
	{
		if ( environmentData != null ) { return Passability.parse( environmentData.get( "Passable", "true" ) ).intersect( travelType ); }

		return true;
	}

	public boolean hasGameEntity()
	{
		return entityData != null;
	}

	public GameEntity getGameEntity()
	{
		return entityData != null ? GameEntity.load( entityData ) : null;
	}

	public TileData getTileData()
	{
		TileData data = TileData.parse( tileData );

		processedTileData = tileData.hashCode();
		tilePassable = data.passableBy;

		return data;
	}

	public static Symbol parse( Element xml, HashMap<Character, Symbol> sharedSymbolMap, HashMap<Character, Symbol> localSymbolMap )
	{
		Symbol symbol = new Symbol();

		// load the base symbol
		if ( xml.getAttribute( "Extends", null ) != null )
		{
			char extendsSymbol = xml.getAttribute( "Extends" ).charAt( 0 );

			Symbol rs = localSymbolMap != null ? localSymbolMap.get( extendsSymbol ) : null;
			if ( rs == null )
			{
				rs = sharedSymbolMap.get( extendsSymbol );
			}

			symbol.character = rs.character;
			symbol.tileData = rs.tileData;
			symbol.environmentData = rs.environmentData;
			symbol.entityData = rs.entityData;
			symbol.metaValue = rs.metaValue;
		}

		// fill in the new values
		symbol.character = xml.get( "Char", "" + symbol.character ).charAt( 0 );

		if ( xml.getChildByName( "TileData" ) != null )
		{
			symbol.tileData = xml.getChildByName( "TileData" );
		}

		if ( xml.getChildByName( "EnvironmentData" ) != null )
		{
			symbol.environmentData = xml.getChildByName( "EnvironmentData" );
		}

		if ( xml.getChildByName( "EntityData" ) != null )
		{
			symbol.entityData = xml.get( "EntityData" );
		}

		symbol.metaValue = xml.get( "MetaValue", symbol.metaValue );

		return symbol;
	}

	public boolean isPassable( EnumBitflag<Passability> travelType )
	{
		if ( processedTileData != tileData.hashCode() )
		{
			getTileData();
		}

		return tilePassable.intersect( travelType );
	}

	@Override
	public String toString()
	{
		return "" + character;
	}

	@Override
	public boolean getPassable( EnumBitflag<Passability> travelType )
	{
		return isPassable( travelType );
	}

	private static final EnumBitflag<Passability> InfluencePassable = new EnumBitflag<Passability>( Passability.WALK );

	@Override
	public int getInfluence()
	{
		if ( character == 'F' )
		{
			if ( hasEnvironmentEntity() )
			{
				if ( !getEnvironmentEntityPassable( InfluencePassable ) ) { return 100; }
			}
		}

		return 0;
	}
}