package Roguelike.Levels;

import Roguelike.DungeonGeneration.DungeonFileParser;
import Roguelike.Global;
import Roguelike.RoguelikeGame;
import Roguelike.Save.SaveLevel;
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
	public LevelData root;
	public LevelData current;

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

		root = new LevelData();
		root.parse( xml );

		current = root;
	}

	public void nextLevel( String name )
	{
		LevelData prev = current;
		current = getLevel( name );

		int depth = prev.levelName.equals( name ) ? prev.currentLevel.depth + 1 : 0;
		prev.currentLevel = null;

		SaveLevel level = new SaveLevel( current.levelName, depth, current.getExtraRooms( prev.levelName, depth, new Random() ), MathUtils.random( Long.MAX_VALUE - 1 ) );
		current.currentLevel = level;

		LoadingScreen.Instance.set( level, Global.CurrentLevel.player, "PlayerSpawn", null );
		RoguelikeGame.Instance.switchScreen( RoguelikeGame.ScreenEnum.LOADING );
	}

	public LevelData getLevel( String name )
	{
		if (name.equals( current.levelName ))
		{
			return current;
		}

		if (current.nextLevel.levelName.equals( name ))
		{
			return current.nextLevel;
		}

		for (BranchData branch : current.branches)
		{
			if (branch.level.levelName.equals( name ))
			{
				return branch.level;
			}
		}

		throw new RuntimeException( "Cant find level with name '" + name + "' connected to level '" + current.levelName + "'" );
	}

	public static class LevelData
	{
		public String levelName;
		public int maxDepth;

		public SaveLevel currentLevel;

		public LevelData nextLevel;
		public Array<BranchData> branches = new Array<BranchData>(  );

		public Array<DungeonFileParser.DFPRoom> getExtraRooms( String prevLevel, int depth, Random ran )
		{
			Array<DungeonFileParser.DFPRoom> rooms = new Array<DungeonFileParser.DFPRoom>(  );

			DungeonFileParser dfp = DungeonFileParser.load( levelName + "/" + levelName );
			if (dfp.entranceRooms.containsKey( prevLevel.toLowerCase() ))
			{
				rooms.add( dfp.entranceRooms.get( prevLevel.toLowerCase() )[1] );
			}
			else
			{
				rooms.add( dfp.entranceRooms.get( "all" )[1] );
			}

			// If depth == levelDepth, getEntranceRoom of nextLevel
			if (depth == maxDepth)
			{
				rooms.add(nextLevel.getEntranceRoom(levelName));
			}
			else
			{
				rooms.add(getEntranceRoom( levelName ));
			}

			// For each branch check if should spawn, if so get entrance room of those
			for (BranchData branch : branches)
			{
				if (depth == branch.depth)
				{
					if (ran.nextFloat() >= branch.chance)
					{
						rooms.add(branch.level.getEntranceRoom(levelName));
					}
				}
			}

			// For each quest get rooms
			return rooms;
		}

		public DungeonFileParser.DFPRoom getEntranceRoom(String prevLevel)
		{
			// Pull entrance room from level xml
			DungeonFileParser dfp =DungeonFileParser.load( levelName + "/" + levelName );

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
			maxDepth = xml.getIntAttribute( "MaxDepth", 3 );

			if (xml.getChildCount() > 0)
			{
				nextLevel = new LevelData();
				nextLevel.parse( xml.getChild( 0 ) );
			}

			XmlReader.Element branchesElement = xml.getChildByName( "Branches" );
			if (branchesElement != null)
			{
				for (int i = 0; i < branchesElement.getChildCount(); i++)
				{
					XmlReader.Element branchElement = branchesElement.getChild( i );

					LevelData branchLevel = new LevelData();
					branchLevel.parse( branchElement );

					BranchData branch = new BranchData();
					branch.level = branchLevel;
					branch.depth = branchElement.getIntAttribute( "SpawnDepth", maxDepth );
					branch.chance = branchElement.getFloatAttribute( "SpawnChance", 0.5f );

					branches.add(branch);
				}
			}
		}
	}

	public static class BranchData
	{
		public int depth;
		public float chance;

		public LevelData level;
	}
}
