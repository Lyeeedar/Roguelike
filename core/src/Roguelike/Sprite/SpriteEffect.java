package Roguelike.Sprite;

import Roguelike.Global.Direction;
import Roguelike.Entity.ActiveAbility;

public class SpriteEffect
{	
	public enum EffectType
	{
		SINGLE,
		DURATION
	}
	
	public Sprite Sprite;
	public EffectType Type;
	public Direction Corner;
	
	public ActiveAbility LinkedAbility;
	
	public SpriteEffect(Sprite sprite, EffectType type, Direction Corner)
	{
		this.Sprite = sprite;
		this.Type = type;
		this.Corner = Corner;
	}
	
}
