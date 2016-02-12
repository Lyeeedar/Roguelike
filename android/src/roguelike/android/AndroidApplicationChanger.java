package roguelike.android;

import Roguelike.AbstractApplicationChanger;
import Roguelike.Global;
import Roguelike.RoguelikeGame;
import android.content.SharedPreferences;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.android.AndroidPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Philip on 20-Jan-16.
 */
public class AndroidApplicationChanger extends AbstractApplicationChanger
{
	public AndroidApplicationChanger()
	{
		super( Gdx.app.getPreferences( "game-settings" ) );
	}

	@Override
	public Application createApplication( RoguelikeGame game, Preferences pref )
	{
		return null;
	}

	@Override
	public void processResources() {}

	@Override
	public void updateApplication( Preferences pref )
	{
		int width = pref.getInteger( "resolutionX" );
		int height = pref.getInteger( "resolutionY" );

		Global.TargetResolution[0] = width;
		Global.TargetResolution[1] = height;
		Global.FPS = pref.getInteger( "fps" );
		Global.AnimationSpeed = 1.0f / pref.getFloat( "animspeed" );

		Global.MovementTypePathfind = pref.getBoolean( "pathfindMovement" );
		Global.MusicVolume = pref.getFloat( "musicVolume" );
		Global.AmbientVolume = pref.getFloat( "ambientVolume" );
		Global.EffectVolume = pref.getFloat( "effectVolume" );
		Global.updateVolume();
	}

	@Override
	public String[] getSupportedDisplayModes()
	{
		Graphics.DisplayMode[] displayModes = Gdx.graphics.getDisplayModes();

		ArrayList<String> modes = new ArrayList<String>();

		for ( int i = 0; i < displayModes.length; i++ )
		{
			String mode = displayModes[i].width + "x" + displayModes[i].height;

			boolean contained = false;
			for ( String m : modes )
			{
				if ( m.equals( mode ) )
				{
					contained = true;
					break;
				}
			}
			if ( !contained )
			{
				modes.add( mode );
			}
		}

		if (displayModes.length == 1)
		{
			String mode = displayModes[0].width/2 + "x" + displayModes[0].height/2;
			modes.add(mode);
		}

		modes.add( "480x360" );
		modes.add( "800x600" );

		Collections.sort( modes, new Comparator<String>()
		{

			@Override
			public int compare( String s1, String s2 )
			{
				int split = s1.indexOf( "x" );
				int rX1 = Integer.parseInt( s1.substring( 0, split ) );

				split = s2.indexOf( "x" );
				int rX2 = Integer.parseInt( s2.substring( 0, split ) );

				if ( rX1 < rX2 ) return -1;
				else if ( rX1 > rX2 ) return 1;
				return 0;
			}

		} );

		String[] m = new String[modes.size()];

		return modes.toArray( m );
	}

	@Override
	public void setToNativeResolution( Preferences prefs )
	{

	}

	public void setDefaultPrefs( Preferences prefs )
	{
		prefs.putBoolean( "pathfindMovement", false );

		prefs.putFloat( "musicVolume", 1 );
		prefs.putFloat( "ambientVolume", 1 );
		prefs.putFloat( "effectVolume", 1 );

		prefs.putInteger( "resolutionX", 480 );
		prefs.putInteger( "resolutionY", 360 );
		prefs.putInteger( "fps", 30 );
		prefs.putFloat( "animspeed", 1 );
	}
}
