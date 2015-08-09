package Roguelike.UI;

import Roguelike.AssetManager;
import Roguelike.Global.Statistic;
import Roguelike.Entity.Entity;
import Roguelike.Entity.Entity.StatusEffectStack;
import Roguelike.Entity.GameEntity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;

public class EntityStatusRenderer
{	
	public static void draw(Entity entity, Batch batch, int x, int y, int width, int height, float heightScale)
	{		
		BitmapFont font = AssetManager.loadFont("Sprites/GUI/stan0755.ttf", 8);
		
		float val = (float)entity.HP / (float)entity.getStatistic(Statistic.MAXHP);
		float barheight = height * heightScale;
		drawHpBar(val, batch, x, y, width, height, heightScale);
		
		Array<StatusEffectStack> stacks = entity.stackStatusEffects();
		
		int statusTileSize = Math.min(width / 3, 32);
		int sx = x;
		int sy = (int)(y+height-barheight - statusTileSize);
		
		for (StatusEffectStack stack : stacks)
		{
			stack.effect.icon.render(batch, sx, sy, statusTileSize, statusTileSize);
			font.draw(batch, ""+stack.count, sx, sy);
			
			sx += statusTileSize;
			
			if (sx >= x+width)
			{
				sx = x;
				sy += statusTileSize;
			}
		}
	}
	
	public static void drawHpBar(float val, Batch batch, int x, int y, int width, int height, float heightScale)
	{
		Texture white = AssetManager.loadTexture("Sprites/white.png");
		
		float barheight = height * heightScale;
		
		batch.setColor(Color.LIGHT_GRAY);		
		batch.draw(white, x-2, y+height-barheight-2, width+4, barheight+4);
		
		batch.setColor(Color.DARK_GRAY);		
		batch.draw(white, x-1, y+height-barheight-1, width+2, barheight+2);
		
		batch.setColor(new Color(Color.RED).lerp(Color.GREEN, val));		
		batch.draw(white, x, y+height-barheight, width*val, barheight);
		batch.setColor(Color.WHITE);
	}
	
	public static Table getMouseOverTable(GameEntity entity, int x, int y, int width, int height, float heightScale, int mousex, int mousey, Skin skin)
	{
		Array<StatusEffectStack> stacks = entity.stackStatusEffects();
		
		float barheight = height * heightScale;	
		int statusTileSize = Math.min(width / 3, 32);
		int sx = x;
		int sy = (int)(y+height-barheight - statusTileSize);
				
		for (StatusEffectStack stack : stacks)
		{			
			if (mousex >= sx && mousex <= sx+statusTileSize && mousey >= sy && mousey <= sy+statusTileSize)
			{
				return stack.effect.createTable(skin);
			}
			
			sx += statusTileSize;
			
			if (sx >= x+width)
			{
				sx = x;
				sy += statusTileSize;
			}
		}
		
		return null;
	}
	
}
