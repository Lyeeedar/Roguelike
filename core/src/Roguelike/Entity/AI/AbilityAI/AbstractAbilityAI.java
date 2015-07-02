package Roguelike.Entity.AI.AbilityAI;

import Roguelike.Entity.ActiveAbility;
import Roguelike.Levels.Level;

import com.badlogic.gdx.utils.XmlReader.Element;

public abstract class AbstractAbilityAI
{	
	public abstract void Init(ActiveAbility ab, int targetx, int targety);
	
	public abstract void parse(Element xml);
	
	public abstract AbstractAbilityAI copy();
	
	public boolean canEnterTile(int x, int y, Level level)
	{
		if (
			x < 0 ||
			y < 0 ||
			x >= level.width-1 ||
			y >= level.height-1
			)
		{
			return false;
		}
		
		return level.getGameTile(x, y).TileData.Passable;
	}

	public abstract void update(ActiveAbility ab);
}
