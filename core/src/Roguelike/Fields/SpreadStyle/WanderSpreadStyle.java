package Roguelike.Fields.SpreadStyle;

import Roguelike.Global.Direction;
import Roguelike.Global.Passability;
import Roguelike.Fields.Field;
import Roguelike.Tiles.GameTile;
import Roguelike.Util.EnumBitflag;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public class WanderSpreadStyle extends AbstractSpreadStyle
{
	private EnumBitflag<Passability> travelType;
	private float updateRate;

	@Override
	public void update( float delta, Field field )
	{
		float updateAccumulator = (Float) field.getData( "SpreadAccumulator", 0.0f );
		Direction lastMove = (Direction) field.getData( "LastMove", Direction.CENTER );

		updateAccumulator += delta;

		while ( updateAccumulator >= updateRate && field.stacks > 0 )
		{
			updateAccumulator -= updateRate;

			Array<Direction> validDirs = new Array<Direction>();
			for ( Direction dir : Direction.values() )
			{
				if ( dir == Direction.CENTER )
				{
					continue;
				}

				// prevent going backwards
				if ( dir.getX() != lastMove.getX() * -1 || dir.getY() != lastMove.getY() * -1 )
				{
					GameTile tile = field.tile.level.getGameTile( field.tile.x + dir.getX(), field.tile.y + dir.getY() );

					if ( tile.tileData.passableBy.intersect( travelType ) )
					{
						if ( tile.environmentEntity != null
								&& !tile.environmentEntity.canTakeDamage
								&& !tile.environmentEntity.passableBy.intersect( travelType ) )
						{
							// treat as impassible tile
						}
						else
						{
							validDirs.add( dir );
						}
					}
				}
			}

			if ( validDirs.size > 0 )
			{
				Direction dir = validDirs.random();
				GameTile newTile = field.tile.level.getGameTile( field.tile.x + dir.getX(), field.tile.y + dir.getY() );
				field.trySpawnInTile( newTile, field.stacks - 1 );
				field.tile.clearField( field.layer );

				Field newField = newTile.fields.get( field.layer );
				if ( newField != null && newField.fieldName.equals( field.fieldName ) )
				{
					newField.data.put( "LastMove", dir );

					if ( field.stacks - 1 == 0 )
					{
						field.onNaturalDeath();
					}
				}

				return;
			}
		}

		field.setData( "SpreadAccumulator", updateAccumulator );
	}

	@Override
	public void parse( Element xml )
	{
		updateRate = xml.getFloat( "Update", 0.5f );
		travelType = Passability.parseArray( xml.get( "TravelType", "Walk" ) );
	}
}
