package Roguelike;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public abstract class AbstractApplicationChanger
{
	public Preferences prefs;

	public AbstractApplicationChanger( Preferences prefs )
	{
		this.prefs = prefs;
		if ( !prefs.getBoolean( "created" ) )
		{
			setDefaultPrefs( prefs );
			prefs.putBoolean( "created", true );
			prefs.putString( "window-name", "Chronicles Of Aether" );

			prefs.flush();
		}
	}

	public void setDefaultPrefs( Preferences prefs )
	{
		prefs.putBoolean( "pathfindMovement", false );

		prefs.putFloat( "musicVolume", 1 );
		prefs.putFloat( "ambientVolume", 1 );
		prefs.putFloat( "effectVolume", 1 );

		prefs.putInteger( "resolutionX", 800 );
		prefs.putInteger( "resolutionY", 600 );
		prefs.putBoolean( "fullscreen", false );
		prefs.putBoolean( "borderless", false );
		prefs.putBoolean( "vSync", true );
		prefs.putInteger( "fps", 0 );
		prefs.putFloat( "animspeed", 1 );
		prefs.putInteger( "msaa", 16 );
	}

	public void createApplication()
	{

		if ( Gdx.app != null )
		{
			System.err.println( "Application already exists!" );
			return;
		}

		Gdx.app = createApplication( Global.Game, prefs );
	}

	public abstract Application createApplication( RoguelikeGame game, Preferences pref );

	public abstract void updateApplication( Preferences pref );

	public abstract String[] getSupportedDisplayModes();

	public abstract void setToNativeResolution( Preferences prefs );
}
