package Roguelike.Save;

import java.util.HashMap;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Items.Inventory;
import Roguelike.Save.SaveGameEntity.CooldownWrapper;
import Roguelike.StatusEffect.StatusEffect;

public class SaveEnvironmentEntity extends SaveableObject<EnvironmentEntity>
{
	public int hp;
	public int[] pos = new int[2];
	public Array<StatusEffect> statuses = new Array<StatusEffect>();
	public Inventory inventory;
	public String UID;
	public Element creationData;
	public HashMap<String, Object> data;
	public String levelUID;

	@Override
	public void store(EnvironmentEntity obj)
	{
		hp = obj.HP;
		pos = new int[]{obj.tile.x, obj.tile.y};
		for (StatusEffect status : obj.statusEffects)
		{			
			statuses.add(status);
		}
		inventory = obj.inventory;
		
		UID = obj.UID;
		
		creationData = obj.creationData;
		data = (HashMap<String, Object>)obj.data.clone();
		
		levelUID = obj.tile.level.UID;
	}

	@Override
	public EnvironmentEntity create()
	{
		EnvironmentEntity entity = EnvironmentEntity.load(creationData, levelUID);
		entity.data = (HashMap<String, Object>)data.clone();
		
		entity.HP = hp;
		for (StatusEffect saveStatus : statuses)
		{
			entity.addStatusEffect(saveStatus);
		}
		entity.inventory = inventory;
		
		entity.UID = UID;
		
		return entity;
	}

}
