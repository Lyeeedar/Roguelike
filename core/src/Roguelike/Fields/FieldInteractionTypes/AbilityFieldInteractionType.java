package Roguelike.Fields.FieldInteractionTypes;

import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Entity.GameEntity;
import Roguelike.Fields.Field;
import com.badlogic.gdx.utils.XmlReader;

/**
 * Created by Philip on 16-Jan-16.
 */
public class AbilityFieldInteractionType extends AbstractFieldInteractionType
{
	private ActiveAbility ability;

	@Override
	public Field process( Field src, Field dst )
	{
		ActiveAbility aa = ability.copy();
		GameEntity temp = new GameEntity();
		temp.factions.add( src.fieldName );
		temp.tile[0][0] = src.tile;

		aa.setCaster( temp );
		aa.lockTarget( src.tile );
		aa.source = src.tile;

		boolean finished = aa.update();

		if ( !finished )
		{
			src.tile.level.addActiveAbility( aa );
		}

		return null;
	}

	@Override
	public void parse( XmlReader.Element xml )
	{
		ability = ActiveAbility.load( xml );
	}
}
