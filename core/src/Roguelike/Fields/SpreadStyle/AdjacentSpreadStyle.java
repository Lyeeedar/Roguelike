package Roguelike.Fields.SpreadStyle;

import Roguelike.Global.Direction;
import Roguelike.Global.Passability;
import Roguelike.Fields.Field;
import Roguelike.Fields.Field.FieldLayer;
import Roguelike.Tiles.GameTile;
import Roguelike.Util.EnumBitflag;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public class AdjacentSpreadStyle extends AbstractSpreadStyle
{
	private EnumBitflag<Passability> travelType;
	private String[] spreadTags;
	private float updateRate;

	@Override
	public void update( float delta, Field field )
	{
		float updateAccumulator = (Float) field.getData( "SpreadAccumulator", 0.0f );

		updateAccumulator += delta;

		while ( updateAccumulator >= updateRate && field.stacks > 0 )
		{
			updateAccumulator -= updateRate;

			Array<GameTile> validTiles = new Array<GameTile>();
			for ( Direction dir : Direction.values() )
			{
				if ( dir == Direction.CENTER )
				{
					continue;
				}

				GameTile tile = field.tile.level.getGameTile( field.tile.x + dir.getX(), field.tile.y + dir.getY() );

				boolean check = false;
				if ( tile == null )
				{

				}
				else if ( tile.entity != null )
				{
					check = true;
				}
				else if ( tile.environmentEntity != null && tile.environmentEntity.canTakeDamage )
				{
					check = true;
				}
				else if ( tile.fields.size > 0 && spreadTags.length > 0 )
				{
					outer:
						for ( FieldLayer layer : FieldLayer.values() )
						{
							Field otherfield = tile.fields.get( layer );

							if ( otherfield != null )
							{
								for ( String spreadTag : spreadTags )
								{
									for ( String tag : otherfield.tags )
									{
										if ( spreadTag.equals( tag ) )
										{
											check = true;
											break outer;
										}
									}
								}
							}
						}
				}

				if ( check && tile.tileData.passableBy.intersect( travelType ) )
				{
					validTiles.add( tile );
				}
			}

			if ( validTiles.size > 0 )
			{
				GameTile newTile = validTiles.random();
				field.trySpawnInTile( newTile, 1 );
			}
		}

		field.setData( "SpreadAccumulator", updateAccumulator );
	}

	@Override
	public void parse( Element xml )
	{
		updateRate = xml.getFloat( "Update", 2 );
		travelType = Passability.parseArray( xml.get( "TravelType", "Walk" ) );
		spreadTags = xml.get( "SpreadTags", "" ).toLowerCase().split( "," );
	}
}
