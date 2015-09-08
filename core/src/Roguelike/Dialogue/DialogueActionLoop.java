package Roguelike.Dialogue;

import Roguelike.Dialogue.DialogueManager.ReturnType;

import com.badlogic.gdx.utils.XmlReader.Element;

public class DialogueActionLoop extends AbstractDialogueAction
{
	private Dialogue dialogue;
	private String condition;
	private String[] reliesOn;

	@Override
	public ReturnType process()
	{
		ReturnType returnType = dialogue.advance();

		if ( returnType != ReturnType.RUNNING && dialogue.index == dialogue.actions.size )
		{
			if ( processCondition( condition, reliesOn ) ) { return ReturnType.COMPLETED; }
			dialogue.index = 0;

			if ( returnType == ReturnType.ADVANCE )
			{
				dialogue.advance();
			}
		}

		return ReturnType.RUNNING;
	}

	@Override
	public void parse( Element xml )
	{
		dialogue = Dialogue.load( xml, manager );
		condition = xml.getAttribute( "Condition" ).toLowerCase();
		reliesOn = xml.getAttribute( "ReliesOn", "" ).toLowerCase().split( "," );
	}
}
