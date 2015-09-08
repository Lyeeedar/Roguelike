package Roguelike.Dialogue;

import Roguelike.Dialogue.DialogueManager.ReturnType;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public class DialogueActionBranch extends AbstractDialogueAction
{
	public Array<BranchWrapper> branches = new Array<BranchWrapper>();
	public int currentBranch = -1;

	@Override
	public ReturnType process()
	{
		if ( currentBranch == -1 )
		{
			for ( int i = 0; i < branches.size; i++ )
			{
				if ( processCondition( branches.get( i ).condition, branches.get( i ).reliesOn ) )
				{
					currentBranch = i;
					branches.get( currentBranch ).dialogue.index = 0;
					break;
				}
			}
		}

		if ( currentBranch != -1 )
		{
			ReturnType returnType = branches.get( currentBranch ).dialogue.advance();

			if ( returnType != ReturnType.RUNNING && branches.get( currentBranch ).dialogue.index == branches.get( currentBranch ).dialogue.actions.size )
			{
				currentBranch = -1;
			}

			return returnType;
		}
		else
		{
			return ReturnType.ADVANCE;
		}
	}

	@Override
	public void parse( Element xml )
	{
		for ( int i = 0; i < xml.getChildCount(); i++ )
		{
			branches.add( BranchWrapper.load( xml.getChild( i ), manager ) );
		}
	}

	private static class BranchWrapper
	{
		public Dialogue dialogue;
		public String condition;
		public String[] reliesOn;

		public void parse( Element xml, DialogueManager manager )
		{
			condition = xml.get( "Condition" ).toLowerCase();
			reliesOn = xml.getAttribute( "ReliesOn", "" ).toLowerCase().split( "," );
			dialogue = Dialogue.load( xml, manager );
		}

		public static BranchWrapper load( Element xml, DialogueManager manager )
		{
			BranchWrapper bw = new BranchWrapper();
			bw.parse( xml, manager );
			return bw;
		}
	}
}
