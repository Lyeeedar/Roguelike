package Roguelike.Entity.AI.AbilityAI;

import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.Global.Direction;
import Roguelike.Entity.ActiveAbility;
import Roguelike.Entity.ActiveAbility.AbilityTile;
import Roguelike.Sprite.SpriteEffect;
import Roguelike.Sprite.SpriteEffect.EffectType;
import Roguelike.Sprite.Sprite;

import com.badlogic.gdx.utils.XmlReader.Element;

public class AbilityAIStrike extends AbstractAbilityAI
{	
	Sprite hitEffect;
	
	@Override
	public void update(ActiveAbility ab)
	{
		for (AbilityTile tile : ab.AffectedTiles)
		{
			if (tile.Tile.Entity != null && !tile.Tile.Entity.isAllies(ab.Caster))
			{
				int damage = Global.calculateDamage(ab.getFullStatistics(), tile.Tile.Entity.getStatistics());
				
				tile.Tile.Entity.HP -= damage;
				tile.Tile.Entity.SpriteEffects.add(new SpriteEffect(hitEffect, EffectType.SINGLE, Direction.CENTER));
			}
		}
		
		ab.advanceAI();
	}

	@Override
	public void Init(ActiveAbility ab, int targetx, int targety)
	{
	}

	@Override
	public void parse(Element xml)
	{
		hitEffect = AssetManager.loadSprite(xml.getChildByName("HitEffect"));
	}

	@Override
	public AbstractAbilityAI copy()
	{
		AbilityAIStrike ai = new AbilityAIStrike();
		ai.hitEffect = hitEffect.copy();
		
		return ai;
	}
}
