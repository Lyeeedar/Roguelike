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
		for ( String name : reliesOn )
		{
			if ( !manager.data.containsKey( name ) )
			{
				String flag = "";
				if ( Global.WorldFlags.containsKey( name ) )
				{
					flag = Global.WorldFlags.get( name );
				}
				else if ( Global.RunFlags.containsKey( name ) )
				{
					flag = Global.RunFlags.get( name );
				}
				else
				{
					flag = "0";
				}

				if (Global.isNumber( flag ))
				{
					manager.data.put( name, Integer.parseInt( flag ) );
				}
				else
				{
					manager.data.put( name, 1 );
				}
			}
		}

		return EquationHelper.evaluate( condition, manager.data ) > 0;
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
		ClassMap.put( "GAINABILITY", DialogueActionGainAbility.class );
	}
}
