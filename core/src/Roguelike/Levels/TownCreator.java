package Roguelike.Levels;

import Roguelike.Ability.AbilityTree;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.AssetManager;
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
import Roguelike.UI.ClassList;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
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

		Global.CharGenMode = true;

		Table table = new Table();
		ButtonGroup<CheckBox> genderGroup = new ButtonGroup<CheckBox>(  );
		final ButtonGroup<TextButton> classGroup = new ButtonGroup<TextButton>(  );

		final CheckBox male = new CheckBox( "Male", skin );
		male.addListener( new ChangeListener() {
			@Override
			public void changed( ChangeEvent event, Actor actor )
			{
				if (classGroup.getChecked() != null)
				{
					String selected = "" + classGroup.getChecked().getText();

					for ( ClassList.ClassDesc desc : classes )
					{
						if ( desc.name.equals( selected ) )
						{
							GameEntity newPlayer = male.isChecked() ? desc.male : desc.female;
							Global.CurrentLevel.player.tile[ 0 ][ 0 ].addGameEntity( newPlayer );
							Global.CurrentLevel.player = newPlayer;

							for (int i = 0; i < Global.CurrentLevel.player.slottedAbilities.size; i++)
							{
								AbilityTree ab = Global.CurrentLevel.player.slottedAbilities.get( i );
								if (ab != null)
								{
									((ActiveAbility)ab.current.current).hasValidTargets = true;
								}
							}
							break;
						}
					}
				}
			}
		} );
		CheckBox female = new CheckBox( "Female", skin );

		genderGroup.add( male, female );

		table.add( male );
		table.add( female );
		table.row();

		Table choiceTable = new Table(  );
		for ( final ClassList.ClassDesc desc : classes )
		{
			if (desc.unlockedBy != null && !Global.WorldFlags.containsKey( desc.unlockedBy ))
			{
				continue;
			}

			TextButton button = new TextButton( desc.name, skin );
			button.addListener( new ClickListener(  )
			{
				public void clicked( InputEvent event, float x, float y )
				{
					GameEntity newPlayer = male.isChecked() ? desc.male : desc.female;
					Global.CurrentLevel.player.tile[0][0].addGameEntity( newPlayer );
					Global.CurrentLevel.player = newPlayer;

					for (int i = 0; i < Global.CurrentLevel.player.slottedAbilities.size; i++)
					{
						AbilityTree ab = Global.CurrentLevel.player.slottedAbilities.get( i );
						if (ab != null)
						{
							((ActiveAbility)ab.current.current).hasValidTargets = true;
						}
					}
				}
			} );
			choiceTable.add( button ).expandX().fillX();
			choiceTable.row();

			classGroup.add( button );
		}

		ScrollPane scrollPane = new ScrollPane( choiceTable, skin );
		scrollPane.setScrollingDisabled( true, false );
		scrollPane.setForceScroll( false, true );

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

				Item money = Item.load( "Treasure/Money" );
				money.count = Integer.parseInt( Global.WorldFlags.get( "startingfunds" ) );

				Global.CurrentLevel.player.inventory.addItem( money );
			}
		} );
		table.add( startButton ).colspan( 2 ).expandX().fillX();
		table.row();

		GameScreen.Instance.queueContextMenu( table );
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
