package Roguelike.Save;

import java.util.HashMap;

import Roguelike.Entity.ActivationAction.ActivationActionGroup;
import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Global;
import Roguelike.Items.Inventory;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.TilingSprite;
import Roguelike.StatusEffect.StatusEffect;
import Roguelike.Tiles.Point;

import Roguelike.Util.EnumBitflag;
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
	public EnumBitflag<Global.Passability> passableBy;

	public Array<ActivationActionGroup> onActivateActions;
	public Array<ActivationActionGroup> onTurnActions;
	public Array<ActivationActionGroup> onHearActions;
	public Array<ActivationActionGroup> onDeathActions;
	public Array<ActivationActionGroup> noneActions;

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

		entity.HP = hp;
		for ( StatusEffect saveStatus : statuses )
		{
			entity.addStatusEffect( saveStatus );
		}
		entity.inventory = inventory;

		entity.UID = UID;

		entity.isVariableMapDirty = true;

		return entity;
	}

}
