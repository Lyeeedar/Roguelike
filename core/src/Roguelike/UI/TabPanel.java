package Roguelike.UI;

import Roguelike.AssetManager;
import Roguelike.Sprite.Sprite;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Array;

public class TabPanel extends WidgetGroup
{
	private final Array<Tab> tabs = new Array<Tab>();
	private Tab selectedTab = null;
	private final int tabHeaderSize = 24;
	
	private float prefWidth = 300;
	private float prefHeight = 300;
	
	private final Sprite buttonUp;
	private final Sprite buttonDown;
	
	public TabPanel()
	{
		this.buttonUp = AssetManager.loadSprite("GUI/ButtonUp");
		this.buttonDown = AssetManager.loadSprite("GUI/ButtonDown");
		addListener(new TabPanelListener());
	}
	
	@Override
	public void layout()
	{
		float bodyWidth = getWidth() - tabHeaderSize;
		float bodyHeight = getHeight();
		
		float xoffset = getX();
		float yoffset = getY();
		
		float y = getHeight() - tabHeaderSize;
		for (Tab tab : tabs)
		{
			y -= tabHeaderSize;
			
			tab.body.setBounds(xoffset+tabHeaderSize, yoffset, bodyWidth, bodyHeight);
		}
	}
	
	@Override
	public void draw (Batch batch, float parentAlpha)
	{
		super.draw(batch, parentAlpha);
		
		float xoffset = getX();
		float yoffset = getY();
		
		float y = getHeight() - tabHeaderSize;
		for (Tab tab : tabs)
		{
			if (tab == selectedTab)
			{
				buttonDown.render(batch, (int)xoffset, (int)(y+yoffset), tabHeaderSize, tabHeaderSize);
			}
			else
			{
				buttonUp.render(batch, (int)xoffset, (int)(y+yoffset), tabHeaderSize, tabHeaderSize);
			}
			
			tab.header.render(batch, (int)xoffset, (int)(y+yoffset), tabHeaderSize, tabHeaderSize);
			y -= tabHeaderSize;			
		}
	}
	
	public void focusCurrentTab(Stage stage)
	{
		stage.setScrollFocus(selectedTab.body);
	}
	
	public void addTab(Sprite header, Actor body)
	{
		final Tab tab = new Tab(header, body);
		tabs.add(tab);
		
		addActor(body);
		
		body.setVisible(false);
		
		selectTab(tab);
		
//		header.addListener(new InputListener()
//		{
//			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button)
//			{
//				selectTab(tab);
//				return true;
//			}
//		});
	}
	
	public void selectTab(Tab tab)
	{
		if (selectedTab != null)
		{
			selectedTab.body.setVisible(false);
		}
		
		selectedTab = tab;
		
		selectedTab.body.setVisible(true);
	}
	
	public float getPrefWidth()
	{
		return prefWidth;
	}

	public float getPrefHeight()
	{
		return prefHeight;
	}
	
	private class Tab
	{
		Sprite header;
		Actor body;
		
		public Tab(Sprite header, Actor body)
		{
			this.header = header;
			this.body = body;
		}
	}
	
	private class TabPanelListener extends InputListener
	{
		public boolean touchDown (InputEvent event, float x, float y, int pointer, int button)
		{
			if (x < tabHeaderSize)
			{
				y = getHeight() - y;
				
				int index = (int)Math.floor(y / tabHeaderSize);
				
				if (index < tabs.size)
				{
					Tab tab = tabs.get(index);
					selectTab(tab);
				}
			}
			
			return false;
		}
	}
}
