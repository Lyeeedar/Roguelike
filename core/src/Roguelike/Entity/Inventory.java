package Roguelike.Entity;

import java.util.HashMap;
import java.util.Iterator;

import Roguelike.AssetManager;
import Roguelike.Global.Statistics;
import Roguelike.RoguelikeGame;
import Roguelike.Items.Item;
import Roguelike.Items.Item.EquipmentSlot;
import Roguelike.Items.Item.ItemType;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public class Inventory
{
	public Array<Item> m_items = new Array<Item>();
	
	private HashMap<EquipmentSlot, Item> m_equipment = new HashMap<EquipmentSlot, Item>();
	
	public void load(Element xml)
	{
		Element equipElement = xml.getChildByName("Equipment");
		if (equipElement != null)
		{
			for (int i = 0; i < equipElement.getChildCount(); i++)
			{
				Element el = equipElement.getChild(i);
				
				Item item = Item.load(el.getText());
				addItem(item);
				equip(item);
			}
		}
		
		for (Element el : xml.getChildrenByName("Item"))
		{
			Item item = Item.load(el.getText());
			addItem(item);
		}
	}
	
	public void addItem(Item item)
	{
		if (m_items.contains(item, true))
		{
			item.Count++;
		}
		else
		{
			m_items.add(item);
		}
	}
	
	public void removeItem(Item item)
	{
		m_items.removeValue(item, true);
	}
	
	public int getStatistic(Statistics stat)
	{
		int val = 0;
		
		for (EquipmentSlot slot : EquipmentSlot.values())
		{
			if (m_equipment.containsKey(slot))
			{
				val += m_equipment.get(slot).getStatistic(stat);
			}
		}
		
		return val;
	}
	
	public void equip(Item item)
	{
		if (item.Slot == null) { return; }
		
		m_equipment.put(item.Slot, item);	
	}
	
	public void unequip(Item item)
	{
		m_equipment.remove(item.Slot);	
	}
	
	public void toggleEquip(Item item)
	{
		if (isEquipped(item))
		{
			unequip(item);
		}
		else
		{
			equip(item);
		}
	}
	
	public Item getEquip(EquipmentSlot slot)
	{
		return m_equipment.containsKey(slot) ? m_equipment.get(slot) : null;
	}
	
	public boolean isEquipped(Item item)
	{
		return m_equipment.get(item.Slot) == item;
	}
	
	public Iterator<Item> iterator(ItemType type)
	{
		return new ItemIterator(type, m_items.iterator());
	}
	
	private class ItemIterator implements Iterator<Item>
	{
		private ItemType type;
		private Iterator<Item> itr;
		private Item queued;
		
		public ItemIterator(ItemType type, Iterator<Item> itr)
		{
			this.type = type;
			this.itr = itr;
		}
		
		@Override
		public boolean hasNext()
		{
			while (itr.hasNext())
			{
				Item i = itr.next();
				
				if (type == ItemType.ALL || i.Type == type)
				{
					queued = i;
					break;
				}
			}
			
			return queued != null;
		}

		@Override
		public Item next()
		{
			Item temp = queued;
			queued = null;
			
			return temp;
		}

		@Override
		public void remove()
		{
		}
	}
}
