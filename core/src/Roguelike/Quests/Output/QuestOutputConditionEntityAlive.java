package Roguelike.Quests.Output;

import Roguelike.Entity.GameEntity;
import Roguelike.Global;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;

/**
 * Created by Philip on 23-Jan-16.
 */
public class QuestOutputConditionEntityAlive extends AbstractQuestOutputCondition
{
	public String entityName;
	public boolean not;

	@Override
	public boolean evaluate()
	{
		Array<GameEntity> entities = new Array<GameEntity>(  );
		Global.CurrentLevel.getAllEntities(entities);

		boolean found = false;
		for (GameEntity entity : entities)
		{
			if (entity.HP > 0 && entity.name.equalsIgnoreCase( entityName ))
			{
				found = true;
				break;
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
		entityName = xml.getText();
		not = xml.getBooleanAttribute( "Not", false );
	}
}
