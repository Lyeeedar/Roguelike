package Roguelike.Save;

import java.util.EnumMap;
import java.util.HashMap;

import Roguelike.Entity.ActivationAction.ActivationActionGroup;
import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Global;
import Roguelike.Items.Inventory;
import Roguelike.Lights.Light;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.TilingSprite;
import Roguelike.StatusEffect.StatusEffect;
import Roguelike.Tiles.Point;

import Roguelike.Util.EnumBitflag;
import Roguelike.Util.FastEnumMap;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public final class SaveEnvironmentEntity extends SaveableObject<EnvironmentEntity>
{
	public int hp;
	public Point pos = new Point();
	public Array<StatusEffect> statuses = new Array<StatusEffect>();
	public Inventory inventory;
	public String UID;

	public Sprite sprite;
	public TilingSprite tilingSprite;
	public Light light;
	public EnumBitflag<Global.Passability> passableBy;
	public boolean canTakeDamage;

	public boolean attachToWall;
	public boolean overhead;
	public Global.Direction location;

	public Array<ActivationActionGroup> onActivateActions;
	public Array<ActivationActionGroup> onTurnActions;
	public Array<ActivationActionGroup> onHearActions;
	public Array<ActivationActionGroup> onDeathActions;
	public Array<ActivationActionGroup> noneActions;
	public Array<ActivationActionGroup> proximityActions;

	public FastEnumMap<Global.Statistic, Integer> stats;

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

		sprite = obj.sprite;
		tilingSprite = obj.tilingSprite;
		passableBy = obj.passableBy;
		onActivateActions = obj.onActivateActions;
		onTurnActions = obj.onTurnActions;
		onHearActions = obj.onHearActions;
		onDeathActions = obj.onDeathActions;
		noneActions = obj.noneActions;
		proximityActions = obj.proximityActions;

		canTakeDamage = obj.canTakeDamage;

		attachToWall = obj.attachToWall;
		overhead = obj.overHead;
		location = obj.location;

		light = obj.light;

		stats = obj.statistics;
	}

	@Override
	public EnvironmentEntity create()
	{
		EnvironmentEntity entity = new EnvironmentEntity();
		entity.sprite = sprite;
		entity.tilingSprite = tilingSprite;
		entity.passableBy = passableBy;
		entity.onActivateActions = onActivateActions;
		entity.onTurnActions = onTurnActions;
		entity.onHearActions = onHearActions;
		entity.onDeathActions = onDeathActions;
		entity.noneActions = noneActions;
		entity.proximityActions = proximityActions;

		entity.canTakeDamage = canTakeDamage;

		entity.HP = hp;
		for ( StatusEffect saveStatus : statuses )
		{
			entity.addStatusEffect( saveStatus );
		}
		entity.inventory = inventory;

		entity.UID = UID;

		entity.statistics = stats;

		entity.attachToWall = attachToWall;
		entity.overHead = overhead;
		entity.location = location;

		entity.light = light;

		entity.isVariableMapDirty = true;

		return entity;
	}

}
