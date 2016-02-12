package Roguelike.Entity.ActivationAction;

import Roguelike.Entity.Entity;
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
					apply( ee );
				}
			}
		}
		else
		{
			apply( owningEntity );
		}
	}

	private void apply( EnvironmentEntity entity )
	{
		Array<ActivationActionGroup> output = new Array<ActivationActionGroup>(  );
		entity.getAllActivationActions( output );

		for (ActivationActionGroup group : output)
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
