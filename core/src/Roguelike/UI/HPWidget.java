package Roguelike.UI;

import Roguelike.AssetManager;
import Roguelike.Entity.Entity;
import Roguelike.Global.Statistics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;

public class HPWidget extends Widget
{
	Entity entity;
	private BitmapFont font;
	private Texture white;
	
	public HPWidget(Entity entity)
	{
		this.entity = entity;
		
		this.font = new BitmapFont();
		this.white = AssetManager.loadTexture("Sprites/white.png");
	}
	
	@Override
	public void draw (Batch batch, float parentAlpha)
	{
		super.draw(batch, parentAlpha);
		
		float hp = (float)entity.HP / (float)entity.getStatistic(Statistics.MAXHP);
		
		batch.setColor(Color.RED);
		batch.draw(white, getX(), getY(), getWidth(), getHeight());
		
		batch.setColor(Color.GREEN);
		batch.draw(white, getX(), getY(), getWidth() * hp, getHeight());
		
		font.draw(batch, entity.HP + " / " + entity.getStatistic(Statistics.MAXHP), getX() + getWidth()/2, getY() + getHeight()/2);
	}
}
