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
		float updateAccumulator = (Float)field.getData("SpreadAccumulator", 0.0f);
		
		updateAccumulator += delta;
		
		while (updateAccumulator >= updateRate && field.stacks > 0)
		{
			updateAccumulator -= updateRate;
			
			Array<GameTile> validTiles = new Array<GameTile>();
			for (Direction dir : Direction.values())
			{
				if (dir == Direction.CENTER) { continue; }
				
				GameTile tile = field.tile.level.getGameTile(field.tile.x+dir.GetX(), field.tile.y+dir.GetY());
				
				boolean check = false;
				if (tile.entity != null)
				{
					check = true;
				}
				else if (tile.environmentEntity != null && tile.environmentEntity.canTakeDamage)
				{
					check = true;
				}
				
				if (check && Passability.isPassable(tile.tileData.passableBy, travelType))
				{
					validTiles.add(tile);
				}
			}
			
			if (validTiles.size > 0)
			{
				GameTile newTile = validTiles.random();				
				field.trySpawnInTile(newTile, 1);
			}
		}
		
		field.setData("SpreadAccumulator", updateAccumulator);
	}

	@Override
	public void parse(Element xml)
	{
		updateRate = xml.getFloat("Update", 2);
		travelType = Passability.parseArray(xml.get("TravelType", "Walk"));
	}
}
