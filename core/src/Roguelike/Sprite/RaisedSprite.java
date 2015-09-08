package Roguelike.Sprite;

import Roguelike.AssetManager;

import com.badlogic.gdx.utils.XmlReader.Element;

public class RaisedSprite
{
	public String name;
	public Sprite frontSprite;
	public Sprite topSprite;

	public void parse( Element xml )
	{
		name = xml.get( "Name" );

		frontSprite = AssetManager.loadSprite( xml.getChildByName( "Front" ) );
		topSprite = AssetManager.loadSprite( xml.getChildByName( "Top" ) );
	}

	public static RaisedSprite load( Element xml )
	{
		RaisedSprite sprite = new RaisedSprite();
		sprite.parse( xml );
		return sprite;
	}
}
