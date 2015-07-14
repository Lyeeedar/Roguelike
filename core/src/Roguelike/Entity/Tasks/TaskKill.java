package Roguelike.Entity.Tasks;

import Roguelike.Entity.GameEntity;

public class TaskKill extends AbstractTask
{

	@Override
	public float processTask(GameEntity obj)
	{
		obj.Channeling = null;
		
		obj.HP = 0;
		return 1;
	}

}
