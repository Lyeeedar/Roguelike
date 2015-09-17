package Roguelike.Sprite;

import Roguelike.Global;
import Roguelike.Sound.SoundInstance;
import Roguelike.Sprite.SpriteAnimation.AbstractSpriteAnimation;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public final class Sprite
{
	public enum AnimationMode
	{
		NONE, TEXTURE, SHRINK, SINE
	}

	public String fileName;

	private static final Color tempColour = new Color();

	public Color colour = new Color( Color.WHITE );

	public float renderDelay = -1;

	public float animationDelay;
	public float animationAccumulator;

	public float rotation;

	public int size = 1;

	public Array<TextureRegion> textures;

	public AbstractSpriteAnimation spriteAnimation;

	public AnimationState animationState;

	public SoundInstance sound;

	public boolean drawActualSize;

	public float[] baseScale = { 1, 1 };

	public Sprite( String fileName, float animationDelay, Array<TextureRegion> textures, Color colour, AnimationMode mode, SoundInstance sound, boolean drawActualSize )
	{
		this.fileName = fileName;
		this.textures = textures;
		this.animationDelay = animationDelay;
		this.sound = sound;
		this.drawActualSize = drawActualSize;

		animationState = new AnimationState();
		animationState.mode = mode;

		this.colour = colour;
	}

	public boolean update( float delta )
	{
		if ( renderDelay > 0 )
		{
			renderDelay -= delta;

			if ( renderDelay > 0 ) { return false; }
		}

		boolean looped = false;
		animationAccumulator += delta;

		while ( animationAccumulator >= animationDelay )
		{
			animationAccumulator -= animationDelay;

			if ( animationState.mode == AnimationMode.TEXTURE )
			{
				animationState.texIndex++;
				if ( animationState.texIndex >= textures.size )
				{
					animationState.texIndex = 0;
					looped = true;
				}
			}
			else if ( animationState.mode == AnimationMode.SHRINK )
			{
				animationState.isShrunk = !animationState.isShrunk;
				looped = animationState.isShrunk;
			}
			else if ( animationState.mode == AnimationMode.SINE )
			{
				looped = true;
			}
		}

		if ( animationState.mode == AnimationMode.SINE )
		{
			animationState.sinOffset = (float) Math.sin( animationAccumulator / ( animationDelay / ( 2 * Math.PI ) ) );
		}

		if ( spriteAnimation != null )
		{
			looped = spriteAnimation.update( delta );
			if ( looped )
			{
				spriteAnimation = null;
			}
		}

		return looped;
	}

	public void render( Batch batch, int x, int y, int width, int height )
	{
		float scaleX = baseScale[0];
		float scaleY = baseScale[1];

		if ( spriteAnimation != null )
		{
			int[] offset = spriteAnimation.getRenderOffset();
			if ( offset != null )
			{
				x += offset[0];
				y += offset[1];
			}

			float[] scale = spriteAnimation.getRenderScale();
			if ( scale != null )
			{
				scaleX = scale[0];
				scaleY = scale[1];
			}
		}

		render( batch, x, y, width, height, scaleX, scaleY, animationState );
	}

	public void render( Batch batch, int x, int y, int width, int height, float scaleX, float scaleY, AnimationState animationState )
	{
		Color oldCol = null;
		if ( colour.a == 0 )
		{
			return;
		}
		else if ( colour.r != 1 || colour.g != 1 || colour.b != 1 )
		{
			oldCol = batch.getColor();

			Color col = tempColour.set( oldCol ).mul( colour );
			batch.setColor( col );
		}

		drawTexture( batch, textures.items[animationState.texIndex], x, y, width, height, scaleX, scaleY, animationState );

		if ( oldCol != null )
		{
			batch.setColor( oldCol );
		}
	}

	private void drawTexture( Batch batch, TextureRegion texture, int x, int y, int width, int height, float scaleX, float scaleY, AnimationState animationState )
	{
		if ( renderDelay > 0 ) { return; }

		if ( drawActualSize )
		{
			float widthRatio = width / 32.0f;
			float heightRatio = height / 32.0f;

			int trueWidth = (int) ( texture.getRegionWidth() * widthRatio );
			int trueHeight = (int) ( texture.getRegionHeight() * heightRatio );

			int widthOffset = ( trueWidth - width ) / 2;
			int heightOffset = ( trueHeight - height ) / 2;

			x -= widthOffset;
			y -= heightOffset;
			width = trueWidth;
			height = trueHeight;
		}

		width = width * size;
		height = height * size;

		if ( animationState.mode == AnimationMode.SHRINK && animationState.isShrunk )
		{
			height *= 0.9f;
		}
		else if ( animationState.mode == AnimationMode.SINE )
		{
			y += ( height / 20 ) * animationState.sinOffset;
		}

		// Check if not onscreen
		if ( x + width < 0 || y + height < 0 || x > Global.Resolution[0] || y > Global.Resolution[1] ) { return; // skip
		// drawing
		}

		batch.draw( texture, x, y, width / 2.0f, height / 2.0f, width, height, scaleX, scaleY, rotation );
	}

	public TextureRegion getCurrentTexture()
	{
		return textures.get( animationState.texIndex );
	}

	public Sprite copy()
	{
		Sprite sprite = new Sprite( fileName, animationDelay, textures, colour, animationState.mode, sound, drawActualSize );
		if ( spriteAnimation != null )
		{
			sprite.spriteAnimation = spriteAnimation.copy();
		}

		return sprite;
	}

	public static final class AnimationState
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

		public void set( AnimationState other )
		{
			mode = other.mode;
			texIndex = other.texIndex;
			isShrunk = other.isShrunk;
			sinOffset = other.sinOffset;
		}
	}
}
