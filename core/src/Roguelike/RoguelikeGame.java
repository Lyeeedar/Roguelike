package Roguelike;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;

import Roguelike.Screens.*;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;

import javax.swing.*;

public class RoguelikeGame extends Game
{
	public static RoguelikeGame Instance;

	public RoguelikeGame()
	{
		Instance = this;
	}

	public enum ScreenEnum
	{
		MAINMENU, GAME, LOADING, CHARACTERCREATION, OPTIONS, CREDITS
	}

	public final HashMap<ScreenEnum, Screen> screens = new HashMap<ScreenEnum, Screen>();

	@Override
	public void create()
	{
		Global.ApplicationChanger.processResources();

		if (!Global.ANDROID)
		{
			final StringWriter sw = new StringWriter();
			final PrintWriter pw = new PrintWriter( sw );

			final Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler()
			{
				public void uncaughtException(Thread myThread, Throwable e)
				{
					e.printStackTrace( pw );
					String exceptionAsString = sw.toString();

					FileHandle file = Gdx.files.local( "error.log" );
					file.writeString( exceptionAsString, false );

					JOptionPane.showMessageDialog( null, "An fatal error occured. Please send error.log to me so that I can fix it.", "An error occured", JOptionPane.ERROR_MESSAGE );

					e.printStackTrace();
				}
			};

			Thread.currentThread().setDefaultUncaughtExceptionHandler(handler);
		}

		screens.put( ScreenEnum.GAME, new GameScreen() );
		screens.put( ScreenEnum.MAINMENU, new MainMenuScreen() );
		screens.put( ScreenEnum.LOADING, new LoadingScreen() );
		screens.put( ScreenEnum.CHARACTERCREATION, new CharacterCreationScreen() );
		screens.put( ScreenEnum.OPTIONS, new OptionsScreen() );
		screens.put( ScreenEnum.CREDITS, new CreditsScreen() );

		switchScreen( ScreenEnum.MAINMENU );
	}

	public void switchScreen( ScreenEnum screen )
	{
		this.setScreen( screens.get( screen ) );
	}
}
