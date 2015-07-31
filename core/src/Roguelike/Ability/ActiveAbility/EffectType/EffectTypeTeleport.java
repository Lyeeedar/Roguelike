package Roguelike.Ability.ActiveAbility.EffectType;

import Roguelike.Global;
import Roguelike.Global.Direction;
import Roguelike.RoguelikeGame;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Entity.GameEntity;
import Roguelike.Pathfinding.BresenhamLine;
import Roguelike.Sprite.MoveAnimation;
import Roguelike.Sprite.MoveAnimation.MoveEquation;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public class EffectTypeTeleport extends AbstractEffectType
{
	public enum ToMove
	{
		CASTER,
		TARGET
	}
	
	public enum MoveType
	{
		TOWARDS,
		AWAY
	}
	
	public enum DestinationType
	{
		EXACT,
		LINE,
		CONE
	}
	
	public enum Style
	{
		BLINK,
		CHARGE,
		LEAP
	}
	
	private ToMove toMove;
	private MoveType moveType;
	private DestinationType destinationType;
	private int distance = 1;
	
	private Style style;
	
	@Override
	public void update(ActiveAbility aa, float time, GameTile tile, GameTile epicenter)
	{
		GameTile destination = null;
		GameEntity entityToMove = null;
		
		if (toMove == ToMove.CASTER)
		{
			destination = getDestination(aa.source, tile);			
			entityToMove = aa.caster;
		}
		else if (toMove == ToMove.TARGET)
		{
			destination = getDestination(tile, aa.source);			
			entityToMove = tile.entity;
		}
				
		if (destination != null && entityToMove != null && entityToMove.tile != destination)
		{
			int[] diff = destination.addObject(entityToMove);
			entityToMove.actionDelayAccumulator = 0;
			
			if (style == Style.CHARGE)
			{
				int distMoved = ( Math.abs(diff[0]) + Math.abs(diff[1]) ) / Global.TileSize;
				entityToMove.sprite.spriteAnimation = new MoveAnimation(0.02f * distMoved, diff, MoveEquation.EXPONENTIAL);
			}
			else if (style == Style.LEAP)
			{
				int distMoved = (( Math.abs(diff[0]) + Math.abs(diff[1]) ) / Global.TileSize) / 3;
				entityToMove.sprite.spriteAnimation = new MoveAnimation(0.1f * distMoved, diff, MoveEquation.LEAP);
			}
		}
	}
	
	private GameTile getDestination(GameTile src, GameTile target)
	{
		Direction dir = null;
		
		if (moveType == MoveType.TOWARDS)
		{
			dir = Direction.getDirection(src, target);
		}
		else
		{
			dir = Direction.getDirection(target, src);
		}
		
		GameTile destination = null;
		
		if (destinationType == DestinationType.EXACT)
		{
			destination = target;
		}
		else if (destinationType == DestinationType.LINE)
		{
			destination = src.level.getGameTile(src.x + dir.GetX() * distance, src.y + dir.GetY() * distance);
		}
		else
		{
			Array<int[]> possibleTiles = Direction.buildCone(dir, new int[]{src.x,  src.y}, distance);
			int[] pos = possibleTiles.get(MathUtils.random(possibleTiles.size-1));
			
			destination = src.level.getGameTile(pos);
		}
			
		if (style != Style.CHARGE)
		{
			if (destination.getPassable() && destination.entity == null)
			{
				return destination;
			}
			else
			{
				for (Direction d : Direction.values())
				{
					GameTile tile = getTile(destination, d);
					
					if (tile.getPassable() && tile.entity == null)
					{
						return tile;
					}
				}
			}
		}
		
		int[][] possibleTiles = BresenhamLine.lineNoDiag(src.x, src.y, destination.x, destination.y);
		
		for (int i = possibleTiles.length-1; i >= 0; i--)
		{
			GameTile tile = src.level.getGameTile(possibleTiles[i]);
			
			if (tile.getPassable() && tile.entity == null)
			{
				return tile;
			}
		}
		
		return src;
	}
	
	private GameTile getTile(GameTile current, Direction dir)
	{
		int nx = current.x + dir.GetX();
		int ny = current.y + dir.GetY();
		
		return current.level.getGameTile(nx, ny);
	}

	@Override
	public void parse(Element xml)
	{
		toMove = ToMove.valueOf(xml.get("Move", "Caster").toUpperCase());
		moveType = MoveType.valueOf(xml.get("Direction", "Towards").toUpperCase());
		destinationType = DestinationType.valueOf(xml.get("Type", "Exact").toUpperCase());
		distance = xml.getInt("Distance", 1);
		
		style = Style.valueOf(xml.get("Style", "Blink").toUpperCase());
	}

	@Override
	public AbstractEffectType copy()
	{
		EffectTypeTeleport effect = new EffectTypeTeleport();
		effect.toMove = toMove;
		effect.moveType = moveType;
		effect.destinationType = destinationType;
		
		effect.distance = distance;
		
		effect.style = style;
		
		return effect;
	}

}
