package Roguelike.Ability.ActiveAbility.HitType;

import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Entity.Entity;
import Roguelike.Entity.GameEntity;
import com.badlogic.gdx.utils.XmlReader;

/**
 * Created by Philip on 02-Jan-16.
 */
public class HitTypeAlly extends AbstractHitType
{
	@Override
	public AbstractHitType copy()
	{
		return new HitTypeAlly();
	}

	@Override
	public void parse( XmlReader.Element xml )
	{

	}

	@Override
	public boolean isTargetValid( ActiveAbility aa, Entity entity )
	{
		if (entity instanceof GameEntity )
		{
			GameEntity ge = (GameEntity)entity;

			return ge.isAllies( aa.getCaster() );
		}

		return false;
	}
}
