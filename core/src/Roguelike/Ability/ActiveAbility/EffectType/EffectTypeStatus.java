package Roguelike.Ability.ActiveAbility.EffectType;

import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.GameEvent.Damage.DamageObject;
import Roguelike.GameEvent.Damage.StatusEvent;
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
			DamageObject ao = new DamageObject( aa.caster, tile.entity, aa.variableMap );
			statusEvent.handle( ao, aa );
		}

		if ( tile.environmentEntity != null )
		{
			DamageObject ao = new DamageObject( aa.caster, tile.environmentEntity, aa.variableMap );
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
		return String.join( "\n", statusEvent.toString( aa.variableMap, aa ) );
	}
}
