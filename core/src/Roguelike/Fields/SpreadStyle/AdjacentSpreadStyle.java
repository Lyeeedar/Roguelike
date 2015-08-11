package Roguelike.Fields.SpreadStyle;

import Roguelike.Fields.Field;
import Roguelike.Global.Direction;
import Roguelike.Global.Passability;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public class AdjacentSpreadStyle extends AbstractSpreadStyle
{
	private Array<Passability> travelType;
	private float updateRate;

	@Override
	public void update(float delta, Field field)
	{
		float updateAccumulator = (Float)field.getData("UpdateAccumulator", 0);
		
		updateAccumulator += delta;
		
		while (updateAccumulator >= updateRate && field.stacks > 1)
		{
			updateAccumulator -= updateRate;
			
			Array<GameTile> validTiles = new Array<GameTile>();
			for (Direction dir : Direction.values())
			{
				GameTile tile = field.tile.level.getGameTile(field.tile.x+dir.GetX(), field.tile.y+dir.GetY());
				
				if (tile.getPassable(travelType))
				{
					if (tile.entity != null)
					{
						validTiles.add(tile);
					}
					else if (tile.environmentEntity != null && tile.environmentEntity.canTakeDamage)
					{
						validTiles.add(tile);
					}
				}
			}
			
			if (validTiles.size > 0)
			{
				GameTile tile = validTiles.random();				
				field.trySpawnInTile(tile.x, tile.y);
			}
		}
		
		field.setData("UpdateAccumulator", updateAccumulator);
	}

	@Override
	public void parse(Element xml)
	{
		updateRate = xml.getFloat("Update", 2);
		travelType = Passability.parseArray(xml.get("TravelType", "Ground"));
	}
}
