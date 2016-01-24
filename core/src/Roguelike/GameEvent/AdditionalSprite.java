package Roguelike.GameEvent;

import Roguelike.AssetManager;
import Roguelike.Sprite.Sprite;
import com.badlogic.gdx.utils.XmlReader;

/**
 * Created by Philip on 24-Jan-16.
 */
public class AdditionalSprite
{
	public Sprite sprite;
	public int priorityDiff = 1;

	public void parse( XmlReader.Element xml )
	{
		sprite = AssetManager.loadSprite( xml );
		priorityDiff = xml.getIntAttribute( "Priority", 1 );
	}
}
