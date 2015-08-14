package Roguelike.Sound;

import java.util.HashSet;

import Roguelike.AssetManager;
import Roguelike.Global.Passability;
import Roguelike.Pathfinding.AStarPathfind;
import Roguelike.Tiles.GameTile;
import Roguelike.Tiles.Point;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.XmlReader.Element;

public class SoundInstance
{
	private static final Array<Passability> SoundPassability = new Array<Passability>(new Passability[]{Passability.LEVITATE, Passability.ENTITY});
	
	public Sound sound;
	
	public float minPitch = 0.8f;
	public float maxPitch = 1.2f;
	public float volume = 0.5f;
		
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
		Point shoutSource = Pools.obtain(Point.class).set(tile.x, tile.y);
		
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
							AStarPathfind astar = new AStarPathfind(tile.level.getGrid(), tile.x, tile.y, x, y, true, false, SoundPassability);
							Array<Point> path = astar.getPath();
														
							if (path != null && path.size < maxAudibleDist)
							{
								if (t.entity == tile.level.player)
								{
									playerDist = path.size;
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
							
							if (path != null)
							{
								Pools.freeAll(path);
							}
						}
					}
				}
			}
		}
		else
		{
			AStarPathfind astar = new AStarPathfind(tile.level.getGrid(), tile.x, tile.y, tile.level.player.tile.x, tile.level.player.tile.y, true, false, SoundPassability);
			Array<Point> path = astar.getPath();
			
			if (path != null) 
			{ 
				playerDist = path.size;
				Pools.freeAll(path);
			}
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
						
			sound.play(vol, minPitch+MathUtils.random()*(maxPitch-minPitch), 0);
		}
	}
}
