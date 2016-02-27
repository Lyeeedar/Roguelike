package Roguelike.Util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import java.util.HashMap;

/**
 * Created by Philip on 27-Feb-16.
 */
public class Controls
{
	public enum Keys
	{
		LEFT,
		RIGHT,
		UP,
		DOWN,
		CANCEL,
		ACCEPT,
		WAIT
	}

	private FastEnumMap<Keys, Integer> keyMap = new FastEnumMap<Keys, Integer>( Keys.class );

	public Controls()
	{
		defaultArrow();
	}

	public void defaultArrow()
	{
		keyMap.put( Keys.LEFT, Input.Keys.LEFT );
		keyMap.put( Keys.RIGHT, Input.Keys.RIGHT );
		keyMap.put( Keys.UP, Input.Keys.UP );
		keyMap.put( Keys.DOWN, Input.Keys.DOWN );
		keyMap.put( Keys.CANCEL, Input.Keys.ESCAPE );
		keyMap.put( Keys.ACCEPT, Input.Keys.ENTER );
		keyMap.put( Keys.WAIT, Input.Keys.SPACE );
	}

	public void defaultWASD()
	{
		keyMap.put( Keys.LEFT, Input.Keys.A );
		keyMap.put( Keys.RIGHT, Input.Keys.D );
		keyMap.put( Keys.UP, Input.Keys.W );
		keyMap.put( Keys.DOWN, Input.Keys.S );
		keyMap.put( Keys.CANCEL, Input.Keys.ESCAPE );
		keyMap.put( Keys.ACCEPT, Input.Keys.ENTER );
		keyMap.put( Keys.WAIT, Input.Keys.SPACE );
	}

	public void defaultNumPad()
	{
		keyMap.put( Keys.LEFT, Input.Keys.NUMPAD_4 );
		keyMap.put( Keys.RIGHT, Input.Keys.NUMPAD_6 );
		keyMap.put( Keys.UP, Input.Keys.NUMPAD_8 );
		keyMap.put( Keys.DOWN, Input.Keys.NUMPAD_2 );
		keyMap.put( Keys.CANCEL, Input.Keys.PERIOD );
		keyMap.put( Keys.ACCEPT, Input.Keys.ENTER );
		keyMap.put( Keys.WAIT, Input.Keys.NUMPAD_5 );
	}

	public void setKeyMap(Keys key, int keycode)
	{
		keyMap.put( key, keycode );
	}

	public int getKeyCode( Keys key )
	{
		return keyMap.get( key );
	}

	public boolean isKey(Keys key, int keycode)
	{
		return keyMap.get( key ) == keycode;
	}

	public boolean isKeyDown(Keys key)
	{
		return Gdx.input.isKeyPressed( keyMap.get( key ) );
	}
}
