package Roguelike.DungeonGeneration;

import java.util.HashMap;
import java.util.Random;

import Roguelike.Global.Direction;
import Roguelike.Global.Passability;
import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Levels.LevelManager;
import Roguelike.Pathfinding.PathfindingTile;
import Roguelike.Tiles.TileData;
import Roguelike.Util.EnumBitflag;

import com.badlogic.gdx.utils.XmlReader.Element;

//----------------------------------------------------------------------
public final class Symbol implements PathfindingTile
{
	public Room containingRoom;

	public Character extendsSymbol;
	public char character = 'U';

	public Element tileData;

	public Element environmentData;
	public Direction attachLocation;

	public Object entityData;

	public Element fieldData;

	public String metaValue;

	// ----------------------------------------------------------------------
	private long processedTileData;
	private EnumBitflag<Passability> tilePassable;

	// ----------------------------------------------------------------------

	public boolean shouldPlaceCorridorFeatures()
	{
		return containingRoom == null || containingRoom.roomData == null || !containingRoom.roomData.skipPlacingCorridor;
	}

	public Symbol copy()
	{
		Symbol s = new Symbol();
		s.extendsSymbol = extendsSymbol;
		s.character = character;
		s.tileData = tileData;
		s.environmentData = environmentData;
		s.fieldData = fieldData;
		s.attachLocation = attachLocation;
		s.entityData = entityData;
		s.metaValue = metaValue;

		return s;
	}

	public boolean hasEnvironmentEntity()
	{
		return environmentData != null;
	}

	public EnvironmentEntity getEnvironmentEntity( LevelManager.LevelData levelData )
	{
		if ( environmentData != null )
		{
			EnvironmentEntity ee = EnvironmentEntity.load( environmentData, levelData );
			return ee;
		}

		return null;
	}

	public boolean getEnvironmentEntityPassable( EnumBitflag<Passability> travelType )
	{
		if ( environmentData != null ) { return Passability.parse( environmentData.get( "Passable", "false" ) ).intersect( travelType ); }

		return true;
	}

	public boolean hasGameEntity()
	{
		return entityData != null;
	}

	public TileData getTileData()
	{
		TileData data = TileData.parse( tileData );

		processedTileData = tileData.hashCode();
		tilePassable = data.passableBy;

		return data;
	}

	public static Symbol parse( Element xml )
	{
		Symbol symbol = new Symbol();

		// load the base symbol
		String extendsString = xml.getAttribute( "Extends", null );
		if (extendsString != null)
		{
			symbol.extendsSymbol = extendsString.charAt( 0 );
		}

		// fill in the new values
		symbol.character = xml.get( "Char", "" + symbol.character ).charAt( 0 );

		Element tileDataElement = xml.getChildByName( "TileData" );
		if ( tileDataElement != null )
		{
			symbol.tileData = tileDataElement;
		}

		Element enviromentDataElement = xml.getChildByName( "EnvironmentData" );
		if ( enviromentDataElement != null )
		{
			symbol.environmentData = enviromentDataElement;
		}

		Element entityElement = xml.getChildByName( "EntityData" );
		if (entityElement != null)
		{
			if (entityElement.getAttribute( "Extends", null ) != null || entityElement.getChildCount() > 0)
			{
				symbol.entityData = entityElement;
			}
			else
			{
				symbol.entityData = entityElement.getText();
			}
		}

		Element fieldElement = xml.getChildByName( "FieldData" );
		if ( fieldElement != null )
		{
			symbol.fieldData = fieldElement;
		}

		symbol.metaValue = xml.get( "MetaValue", symbol.metaValue );

		return symbol;
	}

	public void resolveExtends( HashMap<Character, Symbol> symbolMap )
	{
		if ( extendsSymbol != null )
		{
			Symbol otherSymbol = symbolMap.get( extendsSymbol );
			if (otherSymbol == null)
			{
				if (otherSymbol == null)
				{
					throw new RuntimeException("Attempted to use undefined symbol: " + extendsSymbol);
				}
			}

			extendsSymbol = null;

			otherSymbol.resolveExtends( symbolMap );

			if (tileData == null) { tileData = otherSymbol.tileData; }

			if (tileData == null)
			{
				throw new RuntimeException( "Failed to get tiledata for symbol: " + character );
			}

			if (environmentData == null) { environmentData = otherSymbol.environmentData; }
			if (entityData == null) { entityData = otherSymbol.entityData; }
			if (metaValue == null) { metaValue = otherSymbol.metaValue; }
		}
	}

	public boolean isPassable( EnumBitflag<Passability> travelType )
	{
		if (tileData == null)
		{
			throw new RuntimeException( "No tiledata parsed for '"+character+"' extends '"+extendsSymbol+"'" );
		}

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
	public boolean getPassable( EnumBitflag<Passability> travelType, Object self )
	{
		return isPassable( travelType );
	}

	private static final EnumBitflag<Passability> InfluencePassable = new EnumBitflag<Passability>( Passability.WALK );

	@Override
	public int getInfluence( EnumBitflag<Passability> travelType, Object self )
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