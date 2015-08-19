package Roguelike.Tiles;

import java.util.EnumMap;

import Roguelike.Global;
import Roguelike.Global.Passability;
import Roguelike.Entity.Entity;
import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Entity.GameEntity;
import Roguelike.Fields.Field;
import Roguelike.Fields.Field.FieldLayer;
import Roguelike.Items.Item;
import Roguelike.Levels.Level;
import Roguelike.Pathfinding.PathfindingTile;
import Roguelike.Sprite.SpriteEffect;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class GameTile implements PathfindingTile
{
	public int x;
	public int y;

	public TileData tileData;

	public Color light;

	public GameEntity entity;
	public EnvironmentEntity environmentEntity;
	public EnumMap<FieldLayer, Field> fields = new EnumMap<FieldLayer, Field>( FieldLayer.class );

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
		if ( fields.size() > 0 )
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
		}
	}

	public final void addField( Field field )
	{
		clearField( field.layer );
		fields.put( field.layer, field );
		field.tile = this;
	}

	public final int[] addGameEntity( GameEntity obj )
	{
		GameTile oldTile = obj.tile;

		entity = obj;
		obj.tile = this;

		if ( oldTile != null )
		{
			oldTile.entity = null;
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
	public final boolean getPassable( Array<Passability> travelType )
	{
		if ( fields.size() > 0 )
		{
			for ( FieldLayer layer : FieldLayer.values() )
			{
				Field field = fields.get( layer );
				if ( field != null )
				{
					if ( Passability.isPassable( field.allowPassability, travelType ) )
					{
						return true;
					}
					else if ( Passability.isPassable( field.restrictPassability, travelType ) ) { return false; }
				}
			}
		}

		if ( environmentEntity != null && !Passability.isPassable( environmentEntity.passableBy, travelType ) ) { return false; }

		boolean passable = Passability.isPassable( tileData.passableBy, travelType );

		if ( !passable ) { return false; }

		return entity == null || travelType.contains( Passability.ENTITY, true );
	}

	@Override
	public int getInfluence()
	{
		return 0;
	}
}
