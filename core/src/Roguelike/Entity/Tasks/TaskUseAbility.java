package Roguelike.Entity.Tasks;

import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Entity.GameEntity;
import Roguelike.Tiles.Point;

public class TaskUseAbility extends AbstractTask
{
	public Point target;
	public ActiveAbility ability;

	public TaskUseAbility( Point target, ActiveAbility ability )
	{
		this.target = target;
		this.ability = ability;
	}

	@Override
	public void processTask( GameEntity obj )
	{
		ability.cooldownAccumulator = ability.cooldown;

		ActiveAbility aa = ability.copy();

		aa.caster = obj;
		aa.source = obj.tile[0][0];
		aa.variableMap = obj.getVariableMap();

		aa.lockTarget( obj.tile[0][0].level.getGameTile( target ) );

		boolean finished = aa.update();

		if ( !finished )
		{
			obj.tile[0][0].level.addActiveAbility( aa );
		}
	}

}
