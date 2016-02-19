package Roguelike.Entity.ActivationAction;

import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Entity.Entity;
import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Entity.GameEntity;
import com.badlogic.gdx.utils.XmlReader;

/**
 * Created by Philip on 19-Feb-16.
 */
public class ActivationActionAbility extends AbstractActivationAction
{
	private ActiveAbility ability;
	private boolean self;

	@Override
	public void evaluate( EnvironmentEntity owningEntity, Entity activatingEntity, float delta )
	{
		ActiveAbility aa = ability.copy();
		GameEntity temp = new GameEntity();
		temp.tile[0][0] = owningEntity.tile[0][0];

		aa.setCaster( temp );

		if (self)
		{
			aa.lockTarget( owningEntity.tile[0][0] );
		}
		else
		{
			aa.lockTarget( activatingEntity.tile[0][0] );
		}


		aa.source = owningEntity.tile[0][0];

		boolean finished = aa.update();

		if ( !finished )
		{
			owningEntity.tile[0][0].level.addActiveAbility( aa );
		}
	}

	@Override
	public void parse( XmlReader.Element xml )
	{
		ability = ActiveAbility.load( xml );
		self = xml.getBooleanAttribute( "Self", false );
	}
}
