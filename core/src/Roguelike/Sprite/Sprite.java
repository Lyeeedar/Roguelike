package Roguelike.Sprite;

import Roguelike.Global;
import Roguelike.Sound.SoundInstance;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
	
	public String fileName;
	
	public Color colour = new Color(Color.WHITE);
	
	public float renderDelay = -1;
	
	public float animationDelay;
	public float animationAccumulator;
	
	public float rotation;
		
	public Array<TextureRegion> textures;
	
	public Array<TextureRegion> extraLayers = new Array<TextureRegion>();
	
	public SpriteAnimation spriteAnimation;
	
	public AnimationState animationState;
	
	public SoundInstance sound;
	
 	public Sprite(String fileName, float animationDelay, Array<TextureRegion> textures, Color colour, AnimationMode mode, SoundInstance sound)
	{		
 		this.fileName = fileName;
		this.textures = textures;
		this.animationDelay = animationDelay;
		this.sound = sound;
		
		animationState = new AnimationState();
		animationState.mode = mode;
		
		this.colour = colour;
	}
	
	public boolean update(float delta)
	{
		if (renderDelay > 0)
		{
			renderDelay -= delta;
			
			if (renderDelay > 0)
			{
				return false;
			}
		}
		
		boolean looped = false;
		animationAccumulator += delta;
		
		while (animationAccumulator >= animationDelay)
		{			
			animationAccumulator -= animationDelay;
			
			if (animationState.mode == AnimationMode.TEXTURE)
			{
				animationState.texIndex++;
				if (animationState.texIndex >= textures.size)
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
		
		drawTexture(batch, textures.get(animationState.texIndex), x, y, width, height, animationState);
		
		for (TextureRegion tex : extraLayers)
		{
			drawTexture(batch, tex, x, y, width, height, animationState);
		}
		
		batch.setColor(oldCol);
	}
	
	private void drawTexture(Batch batch, TextureRegion texture, int x, int y, int width, int height, AnimationState animationState)
	{	
		if (renderDelay > 0) { return; }
		
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
				x > Global.Resolution[0] ||
				y > Global.Resolution[1]
			)
		{
			return; // skip drawing
		}
		
		batch.draw(texture, x, y,width / 2.0f, height / 2.0f, width, height, 1, 1, rotation);
	}

	public TextureRegion getCurrentTexture()
	{
		return textures.get(animationState.texIndex);
	}
	
	public Sprite copy()
	{
		return new Sprite(fileName, animationDelay, textures, colour, animationState.mode, sound);
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
