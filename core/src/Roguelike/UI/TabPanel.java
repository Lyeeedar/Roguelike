package Roguelike.UI;

import Roguelike.AssetManager;
import Roguelike.Screens.GameScreen;
import Roguelike.Sprite.Sprite;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.Array;

public class TabPanel extends Widget
{
	private final Array<Tab> tabs = new Array<Tab>();
	private Tab selectedTab = null;
	private final int tabHeaderSize = 32;

	private final Sprite buttonUp;
	private final Sprite buttonDown;
	private final Sprite buttonBorder;

	public TabPanel()
	{
		this.buttonUp = AssetManager.loadSprite( "GUI/Button" );
		this.buttonDown = AssetManager.loadSprite( "GUI/ButtonDown" );
		this.buttonBorder = AssetManager.loadSprite( "GUI/ButtonBorder" );
		addListener( new TabPanelListener() );
	}

	@Override
	public void layout()
	{
		for ( Tab tab : tabs )
		{
			tab.body.setBounds( getX() + tabHeaderSize, getY(), getWidth(), getHeight() );
		}
	}

	@Override
	public void draw( Batch batch, float parentAlpha )
	{
		super.draw( batch, parentAlpha );

		float xoffset = getX();
		float yoffset = getY();

		float y = getHeight() - tabHeaderSize;

		selectedTab.body.draw( batch, parentAlpha );

		batch.setColor( Color.WHITE );

		for ( Tab tab : tabs )
		{
			if ( tab == selectedTab )
			{
				batch.setColor( Color.WHITE );
				buttonDown.render( batch, (int) xoffset, (int) ( y + yoffset ), tabHeaderSize, tabHeaderSize );
			}
			else
			{
				batch.setColor( Color.LIGHT_GRAY );
				buttonUp.render( batch, (int) xoffset, (int) ( y + yoffset ), tabHeaderSize, tabHeaderSize );
			}

			tab.header.render( batch, (int) xoffset, (int) ( y + yoffset ), tabHeaderSize, tabHeaderSize );

			buttonBorder.render( batch, (int) xoffset, (int) ( y + yoffset ), tabHeaderSize, tabHeaderSize );

			y -= tabHeaderSize;
		}
	}

	public void focusCurrentTab( Stage stage )
	{
		stage.setScrollFocus( selectedTab.body );
	}

	public Tab addTab( Sprite header, Actor body )
	{
		final Tab tab = new Tab( header, body );
		tabs.add( tab );

		body.setVisible( false );

		selectTab( tab );

		return tab;
	}

	public void selectTab( Tab tab )
	{
		if ( selectedTab != null )
		{
			selectedTab.body.setVisible( false );
		}

		selectedTab = tab;

		selectedTab.body.setVisible( true );
	}

	public void selectTab( Actor body )
	{
		for ( Tab tab : tabs )
		{
			if ( tab.body == body )
			{
				selectTab( tab );
				break;
			}
		}
	}

	public void toggleTab( Actor body )
	{
		if ( selectedTab.body == body )
		{
			selectTab( tabs.get( 0 ) );
		}
		else
		{
			selectTab( body );
		}
	}

	@Override
	public float getPrefWidth()
	{
		return tabHeaderSize + selectedTab.body.getWidth();
	}

	@Override
	public float getPrefHeight()
	{
		return Math.max( getHeight(), selectedTab.body.getHeight() );
	}

	public class Tab
	{
		Sprite header;
		Actor body;

		public Tab( Sprite header, Actor body )
		{
			this.header = header;
			this.body = body;
		}
	}

	private class TabPanelListener extends InputListener
	{
		@Override
		public boolean touchDown( InputEvent event, float x, float y, int pointer, int button )
		{
			GameScreen.Instance.clearContextMenu( false );

			if ( x < tabHeaderSize )
			{
				y = getHeight() - y;

				int index = (int) Math.floor( y / tabHeaderSize );

				if ( index < tabs.size )
				{
					Tab tab = tabs.get( index );
					selectTab( tab );
				}

				return true;
			}

			return false;
		}
	}
}
