package Roguelike.desktop;

import Roguelike.Global;
import Roguelike.RoguelikeGame;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher
{
	public static void main( String[] arg )
	{
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.foregroundFPS = 0;
		config.width = 800;
		config.height = 600;
		config.samples = 16;

		Global.TargetResolution[0] = 800;
		Global.TargetResolution[1] = 600;

		new LwjglApplication( new RoguelikeGame(), config );
	}
}
