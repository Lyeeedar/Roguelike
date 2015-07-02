package Roguelike.Entity.Tasks;

import Roguelike.Global.Direction;
import Roguelike.Entity.ActiveAbility;
import Roguelike.Entity.Entity;
import Roguelike.Pathfinding.BresenhamLine;

public class TaskUseAbility extends AbstractTask
{
	public int[] target;
	public ActiveAbility ability;
	
	public TaskUseAbility(int[] target, ActiveAbility ability)
	{
		this.target = target;
		this.ability = ability;
	}
	
	@Override
	public float processTask(Entity obj)
	{	
		ability.cooldownAccumulator = ability.getCooldown() * obj.getActionDelay();
		ActiveAbility ab = ability.copy();
		
		int[] spos = BresenhamLine.line(obj.Tile.x, obj.Tile.y, target[0], target[1], obj.Tile.Level.getGrid(), false, false)[1];
				
		ab.Caster = obj;
		ab.addAffectedTile(obj.Tile.Level.getGameTile(spos), Direction.getDirection(spos[0]-obj.Tile.x, spos[1]-obj.Tile.y).GetAngle());
		ab.level = obj.Tile.Level;
		
		ab.Init(target[0], target[1]);
		
		obj.Tile.Level.ActiveAbilities.add(ab);
		
		obj.Channeling = ab;
		
		return 1.5f;
	}

}
