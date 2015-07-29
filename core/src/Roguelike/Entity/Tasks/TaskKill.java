package Roguelike.Entity.Tasks;

import Roguelike.Entity.GameEntity;

public class TaskKill extends AbstractTask
{

	@Override
	public void processTask(GameEntity obj)
	{		
		obj.HP = 0;
	}

}
