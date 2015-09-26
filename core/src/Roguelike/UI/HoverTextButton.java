package Roguelike.UI;

import Roguelike.AssetManager;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;

public class HoverTextButton extends Widget
{
	public enum VerticalAlignment
	{
		TOP, CENTRE, BOTTOM
	}

	public enum HorizontalAlignment
	{
		LEFT, CENTRE, RIGHT
	}

	private float hPad = 15;
	private float vPad = 15;

	private final BitmapFont normalFont;
	private final BitmapFont highlightFont;

	private final NinePatch buttonBackground;

	private final int buttonWidth;

	private final String text;
	private final GlyphLayout layout = new GlyphLayout();

	private float lineWidth;
	private float lineHeight;

	public VerticalAlignment valign = VerticalAlignment.CENTRE;
	public HorizontalAlignment halign = HorizontalAlignment.CENTRE;

	private boolean isHighlighted;

	public HoverTextButton( String text, int size, int buttonWidth )
	{
		this.text = text;

		Color textCol = new Color( 0.87f, 0.77f, 0.6f, 1 );
		this.normalFont = AssetManager.loadFont( "Sprites/GUI/stan0755.ttf", size, textCol, 1, Color.DARK_GRAY, true );
		this.highlightFont = AssetManager.loadFont( "Sprites/GUI/stan0755.ttf", size, textCol, 1, Color.LIGHT_GRAY, true );

		this.buttonBackground = new NinePatch( AssetManager.loadTextureRegion( "Sprites/GUI/Button.png" ), 12, 12, 12, 12 );
		this.buttonWidth = buttonWidth;

		this.addListener( new HoverTextButtonListener() );

		calculateWidth();
	}

	public void changePadding( float horizontal, float vertical )
	{
		hPad = horizontal;
		vPad = vertical;
		calculateWidth();
	}

	private void calculateWidth()
	{
		layout.setText( normalFont, text );
		lineWidth = layout.width;
		lineHeight = layout.height;
	}

	@Override
	public float getPrefWidth()
	{
		return lineWidth + hPad * 2;
	}

	@Override
	public float getPrefHeight()
	{
		return lineHeight + vPad * 2;
	}

	@Override
	public void draw( Batch batch, float parentAlpha )
	{
		batch.setColor( Color.WHITE );

		float x = getX();
		float y = getY();

		float width = getWidth();
		float height = getHeight();

		float backgroundWidth = buttonWidth;
		buttonBackground.draw( batch, ( x + width / 2 ) - ( backgroundWidth / 2 ) - 5, ( y + height / 2 ) - ( lineHeight / 2 ) - 20, backgroundWidth + 10, lineHeight + 40 );

		if ( valign == VerticalAlignment.TOP )
		{
			y = y + height - vPad;
		}
		else if ( valign == VerticalAlignment.CENTRE )
		{
			y = y + ( height / 2 ) + ( lineHeight / 2 );
		}
		else
		{
			y = y + vPad + lineHeight;
		}

		if ( halign == HorizontalAlignment.LEFT )
		{
			x = x + hPad;
		}
		else if ( halign == HorizontalAlignment.CENTRE )
		{
			x = x + ( width / 2 ) - ( lineWidth / 2 );
		}
		else
		{
			x = x + width - lineWidth - hPad;
		}

		BitmapFont font = isHighlighted ? highlightFont : normalFont;

		font.draw( batch, text, x, y );
	}

	private class HoverTextButtonListener extends InputListener
	{
		@Override
		public boolean mouseMoved( InputEvent event, float x, float y )
		{
			isHighlighted = true;
			return true;
		}

		@Override
		public void enter( InputEvent event, float x, float y, int pointer, Actor fromActor )
		{
			isHighlighted = true;
		}

		@Override
		public void exit( InputEvent event, float x, float y, int pointer, Actor toActor )
		{
			isHighlighted = false;
		}
	}
}
