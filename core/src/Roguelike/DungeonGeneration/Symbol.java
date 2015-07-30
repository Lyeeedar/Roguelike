package Roguelike.DungeonGeneration;

import java.util.HashMap;
import java.util.HashSet;

import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Entity.GameEntity;
import Roguelike.Pathfinding.PathfindingTile;
import Roguelike.Tiles.TileData;

import com.badlogic.gdx.utils.XmlReader.Element;

//----------------------------------------------------------------------
public class Symbol implements PathfindingTile
{		
	public char character;

	public Element tileData;

	public Element environmentData;
	public EnvironmentEntity environmentEntityObject;

	public String entityData;

	public String metaValue;
	
	//----------------------------------------------------------------------
	private Element processedEnvironmentData;
	private Element processedTileData;
	
	private boolean eePassable;
	private boolean tilePassable;	
	//----------------------------------------------------------------------
	
	public Symbol copy()
	{
		Symbol s = new Symbol();
		s.character = character;
		s.tileData = tileData;
		s.environmentData = environmentData;
		s.environmentEntityObject = environmentEntityObject;
		s.entityData = entityData;
		s.metaValue = metaValue;
		
		return s;
	}
	
	public boolean hasEnvironmentEntity()
	{
		return environmentEntityObject != null || environmentData != null;
	}
	
	public EnvironmentEntity getEnvironmentEntity()
	{
		if (environmentEntityObject != null)
		{
			return environmentEntityObject;
		}
		
		if (environmentData != null)
		{
			EnvironmentEntity ee = EnvironmentEntity.load(environmentData);
			
			processedEnvironmentData = environmentData;
			eePassable = ee.passable;
			
			return ee;
		}
		
		processedEnvironmentData = null;
		eePassable = true;
		
		return null;
	}

	public boolean getEnvironmentEntityPassable()
	{
		if (environmentEntityObject != null)
		{
			return environmentEntityObject.passable;
		}
		
		if (environmentData != null)
		{
			return environmentData.getBoolean("Passable", true);
		}
		
		return true;
	}
	
	public boolean hasGameEntity()
	{
		return entityData != null;
	}
	
	public GameEntity getGameEntity()
	{
		return entityData != null ? GameEntity.load(entityData) : null;
	}
	
	public TileData getTileData()
	{
		TileData data = TileData.parse(tileData);
		
		processedTileData = tileData;
		tilePassable = data.passable;
		
		return data;
	}

	public static Symbol parse(Element xml, HashMap<Character, Symbol> sharedSymbolMap, HashMap<Character, Symbol> localSymbolMap)
	{
		Symbol symbol = new Symbol();

		// load the base symbol
		if (xml.getAttribute("Extends", null) != null)
		{
			char extendsSymbol = xml.getAttribute("Extends").charAt(0);

			Symbol rs = localSymbolMap != null ? localSymbolMap.get(extendsSymbol) : null;
			if (rs == null)
			{
				rs = sharedSymbolMap.get(extendsSymbol);
			}

			symbol.character = rs.character;
			symbol.tileData = rs.tileData;
			symbol.environmentData = rs.environmentData;
			symbol.entityData = rs.entityData;
			symbol.metaValue = rs.metaValue;
		}

		// fill in the new values
		symbol.character = xml.get("Char", ""+symbol.character).charAt(0);

		if (xml.getChildByName("TileData") != null)
		{
			symbol.tileData = xml.getChildByName("TileData");
		}

		if (xml.getChildByName("EnvironmentData") != null)
		{
			symbol.environmentData = xml.getChildByName("EnvironmentData");
		}

		if (xml.getChildByName("EntityData") != null)
		{
			symbol.entityData = xml.get("EntityData");
		}

		symbol.metaValue = xml.get("MetaValue", symbol.metaValue);

		return symbol;
	}

	public boolean isPassable()
	{
		if (processedTileData != tileData)
		{
			getTileData();
		}
		
		return tilePassable;
	}

	@Override
	public String toString()
	{
		return ""+character;
	}

	@Override
	public boolean getPassable()
	{
		return isPassable();
	}

	@Override
	public int getInfluence()
	{
		if (character == 'F')
		{
			if (hasEnvironmentEntity())
			{
				if (!getEnvironmentEntityPassable())
				{
					return 100;
				}
			}
		}
		
		return 0;
	}
}