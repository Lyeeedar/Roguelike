package Roguelike.Fields.OnDeathEffect;

import Roguelike.AssetManager;
import Roguelike.Fields.Field;
import Roguelike.Lights.Light;
import Roguelike.Global.Direction;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.SpriteEffect;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.XmlReader.Element;

public class SpriteOnDeathEffect extends AbstractOnDeathEffect
{
	private Sprite sprite;

	@Override
	public void process(Field field, GameTile tile)
	{
		Light l = field.light != null ? field.light.copyNoFlag() : null;
		tile.spriteEffects.add(new SpriteEffect(sprite.copy(), Direction.CENTER, l));
	}

	@Override
	public void parse(Element xml)
	{
		sprite = AssetManager.loadSprite(xml);
	}

}
