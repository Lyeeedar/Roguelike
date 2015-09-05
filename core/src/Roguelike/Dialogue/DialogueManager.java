package Roguelike.Dialogue;

import java.io.IOException;
import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Global;
import Roguelike.Entity.Entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

public class DialogueManager
{
	// ----------------------------------------------------------------------
	public enum ReturnType
	{
		RUNNING, COMPLETED, ADVANCE
	}

	// ----------------------------------------------------------------------
	public HashMap<String, Integer> data = new HashMap<String, Integer>();
	public Array<DialogueChunkWrapper> dialogueChunks = new Array<DialogueChunkWrapper>();

	public int currentDialogue = -1;
	public Entity entity;
	public DialogueActionInput currentInput;
	public int mouseOverInput = -1;

	// ----------------------------------------------------------------------
	public boolean processCondition( String condition, String[] reliesOn )
	{
		for ( String name : reliesOn )
		{
			if ( !data.containsKey( name.toLowerCase() ) )
			{
				data.put( name.toLowerCase(), 0 );
			}
		}

		ExpressionBuilder expB = EquationHelper.createEquationBuilder( condition );
		EquationHelper.setVariableNames( expB, data, "" );

		Expression exp = EquationHelper.tryBuild( expB );
		if ( exp == null ) { return false; }

		EquationHelper.setVariableValues( exp, data, "" );

		int raw = (int) exp.evaluate();

		return raw > 0;
	}

	// ----------------------------------------------------------------------
	public void initialiseDialogue()
	{
		if ( currentDialogue == -1 )
		{
			for ( int i = 0; i < dialogueChunks.size; i++ )
			{
				if ( processCondition( dialogueChunks.get( i ).condition, dialogueChunks.get( i ).reliesOn ) )
				{
					currentDialogue = i;
					Global.CurrentDialogue = this;
					dialogueChunks.get( currentDialogue ).dialogue.index = 0;
					break;
				}
			}
		}
	}

	// ----------------------------------------------------------------------
	public void advance()
	{
		if ( currentDialogue >= 0 )
		{
			dialogueChunks.get( currentDialogue ).dialogue.advance();

			if ( dialogueChunks.get( currentDialogue ).dialogue.index == dialogueChunks.get( currentDialogue ).dialogue.actions.size )
			{
				Global.CurrentDialogue = null;
				currentDialogue = -1;
			}
			else
			{
				Global.CurrentDialogue = this;
			}
		}
	}

	// ----------------------------------------------------------------------
	public void parse( Element xml )
	{
		for ( int i = 0; i < xml.getChildCount(); i++ )
		{
			Element child = xml.getChild( i );
			dialogueChunks.add( DialogueChunkWrapper.load( child, this ) );
		}
	}

	// ----------------------------------------------------------------------
	public static DialogueManager load( String path, Entity entity )
	{
		DialogueManager manager = new DialogueManager();
		manager.entity = entity;

		XmlReader xmlReader = new XmlReader();
		Element xml = null;

		try
		{
			xml = xmlReader.parse( Gdx.files.internal( path + ".xml" ) );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}

		manager.parse( xml );

		return manager;
	}

	// ----------------------------------------------------------------------
	private static class DialogueChunkWrapper
	{
		public String condition;
		public String[] reliesOn;
		public Dialogue dialogue;

		public void parse( Element xml, DialogueManager manager )
		{
			condition = xml.getAttribute( "Condition", "1" ).toLowerCase();
			reliesOn = xml.getAttribute( "ReliesOn", "" ).toLowerCase().split( "," );
			dialogue = Dialogue.load( xml, manager );
		}

		public static DialogueChunkWrapper load( Element xml, DialogueManager manager )
		{
			DialogueChunkWrapper dcw = new DialogueChunkWrapper();
			dcw.parse( xml, manager );
			return dcw;
		}
	}
}
