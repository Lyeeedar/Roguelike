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
			prefs.putBoolean( "created", true );
			prefs.putString( "window-name", "Roguelike3D" );
			prefs.putInteger( "resolutionX", 800 );
			prefs.putInteger( "resolutionY", 600 );
			prefs.putBoolean( "fullscreen", false );
			prefs.putBoolean( "borderless", false );
			prefs.putBoolean( "vSync", true );
			prefs.putInteger( "fps", 0 );
			prefs.putInteger( "msaa", 16 );
			prefs.putString( "Renderer", "Deferred" );
			prefs.flush();
		}
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
