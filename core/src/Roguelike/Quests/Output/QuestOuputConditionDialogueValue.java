package Roguelike.Quests.Output;

import Roguelike.Entity.GameEntity;
import Roguelike.Global;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;

/**
 * Created by Philip on 24-Jan-16.
 */
public class QuestOuputConditionDialogueValue extends AbstractQuestOutputCondition
{
	public String entityName;
	public String dialogueKey;
	public int dialogueValue;

	public boolean not;

	@Override
	public boolean evaluate()
	{
		Array<GameEntity> entities = new Array<GameEntity>(  );
		Global.CurrentLevel.getAllEntities( entities );

		boolean found = false;
		for (GameEntity entity : entities)
		{
			if ( (entityName == null || entity.name.equals( entityName ) ) && entity.dialogue != null)
			{
				if (entity.dialogue.data.containsKey( dialogueKey ) && entity.dialogue.data.get( dialogueKey ) == dialogueValue )
				{
					found = true;
					break;
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
		dialogueKey = xml.get( "Key" ).toLowerCase();
		dialogueValue = xml.getInt( "Value" );

		not = xml.getBooleanAttribute( "Not", false );
	}
}
