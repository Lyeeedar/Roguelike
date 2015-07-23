package Roguelike.Ability.ActiveAbility.EffectType;

import Roguelike.Global.Direction;
import Roguelike.RoguelikeGame;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Entity.GameEntity;
import Roguelike.Pathfinding.BresenhamLine;
import Roguelike.Sprite.MoveAnimation;
import Roguelike.Sprite.MoveAnimation.MoveEquation;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.XmlReader.Element;

public class EffectTypeTeleport extends AbstractEffectType
{
	public enum Mode
	{
		CASTERTOTARGET,
		TARGETTOCASTER
	}
	
	public enum Style
	{
		BLINK,
		CHARGE,
		LEAP
	}
	
	private Mode mode;
	private Style style;
	
	@Override
	public void update(ActiveAbility aa, float time, GameTile tile)
	{
		GameTile destination = null;
		GameEntity entityToMove = null;
		
		if (mode == Mode.CASTERTOTARGET)
		{
			destination = getDestination(aa.caster.tile, tile, style);			
			entityToMove = aa.caster;
		}
		else if (mode == Mode.TARGETTOCASTER)
		{
			destination = getDestination(tile, aa.caster.tile, style);			
			entityToMove = tile.entity;
		}
				
		if (destination != null && entityToMove != null)
		{
			int[] diff = destination.addObject(entityToMove);
			
			if (style == Style.CHARGE)
			{
				int distMoved = ( Math.abs(diff[0]) + Math.abs(diff[1]) ) / RoguelikeGame.TileSize;
				entityToMove.sprite.SpriteAnimation = new MoveAnimation(0.025f * distMoved, diff, MoveEquation.EXPONENTIAL);
			}
			else if (style == Style.LEAP)
			{
				int distMoved = ( Math.abs(diff[0]) + Math.abs(diff[1]) ) / RoguelikeGame.TileSize;
				entityToMove.sprite.SpriteAnimation = new MoveAnimation(0.025f * distMoved, diff, MoveEquation.LEAP);
			}
		}
	}
	
	private GameTile getDestination(GameTile src, GameTile target, Style style)
	{
		GameTile destination = null;
		
		if (target.getPassable(null) && target.entity == null)
		{
			destination = target;
		}
		else
		{
			if (style == Style.CHARGE)
			{
				int[][] possibleTiles = BresenhamLine.lineNoDiag(src.x, src.y, target.x, target.y);
				
				for (int i = possibleTiles.length-1; i >= 0; i--)
				{
					GameTile tile = target.level.getGameTile(possibleTiles[i]);
					
					if (tile.getPassable(null) && tile.entity == null)
					{
						destination = tile;
						break;
					}
				}
			}
			else
			{
				for (Direction d : Direction.values())
				{
					GameTile tile = getTile(target, d);
					
					if (tile.getPassable(null) && tile.entity == null)
					{
						destination = tile;
						break;
					}
				}
			}
		}
		
		return destination;
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
		mode = Mode.valueOf(xml.get("Mode", "CasterToTarget").toUpperCase());
		style = Style.valueOf(xml.get("Style", "Charge").toUpperCase());
	}

	@Override
	public AbstractEffectType copy()
	{
		EffectTypeTeleport effect = new EffectTypeTeleport();
		effect.mode = mode;
		effect.style = style;
		
		return effect;
	}

}
