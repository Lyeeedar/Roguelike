package Roguelike.desktop;

import Roguelike.Global;
import Roguelike.RoguelikeGame;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;

public class DesktopLauncher
{
	public static void main( String[] arg )
	{
		Global.Game = new RoguelikeGame();
		Global.ApplicationChanger = new LwjglApplicationChanger();
		Global.ApplicationChanger.createApplication();

		new QuestProcessor();
		//new AtlasCreator();
	}
}
