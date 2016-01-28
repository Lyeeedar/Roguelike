package Roguelike.Ability.ActiveAbility.HitType;

import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Entity.Entity;
import com.badlogic.gdx.utils.XmlReader;

/**
 * Created by Philip on 28-Jan-16.
 */
public class HitTypeEmpty extends AbstractHitType
{
	@Override
	public AbstractHitType copy()
	{
		return new HitTypeEmpty();
	}

	@Override
	public void parse( XmlReader.Element xml )
	{

	}

	@Override
	public boolean isTargetValid( ActiveAbility aa, Entity entity )
	{
		return entity == null;
	}
}
