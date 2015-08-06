package roguelike.android;


import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import Roguelike.Global;
import Roguelike.RoguelikeGame;
import Roguelike.RoguelikeGame.ScreenEnum;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();	
		config.disableAudio = false;
		
		Global.TargetResolution[0] = 480;
		Global.TargetResolution[1] = 360;
		
		Global.ANDROID = true;
		
		initialize(new RoguelikeGame(), config);
	}
}
