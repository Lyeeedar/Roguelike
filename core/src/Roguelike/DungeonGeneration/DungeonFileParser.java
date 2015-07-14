package Roguelike.DungeonGeneration;

import java.io.IOException;
import java.util.HashMap;

import Roguelike.AssetManager;
import Roguelike.Entity.GameEntity;
import Roguelike.Lights.Light;
import Roguelike.Sprite.Sprite;
import Roguelike.Tiles.GameTile;
import Roguelike.Tiles.TileData;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class DungeonFileParser
{
	public enum SymbolType
	{
		TILE,
		ENTITY
	}
	
	public class Symbol
	{		
		public char character;
		public SymbolType type;
		
		public TileData tileData;
		public String entityName;		
	}
	
	public class Room
	{
		public int width;
		public int height;
		public HashMap<Character, Symbol> localSymbolMap;
		public char[][] roomDef;
	}
	
	public HashMap<Character, Symbol> sharedSymbolMap;
	
	public Array<Room> rooms = new Array<Room>();
	
	public void placeRoom(int x, int y, int width, int height, GameTile[][] tiles)
	{
		Array<Room> validRooms = new Array<Room>();
		for(Room r : rooms)
		{
			if (r.width <= width && r.height <= height)
			{
				validRooms.add(r);
			}
		}
		
		if (validRooms.size == 0) { return; }
		
		Room chosen = validRooms.get(MathUtils.random(validRooms.size-1));
		
		for (int rx = 0; rx < chosen.width; rx++)
		{
			for (int ry = 0; ry < chosen.height; ry++)
			{
				if (x+rx >= tiles.length-1 || y+ry >= tiles[0].length-1) { continue; }
				
				char c = chosen.roomDef[rx][ry];
				Symbol s = chosen.localSymbolMap.get(c);
				if (s == null) { s = sharedSymbolMap.get(c); }
				if (s == null) { s = sharedSymbolMap.get('.'); }
				
				GameTile tile = tiles[x+rx][y+ry];
				
				if (s.tileData == null)
				{
					tile.TileData = sharedSymbolMap.get('.').tileData;
				}
				else
				{
					tile.TileData = s.tileData;
				}
				
				if (s.entityName != null)
				{
					tile.addObject(GameEntity.load(s.entityName));
				}
			}
		}
	}
	
	private HashMap<Character, Symbol> parseSymbolList(Element symbolsElement)
	{		
		HashMap<Character, Symbol> map = new HashMap<Character, Symbol>();
		
		for (Element symbolElement : symbolsElement.getChildrenByName("Symbol"))
		{
			Symbol symbol = new Symbol();
			symbol.character = symbolElement.get("Char").charAt(0);
			symbol.type = SymbolType.valueOf(symbolElement.get("Type").toUpperCase());
			
			if (symbol.type == SymbolType.TILE)
			{
				Element tileElement = symbolElement.getChildByName("Data");
				Sprite floorSprite = AssetManager.loadSprite(tileElement.getChildByName("FloorSprite"));
				Sprite featureSprite = tileElement.getChildByName("FeatureSprite") != null ? AssetManager.loadSprite(tileElement.getChildByName("FeatureSprite")) : null;
				boolean opaque = tileElement.getBoolean("Opaque");
				boolean passable = tileElement.getBoolean("Passable");
				Light light = tileElement.getChildByName("Light") != null ? Light.load(tileElement.getChildByName("Light")) : null;
				symbol.tileData = new TileData(floorSprite, featureSprite, light, opaque, passable, "");
			}
			else
			{
				symbol.entityName = symbolElement.get("Data");
			}
			
			map.put(symbol.character, symbol);
		}
		
		return map;
	}

	//----------------------------------------------------------------------
	private void internalLoad(String name)
	{
		XmlReader xml = new XmlReader();
		Element xmlElement = null;

		try
		{
			xmlElement = xml.parse(Gdx.files.internal("Levels/"+name+".xml"));
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		sharedSymbolMap = xmlElement.getChildByName("Symbols") != null ? parseSymbolList(xmlElement.getChildByName("Symbols")) : new HashMap<Character, Symbol>();
		
		Element roomsElement = xmlElement.getChildByName("Rooms");
		for (int i = 0; i < roomsElement.getChildCount(); i++)
		{
			Element roomElement = roomsElement.getChild(i);
			
			Room room = new Room();
			
			room.width = roomElement.getInt("width");
			room.height = roomElement.getInt("height");
			room.localSymbolMap = roomElement.getChildByName("Symbols") != null ? parseSymbolList(roomElement.getChildByName("Symbols")) : new HashMap<Character, Symbol>();
			room.roomDef = new char[room.width][room.height];
			
			Element rowsElement = roomElement.getChildByName("Rows");
			for (int y = 0; y < room.height; y++)
			{
				Element rowElement = rowsElement.getChild(y);
				String row = rowElement.getText();
				for (int x = 0; x < room.width; x++)
				{
					room.roomDef[x][y] = row.charAt(x);
				}
			}
			
			rooms.add(room);
		}
	}
	
	public static DungeonFileParser load(String name)
	{
		DungeonFileParser dfp = new DungeonFileParser();
		
		dfp.internalLoad(name);
		
		return dfp;
	}
}
