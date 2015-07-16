package Roguelike.DungeonGeneration;

import java.io.IOException;
import java.util.HashMap;

import Roguelike.DungeonGeneration.RecursiveDockGenerator.Room;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class DungeonFileParser
{
	//----------------------------------------------------------------------
	public enum SymbolType
	{
		TILE,
		GAMEENTITY,
		ENVIRONMENTENTITY
	}
	
	//----------------------------------------------------------------------
	public static class Symbol
	{		
		public char character;
		public SymbolType type;
		public Element data;
		
		public static Symbol parse(Element xml)
		{
			Symbol symbol = new Symbol();
			symbol.character = xml.get("Char").charAt(0);
			symbol.type = SymbolType.valueOf(xml.get("Type").toUpperCase());
			symbol.data = xml.getChildByName("Data");
			
			return symbol;
		}
		
		public boolean isDoor()
		{
			return type == SymbolType.ENVIRONMENTENTITY && data.get("Type").equals("Door");
		}
		
		public boolean isPassable()
		{
			return type == SymbolType.TILE && data.getBoolean("Passable");
		}
	}
	
	//----------------------------------------------------------------------
	public static class DFPRoom
	{
		public int width;
		public int height;
		public HashMap<Character, Symbol> localSymbolMap = new HashMap<Character, Symbol>();
		public HashMap<Character, Symbol> sharedSymbolMap;
		public char[][] roomDef;
		
		public static DFPRoom parse(Element xml, HashMap<Character, Symbol> sharedSymbolMap)
		{
			DFPRoom room = new DFPRoom();
			room.sharedSymbolMap = sharedSymbolMap;
			
			Element rowsElement = xml.getChildByName("Rows");
			room.height = rowsElement.getChildCount();
			for (int i = 0; i < room.height; i++)
			{
				if (rowsElement.getChild(i).getText().length() > room.width)
				{
					room.width = rowsElement.getChild(i).getText().length();
				}
			}
			
			room.roomDef = new char[room.width][room.height];
			for (int x = 0; x < room.width; x++)
			{
				for (int y = 0; y < room.height; y++)
				{
					room.roomDef[x][y] = rowsElement.getChild(y).getText().charAt(x);
				}
			}
			
			Element symbolsElement = xml.getChildByName("Symbols");
			if (symbolsElement != null)
			{
				for (int i = 0; i < symbolsElement.getChildCount(); i++)
				{
					Symbol symbol = Symbol.parse(symbolsElement.getChild(i));
					room.localSymbolMap.put(symbol.character, symbol);
				}
			}
			
			return room;
		}
		
		public void fillRoom(Room room)
		{
			room.width = width;
			room.height = height;
			room.roomContents = new Symbol[width][height];
			
			for (int x = 0; x < width; x++)
			{
				for (int y = 0; y < height; y++)
				{
					char c = roomDef[x][y];
					Symbol s = localSymbolMap.get(c);
					if (s == null) { s = sharedSymbolMap.get(c); }
					if (s == null) { s = sharedSymbolMap.get('.'); }
					
					room.roomContents[x][y] = s;
				}
			}
		}
	}
	
	//----------------------------------------------------------------------
	public HashMap<Character, Symbol> sharedSymbolMap = new HashMap<Character, Symbol>();
	
	//----------------------------------------------------------------------
	public Array<DFPRoom> requiredRooms = new Array<DFPRoom>();
	
	//----------------------------------------------------------------------
		public Array<DFPRoom> optionalRooms = new Array<DFPRoom>();
		
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
		
		Element symbolsElement = xmlElement.getChildByName("Symbols");
		if (symbolsElement != null)
		{
			for (int i = 0; i < symbolsElement.getChildCount(); i++)
			{
				Symbol symbol = Symbol.parse(symbolsElement.getChild(i));
				sharedSymbolMap.put(symbol.character, symbol);
			}
		}
		
		Element requiredElement = xmlElement.getChildByName("Required");
		if (requiredElement != null)
		{
			for (int i = 0; i < requiredElement.getChildCount(); i++)
			{
				DFPRoom room = DFPRoom.parse(requiredElement.getChild(i), sharedSymbolMap);
				requiredRooms.add(room);
			}
		}
		
		Element optionalElement = xmlElement.getChildByName("Optional");
		if (optionalElement != null)
		{
			for (int i = 0; i < optionalElement.getChildCount(); i++)
			{
				DFPRoom room = DFPRoom.parse(optionalElement.getChild(i), sharedSymbolMap);
				optionalRooms.add(room);
			}
		}
	}
	
	//----------------------------------------------------------------------
	public static DungeonFileParser load(String name)
	{
		DungeonFileParser dfp = new DungeonFileParser();
		
		dfp.internalLoad(name);
		
		return dfp;
	}
}
