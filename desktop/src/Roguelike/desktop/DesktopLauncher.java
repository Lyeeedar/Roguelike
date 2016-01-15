package Roguelike.desktop;

import Roguelike.Global;
import Roguelike.RoguelikeGame;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;

public class DesktopLauncher
{
	public static void main( String[] arg )
	{
		TexturePacker.Settings settings = new TexturePacker.Settings();
		settings.combineSubdirectories = true;
		settings.duplicatePadding = true;
		settings.useIndexes = false;
		//TexturePacker.process( settings, "Sprites", "Atlases", "SpriteAtlas" );

		Global.Game = new RoguelikeGame();
		Global.ApplicationChanger = new LwjglApplicationChanger();
		Global.ApplicationChanger.createApplication();
	}
}
