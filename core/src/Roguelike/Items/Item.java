package Roguelike.Items;

import java.io.IOException;
import java.util.EnumMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

import Roguelike.AssetManager;
import Roguelike.Entity.Inventory;
import Roguelike.GameEvent.GameEventHandler;
import Roguelike.GameEvent.Constant.ConstantEvent;
import Roguelike.Lights.Light;
import Roguelike.Sprite.Sprite;
import Roguelike.Global.Statistics;
import Roguelike.Global.Tier1Element;

public class Item extends GameEventHandler
{
	//----------------------------------------------------------------------
	public enum WeaponType
	{
		NONE("strike/strike"),
		SWORD("slash/slash"),
		SPEAR("thrust/thrust"),
		AXE("slash/slash"),
		BOW("arrow"),
		WAND("bolt");
		
		public final Sprite hitSprite;
		
		private WeaponType(String hit)
		{
			hitSprite = hit != null ? AssetManager.loadSprite(hit, 0.1f) : null;
		}
	}
	
	//----------------------------------------------------------------------
	public enum EquipmentSlot
	{
		// Armour
		HEAD,
		BODY,
		LEGS,
		
		// Jewelry
		EARRING,
		NECKLACE,
		RING,
		
		// Other
		LANTERN,
		
		// Weapons
		MAINWEAPON,
		OFFWEAPON
	}
	
	//----------------------------------------------------------------------
	public enum ItemType
	{
		ARMOUR,
		WEAPON,
		JEWELRY,
		TREASURE,
		MISC,
		
		ALL
	}
	
	//----------------------------------------------------------------------
	private Item()
	{
		
	}
	
	public String name;
	public String description;
	private Sprite icon;
	public Sprite hitEffect;
	public WeaponType weaponType = WeaponType.NONE;
	public EquipmentSlot slot;
	public ItemType type;
	public int count;
	public Light light;
	public boolean canDrop = true;
	
	//----------------------------------------------------------------------
	public static Item generateRandomItem()
	{
		Item item = new Item();
		
		if (MathUtils.randomBoolean())
		{
			// Generate weapon
			item.type = ItemType.WEAPON;
			item.slot = EquipmentSlot.MAINWEAPON;
			
			int type = MathUtils.random(5);
			
			if (type == 0) { item.weaponType = WeaponType.SWORD; }
			else if (type == 1) { item.weaponType = WeaponType.SPEAR; }
			else if (type == 2) { item.weaponType = WeaponType.AXE; }
			else if (type == 3) { item.weaponType = WeaponType.BOW; }
			else if (type == 4) { item.weaponType = WeaponType.WAND; }
			
			ConstantEvent stats = new ConstantEvent();
			item.constantEvent = stats;
			
			for (Tier1Element el : Tier1Element.values())
			{
				stats.putStatistic(el.Attack, ""+MathUtils.random(150));
				stats.putStatistic(el.Pierce, ""+MathUtils.random(20));
			}
			
			item.name = item.weaponType.toString().toLowerCase();
		}
		else
		{
			// Generate armour
			item.type = ItemType.ARMOUR;
			
			int type = MathUtils.random(3);
			
			if (type == 0) { item.slot = EquipmentSlot.HEAD; }
			else if (type == 1) { item.slot = EquipmentSlot.BODY; }
			else if (type == 2) { item.slot = EquipmentSlot.LEGS; }
			
			ConstantEvent stats = new ConstantEvent();
			item.constantEvent = stats;
			
			for (Tier1Element el : Tier1Element.values())
			{
				stats.putStatistic(el.Defense, ""+MathUtils.random(20));
				stats.putStatistic(el.Hardiness, ""+MathUtils.random(50));
			}
			
			item.name = item.type.toString().toLowerCase();
		}
		
		return item;
	}
	
	//----------------------------------------------------------------------
	public Texture getEquipTexture()
	{
		if (slot == EquipmentSlot.MAINWEAPON)
		{
			if (weaponType == WeaponType.SWORD)
			{
				return AssetManager.loadTexture("Sprites/player/hand1/sword2.png");
			}
			else if (weaponType == WeaponType.SPEAR)
			{
				return AssetManager.loadTexture("Sprites/player/hand1/spear1.png");
			}
			else if (weaponType == WeaponType.AXE)
			{
				return AssetManager.loadTexture("Sprites/player/hand1/axe.png");
			}
			else if (weaponType == WeaponType.BOW)
			{
				return AssetManager.loadTexture("Sprites/player/hand1/bow.png");
			}
			else if (weaponType == WeaponType.WAND)
			{
				return AssetManager.loadTexture("Sprites/player/hand1/sceptre.png");
			}
		}
		else if (slot == EquipmentSlot.OFFWEAPON)
		{
			return AssetManager.loadTexture("Sprites/player/hand2/shield_round1.png");
		}
		else if (slot == EquipmentSlot.HEAD)
		{
			return AssetManager.loadTexture("Sprites/player/head/chain.png");
		}
		else if (slot == EquipmentSlot.BODY)
		{
			return AssetManager.loadTexture("Sprites/player/body/chainmail3.png");
		}
		else if (slot == EquipmentSlot.LEGS)
		{
			return AssetManager.loadTexture("Sprites/player/legs/leg_armor02.png");
		}
		
		return AssetManager.loadTexture("Sprites/blank.png");
	}
	
