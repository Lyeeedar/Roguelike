package Roguelike.Save;

import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Entity.GameEntity;
import Roguelike.Items.Inventory;
import Roguelike.StatusEffect.StatusEffect;

import com.badlogic.gdx.utils.Array;

public class SaveGameEntity extends SaveableObject<GameEntity>
{
	public String fileName;
	public int hp;
	public int essence;
	public int[] pos = new int[2];
	public boolean isPlayer = false;
	public Array<StatusEffect> statuses = new Array<StatusEffect>();
	public Array<CooldownWrapper> abilityCooldown = new Array<CooldownWrapper>();
	public Inventory inventory;
	public String UID;
	// need to save ai

	@Override
	public void store(GameEntity obj)
	{
		fileName = obj.fileName;
		hp = obj.HP;
		essence = obj.essence;
		pos = new int[]{obj.tile.x, obj.tile.y};
		for (StatusEffect status : obj.statusEffects)
		{			
			statuses.add(status);
		}
		inventory = obj.inventory;
		
		for (ActiveAbility aa : obj.slottedActiveAbilities)
		{
			abilityCooldown.add(new CooldownWrapper(aa.cooldownAccumulator));
		}
		
		UID = obj.UID;
	}

	@Override
	public GameEntity create()
	{
		GameEntity entity = GameEntity.load(fileName);
		
		entity.essence = essence;
		entity.HP = hp;
		for (StatusEffect saveStatus : statuses)
		{
			entity.addStatusEffect(saveStatus);
		}
		entity.inventory = inventory;
		
		for (int i = 0; i < abilityCooldown.size; i++)
		{
			entity.slottedActiveAbilities.get(i).cooldownAccumulator = abilityCooldown.get(i).val;
		}
		
		entity.UID = UID;
		
		return entity;
	}

	// Neccessary due to Kryo issue with boxed primitives.
	public static class CooldownWrapper
	{
		public float val;
		
		public CooldownWrapper()
		{
		
		}
		
		public CooldownWrapper(float val)
		{
			this.val = val;
		}
	}
}
