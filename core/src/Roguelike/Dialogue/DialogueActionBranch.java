package Roguelike.Dialogue;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Global;
import Roguelike.Dialogue.DialogueManager.ReturnType;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

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

			if ( branches.get( currentBranch ).dialogue.index == branches.get( currentBranch ).dialogue.actions.size )
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

	// ----------------------------------------------------------------------
	public boolean processCondition( String condition, String[] reliesOn )
	{
		for ( String name : reliesOn )
		{
			if ( !manager.data.containsKey( name ) && !Global.GlobalVariables.containsKey( name ) )
			{
				manager.data.put( name, 0 );
			}
		}

		ExpressionBuilder expB = EquationHelper.createEquationBuilder( condition );
		EquationHelper.setVariableNames( expB, manager.data, "" );
		EquationHelper.setVariableNames( expB, Global.GlobalVariables, "" );

		Expression exp = EquationHelper.tryBuild( expB );
		if ( exp == null ) { return false; }

		EquationHelper.setVariableValues( exp, manager.data, "" );
		EquationHelper.setVariableValues( exp, Global.GlobalVariables, "" );

		int raw = (int) exp.evaluate();

		return raw > 0;
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
