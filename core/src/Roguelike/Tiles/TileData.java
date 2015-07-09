package Roguelike.Tiles;

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
	
	public TileData(Sprite floorSprite, Sprite featureSprite, Light light, boolean opaque, boolean passable, String description)
	{
		this.floorSprite = floorSprite;
		this.featureSprite = featureSprite;
		this.light = light;
		this.Opaque = opaque;
		this.Passable = passable;
		this.Description = description;
	}
}
