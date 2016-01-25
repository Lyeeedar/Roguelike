package Roguelike.Entity.ActivationAction;

import Roguelike.Entity.EnvironmentEntity;
import com.badlogic.gdx.utils.XmlReader;

/**
 * Created by Philip on 25-Jan-16.
 */
public class ActivationActionKillThis extends AbstractActivationAction
{
	@Override
	public void evaluate( EnvironmentEntity entity, float delta )
	{
		entity.HP = 0;
	}

	@Override
	public void parse( XmlReader.Element xml )
	{

	}
}
