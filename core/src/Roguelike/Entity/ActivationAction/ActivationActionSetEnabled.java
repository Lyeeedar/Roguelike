package Roguelike.Entity.ActivationAction;

import Roguelike.Entity.EnvironmentEntity;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;

/**
 * Created by Philip on 25-Jan-16.
 */
public class ActivationActionSetEnabled extends AbstractActivationAction
{
	public String entityName;
	public String actionName;
	public boolean enabled;

	public ActivationActionSetEnabled()
	{

	}

	public ActivationActionSetEnabled( String entity, String action, boolean enabled )
	{
		this.entityName = entity;
		this.actionName = action;
		this.enabled = enabled;
	}

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
					apply( ee );
				}
			}
		}
		else
		{
			apply( entity );
		}
	}

	private void apply( EnvironmentEntity entity )
	{
		for (ActivationActionGroup group : entity.onActivateActions)
		{
			if (group.name.equals( actionName ))
			{
				group.enabled = enabled;
			}
		}

		for (ActivationActionGroup group : entity.onTurnActions)
		{
			if (group.name.equals( actionName ))
			{
				group.enabled = enabled;
			}
		}

		for (ActivationActionGroup group : entity.onHearActions)
		{
			if (group.name.equals( actionName ))
			{
				group.enabled = enabled;
			}
		}

		for (ActivationActionGroup group : entity.onDeathActions)
		{
			if (group.name.equals( actionName ))
			{
				group.enabled = enabled;
			}
		}
	}

	@Override
	public void parse( XmlReader.Element xml )
	{
		entityName = xml.getAttribute( "Entity", null );
		actionName = xml.getText();
		enabled = xml.getBooleanAttribute( "Enabled", true );

	}
}
