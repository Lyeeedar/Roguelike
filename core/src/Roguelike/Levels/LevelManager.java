package Roguelike.Levels;

import Roguelike.DungeonGeneration.DungeonFileParser;
import Roguelike.Global;
import Roguelike.Quests.Quest;
import Roguelike.RoguelikeGame;
import Roguelike.Save.SaveLevel;
import Roguelike.Screens.GameScreen;
import Roguelike.Screens.LoadingScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;

import java.io.IOException;
import java.util.Random;

/**
 * Created by Philip on 18-Dec-15.
 */
public class LevelManager
{
	public int hpDropCounter = 0;

	public int totalDepth = 1;

	public LevelData root;
	public LevelData current;

	public Array<Quest> activeQuests = new Array<Quest>(  );

	public LevelManager()
	{
		XmlReader xmlReader = new XmlReader();
		XmlReader.Element xml = null;

		try
		{
			xml = xmlReader.parse( Gdx.files.internal( "Levels/LevelGraph.xml" ) );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}

		root = new LevelData( this );
		root.parse( xml );

		current = root;
	}

	public void evaluateQuestOutput()
	{
		for (Quest quest : activeQuests)
		{
			quest.evaluateOutputs();
		}
		activeQuests.clear();
	}

	public void nextLevel( String name )
	{
		if (name.equals( "Town" ))
		{
			TownCreator townCreator = new TownCreator();
			townCreator.create();
			return;
		}

		evaluateQuestOutput();

		LevelData prev = current;
		current = getLevel( current, name );

		int depth = prev.levelName.equals( name ) ? prev.currentLevel.depth + 1 : 1;
		prev.currentLevel = null;

		SaveLevel level = new SaveLevel( current.levelName, depth, current.getExtraRooms( prev.levelName, depth, new Random() ), MathUtils.random( Long.MAX_VALUE - 1 ) );
		current.currentLevel = level;

		if (depth == current.maxDepth)
		{
			// spawn boss
			current.currentLevel.isBossLevel = true;
		}
		else if (depth == 1)
		{
			GameScreen.Instance.displayLevelEntryMessage( current.levelTitle, current.levelDescription );
		}

		LoadingScreen.Instance.set( level, Global.CurrentLevel.player, "PlayerSpawn", null );
		RoguelikeGame.Instance.switchScreen( RoguelikeGame.ScreenEnum.LOADING );

		totalDepth++;
	}

	public LevelData getLevel( LevelData current, String name )
	{
		if (name.equals( current.levelName ))
		{
			return current;
		}

		LevelData nextLevel = null;
		if (current.nextLevel.levelName.equals( name ))
		{
			nextLevel = current.nextLevel;
		}

		for (BranchData branch : current.branches)
		{
			if (branch.level.levelName.equals( name ))
			{
				nextLevel = branch.level;
			}
		}

		if ( nextLevel != null )
		{
			if ( nextLevel.levelName.equals( "GoTo" ) )
			{
				nextLevel = root.getLabelledLevel( nextLevel.label );
			}

			return nextLevel;
		}

		throw new RuntimeException( "Cant find level with name '" + name + "' connected to level '" + current.levelName + "'" );
	}

	public static class LevelData
	{
		public String levelName;
		public int maxDepth;
		public String label;

		public SaveLevel currentLevel;

		public LevelData nextLevel;
		public Array<BranchData> branches = new Array<BranchData>(  );

		public LevelManager root;

		public String levelTitle;
		public String levelDescription;

		public LevelData() {}

		public LevelData( LevelManager root )
		{
			this.root = root;
		}

