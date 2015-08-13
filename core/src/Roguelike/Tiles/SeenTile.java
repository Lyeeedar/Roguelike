package Roguelike.Tiles;

import Roguelike.Global;
import Roguelike.Global.Direction;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.Sprite.AnimationState;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;

public class SeenTile
{
	public boolean seen = false;
	public Array<SeenHistoryItem> history = new Array<SeenHistoryItem>();
	public GameTile gameTile;
			
	public Table createTable(Skin skin)
	{
		Table table = new Table();
		
		if (gameTile.visible)
		{
			table.add(new Label("You see:", skin));
		}
		else
		{
			table.add(new Label("You remember seeing:", skin));
		}
		
		table.row();
		
		for (SeenHistoryItem shi : history)
		{
			Label l = new Label(shi.description, skin);
			l.setWrap(true);
			table.add(l).expand().left().width(com.badlogic.gdx.scenes.scene2d.ui.Value.percentWidth(1, table));
			table.row();
		}
		
		return table;
	}
	
	public static class SeenHistoryItem
	{
		public Sprite sprite;
		public AnimationState animationState = new AnimationState();
		public Direction location = Direction.CENTER;
		
		String description;
		float turn;
		
		public SeenHistoryItem()
		{
			
		}
		
		public SeenHistoryItem(Sprite sprite, String desc)
		{
			set(sprite, desc);
		}
		
		public SeenHistoryItem set(Sprite sprite, String desc)
		{
			this.sprite = sprite;
			this.animationState.set(sprite.animationState);
			this.description = desc;
			this.location = Direction.CENTER;
			
			turn = Global.AUT;
			
			return this;
		}
		
		public SeenHistoryItem copy()
		{
			SeenHistoryItem item = Pools.obtain(SeenHistoryItem.class);
			
			item.sprite = sprite;
			item.animationState = animationState;
			item.location = location;
			item.description = description;
			item.turn = turn;
			
			return item;
		}
	}
}
