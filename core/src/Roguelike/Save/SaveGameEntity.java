package Roguelike.Save;

import Roguelike.Global;
import Roguelike.Entity.GameEntity;
import Roguelike.StatusEffect.StatusEffect;

import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class SaveGameEntity extends SaveableObject<GameEntity>
{
	public String fileName;
	public int hp;
	public int[] pos = new int[2];
	public boolean isPlayer = false;
	//public Array<SaveStatusEffect> statuses = new Array<SaveStatusEffect>();
	// need to save ai

	@Override
	public void store(GameEntity obj)
	{
		fileName = obj.fileName;
		hp = obj.HP;
		pos = new int[]{obj.tile.x, obj.tile.y};
//		for (StatusEffect status : obj.statusEffects)
//		{
//			SaveStatusEffect saveStatus = new SaveStatusEffect();
//			saveStatus.store(status);
//			
//			statuses.add(saveStatus);
//		}
	}

	@Override
	public GameEntity create()
	{
		GameEntity entity = GameEntity.load(fileName);
		
		entity.HP = hp;
//		for (SaveStatusEffect saveStatus : statuses)
//		{
//			entity.addStatusEffect(saveStatus.create());
//		}
		
		return entity;
	}


}
