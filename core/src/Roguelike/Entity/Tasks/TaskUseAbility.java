package Roguelike.Entity.Tasks;

import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Entity.GameEntity;
import Roguelike.Global.Direction;
import Roguelike.Lights.Light;
import Roguelike.Sound.SoundInstance;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.SpriteEffect;

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
	public void processTask(GameEntity obj)
	{	
		ability.cooldownAccumulator = ability.cooldown;
		
		ActiveAbility aa = ability.copy();
		
		aa.caster = obj;
		aa.source = obj.tile;
		aa.variableMap = obj.getVariableMap();
		
		aa.lockTarget(obj.tile.level.getGameTile(target));
		
		boolean finished = aa.update();
		
		if (!finished)
		{
			obj.tile.level.addActiveAbility(aa);
		}
		
		if (aa.getUseSprite() != null)
		{
			Light l = aa.light != null ? aa.light.copy() : null;
			Sprite s = aa.getUseSprite().copy();
			obj.tile.spriteEffects.add(new SpriteEffect(s, Direction.CENTER, l));
			
			SoundInstance sound = s.sound;
			if (sound != null) { sound.play(obj.tile); }
		}
	}

}
