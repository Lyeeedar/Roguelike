package Roguelike.Levels;

import Roguelike.DungeonGeneration.DungeonFileParser;
import Roguelike.Entity.GameEntity;
import Roguelike.Global;
import Roguelike.Items.Item;
import Roguelike.Levels.TownEvents.EventList;
import Roguelike.Quests.QuestManager;
import Roguelike.RoguelikeGame;
import Roguelike.Save.SaveLevel;
import Roguelike.Screens.GameScreen;
import Roguelike.Screens.LoadingScreen;
import Roguelike.UI.ButtonKeyboardHelper;
import Roguelike.UI.ClassList;
import Roguelike.UI.Seperator;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;

import java.io.IOException;

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
		// Get extra rooms
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

		// Build changed flag list and evaluate events
		ObjectMap<String, String> validFlags = new ObjectMap<String, String>(  );
		validFlags.putAll( Global.RunFlags );
		for (String key : Global.WorldFlags.keys())
		{
			String value = Global.WorldFlags.get( key );

			if (!value.equals( Global.WorldFlagsCopy.get( key ) ) )
			{
				validFlags.put( key, value );
			}
		}

		EventList eventList = new EventList();
		eventList.evaluate(validFlags);

		// Update the flag copy
		Global.WorldFlagsCopy.clear();
		Global.WorldFlagsCopy.putAll( Global.WorldFlags );

		//Begin the new game
		Array<ClassList.ClassDesc> classes = ClassList.parse();
		createCharGenTable(classes);

		Global.LevelManager = new LevelManager();
		Global.QuestManager = new QuestManager();
		Global.RunFlags.clear();

		SaveLevel level = new SaveLevel( "Town", 0, rooms, 0 );
		Global.LevelManager.current.currentLevel = level;
		LoadingScreen.Instance.set( level, classes.get( 0 ).male, "Tavern", null );
		RoguelikeGame.Instance.switchScreen( RoguelikeGame.ScreenEnum.LOADING );
	}

	private void createCharGenTable( final Array<ClassList.ClassDesc> classes )
	{
		Skin skin = Global.loadSkin();

		ButtonKeyboardHelper keyboardHelper = new ButtonKeyboardHelper(  );

		Global.CharGenMode = true;

		Table table = new Table();
		table.defaults().pad( 5 );

		Label title = new Label( "Character Creation", skin, "title" );
		table.add( title ).expandX().left();
		table.row();

		table.add( new Seperator( skin ) ).expandX().fillX();
		table.row();

		ButtonGroup<CheckBox> genderGroup = new ButtonGroup<CheckBox>(  );
		final ButtonGroup<TextButton> classGroup = new ButtonGroup<TextButton>(  );

		Table genderTable = new Table(  );
		genderTable.defaults().padLeft( 20 );

		Label genderLabel = new Label( "Gender: ", skin );
		genderTable.add( genderLabel ).width( 100 );

		final CheckBox male = new CheckBox( " Male", skin );
		male.addListener( new ChangeListener() {
			@Override
			public void changed( ChangeEvent event, Actor actor )
			{
				if (classGroup.getChecked() != null)
				{
					String selected = "" + classGroup.getChecked().getText();
					selected = selected.substring( 1 );

					for ( ClassList.ClassDesc desc : classes )
					{
						if ( desc.name.equals( selected ) )
						{
							GameEntity newPlayer = male.isChecked() ? desc.male : desc.female;
							Global.CurrentLevel.player.tile[ 0 ][ 0 ].addGameEntity( newPlayer );
							Global.CurrentLevel.player = newPlayer;

							break;
						}
					}
				}
			}
		} );
		CheckBox female = new CheckBox( " Female", skin );

		genderGroup.add( male, female );

		genderTable.add( male );
		genderTable.add( female );

		keyboardHelper.add( male, 0 );
		keyboardHelper.add( female, 1 );

		table.add( genderTable ).expandX().left();
		table.row();

		table.add( new Seperator( skin ) ).expandX().fillX();
		table.row();

		Table classSection = new Table();

		Label classTitle = new Label("Class:", skin);
		classSection.add( classTitle ).padLeft( 20 ).width( 100 ).expandY().top();

		Table choiceTable = new Table(  );
		choiceTable.defaults().padBottom( 20 );

		final Label classDescription = new Label( classes.get( 0 ).description, skin );
		classDescription.setWrap( true );

		for ( final ClassList.ClassDesc desc : classes )
		{
			if (desc.unlockedBy != null && !Global.WorldFlags.containsKey( desc.unlockedBy ))
			{
				continue;
			}

			final CheckBox checkBox = new CheckBox( " " + desc.name, skin );
			checkBox.addListener( new ChangeListener()
			{
				@Override
				public void changed( ChangeEvent event, Actor actor )
				{
					if (checkBox.isChecked() && Global.CurrentLevel != null && Global.CurrentLevel.player != null)
					{
						GameEntity newPlayer = male.isChecked() ? desc.male : desc.female;
						Global.CurrentLevel.player.tile[0][0].addGameEntity( newPlayer );
						Global.CurrentLevel.player = newPlayer;

						classDescription.setText( desc.description );
					}
				}
			});

			choiceTable.add( checkBox );
			choiceTable.row();

			classGroup.add( checkBox );

			keyboardHelper.add( checkBox, 0 );
			keyboardHelper.add( checkBox, 1 );
		}

		ScrollPane scrollPane = new ScrollPane( choiceTable, skin );
		scrollPane.setScrollingDisabled( true, false );
		scrollPane.setVariableSizeKnobs( true );
		scrollPane.setFadeScrollBars( false );
		scrollPane.setScrollbarsOnTop( false );
		//scrollPane.setForceScroll( false, true );
		scrollPane.setFlickScroll( false );

		classSection.add( scrollPane ).padLeft( 20 ).expandY().top();
		classSection.add( new Seperator( skin, true ) ).expandY().fillY().pad( 15, 25, 15, 25 );
		classSection.add( classDescription ).expand().fillX().top();

		table.add( classSection ).expand().fill();
		table.row();

		table.add( new Seperator( skin ) ).expandX().fillX();
		table.row();

		TextButton startButton = new TextButton( "Begin", skin );
		startButton.addListener( new ClickListener(  )
		{
			public void clicked( InputEvent event, float x, float y )
			{
				Global.CharGenMode = false;
				GameScreen.Instance.clearContextMenu( true );

				Item money = Item.load( "Treasure/Money" );
				money.count = Integer.parseInt( Global.WorldFlags.get( "startingfunds" ) );

				Global.CurrentLevel.player.inventory.addItem( money );
			}
		} );
		table.add( startButton ).expandX().fillX();
		table.row();

		keyboardHelper.add( startButton, 0 );
		keyboardHelper.add( startButton, 1 );

		GameScreen.Instance.queueContextMenu( table, keyboardHelper );
	}

	public static class Building
	{
		public String name;
		public String key;
		public Array<DungeonFileParser.DFPRoom> rooms = new Array<DungeonFileParser.DFPRoom>(  );

		public Building(String name, String key)
		{
			this.name = name.toLowerCase();
			this.key = key != null ? key.toLowerCase() : null;

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
