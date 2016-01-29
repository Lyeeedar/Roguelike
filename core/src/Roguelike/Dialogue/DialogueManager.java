package Roguelike.Dialogue;

import java.io.IOException;
import java.util.HashMap;

import Roguelike.Entity.GameEntity;
import Roguelike.Sound.SoundInstance;
import com.badlogic.gdx.audio.Sound;
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
	public DialogueActionInput currentInput;
	public int mouseOverInput = -1;
	public String popupText;
	public SoundInstance soundToBePlayed;

	// ----------------------------------------------------------------------
	public ExclamationManager exclamationManager;

	// ----------------------------------------------------------------------
	private boolean checkEntityAlive( String name )
	{
		Array<GameEntity> entities = new Array<GameEntity>(  );
		Global.CurrentLevel.getAllEntities( entities );

		for (GameEntity entity : entities)
		{
			if (entity.name != null && name.equalsIgnoreCase( entity.name ) && entity.HP > 0)
			{
				return true;
			}
		}

		return false;
	}

	// ----------------------------------------------------------------------
	public boolean processCondition( String condition, String[] reliesOn )
	{
		if (condition == null)
		{
			return true;
		}

		HashMap<String, Integer> variableMap = new HashMap<String, Integer>(  );
		variableMap.putAll( data );

		for ( String name : reliesOn )
		{
			if (variableMap.containsKey( name ))
			{
				continue;
			}

			String flag = "";
			if ( Global.WorldFlags.containsKey( name ) )
			{
				flag = Global.WorldFlags.get( name );
			}
			else if ( Global.RunFlags.containsKey( name ) )
			{
				flag = Global.RunFlags.get( name );
			}
			else if ( Global.CurrentLevel.player.inventory.getItemCount(name) > 0 )
			{
				flag = "" + Global.CurrentLevel.player.inventory.getItemCount( name );
			}
			else if ( name.endsWith( "alive" ) )
			{
				String entityName = name.replace( "alive", "" );
				if (checkEntityAlive( entityName ))
				{
					flag = "1";
				}
				else
				{
					flag = "0";
				}
			}
			else if ( name.endsWith( "dead" ) )
			{
				String entityName = name.replace( "dead", "" );
				if (checkEntityAlive( entityName ))
				{
					flag = "0";
				}
				else
				{
					flag = "1";
				}
			}
			else
			{
				flag = "0";
			}

			if (Global.isNumber( flag ))
			{
				variableMap.put( name, Integer.parseInt( flag ) );
			}
			else
			{
				variableMap.put( name, 1 );
			}
		}

		return EquationHelper.evaluate( condition, variableMap ) > 0;
	}

	// ----------------------------------------------------------------------
	public void initialiseDialogue( GameEntity entity )
	{
		if ( currentDialogue == -1 )
		{
			for ( int i = 0; i < dialogueChunks.size; i++ )
			{
				if ( processCondition( dialogueChunks.get( i ).condition, dialogueChunks.get( i ).reliesOn ) )
				{
					currentDialogue = i;
					Global.CurrentDialogue = entity;
					dialogueChunks.get( currentDialogue ).dialogue.index = 0;
					break;
				}
			}
		}
	}

	// ----------------------------------------------------------------------
	public void advance( GameEntity entity )
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
				Global.CurrentDialogue = entity;
			}
		}
	}

	// ----------------------------------------------------------------------
	public void parse( Element xml )
	{
		Element exclamations = xml.getChildByName( "Exclamations" );
		if ( exclamations != null )
		{
			exclamationManager = ExclamationManager.load( exclamations );
		}

		Element dialogue = xml.getChildByName( "Dialogue" );
		if ( dialogue != null )
		{
			for ( int i = 0; i < dialogue.getChildCount(); i++ )
			{
				Element child = dialogue.getChild( i );
				dialogueChunks.add( DialogueChunkWrapper.load( child, this ) );
			}
		}
	}

	// ----------------------------------------------------------------------
	public static DialogueManager load( String path )
	{
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

		return load( xml );
	}

	// ----------------------------------------------------------------------
	public static DialogueManager load( Element xml )
	{
		DialogueManager manager = new DialogueManager();
		manager.parse( xml );
		return manager;
	}

	// ----------------------------------------------------------------------
	public static class DialogueChunkWrapper
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
