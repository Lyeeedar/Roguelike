package Roguelike.Dialogue;

import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Global;
import Roguelike.Dialogue.DialogueManager.ReturnType;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import exp4j.Helpers.EquationHelper;

public abstract class AbstractDialogueAction
{
	// ----------------------------------------------------------------------
	public DialogueManager manager;

	// ----------------------------------------------------------------------
	public boolean processCondition( String condition, String[] reliesOn )
	{
		ExpressionBuilder expB = EquationHelper.createEquationBuilder( condition );
		EquationHelper.setVariableNames( expB, manager.data, "" );
		EquationHelper.setVariableNames( expB, Global.GlobalVariables, "" );

		for ( String name : reliesOn )
		{
			if ( !manager.data.containsKey( name ) && !Global.GlobalVariables.containsKey( name ) )
			{
				expB.variable( name );
			}
		}

		Expression exp = EquationHelper.tryBuild( expB );
		if ( exp == null ) { return false; }

		EquationHelper.setVariableValues( exp, manager.data, "" );
		EquationHelper.setVariableValues( exp, Global.GlobalVariables, "" );

		for ( String name : reliesOn )
		{
			if ( !manager.data.containsKey( name ) && !Global.GlobalVariables.containsKey( name ) )
			{
				exp.setVariable( name, 0 );
			}
		}

		int raw = (int) exp.evaluate();

		return raw > 0;
	}

	// ----------------------------------------------------------------------
	public abstract ReturnType process();

	// ----------------------------------------------------------------------
	public abstract void parse( Element xml );

	// ----------------------------------------------------------------------
	public static AbstractDialogueAction load( Element xml, DialogueManager manager )
	{
		Class<AbstractDialogueAction> c = ClassMap.get( xml.getName().toUpperCase() );
		AbstractDialogueAction type = null;

		try
		{
			type = ClassReflection.newInstance( c );
		}
		catch ( ReflectionException e )
		{
			e.printStackTrace();
		}

		type.manager = manager;
		type.parse( xml );

		return type;
	}

	// ----------------------------------------------------------------------
	protected static HashMap<String, Class> ClassMap = new HashMap<String, Class>();

	// ----------------------------------------------------------------------
	static
	{
		ClassMap.put( "TEXT", DialogueActionText.class );
		ClassMap.put( "INPUT", DialogueActionInput.class );
		ClassMap.put( "BRANCH", DialogueActionBranch.class );
		ClassMap.put( "LOOP", DialogueActionLoop.class );
		ClassMap.put( "SETVARIABLE", DialogueActionSetVariable.class );
	}
}
