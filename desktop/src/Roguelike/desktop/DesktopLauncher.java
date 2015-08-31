package Roguelike.desktop;

import Roguelike.Global;
import Roguelike.RoguelikeGame;

public class DesktopLauncher
{
	public static void main( String[] arg )
	{
		Global.Game = new RoguelikeGame();
		Global.ApplicationChanger = new LwjglApplicationChanger();
		Global.ApplicationChanger.createApplication();
	}
}
