package Roguelike.Tiles;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

import Roguelike.AssetManager;
import Roguelike.Lights.Light;
import Roguelike.Sprite.Sprite;

public class TileData
{
	public Sprite[] sprites;
	public Light light;
	
	public boolean opaque;
	public boolean passable;
	
	public String description;
	
	public TileData()
	{
		
	}
	
	public TileData(Sprite[] sprites, Light light, boolean opaque, boolean passable, String description)
	{
		this.sprites = sprites;
		this.light = light;
		this.opaque = opaque;
		this.passable = passable;
		this.description = description;
	}
	
	public static TileData parse(Element xml)
	{
		TileData data = new TileData();
		
		Array<Sprite> sprites = new Array<Sprite>();
		for (Element spriteElement : xml.getChildrenByName("Sprite"))
		{
			sprites.add(AssetManager.loadSprite(spriteElement));
		}
		data.sprites = sprites.toArray(Sprite.class);
		
		if (xml.getChildByName("Light") != null) { data.light = Light.load(xml.getChildByName("Light")); }
		data.opaque = xml.getBoolean("Opaque", false);
		data.passable = xml.getBoolean("Passable", true);
		
		return data;
	}
}
