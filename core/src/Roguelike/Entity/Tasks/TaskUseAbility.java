package Roguelike.Entity.Tasks;

import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Entity.Entity;

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
		ability.cooldownAccumulator = ability.cooldown;
		
		ActiveAbility aa = ability.copy();		
		aa.lockTarget(obj.Tile.Level.getGameTile(target));
		
		boolean finished = aa.update();
		
		if (!finished)
		{
			obj.Tile.Level.addActiveAbility(aa);
		}
		
		return 1.5f;
	}

}
