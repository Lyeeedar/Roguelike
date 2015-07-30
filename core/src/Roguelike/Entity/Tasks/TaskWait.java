package Roguelike.Entity.Tasks;

import Roguelike.Entity.GameEntity;

public class TaskWait extends AbstractTask
{
	@Override
	public void processTask(GameEntity obj)
	{
		obj.applyHealing(2);
	}
}
