package Roguelike.UI;

import Roguelike.Sprite.Sprite;

public class DragDropPayload
{
	public Object obj;
	public Sprite sprite;
	
	public float sx;
	public float sy;
	
	public float x;
	public float y;
	
	public DragDropPayload(Object obj, Sprite sprite, float x, float y)
	{
		this.obj = obj;
		this.sprite = sprite;
		this.sx = x;
		this.sy = y;
		this.x = x;
		this.y = y;
	}
	
	public boolean shouldDraw()
	{
		return Math.abs(sx - x) > 10 || Math.abs(sy - y) > 10;
	}
}
