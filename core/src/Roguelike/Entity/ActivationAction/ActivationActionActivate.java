package Roguelike.Entity.ActivationAction;

import Roguelike.Entity.Entity;
import Roguelike.Entity.EnvironmentEntity;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;

/**
 * Created by Philip on 25-Jan-16.
 */
public class ActivationActionActivate extends AbstractActivationAction
{
	public String entityName;
	public String actionName;

	@Override
	public void evaluate( EnvironmentEntity owningEntity, Entity activatingEntity, float delta )
	{
		if (entityName != null)
		{
			Array<EnvironmentEntity> all = new Array<EnvironmentEntity>(  );
			owningEntity.tile[0][0].level.getAllEnvironmentEntities( all );

			for (EnvironmentEntity ee : all)
			{
				if (ee.name.equals( entityName ))
				{
					apply( ee, owningEntity, delta );
				}
			}
		}
		else
		{
			apply( owningEntity, owningEntity, delta );
		}
	}

	private void apply( EnvironmentEntity owningEntity, Entity activatingEntity, float delta )
	{
		for (ActivationActionGroup group : owningEntity.onActivateActions)
		{
			if (group.name.equals( actionName ) && group.enabled && group.checkCondition( owningEntity, activatingEntity, delta ))
			{
				group.activate( owningEntity, activatingEntity, delta );
			}
		}

		for (ActivationActionGroup group : owningEntity.noneActions)
		{
			if (group.name.equals( actionName ) && group.enabled && group.checkCondition( owningEntity, activatingEntity, delta ))
			{
				group.activate( owningEntity, activatingEntity, delta );
			}
		}
	}

	@Override
	public void parse( XmlReader.Element xml )
	{
		entityName = xml.getAttribute( "Entity", null );
		actionName = xml.getText();
	}
}
