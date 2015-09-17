package Roguelike.Save;

import java.util.HashMap;

import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Items.Inventory;
import Roguelike.StatusEffect.StatusEffect;
import Roguelike.Tiles.Point;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public final class SaveEnvironmentEntity extends SaveableObject<EnvironmentEntity>
{
	public int hp;
	public Point pos = new Point();
	public Array<StatusEffect> statuses = new Array<StatusEffect>();
	public Inventory inventory;
	public String UID;
	public Element creationData;
	public HashMap<String, Object> data;
	public String levelUID;
	public String dungeonUID;
	public int levelDepth;

	@Override
	public void store( EnvironmentEntity obj )
	{
		hp = obj.HP;
		pos.set( obj.tile[0][0].x, obj.tile[0][0].y );
		for ( StatusEffect status : obj.statusEffects )
		{
			statuses.add( status );
		}
		inventory = obj.inventory;

		UID = obj.UID;

		creationData = obj.creationData;
		data = (HashMap<String, Object>) obj.data.clone();

		levelUID = obj.tile[0][0].level.UID;
		dungeonUID = obj.tile[0][0].level.dungeon.UID;
		levelDepth = obj.tile[0][0].level.depth;
	}

	@Override
	public EnvironmentEntity create()
	{
		EnvironmentEntity entity = EnvironmentEntity.load( creationData, dungeonUID, levelUID, levelDepth );
		entity.data = (HashMap<String, Object>) data.clone();

		entity.HP = hp;
		for ( StatusEffect saveStatus : statuses )
		{
			entity.addStatusEffect( saveStatus );
		}
		entity.inventory = inventory;

		entity.UID = UID;

		return entity;
	}

}
