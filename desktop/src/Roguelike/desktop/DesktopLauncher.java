package Roguelike.desktop;

import Roguelike.Global;
import Roguelike.RoguelikeGame;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;

import javax.swing.*;
import java.io.PrintWriter;
import java.io.StringWriter;

public class DesktopLauncher
{
	public static void main( String[] arg )
	{
		Global.Game = new RoguelikeGame();
		Global.ApplicationChanger = new LwjglApplicationChanger();
		Global.ApplicationChanger.createApplication();

		if (!Global.RELEASE)
		{
			new QuestProcessor();
			//new AtlasCreator();
		}
	}
}
