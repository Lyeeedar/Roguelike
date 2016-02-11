package Roguelike.Tiles;

import Roguelike.Entity.Entity;
import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Entity.GameEntity;
import Roguelike.Fields.Field;
import Roguelike.Fields.Field.FieldLayer;
import Roguelike.Global;
import Roguelike.Global.Passability;
import Roguelike.Items.Item;
import Roguelike.Levels.Level;
import Roguelike.Lights.Light;
import Roguelike.Pathfinding.PathfindingTile;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.SpriteEffect;
import Roguelike.Sprite.TilingSprite;
import Roguelike.Util.EnumBitflag;
import Roguelike.Util.FastEnumMap;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;
import java.util.Map;

public class GameTile implements PathfindingTile
{
	private static final Color tempColour = new Color();
	public int x;
	public int y;

	public TileData tileData;
	public HashMap<Light, LightData> lightMap = new HashMap<Light, LightData>();
	public Color ambientColour = new Color();
	public Color light = new Color();
	public GameEntity entity;
	public GameEntity prevEntity;
	public EnvironmentEntity environmentEntity;
	public FastEnumMap<FieldLayer, Field> fields = new FastEnumMap<FieldLayer, Field>( FieldLayer.class );
	public boolean hasFields;
	public boolean hasFieldLight;
	public Level level;
	public Array<SpriteEffect> spriteEffects = new Array<SpriteEffect>();
	public Array<Item> items = new Array<Item>( false, 16 );
	public FastEnumMap<OrbType, Integer> orbs = new FastEnumMap<OrbType, Integer>( OrbType.class );
	public String metaValue;
	public boolean visible;
	public boolean tempVisible;
	public boolean seen;

	public EnumBitflag<Global.Direction> seenBitflag = new EnumBitflag<Global.Direction>( );
	public EnumBitflag<Global.Direction> unseenBitflag = new EnumBitflag<Global.Direction>(  );

	public TileData.SpriteGroup spriteGroup;
	public Light lightObj;

	public float ranVal;

	public GameTile( int x, int y, Level level, TileData tileData, float ranVal )
	{
		this.x = x;
		this.y = y;
		this.level = level;

		this.tileData = tileData;
		this.ranVal = ranVal;

		float total = 0;
		for ( TileData.SpriteGroup group : tileData.spriteGroups )
		{
			total += group.chance;

			if (total >= ranVal)
			{
				spriteGroup = group;
				break;
			}
		}

		if (spriteGroup == null)
		{
			spriteGroup = tileData.spriteGroups.first();
		}
		lightObj = spriteGroup.light;

		light = new Color( Color.WHITE );
	}

	public boolean hasEntityStayedOnTile()
	{
		if (prevEntity != entity)
		{
			prevEntity = null;
		}

		return prevEntity != null;
	}

	public Array<Sprite> getSprites()
	{
		return spriteGroup.sprites;
	}

	public TilingSprite getTilingSprite()
	{
		return spriteGroup.tilingSprite;
	}

	public final void addEnvironmentEntity( EnvironmentEntity entity )
	{
		entity.removeFromTile();

		for ( int x = 0; x < entity.size; x++ )
		{
			for ( int y = 0; y < entity.size; y++ )
			{
				GameTile tile = level.Grid[ this.x + x ][ this.y + y ];
				tile.environmentEntity = entity;
				entity.tile[ x ][ y ] = tile;
			}
		}
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

	public final void addField( Field field )
	{
		clearField( field.layer );
		fields.put( field.layer, field );
		field.tile = this;

		hasFields = fields.size > 0;
		updateFieldLightFlag();
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
			updateFieldLightFlag();
		}
	}

	public final void updateFieldLightFlag()
	{
		hasFieldLight = false;

		if (hasFields)
		{
			for (FieldLayer layer : FieldLayer.values())
			{
				Field field = fields.get( layer );
				if (field != null && field.getSpriteGroup().light != null)
				{
					hasFieldLight = true;
					break;
				}
			}
		}
	}

	public final int[] addGameEntity( GameEntity obj )
	{
		GameTile oldTile = obj.tile[ 0 ][ 0 ];

		obj.removeFromTile();

		for ( int x = 0; x < obj.size; x++ )
		{
			for ( int y = 0; y < obj.size; y++ )
			{
				GameTile tile = level.Grid[ this.x + x ][ this.y + y ];
				tile.entity = obj;
				obj.tile[ x ][ y ] = tile;
			}
		}

		if ( oldTile != null ) { return getPosDiff( oldTile ); }

		return new int[]{ 0, 0 };
	}

	public final int[] getPosDiff( GameTile prevTile )
	{
		int[] oldPos = new int[]{ prevTile.x * Global.TileSize, prevTile.y * Global.TileSize };
		int[] newPos = { x * Global.TileSize, y * Global.TileSize };

		int[] diff = { oldPos[ 0 ] - newPos[ 0 ], oldPos[ 1 ] - newPos[ 1 ] };

		return diff;
	}

	public final float getDist( GameTile prevTile )
	{
		return Vector2.dst( prevTile.x, prevTile.y, x, y );
	}

	@Override
	public final boolean getPassable( EnumBitflag<Passability> travelType, Object self )
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

		if ( environmentEntity != null && environmentEntity != self && !environmentEntity.passableBy.intersect( travelType ) && !environmentEntity.canTakeDamage )
		{
			return false;
		}

		boolean passable = tileData.passableBy.intersect( travelType );

		if ( !passable ) { return false; }

		if ( entity == null || entity == self || travelType.contains( Passability.ENTITY ) )
		{
			return true;
		}

		if ( entity != null && self instanceof GameEntity )
		{
			GameEntity selfEntity = (GameEntity)self;
			if ( !selfEntity.isAllies( entity ) )
			{
				return true;
			}
			else if ( hasEntityStayedOnTile() )
			{
				return true;
			}
			else if (entity.dialogue == null && selfEntity.canSwap && entity.canMove)
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public int getInfluence( EnumBitflag<Passability> travelType, Object self )
	{
		if (environmentEntity != null && !environmentEntity.passableBy.intersect( travelType ) && environmentEntity.canTakeDamage)
		{
			return 100;
		}

		return 0;
	}

	public void setLight( Light light, float intensity, Color colour )
	{
		lightMap.put( light, Global.LightDataPool.obtain().set( intensity, colour ) );
	}

	public void clearLight( Light light )
	{
		LightData data = lightMap.get( light );
		Global.LightDataPool.free( data );
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

	public enum OrbType
	{
		EXPERIENCE("Oryx/uf_split/uf_items/crystal_sun"),
		HEALTH("Oryx/uf_split/uf_items/crystal_blood");

		public final String spriteName;

		OrbType(String spriteName)
		{
			this.spriteName = spriteName;
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
