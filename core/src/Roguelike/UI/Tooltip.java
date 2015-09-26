package Roguelike.UI;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class Tooltip extends Table
{
	private static Tooltip openTooltip;

	public Table Content;
	private TooltipStyle m_style;

	public Tooltip( Table Content, Skin skin, Stage stage )
	{
		super( skin );

		this.Content = Content;
		add( Content ).width( 200 );

		setVisible( false );

		stage.addActor( this );

		setStyle( skin.get( "default", TooltipStyle.class ) );

		pack();
	}

	public void setStyle( TooltipStyle style )
	{
		m_style = style;
		setBackground( m_style.background );
	}

	public void show( InputEvent event, float x, float y )
	{
		Vector2 tmp = new Vector2( x, y );
		event.getListenerActor().localToStageCoordinates( tmp );
		show( tmp.x, tmp.y );
	}

	public void show( float x, float y )
	{
		if ( openTooltip != null )
		{
			openTooltip.setVisible( false );
			openTooltip.remove();
			openTooltip = null;
		}

		setVisible( true );

		Vector2 tmp = new Vector2( x, y );
		tmp.add( 10, 10 );

		// Fit within stage

		if ( tmp.x < 0 )
		{
			tmp.x = 0;
		}

		if ( tmp.x + getWidth() > getStage().getWidth() )
		{
			tmp.x = getStage().getWidth() - getWidth();
		}

		if ( tmp.y < 0 )
		{
			tmp.y = 0;
		}

		if ( tmp.y + getHeight() > getStage().getHeight() )
		{
			tmp.y = getStage().getHeight() - getHeight();
		}

		setPosition( tmp.x, tmp.y );
		toFront();

		// openTooltip = this;
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
