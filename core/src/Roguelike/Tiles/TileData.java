package Roguelike.Tiles;

import Roguelike.Sprite.Sprite;

public class TileData
{
	public Sprite FloorSprite;
	public Sprite CeilingSprite;
	
	public boolean Opaque;
	public boolean Passable;
	
	public String Description;
	
	public TileData(Sprite floorSprite, Sprite ceilingSprite, boolean opaque, boolean passable, String description)
	{
		this.FloorSprite = floorSprite;
		this.CeilingSprite = ceilingSprite;
		this.Opaque = opaque;
		this.Passable = passable;
		this.Description = description;
	}
}
