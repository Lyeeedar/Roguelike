package Roguelike.UI;

import MobiDevelop.UI.HorizontalFlowGroup;
import Roguelike.AssetManager;
import Roguelike.Sprite.Sprite;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;

public class TabPane extends WidgetGroup
{
	private final Array<Tab> tabs = new Array<Tab>();
	private Tab selectedTab = null;
	private final int tabHeaderSize = 24;
	
	private float prefWidth = 300;
	private float prefHeight = 300;
	
	private final Sprite border = AssetManager.loadSprite("GUI/frame");
	
	public TabPane()
	{
		
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
			tab.header.setBounds(xoffset, y+yoffset, tabHeaderSize, tabHeaderSize);
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
				
		float y = getHeight();
		for (Tab tab : tabs)
		{
			if (tab == selectedTab)
			{
				batch.setColor(Color.ORANGE);
				border.render(batch, (int)tab.header.getX()+tabHeaderSize/4, (int)tab.header.getY()+tabHeaderSize/4, (int)tab.header.getWidth(), (int)tab.header.getHeight());
				break;
			}
			
			y -= tabHeaderSize;
		}
	}
	
	public void focusCurrentTab(Stage stage)
	{
		stage.setScrollFocus(selectedTab.body);
	}
	
	public void addTab(Actor header, Actor body)
	{
		final Tab tab = new Tab(header, body);
		tabs.add(tab);
		
		addActor(header);
		addActor(body);
		
		body.setVisible(false);
		
		selectTab(tab);
		
		header.addListener(new InputListener()
		{
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button)
			{
				selectTab(tab);
				return true;
			}
		});
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
		Actor header;
		Actor body;
		
		public Tab(Actor header, Actor body)
		{
			this.header = header;
			this.body = body;
		}
	}
}
