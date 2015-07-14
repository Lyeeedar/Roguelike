package Roguelike.Sprite;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;

public class Sprite
{
	public Color colour = new Color(Color.WHITE);
	
	public float animationDelay;
	public float animationAccumulator;
	
	public float rotation;
	
	public int[] tileSize;
	public int[] tileIndex;
	public int[] tileBounds;
	
	public int currentTexture = 0;
	public Texture[] textures;
	
	public SpriteAnimation SpriteAnimation;
	
	public Sprite(float animationDelay, Array<Texture> textures, int[] tileSize, int[] tileIndex, Color colour)
	{
		this(animationDelay, (Texture[])textures.toArray(Texture.class), tileSize, tileIndex, colour);
	}
	
	public Sprite(float animationDelay, Texture[] textures, int[] tileSize, int[] tileIndex, Color colour)
	{
		this.textures = textures;
		this.animationDelay = animationDelay;
		this.tileSize = tileSize;
		this.tileIndex = tileIndex;
		
		this.tileBounds = new int[]
		{
			tileSize[0] * tileIndex[0], tileSize[1] * tileIndex[1], // src x, y
			tileSize[0], tileSize[1] // src width, height
		};
		
		this.colour = colour;
	}
	
	public boolean update(float delta)
	{		
		boolean looped = false;
		animationAccumulator += delta;
		
		while (animationAccumulator >= animationDelay)
		{
			animationAccumulator -= animationDelay;
			
			currentTexture++;
			if (currentTexture >= textures.length)
			{
				currentTexture = 0;
				looped = true;
			}
		}
		
		if (SpriteAnimation != null)
		{
			boolean finished = SpriteAnimation.update(delta);
			if (finished)
			{
				SpriteAnimation = null;
			}
		}
		
		return looped;
	}
	
	public void render(Batch batch, int x, int y, int width, int height)
	{
		render(batch, x, y, width, height, currentTexture);
	}
	
	public void render(Batch batch, int x, int y, int width, int height, int texIndex)
	{
		Color oldCol = batch.getColor();
		Color col = new Color(oldCol).mul(colour);
		batch.setColor(col);
		
		batch.draw(
				textures[texIndex],
				x, y,
				width/2, height/2, // origin x, y
				width, height, // width, height
				1, 1, // scale
				rotation, // rotation
				tileBounds[0], tileBounds[1], tileBounds[2], tileBounds[3],
				false, false // flip x, y
				);
		
		batch.setColor(oldCol);
	}

	public Sprite copy()
	{
		return new Sprite(animationDelay, textures, tileSize, tileIndex, colour);
	}
}
