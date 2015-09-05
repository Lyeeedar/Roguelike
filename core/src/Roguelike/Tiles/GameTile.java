package Roguelike.Tiles;

import java.util.HashMap;
import java.util.Map;

import Roguelike.Global;
import Roguelike.Global.Passability;
import Roguelike.Entity.Entity;
import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Entity.GameEntity;
import Roguelike.Fields.Field;
import Roguelike.Fields.Field.FieldLayer;
import Roguelike.Items.Item;
import Roguelike.Levels.Level;
import Roguelike.Lights.Light;
import Roguelike.Pathfinding.PathfindingTile;
import Roguelike.Sprite.SpriteEffect;
import Roguelike.Util.EnumBitflag;
import Roguelike.Util.FastEnumMap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;

public class GameTile implements PathfindingTile
{
	public int x;
	public int y;

	public TileData tileData;

	private static final Color tempColour = new Color();

	public HashMap<Light, LightData> lightMap = new HashMap<Light, LightData>();
	public Color ambientColour = new Color();
	public Color light = new Color();

	public GameEntity entity;
	public EnvironmentEntity environmentEntity;
	public FastEnumMap<FieldLayer, Field> fields = new FastEnumMap<FieldLayer, Field>( FieldLayer.class );
	public boolean hasFields;

	public Level level;

	public Array<SpriteEffect> spriteEffects = new Array<SpriteEffect>();

	public Array<Item> items = new Array<Item>( false, 16 );
	public int essence;

	public String metaValue;

	public boolean visible;

	public GameTile( int x, int y, Level level, TileData tileData )
	{
		this.x = x;
		this.y = y;
		this.level = level;

		this.tileData = tileData;

		light = new Color( Color.WHITE );
	}

	public final void addEnvironmentEntity( EnvironmentEntity entity )
	{
		if ( entity.tile != null )
		{
			entity.tile.environmentEntity = null;
		}

		environmentEntity = entity;
		entity.tile = this;
	}

	public final void processFieldEffectsForEntity( Entity e, float cost )
	{
		if ( fields.size > 0 )
		{
			for ( FieldLayer layer : FieldLayer.values() )
			{
				Field field = fields.get( layer );

				if ( field != null )
				{
					field.processOnTurnEffectsForEntity( e, cost );
				}
			}
		}
	}

	public final void clearField( FieldLayer layer )
	{
		if ( fields.containsKey( layer ) )
		{
			Field field = fields.get( layer );
			if ( field != null )
			{
				field.tile = null;
			}

			fields.remove( layer );

			hasFields = fields.size > 0;
		}
	}

	public final void addField( Field field )
	{
		clearField( field.layer );
		fields.put( field.layer, field );
		field.tile = this;

		hasFields = fields.size > 0;
	}

	public final int[] addGameEntity( GameEntity obj )
	{
		GameTile oldTile = obj.tile;

		entity = obj;
		obj.tile = this;

		if ( oldTile != null )
		{
			if ( oldTile.entity == obj )
			{
				oldTile.entity = null;
			}

			return getPosDiff( oldTile );
		}

		return new int[] { 0, 0 };
	}

	public final int[] getPosDiff( GameTile prevTile )
	{
		int[] oldPos = new int[] { prevTile.x * Global.TileSize, prevTile.y * Global.TileSize };
		int[] newPos = { x * Global.TileSize, y * Global.TileSize };

		int[] diff = { oldPos[0] - newPos[0], oldPos[1] - newPos[1] };

		return diff;
	}

	public final float getDist( GameTile prevTile )
	{
		return Vector2.dst( prevTile.x, prevTile.y, x, y );
	}

	@Override
	public final boolean getPassable( EnumBitflag<Passability> travelType )
	{
		if ( fields.size > 0 )
		{
			for ( FieldLayer layer : FieldLayer.values() )
			{
				Field field = fields.get( layer );
				if ( field != null )
				{
					if ( field.allowPassability.intersect( travelType ) )
					{
						return true;
					}
					else if ( field.restrictPassability.intersect( travelType ) ) { return false; }
				}
			}
		}

		if ( environmentEntity != null && !environmentEntity.passableBy.intersect( travelType ) ) { return false; }

		boolean passable = tileData.passableBy.intersect( travelType );

		if ( !passable ) { return false; }

		return entity == null || travelType.contains( Passability.ENTITY );
	}

	@Override
	public int getInfluence()
	{
		return 0;
	}

	public void setLight( Light light, float intensity, Color colour )
	{
		lightMap.put( light, Pools.obtain( LightData.class ).set( intensity, colour ) );
	}

	public void clearLight( Light light )
	{
		LightData data = lightMap.get( light );
		Pools.free( data );
		lightMap.remove( light );
	}

	public void composeLight()
	{
		light.set( ambientColour );

		for ( Map.Entry<Light, LightData> pair : lightMap.entrySet() )
		{
			tempColour.set( pair.getValue().colour );

			tempColour.mul( tempColour.a );
			tempColour.a = 1;

			tempColour.mul( pair.getValue().intensity );
			light.add( tempColour );
		}
	}

	public static class LightData
	{
		public float intensity;
		public Color colour;

		public LightData()
		{

		}

		public LightData set( float intensity, Color colour )
		{
			this.intensity = intensity;
			this.colour = colour;

			return this;
		}
	}
}
