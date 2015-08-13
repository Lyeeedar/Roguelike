package Roguelike.Fields.FieldInteractionTypes;

import java.util.HashSet;

import Roguelike.AssetManager;
import Roguelike.Global.Direction;
import Roguelike.Fields.Field;
import Roguelike.Fields.OnDeathEffect.AbstractOnDeathEffect;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.SpriteEffect;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.XmlReader.Element;

public class PropogateFieldInteractionType extends AbstractFieldInteractionType
{	
	@Override
	public Field process(Field src, Field dst)
	{
		GameTile srcTile = src.tile != null ? src.tile : dst.tile;
		src.tile = srcTile;
		
		HashSet<GameTile> tiles = new HashSet<GameTile>();
		getAllLinkedTiles(tiles, srcTile, dst);
		for (GameTile tile : tiles)
		{
			for (AbstractOnDeathEffect effect : src.onDeathEffects)
			{
				effect.process(src, tile);
			}
		}
		
		return dst;
	}
	
	private void getAllLinkedTiles(HashSet<GameTile> tiles, GameTile src, Field field)
	{
		for (Direction dir : Direction.values())
		{
			GameTile newTile = src.level.getGameTile(src.x+dir.GetX(), src.y+dir.GetY());
			
			if (newTile.fields.get(field.layer) != null && newTile.fields.get(field.layer).fieldName.equals(field.fieldName))
			{
				boolean expand = !tiles.contains(newTile);
				tiles.add(newTile);
				
				if (expand)
				{
					getAllLinkedTiles(tiles, newTile, field);
				}
			}
		}
	}

	@Override
	public void parse(Element xml)
	{
	}

}
