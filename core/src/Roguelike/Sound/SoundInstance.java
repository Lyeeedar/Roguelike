package Roguelike.Sound;

import java.io.IOException;
import java.util.HashSet;

import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.Global.Passability;
import Roguelike.Pathfinding.AStarPathfind;
import Roguelike.Tiles.GameTile;
import Roguelike.Tiles.Point;
import Roguelike.Util.EnumBitflag;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class SoundInstance
{
	private static final EnumBitflag<Passability> SoundPassability = new EnumBitflag<Passability>( new Passability[] { Passability.LEVITATE, Passability.ENTITY } );

	public Sound sound;
	public String name;

	public float minPitch = 0.7f;
	public float maxPitch = 1.5f;
	public float volume = 0.5f;

	public int range = 10;
	public int falloffMin = 5;

	public HashSet<String> shoutFaction;
	public String key;
	public Object value;

	public SoundInstance()
	{

	}

	public SoundInstance( Sound sound )
	{
		this.sound = sound;
	}

	public static SoundInstance load( Element xml )
	{
		SoundInstance sound = new SoundInstance();
		sound.name = xml.get( "Name" );
		sound.sound = AssetManager.loadSound( sound.name );
		sound.range = xml.getInt( "Range", sound.range );
		sound.falloffMin = xml.getInt( "FalloffMin", sound.falloffMin );
		sound.volume = xml.getFloat( "Volume", sound.volume );

		sound.minPitch = xml.getFloat( "Pitch", sound.minPitch );
		sound.minPitch = xml.getFloat( "Pitch", sound.maxPitch );

		sound.minPitch = xml.getFloat( "MinPitch", sound.minPitch );
		sound.maxPitch = xml.getFloat( "MaxPitch", sound.maxPitch );

		return sound;
	}

	public void play( GameTile tile )
	{
		// calculate data propogation
		float playerDist = Integer.MAX_VALUE;
		Point shoutSource = Global.PointPool.obtain().set( tile.x, tile.y );

		int maxAudibleDist = range;// ( range / 4 ) * 3;

		if ( key != null )
		{
			for ( int x = tile.x - range; x < tile.x + range; x++ )
			{
				for ( int y = tile.y - range; y < tile.y + range; y++ )
				{
					if ( x >= 0 && x < tile.level.width && y >= 0 && y < tile.level.height )
					{
						GameTile t = tile.level.getGameTile( x, y );

						if ( t.entity != null )
						{
							AStarPathfind astar = new AStarPathfind( tile.level.getGrid(), tile.x, tile.y, x, y, Global.CanMoveDiagonal, false, 1, SoundPassability, null );
							Array<Point> path = astar.getPath();

							if ( path != null && path.size < maxAudibleDist )
							{
								if ( t.entity == tile.level.player )
								{
									playerDist = path.size;
								}
								else if ( tile.entity.isAllies( shoutFaction ) )
								{
									t.entity.AI.setData( key, value );
								}
								else
								{
									t.entity.AI.setData( "EnemyPos", shoutSource );
								}
							}

							if ( path != null )
							{
								Global.PointPool.freeAll( path );
							}
						}

						if ( t.environmentEntity != null && t.environmentEntity.onHearAction != null )
						{
							AStarPathfind astar = new AStarPathfind( tile.level.getGrid(), tile.x, tile.y, x, y, true, false, 1, SoundPassability, null );
							Array<Point> path = astar.getPath();

							if ( path != null && path.size < maxAudibleDist )
							{
								t.environmentEntity.onHearAction.process( t.environmentEntity, shoutSource, key, value );
							}

							if ( path != null )
							{
								Global.PointPool.freeAll( path );
							}
						}
					}
				}
			}
		}
		else
		{
			playerDist = Vector2.dst( tile.x, tile.y, tile.level.player.tile[0][0].x, tile.level.player.tile[0][0].y );
		}

		// calculate sound play volume
		if ( playerDist <= range && sound != null )
		{
			float vol = volume * Global.EffectVolume;

			if ( playerDist > falloffMin )
			{
				float alpha = 1 - ( playerDist - falloffMin ) / ( range - falloffMin );
				vol *= alpha;
			}

			float xdiff = tile.x - tile.level.player.tile[0][0].x;
			xdiff /= range;

			sound.play( vol, minPitch + MathUtils.random() * ( maxPitch - minPitch ), xdiff );
		}
	}

	private static final ObjectMap<String, Element> soundMap = new ObjectMap<String, Element>(  );
	private static boolean loaded = false;
	public static SoundInstance getSound( String name )
	{
		if ( !loaded )
		{
			loaded = true;

			XmlReader reader = new XmlReader();
			Element xml = null;

			try
			{
				xml = reader.parse( Gdx.files.internal( "Sound/SoundMap.xml" ) );
			}
			catch ( IOException e )
			{
				e.printStackTrace();
			}

			for ( int i = 0; i < xml.getChildCount(); i++ )
			{
				Element el = xml.getChild( i );
				soundMap.put( el.getName(), el );
			}
		}

		if ( soundMap.containsKey( name ) )
		{
			return SoundInstance.load( soundMap.get( name ) );
		}
		else
		{
			SoundInstance sound = new SoundInstance(  );
			sound.name = name;
			sound.sound = AssetManager.loadSound( name );
			return sound;
		}
	}
}
