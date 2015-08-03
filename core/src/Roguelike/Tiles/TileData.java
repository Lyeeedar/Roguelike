package Roguelike.Tiles;

import java.util.EnumSet;
import java.util.HashSet;

import Roguelike.AssetManager;
import Roguelike.Global.Passability;
import Roguelike.Lights.Light;
import Roguelike.Sprite.Sprite;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public class TileData
{
	public Sprite[] sprites;
	public Light light;
	
	public EnumSet<Passability> passableBy;
	public boolean opaque;
	
	public String description;
	
	public boolean canFeature = true;
	public boolean canSpawn = true;
	
	private TileData()
	{
		
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
		
		Element passableElement = xml.getChildByName("Passable");
		if (passableElement != null) 
		{ 
			data.passableBy = Passability.parse(passableElement); 
		}
		
		data.canFeature = xml.getBoolean("CanFeature", true);
		data.canSpawn = xml.getBoolean("CanSpawn", true);
		
		return data;
	}
}
