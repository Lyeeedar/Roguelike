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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.sun.xml.internal.ws.util.StringUtils;

import Roguelike.AssetManager;
import Roguelike.Entity.GameEntity;
import Roguelike.Entity.Inventory;
import Roguelike.GameEvent.GameEventHandler;
import Roguelike.GameEvent.Constant.ConstantEvent;
import Roguelike.Lights.Light;
import Roguelike.Sprite.Sprite;
import Roguelike.Global.Statistic;
import Roguelike.Global.Tier1Element;

public class Item extends GameEventHandler
{
	/*
	IDEAS:
	
	Unlock extra power after condition (absorb x essence, kill x enemy)
	
	
	 */
	
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
		MATERIAL,
		MISC,
		
		ALL
	}
	
	//----------------------------------------------------------------------
	public enum MaterialType
	{
		FABRIC("GUI/Fabric"),
		HIDE("GUI/Hide"),
		LEATHER("GUI/Leather"),
		ORE("GUI/Ore"),
		INGOT("GUI/Ingot"),
		LOG("GUI/Log"),
		PLANK("GUI/Plank"),
		BONE("GUI/Bone"),
		CLAW("GUI/Claw"),
		FANG("GUI/Fang"),
		SCALE("GUI/Scale"),
		FEATHER("GUI/Feather"),
		SHELL("GUI/Shell"),
		VIAL("GUI/Vial"),
		SAC("GUI/Sac"),
		POWDER("GUI/Powder"),
		CRYSTAL("GUI/Crystal"),
		GEM("GUI/Gem");
		
		public Sprite icon;
		
		private MaterialType(String path)
		{
			icon = AssetManager.loadSprite(path);
		}
	}
	
	//----------------------------------------------------------------------
	public Item()
	{
		
	}
	
	public String name = "";
	public String description = "";
	public Sprite icon;
	public Sprite hitEffect;
	
	public WeaponType weaponType = WeaponType.NONE;
	public EquipmentSlot slot;
	public ItemType itemType;
	public MaterialType materialType;
	
	public int count;
	public Light light;
	public boolean canDrop = true;
	public EnumMap<Tier1Element, Integer> elementalStats = Tier1Element.getElementBlock();
		
	//----------------------------------------------------------------------
	public static Item generateRandomItem()
	{
		Recipe recipe = Recipe.getRandomRecipe();
		
		int numMats = recipe.slots.length;
		Item[] materials = new Item[numMats];
		Item mat = Recipe.generateMaterial((int)(MathUtils.randomTriangular(0.5f, 1.5f)*150));
		
		for (int i = 0; i < numMats; i++)
		{
			materials[i] = mat;
		}
		
		return recipe.generate(materials);
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
	public Table createTable(Skin skin, GameEntity entity)
	{
		Inventory inventory = entity.getInventory();
		
		if (slot == EquipmentSlot.MAINWEAPON)
		{
			Item other = inventory.getEquip(slot);
			
			return createWeaponTable(other, entity, skin);
		}
		
		Table table = new Table();
		
		table.add(new Label(getName(), skin)).expandX().left();
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
				for (Statistic stat : Statistic.values())
				{
					int oldval = other.getStatistic(entity, stat);
					int newval = getStatistic(entity, stat);
					
					if (oldval != 0 || newval != 0)
					{

						Table row = new Table();
						table.add(row).expandX().left();
						table.row();
						
						row.add(new Label(Statistic.formatString(stat.toString()) + ": " + oldval + " -> ", skin));
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
				for (Statistic stat : Statistic.values())
				{
					int val = getStatistic(entity, stat);
					
					if (val != 0)
					{
						Table row = new Table();
						table.add(row).expandX().left();
						table.row();
						
						row.add(new Label(Statistic.formatString(stat.toString()) + ": 0 -> ", skin));
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
	private Table createWeaponTable(Item other, GameEntity entity, Skin skin)
	{
		Table table = new Table();
		
		table.add(new Label(name, skin)).expandX().left();
		
		{
			Label label = new Label(StringUtils.capitalize(weaponType.toString().toLowerCase()), skin);
			label.setFontScale(0.7f);
			table.add(label).expandX().right();
		}
		
		table.row();
		
		Label descLabel = new Label(description, skin);
		descLabel.setWrap(true);
		table.add(descLabel).expand().left().width(com.badlogic.gdx.scenes.scene2d.ui.Value.percentWidth(1, table));
		table.row();
		
		int oldDam = 0;
		int newDam = 0;
		for (Tier1Element el : Tier1Element.values())
		{
			int oldval = other == null ? 0 : other.getStatistic(entity, el.Attack);
			int newval = getStatistic(entity, el.Attack);
			
			oldDam += oldval;
			newDam += newval;
		}
		
		String damText = "Damage: " + newDam;
		if (newDam != oldDam)
		{
			int diff = newDam - oldDam;
			
			if (diff > 0)
			{
				damText += "   [GREEN]+"+diff;
			}
			else
			{
				damText += "   [RED]"+diff;
			}
		}
		
		table.add(new Label(damText, skin)).expandX().left();
		table.row();
		
		for (Statistic stat : Statistic.values())
		{
			int oldval = other == null ? 0 : other.getStatistic(entity, stat);
			int newval = getStatistic(entity, stat);
			
			if (oldval != 0 || newval != 0)
			{
				String statText = Statistic.formatString(stat.toString()) + ": " + newval;
				
				if (newval != oldval)
				{
					int diff = newval - oldval;
					
					if (diff > 0)
					{
						damText += "   [GREEN]+"+diff;
					}
					else
					{
						damText += "   [RED]"+diff;
					}
				}
				
				table.add(new Label(statText, skin)).expandX().left();
				table.row();
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
		
		Element elementElement = xmlElement.getChildByName("Elements");
		if (elementElement != null)
		{
			Tier1Element.load(elementElement, elementalStats);
		}
		
		Element lightElement = xmlElement.getChildByName("Light");
		if (lightElement != null) 
		{ 
			light = Light.load(lightElement); 
		}
		
		slot = xmlElement.get("Slot", null) != null ? EquipmentSlot.valueOf(xmlElement.get("Slot").toUpperCase()) : slot;
		itemType = xmlElement.get("Type", null) != null ? ItemType.valueOf(xmlElement.get("Type").toUpperCase()) : itemType;		
		weaponType = xmlElement.get("WeaponType", null) != null ? WeaponType.valueOf(xmlElement.get("WeaponType").toUpperCase()) : weaponType;
		materialType = xmlElement.get("MaterialType", null) != null ? MaterialType.valueOf(xmlElement.get("MaterialType").toUpperCase()) : materialType;
	}
	
	//----------------------------------------------------------------------
	@Override
	public String getName()
	{
		if (itemType == ItemType.MATERIAL)
		{
			return name + " " + StringUtils.capitalize(materialType.toString().toLowerCase());
		}
		
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
		else if (itemType == ItemType.MATERIAL)
		{
			return materialType.icon;
		}
		
		return AssetManager.loadSprite("white");
	}
}
