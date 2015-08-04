package Roguelike.Entity.Tasks;

import Roguelike.Entity.GameEntity;

public class TaskWait extends AbstractTask
{
	private int healAmount = 0;
	
	public TaskWait()
	{
		
	}
	
	public TaskWait(int heal)
	{
		healAmount = heal;
	}
	
	@Override
	public void processTask(GameEntity obj)
	{
		obj.applyHealing(healAmount);
	}
}
