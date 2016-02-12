package Roguelike.Entity.ActivationAction;

import Roguelike.Entity.Entity;
import Roguelike.Entity.EnvironmentEntity;
import Roguelike.StatusEffect.StatusEffect;
import com.badlogic.gdx.utils.XmlReader;
import exp4j.Helpers.EquationHelper;

/**
 * Created by Philip on 12-Feb-16.
 */
public class ActivationActionAddStatus extends AbstractActivationAction
{
	public XmlReader.Element xml;
	public String count;

	@Override
	public void evaluate( EnvironmentEntity owningEntity, Entity activatingEntity, float delta )
	{
		int stacks = EquationHelper.evaluate( count );
		for (int i = 0; i < stacks; i++)
		{
			StatusEffect effect = StatusEffect.load( xml, owningEntity );
			activatingEntity.addStatusEffect( effect );
		}
	}

	@Override
	public void parse( XmlReader.Element xml )
	{
		this.xml = xml;
		count = xml.getAttribute( "Stacks", "1" ).toLowerCase();
	}
}
