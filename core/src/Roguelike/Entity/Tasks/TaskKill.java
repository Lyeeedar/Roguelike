package Roguelike.Entity.Tasks;

import Roguelike.Entity.Entity;

public class TaskKill extends AbstractTask
{

	@Override
	public float processTask(Entity obj)
	{
		obj.Channeling = null;
		
		obj.HP = 0;
		return 1;
	}

}
