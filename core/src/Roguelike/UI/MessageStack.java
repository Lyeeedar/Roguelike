package Roguelike.UI;

import java.util.LinkedList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.Array;

public class MessageStack extends Widget
{
	private final LinkedList<Line> messageList = new LinkedList<Line>();
	private final GlyphLayout layout = new GlyphLayout();
	
	private static BitmapFont font;
	static
	{
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("Sprites/GUI/stan0755.ttf"));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = 10;
		parameter.kerning = true;
		parameter.borderWidth = 1;
		parameter.borderColor = Color.BLACK;
		font = generator.generateFont(parameter); // font size 12 pixels
		generator.dispose(); // don't forget to dispose to avoid memory leaks!
	}
	
	public MessageStack()
	{
		
	}
	
	public void addLine(Line line)
	{
		messageList.addFirst(line);
	}
	
	@Override
	public void draw (Batch batch, float parentAlpha)
	{
		super.draw(batch, parentAlpha);
		batch.setColor(Color.WHITE);
				
		float y = getY();
		
		for (Line l : messageList)
		{
			float height = 1;
			
			float x = getX();
			
			for (Message m : l.messages)
			{
				font.setColor(m.colour);
				layout.setText(font, m.text);
				
				font.draw(batch, m.text, x, y);
				
				x += layout.width;
				height = layout.height;
			}
			
			y += height+5;
			
			if (y > getTop()) { break; }
		}
	}
	
	@Override
	public float getPrefWidth()
	{
		return 0;
	}

	@Override
	public float getPrefHeight()
	{
		return 0;
	}
	
	public static class Line
	{
		public Array<Message> messages = new Array<Message>();
		
		public Line(Message... messageArray)
		{
			messages.addAll(messageArray);
		}
	}
	
	public static class Message
	{
		String text;
		Color colour;
		
		public Message(String text)
		{
			this.text = text;
			this.colour = Color.WHITE;
		}
		
		public Message(String text, Color colour)
		{
			this.text = text;
			this.colour = colour;
		}
	}
}
