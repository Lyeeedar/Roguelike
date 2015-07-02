package Roguelike.Tiles;

import Roguelike.Sprite.Sprite;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;

public class SeenTile
{
	public boolean seen = false;
	public Array<SeenHistoryItem> History = new Array<SeenHistoryItem>();
	
	public SeenHistoryItem CeilingItem;
	
	public GameTile GameTile;
	
	public Table createTable(Skin skin)
	{
		Table table = new Table();
		
		if (GameTile.GetVisible())
		{
			table.add(new Label("You see:", skin));
		}
		else
		{
			table.add(new Label("You remember seeing:", skin));
		}
		
		table.row();
		
		for (SeenHistoryItem shi : History)
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
		public int spriteIndex;
		
		String description;
		int turn;
		
		public SeenHistoryItem(Sprite sprite, String desc)
		{
			this.sprite = sprite;
			this.spriteIndex = sprite.currentTexture;
			this.description = desc;
		}
	}
}