	//----------------------------------------------------------------------
	public Table createTable(Skin skin, Inventory inventory)
	{
		Table table = new Table();
		
		table.add(new Label(name, skin)).expandX().left();
		table.row();
		
		Label descLabel = new Label(description, skin);
		descLabel.setWrap(true);
		table.add(descLabel).expand().left().width(com.badlogic.gdx.scenes.scene2d.ui.Value.percentWidth(1, table));
		table.row();
		
		if (slot != null)
		{
			Item other = inventory.getEquip(slot);
			
			if (other != null)
			{
				for (Statistics stat : Statistics.values())
				{
					int oldval = 1;//other.getStatistic(stat);
					int newval = 1;//getStatistic(stat);
					
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
					int val = 1;//getStatistic(stat);
					
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
	public static Item load(Element xml)
	{
		Item item = new Item();

		item.internalLoad(xml);

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
		
		internalLoad(xmlElement);
	}
		
	//----------------------------------------------------------------------
	private void internalLoad(Element xmlElement)
	{
		
		String extendsElement = xmlElement.getAttribute("Extends", null);
		if (extendsElement != null)
		{
			internalLoad(extendsElement);
		}
		
		name = xmlElement.get("Name", name);		
		description = xmlElement.get("Description",description);
		
		Element iconElement = xmlElement.getChildByName("Icon");	
		if (iconElement != null)
		{
			icon = AssetManager.loadSprite(iconElement);
		}
		
		Element hitElement = xmlElement.getChildByName("HitEffect");
		if (hitElement != null)
		{
			hitEffect = AssetManager.loadSprite(hitElement);
		}
		
		Element eventsElement = xmlElement.getChildByName("Events");
		if (eventsElement != null)
		{
			super.parse(eventsElement);
		}
		
		if (xmlElement.getChildByName("Light") != null) { light = Light.load(xmlElement.getChildByName("Light")); }
		
		slot = xmlElement.get("Slot", null) != null ? EquipmentSlot.valueOf(xmlElement.get("Slot").toUpperCase()) : slot;
		type = xmlElement.get("Type", null) != null ? ItemType.valueOf(xmlElement.get("Type").toUpperCase()) : type;
		
		weaponType = xmlElement.get("WeaponType", null) != null ? WeaponType.valueOf(xmlElement.get("WeaponType").toUpperCase()) : weaponType;
	}
	
	//----------------------------------------------------------------------
	@Override
	public String getName()
	{
		return name;
	}

	//----------------------------------------------------------------------
	@Override
	public String getDescription()
	{
		return description;
	}

	//----------------------------------------------------------------------
	@Override
	public Sprite getIcon()
	{
		if (icon != null)
		{
			return icon;
		}
		
		if (slot == EquipmentSlot.MAINWEAPON)
		{
			if (weaponType == WeaponType.SWORD)
			{
				return AssetManager.loadSprite("GUI/Sword");
			}
			else if (weaponType == WeaponType.SPEAR)
			{
				return AssetManager.loadSprite("GUI/Spear");
			}
			else if (weaponType == WeaponType.AXE)
			{
				return AssetManager.loadSprite("GUI/Axe");
			}
			else if (weaponType == WeaponType.BOW)
			{
				return AssetManager.loadSprite("GUI/Bow");
			}
			else if (weaponType == WeaponType.WAND)
			{
				return AssetManager.loadSprite("GUI/Wand");
			}
		}
		else if (slot == EquipmentSlot.HEAD)
		{
			return AssetManager.loadSprite("GUI/Head");
		}
		else if (slot == EquipmentSlot.BODY)
		{
			return AssetManager.loadSprite("GUI/Body");
		}
		else if (slot == EquipmentSlot.LEGS)
		{
			return AssetManager.loadSprite("GUI/Legs");
		}
		
		return AssetManager.loadSprite("white");
	}
}
