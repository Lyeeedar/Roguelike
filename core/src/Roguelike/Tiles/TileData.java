package Roguelike.Tiles;

import Roguelike.Sprite.Sprite;

public class TileData
{
	public Sprite FloorSprite;
	
	public boolean Opaque;
	public boolean Passable;
	
	public String Description;
	
	public TileData(Sprite floorSprite, boolean opaque, boolean passable, String description)
	{
		this.FloorSprite = floorSprite;
		this.Opaque = opaque;
		this.Passable = passable;
		this.Description = description;
	}
}
