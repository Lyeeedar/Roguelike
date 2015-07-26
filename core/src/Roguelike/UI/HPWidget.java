package Roguelike.UI;

import Roguelike.AssetManager;
import Roguelike.Entity.GameEntity;
import Roguelike.Global.Statistic;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;

public class HPWidget extends Widget
{
	GameEntity entity;
	private BitmapFont font;
	private Texture white;
	
	public HPWidget(GameEntity entity)
	{
		this.entity = entity;
		
		this.font = new BitmapFont();
		this.white = AssetManager.loadTexture("Sprites/white.png");
	}
	
	@Override
	public void draw (Batch batch, float parentAlpha)
	{
		super.draw(batch, parentAlpha);
		
		float hp = (float)entity.HP / (float)entity.getStatistic(Statistic.MAXHP);
		
		batch.setColor(Color.RED);
		batch.draw(white, getX(), getY(), getWidth(), getHeight());
		
		batch.setColor(Color.GREEN);
		batch.draw(white, getX(), getY(), getWidth() * hp, getHeight());
		
		font.draw(batch, entity.HP + " / " + entity.getStatistic(Statistic.MAXHP), getX() + getWidth()/2, getY() + getHeight()/2);
	}
}