		public Array<DungeonFileParser.DFPRoom> getExtraRooms( String prevLevel, int depth, Random ran )
		{
			Array<DungeonFileParser.DFPRoom> rooms = new Array<DungeonFileParser.DFPRoom>(  );

			DungeonFileParser dfp = DungeonFileParser.load( levelName + "/" + levelName );
			if (dfp.entranceRooms.containsKey( prevLevel.toLowerCase() ))
			{
				rooms.add( dfp.entranceRooms.get( prevLevel.toLowerCase() )[1] );
			}
			else if (dfp.entranceRooms.get( "all" ) != null)
			{
				rooms.add( dfp.entranceRooms.get( "all" )[1] );
			}
			else
			{
				rooms.add( dfp.entranceRooms.values().iterator().next()[1] );
			}

			// If depth == levelDepth, getEntranceRoom of nextLevel
			if (depth == maxDepth)
			{
				if (nextLevel != null)
				{
					rooms.add( nextLevel.getEntranceRoom( levelName ) );
				}
			}
			else
			{
				rooms.add( getEntranceRoom( levelName ) );
			}

			// For each branch check if should spawn, if so get entrance room of those
			for (BranchData branch : branches)
			{
				if (depth == branch.depth)
				{
					if (ran.nextFloat() <= branch.chance)
					{
						rooms.add( branch.level.getEntranceRoom( levelName ) );
					}
				}
			}

			// For each quest get rooms
			int numQuests = (int)(ran.nextFloat() * ran.nextFloat() * 2.0f) + 1;
			for (int i = 0; i < numQuests; i++)
			{
				Quest quest = Global.QuestManager.getQuest( levelName, ran );
				if (quest != null)
				{
					root.activeQuests.add( quest );
					rooms.addAll( quest.rooms );
				}
			}

			return rooms;
		}

		public DungeonFileParser.DFPRoom getEntranceRoom(String prevLevel)
		{
			String name = levelName;
			if ( name.equals( "GoTo" ) )
			{
				name = root.root.getLabelledLevel( label ).levelName;
			}

			// Pull entrance room from level xml
			DungeonFileParser dfp = DungeonFileParser.load( name + "/" + name );

			if (dfp.entranceRooms.containsKey( prevLevel.toLowerCase() ))
			{
				return dfp.entranceRooms.get( prevLevel.toLowerCase() )[0];
			}
			else
			{
				return dfp.entranceRooms.get( "all" )[0];
			}
		}

		public void parse( XmlReader.Element xml )
		{
			levelName = xml.getName();
			maxDepth = xml.getIntAttribute( "MaxDepth", 1 );
			label = xml.getAttribute( "Label", levelName );

			levelTitle = xml.getAttribute( "Title", levelName );
			levelDescription = xml.getAttribute( "Description", "PHILIP YOU SHOULD FILL THIS IN" );

			if (xml.getChildCount() > 0)
			{
				nextLevel = new LevelData( root );
				nextLevel.parse( xml.getChild( 0 ) );
			}

			XmlReader.Element branchesElement = xml.getChildByName( "Branches" );
			if (branchesElement != null)
			{
				for (int i = 0; i < branchesElement.getChildCount(); i++)
				{
					XmlReader.Element branchElement = branchesElement.getChild( i );

					LevelData branchLevel = new LevelData( root );
					branchLevel.parse( branchElement );

					BranchData branch = new BranchData();
					branch.level = branchLevel;
					branch.depth = branchElement.getIntAttribute( "SpawnDepth", maxDepth );
					branch.chance = branchElement.getFloatAttribute( "SpawnChance", 0.5f );

					branches.add(branch);
				}
			}
		}

		public LevelData getLabelledLevel( String labelString )
		{
			if ( !levelName.equals( "GoTo" ) && label != null && label.equals( labelString ) )
			{
				return this;
			}

			if ( nextLevel != null )
			{
				LevelData found = nextLevel.getLabelledLevel( labelString );
				if ( found != null )
				{
					return found;
				}
			}

			for ( BranchData branch : branches )
			{
				LevelData found = branch.level.getLabelledLevel( labelString );
				if ( found != null )
				{
					return found;
				}
			}

			return null;
		}
	}

	public static class BranchData
	{
		public int depth;
		public float chance;

		public LevelData level;
	}
}
