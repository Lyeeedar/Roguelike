package Roguelike.Entity.ActivationAction;

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
	public void evaluate( EnvironmentEntity entity, float delta )
	{
		if (entityName != null)
		{
			Array<EnvironmentEntity> all = new Array<EnvironmentEntity>(  );
			entity.tile[0][0].level.getAllEnvironmentEntities( all );

			for (EnvironmentEntity ee : all)
			{
				if (ee.name.equals( entityName ))
				{
					apply( ee, delta );
				}
			}
		}
		else
		{
			apply( entity, delta );
		}
	}

	private void apply( EnvironmentEntity entity, float delta )
	{
		for (ActivationActionGroup group : entity.onActivateActions)
		{
			if (group.name.equals( actionName ) && group.enabled && group.checkCondition( entity, delta ))
			{
				group.activate( entity, delta );
			}
		}

		for (ActivationActionGroup group : entity.onTurnActions)
		{
			if (group.name.equals( actionName ) && group.enabled && group.checkCondition( entity, delta ))
			{
				group.activate( entity, delta );
			}
		}

		for (ActivationActionGroup group : entity.onHearActions)
		{
			if (group.name.equals( actionName ) && group.enabled && group.checkCondition( entity, delta ))
			{
				group.activate( entity, delta );
			}
		}

		for (ActivationActionGroup group : entity.onDeathActions)
		{
			if (group.name.equals( actionName ) && group.enabled && group.checkCondition( entity, delta ))
			{
				group.activate( entity, delta );
			}
		}

		for (ActivationActionGroup group : entity.noneActions)
		{
			if (group.name.equals( actionName ) && group.enabled && group.checkCondition( entity, delta ))
			{
				group.activate( entity, delta );
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
