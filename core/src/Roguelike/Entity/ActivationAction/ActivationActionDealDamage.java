package Roguelike.Entity.ActivationAction;

import Roguelike.Entity.Entity;
import Roguelike.Entity.EnvironmentEntity;
import com.badlogic.gdx.utils.XmlReader;

/**
 * Created by Philip on 11-Feb-16.
 */
public class ActivationActionDealDamage extends AbstractActivationAction
{
	public int dam;

	@Override
	public void evaluate( EnvironmentEntity owningEntity, Entity activatingEntity, float delta )
	{
		activatingEntity.applyDamage( dam, owningEntity );
	}

	@Override
	public void parse( XmlReader.Element xml )
	{
		dam = Integer.parseInt( xml.getText() );
	}
}
