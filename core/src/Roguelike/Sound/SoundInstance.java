package Roguelike.Sound;

import java.util.HashSet;

import Roguelike.AssetManager;
import Roguelike.Pathfinding.AStarPathfind;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.XmlReader.Element;

public class SoundInstance
{
	public Sound sound;
	
	public float pitch = 1;
	public float volume = 1;
		
	public int range = 10;
	public int falloffMin = 5;
	
	public HashSet<String> shoutFaction;
	public String key;
	public Object value;
	
	private SoundInstance()
	{
		
	}
	
	public SoundInstance(Sound sound)
	{
		this.sound = sound;
	}
	
	public static SoundInstance load(Element xml)
	{
		SoundInstance sound = new SoundInstance();
		sound.sound = AssetManager.loadSound(xml.get("Name"));
		
		return sound;
	}
	
	public void play(GameTile tile)
	{
		// calculate data propogation
		float playerDist = Integer.MAX_VALUE;
		int[] shoutSource = {tile.x, tile.y};
		
		int maxAudibleDist = (range/4)*3;
		
		if (key != null)
		{
			for (int x = tile.x-range; x < tile.x+range; x++)
			{
				for (int y = tile.y-range; y < tile.y+range; y++)
				{
					if (x >= 0 && x < tile.level.width && y >= 0 && y < tile.level.height)
					{
						GameTile t = tile.level.getGameTile(x, y);
						
						if (t.entity != null)
						{
							AStarPathfind astar = new AStarPathfind(tile.level.getGrid(), tile.x, tile.y, x, y, true, false);
							int[][] path = astar.getPath();
														
							if (path != null && path.length < maxAudibleDist)
							{
								if (t.entity == tile.level.player)
								{
									playerDist = path.length;
								}
								else if (tile.entity.isAllies(shoutFaction))
								{
									t.entity.AI.setData(key, value);
								}
								else
								{
									t.entity.AI.setData("EnemyPos", shoutSource);
								}
							}
						}
					}
				}
			}
		}
		else
		{
			AStarPathfind astar = new AStarPathfind(tile.level.getGrid(), tile.x, tile.y, tile.level.player.tile.x, tile.level.player.tile.y, true, false);
			int[][] path = astar.getPath();
			
			if (path != null) { playerDist = path.length; }
		}
		
		// calculate sound play volume
		if (playerDist < range)
		{			
			float vol = volume;
			
			if (playerDist > falloffMin)
			{
				float alpha = 1 - (playerDist - falloffMin) / (range - falloffMin);
				vol *= alpha;
			}
						
			sound.play(vol, pitch, 0);
		}
	}
}
