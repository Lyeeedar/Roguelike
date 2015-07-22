package Roguelike.DungeonGeneration;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import Roguelike.DungeonGeneration.RecursiveDockGenerator.Room;
import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Pathfinding.PathfindingTile;
import Roguelike.Tiles.TileData;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class DungeonFileParser
{
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
			if (rowsElement.getChildCount() > 0)
			{
				// Rows defined here
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
			}
			else
			{
				// Rows in seperate csv file
				String fileName = rowsElement.getText();
				FileHandle handle = Gdx.files.internal(fileName+".csv");
				String content = handle.readString();
				
				String[] lines = content.split(System.getProperty("line.separator"));
				room.height = lines.length;
				
				String[][] rows = new String[lines.length][];				
				for (int i = 0; i < lines.length; i++)
				{
					rows[i] = lines[i].split(" ");
					
					room.width = rows[i].length;
				}
				
				room.roomDef = new char[room.width][room.height];
				for (int x = 0; x < room.width; x++)
				{
					for (int y = 0; y < room.height; y++)
					{
						room.roomDef[x][y] = rows[x][y].charAt(0);
					}
				}
			}
						
			
			
			Element symbolsElement = xml.getChildByName("Symbols");
			if (symbolsElement != null)
			{
				for (int i = 0; i < symbolsElement.getChildCount(); i++)
				{
					Symbol symbol = Symbol.parse(symbolsElement.getChild(i), sharedSymbolMap, room.localSymbolMap);
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
					if (s == null) 
					{ 
						System.out.println("Failed to find symbol for character '" + c +"'! Falling back to using '.'");
						s = sharedSymbolMap.get('.'); 
					}
					
					if (s == null)
					{
						s = sharedSymbolMap.get('.'); 
					}
					
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
	public Color ambient;
	
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
				Symbol symbol = Symbol.parse(symbolsElement.getChild(i), sharedSymbolMap, null);
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
		
		Element ae = xmlElement.getChildByName("Ambient");
		ambient = new Color(ae.getFloat("Red"), ae.getFloat("Blue"), ae.getFloat("Green"), ae.getFloat("Alpha"));
	}
	
	//----------------------------------------------------------------------
	public static DungeonFileParser load(String name)
	{
		DungeonFileParser dfp = new DungeonFileParser();
		
		dfp.internalLoad(name);
		
		return dfp;
	}
}
