package Roguelike.Quests.Output;

import Roguelike.Entity.ActivationAction.ActivationActionGroup;
import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Global;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;

/**
 * Created by Philip on 12-Feb-16.
 */
public class QuestOutputConditionActionEnabled extends AbstractQuestOutputCondition
{
	public String entityName;
	public String actionName;

	public boolean not;

	@Override
	public boolean evaluate()
	{
		Array<EnvironmentEntity> entities = new Array<EnvironmentEntity>(  );
		Global.CurrentLevel.getAllEnvironmentEntities( entities );

		boolean found = false;
		outer:
		for (EnvironmentEntity entity : entities)
		{
			if ( entityName == null || entity.name.equals( entityName ) )
			{
				Array<ActivationActionGroup> output = new Array<ActivationActionGroup>(  );
				entity.getAllActivationActions( output );

				for (ActivationActionGroup group : output)
				{
					if (group.name.equals( actionName ) && group.enabled == !not)
					{
						found = true;
						break outer;
					}
				}
			}
		}

		if (not)
		{
			found = !found;
		}

		return found;
	}

	@Override
	public void parse( XmlReader.Element xml )
	{
		entityName = xml.get( "Entity", null );
		actionName = xml.getText();

		not = xml.getBooleanAttribute( "Not", false );
	}
}
