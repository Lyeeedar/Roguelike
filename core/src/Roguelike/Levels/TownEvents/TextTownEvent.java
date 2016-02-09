package Roguelike.Levels.TownEvents;

import Roguelike.Global;
import Roguelike.Screens.GameScreen;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;

import java.util.HashMap;

/**
 * Created by Philip on 09-Feb-16.
 */
public class TextTownEvent extends AbstractTownEvent
{
	String text;

	@Override
	public void evaluate( ObjectMap<String, String> flags )
	{
		Skin skin = Global.loadSkin();

		Table message = new Table();
		Label messageText = new Label( text, skin);
		messageText.setWrap( true );
		message.add( messageText ).expand().fill();
		message.row();
		TextButton continueButton = new TextButton( "Continue", skin );
		continueButton.addListener( new ClickListener(  )
		{
			public void clicked( InputEvent event, float x, float y )
			{
				GameScreen.Instance.lockContextMenu = false;
				GameScreen.Instance.clearContextMenu();
			}
		} );
		message.add( continueButton ).colspan( 2 ).expandX().fillX();
		message.row();

		GameScreen.Instance.queueContextMenu( message );
	}

	@Override
	public void parse( XmlReader.Element xml )
	{
		text = xml.getText();
	}
}
