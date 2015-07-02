package Roguelike.Items;

import java.io.IOException;
import java.util.EnumMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

import Roguelike.AssetManager;
import Roguelike.Entity.Inventory;
import Roguelike.Sprite.Sprite;
import Roguelike.Global.Statistics;

public class Item
{
	public enum EquipmentSlot
	{
		// Armour
		HEAD,
		BODY,
		FEET,
		HANDS,
		
		// Jewelry
		EARRING,
		NECKLACE,
		RING,
		
		// Weapons
		MAINWEAPON,
		OFFWEAPON
	}
	
	public enum ItemType
	{
		ARMOUR,
		WEAPON,
		JEWELRY,
		CONSUMABLE,
		BOOK,
		TREASURE,
		MISC,
		
		ALL
	}
	
	private Item()
	{
		
	}
	
	public String Name;
	public String Description;
	public Sprite Icon;
	public Sprite HitEffect;
	public EquipmentSlot Slot;
	public ItemType Type;
	public int Count;
	public WordList Words;
	
	private EnumMap<Statistics, Integer> m_statistics = Statistics.getStatisticsBlock();
	
	//----------------------------------------------------------------------
	public int getStatistic(Statistics stat)
	{
		return m_statistics.get(stat);
	}
	
	//----------------------------------------------------------------------
	public Table createTable(Skin skin, Inventory inventory)
	{
		Table table = new Table();
		
		table.add(new Label(Name, skin)).expandX().left();
		table.row();
		
		Label descLabel = new Label(Description, skin);
		descLabel.setWrap(true);
		table.add(descLabel).expand().left().width(com.badlogic.gdx.scenes.scene2d.ui.Value.percentWidth(1, table));
		table.row();
		
		if (Slot != null)
		{
			Item other = inventory.getEquip(Slot);
			
			if (other != null)
			{
				for (Statistics stat : Statistics.values())
				{
					int oldval = other.m_statistics.get(stat);
					int newval = m_statistics.get(stat);
					
					if (oldval != 0 || newval != 0)
					{

						Table row = new Table();
						table.add(row).expandX().left();
						table.row();
						
						row.add(new Label(stat.toString() + ": " + oldval + " -> ", skin));
						Label nval = new Label("" + newval, skin);
						
						if (newval < oldval)
						{
							nval.setColor(Color.RED);
						}
						else if (newval > oldval)
						{
							nval.setColor(Color.GREEN);
						}
						
						
						row.add(nval);
					}
				}
			}
			else
			{
				for (Statistics stat : Statistics.values())
				{
					int val = m_statistics.get(stat);
					
					if (val != 0)
					{
						Table row = new Table();
						table.add(row).expandX().left();
						table.row();
						
						row.add(new Label(stat.toString() + ": 0 -> ", skin));
						Label nval = new Label(""+val, skin);
						nval.setColor(Color.GREEN);
						row.add(nval);
					}
				}
			}
		}
		
		return table;
	}
	
	//----------------------------------------------------------------------
	public static Item load(String name)
	{
		Item item = new Item();
		
		item.internalLoad(name);
		
		return item;
	}
	
	//----------------------------------------------------------------------
	private void internalLoad(String name)
	{
		XmlReader xml = new XmlReader();
		Element xmlElement = null;
		
		try
		{
			xmlElement = xml.parse(Gdx.files.internal("Items/"+name+".xml"));
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		String extendsElement = xmlElement.getAttribute("Extends", null);
		if (extendsElement != null)
		{
			internalLoad(extendsElement);
		}
		
		Name = xmlElement.get("Name", Name);		
		Description = xmlElement.get("Description",Description);
		
		Element statElement = xmlElement.getChildByName("Statistics");
		if (statElement != null)
		{
			Statistics.load(statElement, m_statistics);
		}
		
		Element iconElement = xmlElement.getChildByName("Icon");	
		if (iconElement != null)
		{
			Icon = AssetManager.loadSprite(iconElement);
		}
		
		Element hitElement = xmlElement.getChildByName("HitEffect");
		if (hitElement != null)
		{
			HitEffect = AssetManager.loadSprite(hitElement);
			
			if (hitElement.get("WordList", null) != null)
			{
				Words = WordList.loadWordList(hitElement.get("WordList"));
			}
		}
		
		Slot = xmlElement.get("Slot", null) != null ? EquipmentSlot.valueOf(xmlElement.get("Slot").toUpperCase()) : Slot;
		Type = xmlElement.get("Type", null) != null ? ItemType.valueOf(xmlElement.get("Type").toUpperCase()) : Type;
	}
}
