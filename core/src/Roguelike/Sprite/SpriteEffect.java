package Roguelike.Sprite;

import Roguelike.Global.Direction;
import Roguelike.Lights.Light;

public class SpriteEffect
{	
	public Sprite Sprite;
	public Direction Corner;
	public Light light;
		
	public SpriteEffect(Sprite sprite, Direction Corner, Light light)
	{
		this.Sprite = sprite;
		this.Corner = Corner;
		this.light = light;
	}
	
}
