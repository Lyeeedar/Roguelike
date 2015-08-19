package Roguelike;

import java.util.HashMap;

import Roguelike.Screens.CharacterCreationScreen;
import Roguelike.Screens.GameOverScreen;
import Roguelike.Screens.GameScreen;
import Roguelike.Screens.LoadingScreen;
import Roguelike.Screens.MainMenuScreen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;

public class RoguelikeGame extends Game
{
	public static RoguelikeGame Instance;

	public RoguelikeGame()
	{
		Instance = this;
	}

	public enum ScreenEnum
	{
		MAINMENU, GAME, GAMEOVER, LOADING, CHARACTERCREATION
	}

	public final HashMap<ScreenEnum, Screen> screens = new HashMap<ScreenEnum, Screen>();

	@Override
	public void create()
	{
		screens.put( ScreenEnum.GAME, new GameScreen() );
		screens.put( ScreenEnum.MAINMENU, new MainMenuScreen() );
		screens.put( ScreenEnum.GAMEOVER, new GameOverScreen() );
		screens.put( ScreenEnum.LOADING, new LoadingScreen() );
		screens.put( ScreenEnum.CHARACTERCREATION, new CharacterCreationScreen() );

		switchScreen( ScreenEnum.MAINMENU );
	}

	public void switchScreen( ScreenEnum screen )
	{
		this.setScreen( screens.get( screen ) );
	}
}
