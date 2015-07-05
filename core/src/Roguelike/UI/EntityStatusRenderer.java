package Roguelike.UI;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;

import Roguelike.Entity.Entity;
import Roguelike.StatusEffect.StatusEffect;
import Roguelike.AssetManager;
import Roguelike.Global.Statistics;

public class EntityStatusRenderer
{
	public static void draw(Entity entity, Batch batch, int x, int y, int width, int height)
	{
		Texture white = AssetManager.loadTexture("Sprites/white.png");
		
		float val = (float)entity.HP / (float)entity.getStatistic(Statistics.MAXHP);
		float barheight = Math.min(height/10, 10);	
		
		batch.setColor(new Color(Color.RED).lerp(Color.GREEN, val));		
		batch.draw(white, x, y+height-barheight, width*val, barheight);
		batch.setColor(Color.WHITE);
		
		Array<StatusEffectStack> stacks = stackStatusEffects(entity);
		
		int statusTileSize = width / 3;
		int sx = x;
		int sy = (int)(y+height-barheight - statusTileSize);
		
		for (StatusEffectStack stack : stacks)
		{
			stack.effect.icon.render(batch, sx, sy, statusTileSize, statusTileSize);
			
			sx += statusTileSize;
		}
	}
	
	public static Table getMouseOverTable(Entity entity, int x, int y, int width, int height, int mousex, int mousey, Skin skin)
	{
		Array<StatusEffectStack> stacks = stackStatusEffects(entity);
		
		float barheight = Math.min(height/10, 10);	
		int statusTileSize = width / 3;
		int sx = x;
		int sy = (int)(y+height-barheight - statusTileSize);
				
		for (StatusEffectStack stack : stacks)
		{			
			if (mousex >= sx && mousex <= sx+statusTileSize && mousey >= sy && mousey <= sy+statusTileSize)
			{
				return stack.effect.createTable(skin);
			}
			
			sx += statusTileSize;
		}
		
		return null;
	}
	
	private static Array<StatusEffectStack> stackStatusEffects(Entity entity)
	{
		Array<StatusEffectStack> stacks = new Array<StatusEffectStack>();
		
		for (StatusEffect se : entity.statusEffects)
		{
			boolean found = false;
			for (StatusEffectStack stack : stacks)
			{
				if (stack.effect.name.equals(se.name))
				{
					stack.count++;
					found = true;
					break;
				}
			}
			
			if (!found)
			{
				StatusEffectStack stack = new StatusEffectStack();
				stack.count = 1;
				stack.effect = se;
				
				stacks.add(stack);
			}
		}
		
		return stacks;
	}
	
	private static class StatusEffectStack
	{
		public StatusEffect effect;
		int count;
	}
}
