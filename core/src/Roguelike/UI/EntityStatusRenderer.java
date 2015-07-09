package Roguelike.UI;

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

import Roguelike.Entity.Entity;
import Roguelike.Entity.Entity.StatusEffectStack;
import Roguelike.StatusEffect.StatusEffect;
import Roguelike.AssetManager;
import Roguelike.Global.Statistics;

public class EntityStatusRenderer
{
	private static BitmapFont font;
	static
	{
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("Sprites/GUI/SDS_8x8.ttf"));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = 8;
		parameter.borderWidth = 1;
		parameter.borderColor = Color.BLACK;
		font = generator.generateFont(parameter); // font size 12 pixels
		generator.dispose(); // don't forget to dispose to avoid memory leaks!
	}
	
	public static void draw(Entity entity, Batch batch, int x, int y, int width, int height, float heightScale)
	{
		Texture white = AssetManager.loadTexture("Sprites/white.png");
		
		float val = (float)entity.HP / (float)entity.getStatistic(Statistics.MAXHP);
		float barheight = height * heightScale;
		
		batch.setColor(Color.LIGHT_GRAY);		
		batch.draw(white, x-2, y+height-barheight-2, width+4, barheight+4);
		
		batch.setColor(Color.DARK_GRAY);		
		batch.draw(white, x-1, y+height-barheight-1, width+2, barheight+2);
		
		batch.setColor(new Color(Color.RED).lerp(Color.GREEN, val));		
		batch.draw(white, x, y+height-barheight, width*val, barheight);
		batch.setColor(Color.WHITE);
		
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
	
	public static Table getMouseOverTable(Entity entity, int x, int y, int width, int height, float heightScale, int mousex, int mousey, Skin skin)
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
