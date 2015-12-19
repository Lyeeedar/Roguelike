package Roguelike.Ability.ActiveAbility.EffectType;

import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.GameEvent.Damage.DamageObject;
import Roguelike.GameEvent.Damage.StatusEvent;
import Roguelike.Global;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.XmlReader.Element;

public class EffectTypeStatus extends AbstractEffectType
{
	private StatusEvent statusEvent;

	@Override
	public void parse( Element xml )
	{
		statusEvent = new StatusEvent();
		statusEvent.parse( xml );
	}

	@Override
	public void update( ActiveAbility aa, float time, GameTile tile )
	{
		if ( tile.entity != null )
		{
			DamageObject ao = new DamageObject( aa.getCaster(), tile.entity, aa.getVariableMap() );
			statusEvent.handle( ao, aa );
		}

		if ( tile.environmentEntity != null )
		{
			DamageObject ao = new DamageObject( aa.getCaster(), tile.environmentEntity, aa.getVariableMap() );
			statusEvent.handle( ao, aa );
		}
	}

	@Override
	public AbstractEffectType copy()
	{
		EffectTypeStatus e = new EffectTypeStatus();
		e.statusEvent = statusEvent;
		return e;
	}

	@Override
	public String toString( ActiveAbility aa )
	{
		return Global.join( "\n", statusEvent.toString( aa.getVariableMap(), aa ) );
	}
}
