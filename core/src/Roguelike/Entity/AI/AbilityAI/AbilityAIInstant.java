package Roguelike.Entity.AI.AbilityAI;

import Roguelike.Global.Direction;
import Roguelike.Entity.ActiveAbility;
import Roguelike.Entity.ActiveAbility.AbilityTile;

import com.badlogic.gdx.utils.XmlReader.Element;

public class AbilityAIInstant extends AbstractAbilityAI
{	
	int targetX;
	int targetY;
	
	@Override
	public void update(ActiveAbility ab)
	{
		AbilityTile tile = ab.AffectedTiles.get(0);
		
		ab.AffectedTiles.clear();
		ab.addAffectedTile(tile.Tile.Level.getGameTile(targetX, targetY), Direction.CENTER.GetAngle());
		
		ab.advanceAI();
	}

	@Override
	public void Init(ActiveAbility ab, int targetx, int targety)
	{
		this.targetX = targetx;
		this.targetY = targety;
	}

	@Override
	public void parse(Element xml)
	{
	}

	@Override
	public AbstractAbilityAI copy()
	{
		AbilityAIInstant ai = new AbilityAIInstant();
		
		return ai;
	}
}
