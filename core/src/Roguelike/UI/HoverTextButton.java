package Roguelike.UI;

import Roguelike.Ability.AbilityPool.AbilityLine;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;

public class HoverTextButton extends Widget
{
	public enum VerticalAlignment
	{
		TOP,
		CENTRE,
		BOTTOM
	}
	
	public enum HorizontalAlignment
	{
		LEFT,
		CENTRE,
		RIGHT
	}
	
	private final float hPad = 15;
	private final float vPad = 15;
	
	private final BitmapFont normalFont;
	private final BitmapFont highlightFont;
	
	private final String text;
	private final GlyphLayout layout = new GlyphLayout();
	
	private float normalLineWidth;
	private float normalLineHeight;
	
	private float highlightLineWidth;
	private float highlightLineHeight;
	
	public VerticalAlignment valign = VerticalAlignment.BOTTOM;
	public HorizontalAlignment halign = HorizontalAlignment.LEFT;
	
	private boolean isHighlighted;
	
	public HoverTextButton(String text, BitmapFont normalFont, BitmapFont highlightFont)
	{
		this.text = text;
		this.normalFont = normalFont;
		this.highlightFont = highlightFont;
		
		this.addListener(new HoverTextButtonListener());
						
		calculateWidth();
	}
	
	private void calculateWidth()
	{				
		layout.setText(highlightFont, text);
		highlightLineWidth = layout.width;
		highlightLineHeight = layout.height;
		
		layout.setText(normalFont, text);
		normalLineWidth = layout.width;
		normalLineHeight = layout.height;
	}
	
	public float getPrefWidth () 
	{
		float width = isHighlighted ? highlightLineWidth : normalLineWidth;
		
		return width + hPad * 2;
	}

	public float getPrefHeight () 
	{
		float height = isHighlighted ? highlightLineHeight : normalLineHeight;
		
		return height + vPad * 2;
	}
	
	public void draw (Batch batch, float parentAlpha) 
	{
		BitmapFont font = isHighlighted ? highlightFont : normalFont;
		
		float x = getX();
		float y = getY();
		
		float width = getWidth();
		float height = getHeight();
		
		float lineWidth = isHighlighted ? highlightLineWidth : normalLineWidth;
		float lineHeight = isHighlighted ? highlightLineHeight : normalLineHeight;
		
		if (valign == VerticalAlignment.TOP)
		{
			y = y + height - vPad;
		}
		else if (valign == VerticalAlignment.CENTRE)
		{
			y = y + (height / 2) + (lineHeight / 2);
		}
		else
		{
			y = y + vPad + lineHeight;
		}
		
		if (halign == HorizontalAlignment.LEFT)
		{
			x = x + hPad;
		}
		else if (halign == HorizontalAlignment.CENTRE)
		{
			x = x + (width / 2) - (lineWidth / 2);
		}
		else
		{
			x = x + width - lineWidth - hPad;
		}
		
		font.draw(batch, text, x, y);		
	}
	
	private class HoverTextButtonListener extends InputListener
	{		
		public void enter (InputEvent event, float x, float y, int pointer, Actor fromActor) 
		{
			isHighlighted = true;
		}
		
		public void exit (InputEvent event, float x, float y, int pointer, Actor toActor) 
		{
			isHighlighted = false;
		}
	}
}
