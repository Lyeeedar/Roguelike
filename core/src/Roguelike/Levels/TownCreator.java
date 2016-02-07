package Roguelike.Levels;

import Roguelike.AssetManager;
import Roguelike.DungeonGeneration.DungeonFileParser;
import Roguelike.Entity.GameEntity;
import Roguelike.Global;
import Roguelike.Quests.QuestManager;
import Roguelike.RoguelikeGame;
import Roguelike.Save.SaveLevel;
import Roguelike.Screens.GameScreen;
import Roguelike.Screens.LoadingScreen;
import Roguelike.UI.ClassList;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;

import java.io.IOException;
import java.util.Random;

/**
 * Created by Philip on 02-Feb-16.
 */
public class TownCreator
{
	public Array<Building> buildings = new Array<Building>(  );
	public Building houses = new Building( "House", null );

	public TownCreator()
	{
		XmlReader reader = new XmlReader();
		XmlReader.Element xml = null;

		try
		{
			xml = reader.parse( Gdx.files.internal( "Levels/Town/BuildingList.xml" ) );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}

		for (int i = 0; i < xml.getChildCount(); i++)
		{
			XmlReader.Element el = xml.getChild( i );

			buildings.add( new Building( el.getName(), el.getText() ) );
		}
	}

	public void create()
	{
		Array<DungeonFileParser.DFPRoom> rooms = new Array<DungeonFileParser.DFPRoom>(  );
		for (Building building : buildings)
		{
			if ( Global.WorldFlags.containsKey( building.key ) )
			{
				int index = Integer.parseInt( Global.WorldFlags.get( building.key ) ) - 1;

				index = Math.min( index, building.rooms.size-1 );

				rooms.add( building.rooms.get( index ) );
			}
		}
		for (int i = 0; i < 3; i++)
		{
			rooms.add(houses.rooms.get( 0 ));
		}

		Global.CharGenMode = true;

		final Array<ClassList.ClassDesc> classes = ClassList.parse();

		Skin skin = Global.loadSkin();
		Table table = new Table();
		ButtonGroup<CheckBox> genderGroup = new ButtonGroup<CheckBox>(  );

		final CheckBox male = new CheckBox( "Male", skin );
		male.addListener( new ChangeListener() {
			@Override
			public void changed( ChangeEvent event, Actor actor )
			{
				//Global.CurrentLevel.player;
			}
		} );
		CheckBox female = new CheckBox( "Female", skin );

		genderGroup.add( male, female );

		table.add( male );
		table.add( female );
		table.row();

		ButtonGroup<TextButton> classGroup = new ButtonGroup<TextButton>(  );

		Table choiceTable = new Table(  );
		for ( final ClassList.ClassDesc desc : classes )
		{
			TextButton button = new TextButton( desc.name, skin );
			button.addListener( new ClickListener(  )
			{
				public void clicked( InputEvent event, float x, float y )
				{
					GameEntity newPlayer = male.isChecked() ? desc.male : desc.female;
					Global.CurrentLevel.player.tile[0][0].addGameEntity( newPlayer );
					Global.CurrentLevel.player = newPlayer;
				}
			} );
			choiceTable.add( button ).expandX().fillX();
			choiceTable.row();

			classGroup.add( button );
		}

		ScrollPane scrollPane = new ScrollPane( choiceTable, skin );

		table.add( scrollPane ).colspan( 2 ).expand().fill();
		table.row();

		TextButton startButton = new TextButton( "Begin", skin );
		startButton.addListener( new ClickListener(  )
		{
			public void clicked( InputEvent event, float x, float y )
			{
				Global.CharGenMode = false;
				GameScreen.Instance.lockContextMenu = false;
				GameScreen.Instance.clearContextMenu();
			}
		} );
		table.add( startButton ).colspan( 2 ).expandX().fillX();
		table.row();

		GameScreen.Instance.displayContextMenu( table, true );

		Global.LevelManager = new LevelManager();
		Global.QuestManager = new QuestManager();
		Global.	RunFlags.clear();

		SaveLevel level = new SaveLevel( "Town", 0, rooms, 0 );
		Global.LevelManager.current.currentLevel = level;
		LoadingScreen.Instance.set( level, classes.get( 0 ).male, "Tavern", null );
		RoguelikeGame.Instance.switchScreen( RoguelikeGame.ScreenEnum.LOADING );
	}

	public static class Building
	{
		public String name;
		public String key;
		public Array<DungeonFileParser.DFPRoom> rooms = new Array<DungeonFileParser.DFPRoom>(  );

		public Building(String name, String key)
		{
			this.name = name;
			this.key = key;

			XmlReader reader = new XmlReader();
			XmlReader.Element xml = null;

			try
			{
				xml = reader.parse( Gdx.files.internal( "Levels/Town/" + name + ".xml" ) );
			}
			catch ( IOException e )
			{
				e.printStackTrace();
			}

			XmlReader.Element roomsElement = xml.getChildByName( "Rooms" );
			for ( int i = 0; i < roomsElement.getChildCount(); i++ )
			{
				XmlReader.Element roomElement = roomsElement.getChild( i );
				DungeonFileParser.DFPRoom room = DungeonFileParser.DFPRoom.parse( roomElement );

				rooms.add( room );
			}
		}
	}
}
