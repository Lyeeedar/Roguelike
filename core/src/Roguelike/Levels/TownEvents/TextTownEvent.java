package Roguelike.Levels.TownEvents;

import Roguelike.Global;
import Roguelike.Screens.GameScreen;
import Roguelike.UI.ButtonKeyboardHelper;
import Roguelike.UI.Seperator;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;

import java.util.HashMap;

/**
 * Created by Philip on 09-Feb-16.
 */
public class TextTownEvent extends AbstractTownEvent
{
	String text;
	String reward;

	@Override
	public void evaluate( ObjectMap<String, String> flags )
	{
		Skin skin = Global.loadSkin();

		Table message = new Table();
		message.defaults().pad( 10 );

		Label title = new Label("Event", skin, "title");
		message.add( title ).expandX().left();
		message.row();

		message.add( new Seperator( skin ) ).expandX().fillX();
		message.row();

		Table messageBody = new Table();
		Label messageText = new Label( text, skin);
		messageText.setWrap( true );
		messageBody.add( messageText ).expand().fillX();
		messageBody.row();

		if (reward != null)
		{
			Label rewardText = new Label( reward, skin );
			rewardText.setColor( Color.GOLD );
			rewardText.setWrap( true );
			rewardText.setAlignment( Align.center );
			messageBody.add( rewardText ).expand().fillX();
			messageBody.row();
		}

		message.add( messageBody ).expand().fill();
		message.row();

		message.add( new Seperator( skin ) ).expandX().fillX();
		message.row();

		TextButton continueButton = new TextButton( "Continue", skin );
		continueButton.addListener( new ClickListener(  )
		{
			public void clicked( InputEvent event, float x, float y )
			{
				GameScreen.Instance.clearContextMenu( true );
			}
		} );
		message.add( continueButton ).colspan( 2 ).expandX().fillX();
		message.row();

		GameScreen.Instance.queueContextMenu( message, new ButtonKeyboardHelper( continueButton ) );
	}

	@Override
	public void parse( XmlReader.Element xml )
	{
		text = xml.getText();
		reward = xml.getAttribute( "RewardMessage", null );
	}
}
