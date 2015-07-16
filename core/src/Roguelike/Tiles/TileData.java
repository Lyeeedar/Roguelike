package Roguelike.Tiles;

import com.badlogic.gdx.utils.XmlReader.Element;

import Roguelike.AssetManager;
import Roguelike.Lights.Light;
import Roguelike.Sprite.Sprite;

public class TileData
{
	public Sprite floorSprite;
	public Sprite featureSprite;
	public Light light;
	
	public boolean Opaque;
	public boolean Passable;
	
	public String Description;
	
	public TileData()
	{
		
	}
	
	public TileData(Sprite floorSprite, Sprite featureSprite, Light light, boolean opaque, boolean passable, String description)
	{
		this.floorSprite = floorSprite;
		this.featureSprite = featureSprite;
		this.light = light;
		this.Opaque = opaque;
		this.Passable = passable;
		this.Description = description;
	}
	
	public static TileData parse(Element xml)
	{
		TileData data = new TileData();
		
		if (xml.getChildByName("FloorSprite") != null) { data.floorSprite = AssetManager.loadSprite(xml.getChildByName("FloorSprite")); }
		if (xml.getChildByName("FeatureSprite") != null) { data.featureSprite = AssetManager.loadSprite(xml.getChildByName("FeatureSprite")); }
		if (xml.getChildByName("Light") != null) { data.light = Light.load(xml.getChildByName("Light")); }
		data.Opaque = xml.getBoolean("Opaque", false);
		data.Passable = xml.getBoolean("Passable", true);
		
		return data;
	}
}
