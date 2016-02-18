package Roguelike.Fields.OnDeathEffect;

import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Entity.GameEntity;
import Roguelike.Fields.Field;
import Roguelike.Tiles.GameTile;
import com.badlogic.gdx.utils.XmlReader;

/**
 * Created by Philip on 18-Feb-16.
 */
public class AbilityOnDeathEffect extends AbstractOnDeathEffect
{
	private ActiveAbility ability;

	@Override
	public void process( Field field, GameTile tile )
	{
		ActiveAbility aa = ability.copy();
		GameEntity temp = new GameEntity();
		temp.factions.add( field.fieldName );
		temp.tile[0][0] = field.tile;

		aa.setCaster( temp );
		aa.lockTarget( field.tile );
		aa.source = field.tile;

		boolean finished = aa.update();

		if ( !finished )
		{
			field.tile.level.addActiveAbility( aa );
		}
	}

	@Override
	public void parse( XmlReader.Element xml )
	{
		ability = ActiveAbility.load( xml );
	}
}
