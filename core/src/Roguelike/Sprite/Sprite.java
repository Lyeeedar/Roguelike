package Roguelike.Sprite;

import Roguelike.Sound.SoundInstance;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;

public class Sprite
{
	public enum AnimationMode
	{
		NONE,
		TEXTURE,
		SHRINK,
		SINE
	}
	
	public Color colour = new Color(Color.WHITE);
	
	public boolean render = true;
	public float animationDelay;
	public float animationAccumulator;
	
	public float rotation;
	
	public int[] tileSize;
	public int[] tileIndex;
	public int[] tileBounds;
	
	public Texture[] textures;
	
	public Array<Texture> extraLayers = new Array<Texture>();
	
	public SpriteAnimation spriteAnimation;
	
	public AnimationState animationState;
	
	public SoundInstance sound;
	
 	public Sprite(float animationDelay, Array<Texture> textures, int[] tileSize, int[] tileIndex, Color colour, AnimationMode mode, SoundInstance sound)
	{
		this(animationDelay, (Texture[])textures.toArray(Texture.class), tileSize, tileIndex, colour, mode, sound);
	}
	
	public Sprite(float animationDelay, Texture[] textures, int[] tileSize, int[] tileIndex, Color colour, AnimationMode mode, SoundInstance sound)
	{
		this.textures = textures;
		this.animationDelay = animationDelay;
		this.tileSize = tileSize;
		this.tileIndex = tileIndex;
		this.sound = sound;
		
		animationState = new AnimationState();
		animationState.mode = mode;
		
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
			render = true;
			
			animationAccumulator -= animationDelay;
			
			if (animationState.mode == AnimationMode.TEXTURE)
			{
				animationState.texIndex++;
				if (animationState.texIndex >= textures.length)
				{
					animationState.texIndex = 0;
					looped = true;
				}
			}
			else if (animationState.mode == AnimationMode.SHRINK)
			{
				animationState.isShrunk = !animationState.isShrunk;
				looped = animationState.isShrunk;
			}
			else if (animationState.mode == AnimationMode.SINE)
			{
				looped = true;
			}
		}
		
		if (animationState.mode == AnimationMode.SINE)
		{
			animationState.sinOffset = (float)Math.sin(animationAccumulator / (animationDelay/(2*Math.PI)));
		}
		
		if (spriteAnimation != null)
		{
			looped = spriteAnimation.update(delta);
			if (looped)
			{
				spriteAnimation = null;
			}
		}
		
		return looped;
	}
	
	public void render(Batch batch, int x, int y, int width, int height)
	{
		if (spriteAnimation != null)
		{
			int[] offset = spriteAnimation.getRenderOffset();
			x += offset[0];
			y += offset[1];
		}
		
		render(batch, x, y, width, height, animationState);
	}
	
	public void render(Batch batch, int x, int y, int width, int height, AnimationState animationState)
	{
		Color oldCol = batch.getColor();
		Color col = new Color(oldCol).mul(colour);
		batch.setColor(col);
		
		drawTexture(batch, textures[animationState.texIndex], x, y, width, height, animationState);
		
		for (Texture tex : extraLayers)
		{
			drawTexture(batch, tex, x, y, width, height, animationState);
		}
		
		batch.setColor(oldCol);
	}
	
	private void drawTexture(Batch batch, Texture texture, int x, int y, int width, int height, AnimationState animationState)
	{	
		if (!render) { return; }
		
		if (animationState.mode == AnimationMode.SHRINK && animationState.isShrunk)
		{
			height *= 0.9f;
		}
		else if (animationState.mode == AnimationMode.SINE)
		{
			y += (height / 20) * animationState.sinOffset;
		}
		
		// Check if not onscreen
		if (
				x + width < 0 ||
				y + height < 0 ||
				x > Gdx.graphics.getWidth() ||
				y > Gdx.graphics.getHeight()
			)
		{
			return; // skip drawing
		}
		
		batch.draw(
				texture,
				x, y,
				width / 2.0f, height / 2.0f,
				width, height, // width, height
				1, 1, // scale
				rotation, // rotation
				tileBounds[0], tileBounds[1], tileBounds[2], tileBounds[3],
				false, false // flip x, y
		);
	}

	public Texture getCurrentTexture()
	{
		return textures[animationState.texIndex];
	}
	
	public Sprite copy()
	{
		return new Sprite(animationDelay, textures, tileSize, tileIndex, colour, animationState.mode, sound);
	}
	
	public static class AnimationState
	{
		public AnimationMode mode;
		
		public int texIndex;
		public boolean isShrunk;
		public float sinOffset;
		
		public AnimationState copy()
		{
			AnimationState as = new AnimationState();
			
			as.mode = mode;
			as.texIndex = texIndex;
			as.isShrunk = isShrunk;
			as.sinOffset = sinOffset;
			
			return as;
		}
	}
}
