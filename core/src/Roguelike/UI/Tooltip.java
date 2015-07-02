package Roguelike.UI;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class Tooltip extends Table
{
	public Table Content;
	private TooltipStyle m_style;
	
	public Tooltip(Table Content, Skin skin, Stage stage)
	{
		super(skin);
		
		setWidth(200);
				
		this.Content = Content;
		add(Content).width(Value.percentWidth(1, this));
		
		setVisible(false);
		
		stage.addActor(this);
		
		setStyle(skin.get("default", TooltipStyle.class));
		
		pack();
	}
	
	public void setStyle(TooltipStyle style)
	{
		m_style = style;
		Content.setBackground(m_style.background);
	}
	
	public void show(InputEvent event, float x, float y)
	{
		setVisible(true);
		
		Vector2 tmp = new Vector2(x, y);
		event.getListenerActor().localToStageCoordinates(tmp);
		tmp.add(10, 10);
		
		// Fit within stage
		
		if (tmp.x + getWidth() > getStage().getWidth())
		{
			tmp.x = getStage().getWidth() - getWidth();
		}
		
		if (tmp.y + getHeight()/2 > getStage().getHeight())
		{
			tmp.y = getStage().getHeight() - getHeight()/2;
		}
		
		if (tmp.y - getHeight()/2 < 0)
		{
			tmp.y = getHeight()/2;
		}
		
		setPosition(tmp.x, tmp.y);
		toFront();
	}
	
	public static class TooltipStyle
	{

		/** Optional */
		public Drawable background;

		public TooltipStyle()
		{

		}

	}
}
