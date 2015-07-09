package Roguelike.DungeonGeneration;

import java.util.Random;

import com.badlogic.gdx.utils.Array;

import Roguelike.AssetManager;
import Roguelike.Entity.Entity;
import Roguelike.Levels.Level;
import Roguelike.Pathfinding.PathfindingTile;
import Roguelike.Sprite.Sprite;
import Roguelike.Tiles.GameTile;
import Roguelike.Tiles.TileData;

public class VillageGenerator
{
	public enum TileType
	{
		GRASS,
		HOUSE,
		WALL
	}
	
	Random ran = new Random();
	
	private class Building
	{
		int x;
		int y;
		int width;
		int height;
		
		public Building(int x, int y, int width, int height)
		{
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}
	}
	
	private class GenerationTile implements PathfindingTile
	{
		public TileType tileType = TileType.GRASS;
		
		@Override
		public boolean GetPassable()
		{
			return tileType == TileType.GRASS;
		}
	}
	
	int width;
	int height;
	
	final GenerationTile[][] tiles;
	Array<Building> buildings = new Array<Building>();
	
	public VillageGenerator(int width, int height)
	{
		this.width = width;
		this.height = height;
		
		this.tiles = new GenerationTile[width][height];
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				tiles[x][y] = new GenerationTile();
			}
		}
	}
	
	public Level getLevel()
	{
		Sprite wall = AssetManager.loadSprite("Objects/Wall", 0.5f, new int[]{16, 16}, new int[]{10, 3});
		Sprite floor = AssetManager.loadSprite("Objects/Floor", 0.5f, new int[]{16, 16}, new int[]{8, 19});
		Sprite grass = AssetManager.loadSprite("Objects/Floor", 0.5f, new int[]{16, 16}, new int[]{8, 7});
		Sprite ceiling = AssetManager.loadSprite("Objects/Wall", 0.5f, new int[]{16, 16}, new int[]{10, 27});
		
		TileData wallData = new TileData(wall, null, null, true, false, "wall");
		TileData floorData = new TileData(floor, null, null, false, true, "floor");
		TileData grassData = new TileData(grass, null, null, false, true, "grass");
		
		GameTile[][] actualTiles = new GameTile[width][height];
		Level level = new Level(actualTiles);
		
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				GenerationTile oldTile = tiles[x][y];
				
				TileData data = null;
				if (oldTile.tileType == TileType.GRASS)
				{
					data = grassData;
				}
				else if (oldTile.tileType == TileType.HOUSE)
				{
					data = floorData;
				}
				else
				{
					data = wallData;
				}

				GameTile newTile = new GameTile
				(
					x, y,
					level,
					data
				);
				
				actualTiles[x][y] = newTile;
			}
		}
		
		for (Building room : buildings)
		{
			Entity obj = Entity.load("Enemies/orc");			
			actualTiles[room.x+room.width/2][room.y+room.height/2].addObject(obj);
		}
		
		return level;
	}
	
	public void generate() 
	{	
		partition(0, 0, width, height, buildings);
		
		for (Building b : buildings)
		{
			boolean placedDoor = false;
			for (int x = 0; x < b.width; x++)
			{
				for (int y = 0; y < b.height; y++)
				{					
					if (x == 0 || x == b.width-1 || y == 0 || y == b.height-1)
					{
						if (!placedDoor)
						{
							if (ran.nextInt(5) == 0)
							{
								tiles[x+b.x][y+b.y].tileType = TileType.HOUSE;
								placedDoor = true;
							}
							else
							{
								tiles[x+b.x][y+b.y].tileType = TileType.WALL;
							}
						}
						else
						{
							tiles[x+b.x][y+b.y].tileType = TileType.WALL;
						}
					}
					else
					{
						tiles[x+b.x][y+b.y].tileType = TileType.HOUSE;
					}
				}
			}
		}
	}
	
	private int minSize = 5;
	public void partition(int x, int y, int width, int height, Array<Building> output)
	{
		if (width < minSize*3 && height < minSize*3)
		{
			float pw = 0.6f + ran.nextFloat() * 0.3f;
			float ph = 0.6f + ran.nextFloat() * 0.3f;
			
			float ow = ran.nextFloat() * (1.0f - pw);
			float oh = ran.nextFloat() * (1.0f - ph);
			
			int nw = (int)(width*pw);
			int nh = (int)(height*ph);
			
			int now = (int)(width*ow);
			int noh = (int)(height*oh);
			
			output.add(new Building(x+now, y+noh, nw, nh));
		}
		else if (width < minSize*3)
		{
			float split = 0.3f + ran.nextFloat() * 0.4f;
			int splitheight = (int)(height * split);
			
			partition(x, y, width, splitheight, output);
			partition(x, y+splitheight, width, height-splitheight, output);
		}
		else if (height < minSize*3)
		{
			float split = 0.3f + ran.nextFloat() * 0.4f;
			int splitwidth = (int)(width * split);
			
			partition(x, y, splitwidth, height, output);
			partition(x+splitwidth, y, width-splitwidth, height, output);
		}
		else
		{
			boolean vertical = ran.nextBoolean();
			if (vertical)
			{
				float split = 0.3f + ran.nextFloat() * 0.4f;
				int splitwidth = (int)(width * split);
				
				partition(x, y, splitwidth, height, output);
				partition(x+splitwidth, y, width-splitwidth, height, output);
			}
			else
			{
				float split = 0.3f + ran.nextFloat() * 0.4f;
				int splitheight = (int)(height * split);
				
				partition(x, y, width, splitheight, output);
				partition(x, y+splitheight, width, height-splitheight, output);
			}
		}
	}
}


/*
abbey almshouse altar ampitheater apartments archives artgallery asylum
auctionblock bank bar(gate) barn barracks baths bazaar belltower
boardinghouse brewery bridge bridlesmith brothel burialmound butchersrow
butchery cairn calvarystable casino castle catacombs cathedral causeway
censor chapel chapterhouse charnelhouse church churchyard circus citadel
civilcourt clinic clocktower cloister club college colosseum column
commonhall cornmarket court crane(on docks, takes goods off ships)
criminalcourt croft crypt customs customshouse dam dock earthhouse eatery
embassy estate fairground fishmarket flophouse foodmarket fortress foundry
fountian friary fullingmill gaol/jail garden gate gatehouse gateway
generalstore gibbet grammerschool graveyard greathall green greene
greenhouse griddlesmith guardtower guild guildhall gymnasium hall house
harbor haymongergate hill hillfort hippodrome home hospital hostel house
hovel hut hypocaust icehouse inn jail keep kennel lane latrine laundry
leperhouse library lighthouse limekiln livestockmarket lumberyard manor
manorhouse mansion march market marketcenter marketcross mausoleum mayors
meatmarket meetinghall mews mill mint monastary monolith monument moothall
museum necropolis nedlergate nursery orchard palace park parsonage pawnshop
pier pillory place port porterslodge postern poultry priory prison privy
propylaeum punishmentsquare quay resturaunt row saltern school shambles
shirehouse shrine silo slavemarket smelters smithy smokehouse spital square
stable stadium staith standingstone standingstones statue stockfishrow
stocksmarket street tavern temple tenterfield theater theatre thorpe
tolhouse tollbooth tollsell tomb tournamentfield tower town townhall
trainingschools treasury university vicarage warehouse watchtower watermill
weighhouse well wharf windmill woolhouse zoo
*/