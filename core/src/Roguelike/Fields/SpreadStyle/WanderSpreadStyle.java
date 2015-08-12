package Roguelike.Fields.SpreadStyle;

import Roguelike.Fields.Field;
import Roguelike.Global.Direction;
import Roguelike.Global.Passability;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public class WanderSpreadStyle extends AbstractSpreadStyle
{
	private Array<Passability> travelType;
	private float updateRate;
	
	@Override
	public void update(float delta, Field field)
	{
		float updateAccumulator = (Float)field.getData("SpreadAccumulator", 0.0f);
		Direction lastMove = (Direction)field.getData("LastMove", Direction.CENTER);
		
		updateAccumulator += delta;
		
		while (updateAccumulator >= updateRate && field.stacks > 0)
		{
			updateAccumulator -= updateRate;
			
			Array<Direction> validDirs = new Array<Direction>();
			for (Direction dir : Direction.values())
			{
				if (dir == Direction.CENTER) { continue; }
				
				// prevent going backwards
				if (dir.GetX() != lastMove.GetX()*-1 || dir.GetY() != lastMove.GetY()*-1)
				{
					GameTile tile = field.tile.level.getGameTile(field.tile.x+dir.GetX(), field.tile.y+dir.GetY());
					
					if (Passability.isPassable(tile.tileData.passableBy, travelType))
					{
						if (tile.environmentEntity != null && !tile.environmentEntity.canTakeDamage && !Passability.isPassable(tile.environmentEntity.passableBy, travelType))
						{
							// treat as impassible tile
						}
						else
						{
							validDirs.add(dir);
						}
					}
				}
			}
			
			if (validDirs.size > 0)
			{
				Direction dir = validDirs.random();
				GameTile tile = field.tile.level.getGameTile(field.tile.x+dir.GetX(), field.tile.y+dir.GetY());	
				field.trySpawnInTile(tile.x, tile.y);				
				field.stacks--;
				
				lastMove = dir;
			}
		}
		
		field.setData("SpreadAccumulator", updateAccumulator);
		field.setData("LastMove", lastMove);
	}

	@Override
	public void parse(Element xml)
	{
		updateRate = xml.getFloat("Update", 0.5f);
		travelType = Passability.parseArray(xml.get("TravelType", "Walk"));
	}
}
