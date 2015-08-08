package Roguelike.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import Roguelike.Global;
import Roguelike.RoguelikeGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.foregroundFPS = 0;
		config.width = 800;
		config.height = 600;
		config.samples = 8;
		
		Global.TargetResolution[0] = 800;
		Global.TargetResolution[1] = 600;
				
		new LwjglApplication(new RoguelikeGame(), config);
	}
}
