package Roguelike.UI;

import Roguelike.Sprite.Sprite;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

public class SpriteWidget extends Widget
{
	private Sprite drawable;
	private Scaling scaling = Scaling.stretch;
	private int align = Align.center;
	private float imageX, imageY, imageWidth, imageHeight;
	private float width;
	private float height;

	public SpriteWidget (Sprite sprite, int width, int height)
	{
		this.drawable = sprite;
		this.width = width;
		this.height = height;
	}

	public void layout ()
	{
		if (drawable == null) return;

		float regionWidth = width;
		float regionHeight = height;
		float width = getWidth();
		float height = getHeight();

		Vector2 size = scaling.apply(regionWidth, regionHeight, width, height);
		imageWidth = size.x;
		imageHeight = size.y;

		if ((align & Align.left) != 0)
			imageX = 0;
		else if ((align & Align.right) != 0)
			imageX = (int)(width - imageWidth);
		else
			imageX = (int)(width / 2 - imageWidth / 2);

		if ((align & Align.top) != 0)
			imageY = (int)(height - imageHeight);
		else if ((align & Align.bottom) != 0)
			imageY = 0;
		else
			imageY = (int)(height / 2 - imageHeight / 2);
	}

	public void draw (Batch batch, float parentAlpha)
	{
		validate();

		Color color = getColor();
		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

		float x = getX();
		float y = getY();
		float scaleX = getScaleX();
		float scaleY = getScaleY();

		drawable.render(batch, (int) (x + imageX), (int) (y + imageY), (int) (imageWidth * scaleX), (int) (imageHeight * scaleY));
	}

	@Override
	public float getPrefWidth ()
	{
		return width;
	}

	@Override
	public float getPrefHeight ()
	{
		return height;
	}

	@Override
	public void setSize(float width, float height)
	{
		this.width = width;
		this.height = height;
	}

	@Override
	public void act(float delta)
	{
		super.act(delta);
		drawable.update(delta);
	}
}
