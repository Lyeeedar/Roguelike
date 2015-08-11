package Roguelike.Fields.SpreadStyle;

import Roguelike.Global.Direction;
import Roguelike.Global.Passability;
import Roguelike.Fields.Field;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public class FlowSpreadStyle extends AbstractSpreadStyle
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
				
				if (tile.field != null && tile.field.fieldName.equals(field.fieldName))
				{
					// same field, only flow down the gradient
					if (tile.field.stacks < field.stacks)
					{
						validTiles.add(tile);
					}
				}
				else if (tile.getPassable(travelType))
				{
					validTiles.add(tile);
				}
			}
			
			if (validTiles.size > 0)
			{
				GameTile tile = validTiles.random();				
				field.trySpawnInTile(tile.x, tile.y);
				field.stacks--;
			}
		}
		
		field.setData("UpdateAccumulator", updateAccumulator);
	}

	@Override
	public void parse(Element xml)
	{
		updateRate = xml.getFloat("Update", 0.5f);
		travelType = Passability.parseArray(xml.get("TravelType", "Ground"));
	}
}
