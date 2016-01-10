package Roguelike.UI;

import Roguelike.Entity.Entity;
import Roguelike.Global;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;

/**
 * Created by Philip on 10-Jan-16.
 */
public class Message
{
	public String message;
	public Color colour;
	public Widget attachedWidget;

	public Message( String message, Color colour )
	{
		this.message = message;
		this.colour = colour;
	}

	public void add( Stage stage, Skin skin, Entity entity )
	{
		int offsetx = Global.Resolution[ 0 ] / 2 - Global.CurrentLevel.player.tile[ 0 ][ 0 ].x * Global.TileSize;
		int offsety = Global.Resolution[ 1 ] / 2 - Global.CurrentLevel.player.tile[ 0 ][ 0 ].y * Global.TileSize;

		int x = entity.tile[ 0 ][ 0 ].x;
		int y = entity.tile[ 0 ][ 0 ].y;

		int cx = x * Global.TileSize + offsetx;
		int cy = y * Global.TileSize + offsety;

		Label label = new Label( message, skin );
		label.setColor( colour );

		label.addAction( new SequenceAction( Actions.moveTo( cx, cy + Global.TileSize / 2 + Global.TileSize / 2, 0.5f ), Actions.removeActor() ) );
		label.setPosition( cx, cy + Global.TileSize / 2 );
		stage.addActor( label );
		label.setVisible( true );

		attachedWidget = label;
	}

	public void remove()
	{
		attachedWidget.remove();
	}
}
