package Roguelike.Items;

import java.util.HashMap;
import java.util.Iterator;

import Roguelike.Global.Statistic;
import Roguelike.Items.Item.EquipmentSlot;
import Roguelike.Items.Item.ItemCategory;
import Roguelike.Util.FastEnumMap;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public class Inventory
{
	public boolean isVariableMapDirty = true;

	public Array<Item> m_items = new Array<Item>();

	private FastEnumMap<EquipmentSlot, Item> m_equipment = new FastEnumMap<EquipmentSlot, Item>( EquipmentSlot.class );

	public void load( Element xml )
	{
		Element equipElement = xml.getChildByName( "Equipment" );
		if ( equipElement != null )
		{
			for ( int i = 0; i < equipElement.getChildCount(); i++ )
			{
				Element el = equipElement.getChild( i );

				Item item = null;
				if ( el.getChildCount() > 0 )
				{
					item = Item.load( el );
				}
				else
				{
					item = Item.load( el.getText() );
				}

				item.canDrop = el.getBooleanAttribute( "Drop", true );
				item.dropChanceEqn = el.getAttribute( "DropChance", null );

				addItem( item );
				equip( item );
			}
		}

		for ( Element el : xml.getChildrenByName( "Item" ) )
		{
			Item item = null;

			if ( el.getChildCount() > 0 )
			{
				item = Item.load( el );
			}
			else
			{
				Item.load( el.getText() );
			}

			item.canDrop = el.getBooleanAttribute( "Drop", true );
			item.dropChanceEqn = el.getAttribute( "DropChance", null );

			addItem( item );
		}
	}

	public void addItem( Item item )
	{
		if ( m_items.contains( item, true ) )
		{
			item.count++;
		}
		else
		{
			m_items.add( item );
		}
	}

	public void removeItem( Item item )
	{
		m_items.removeValue( item, true );
	}

	public int getStatistic( HashMap<String, Integer> variableMap, Statistic stat )
	{
		int val = 0;

		for ( EquipmentSlot slot : EquipmentSlot.values() )
		{
			if ( m_equipment.containsKey( slot ) )
			{
				Item item = m_equipment.get( slot );
				if ( slot == item.getMainSlot() )
				{
					val += item.getStatistic( variableMap, stat );
				}
			}
		}

		return val;
	}

	public void equip( Item item )
	{
		for ( EquipmentSlot slot : item.slots )
		{
			if ( m_equipment.containsKey( slot ) )
			{
				unequip( m_equipment.get( slot ) );
			}
		}

		for ( EquipmentSlot slot : item.slots )
		{
			m_equipment.put( slot, item );
			isVariableMapDirty = true;
		}
	}

	public void unequip( Item item )
	{
		for ( EquipmentSlot slot : item.slots )
		{
			m_equipment.remove( slot );
			isVariableMapDirty = true;
		}
	}

	public void toggleEquip( Item item )
	{
		if ( isEquipped( item ) )
		{
			unequip( item );
		}
		else
		{
			equip( item );
		}
	}

	public Item getEquip( EquipmentSlot slot )
	{
		Item item = m_equipment.get( slot );
		if ( item != null )
		{
			if ( slot == item.getMainSlot() ) { return item; }
		}

		return null;
	}

	public boolean isEquipped( Item item )
	{
		return m_equipment.get( item.getMainSlot() ) == item;
	}

	public Iterator<Item> iterator( ItemCategory type )
	{
		return new ItemIterator( type, m_items.iterator() );
	}

	private class ItemIterator implements Iterator<Item>
	{
		private ItemCategory type;
		private Iterator<Item> itr;
		private Item queued;

		public ItemIterator( ItemCategory type, Iterator<Item> itr )
		{
			this.type = type;
			this.itr = itr;
		}

		@Override
		public boolean hasNext()
		{
			while ( itr.hasNext() )
			{
				Item i = itr.next();

				if ( type == ItemCategory.ALL || i.category == type )
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
