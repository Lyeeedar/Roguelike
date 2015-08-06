package Roguelike.Items;

import java.io.IOException;
import java.util.EnumMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

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

import exp4j.Helpers.EquationHelper;
import Roguelike.AssetManager;
import Roguelike.Entity.GameEntity;
import Roguelike.GameEvent.GameEventHandler;
import Roguelike.GameEvent.Constant.ConstantEvent;
import Roguelike.Lights.Light;
import Roguelike.Sound.SoundInstance;
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
		NONE("strike/strike", "knife_stab"),
		SWORD("slash/slash", "knife_stab"),
		SPEAR("thrust/thrust", "knife_stab"),
		AXE("slash/slash", "knife_stab"),
		BOW("arrow", "arrow_approaching_and_hitting_target"),
		WAND("bolt", "arrow_approaching_and_hitting_target");

		public final Sprite hitSprite;

		private WeaponType(String hit)
		{
			hitSprite = hit != null ? AssetManager.loadSprite(hit, 0.1f) : null;
		}
		
		private WeaponType(String hit, String sound)
		{
			hitSprite = hit != null ? AssetManager.loadSprite(hit, 0.1f) : null;
			hitSprite.sound = new SoundInstance(AssetManager.loadSound(sound));
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
		FABRIC("GUI/Fabric", false, true),
		HIDE("GUI/Hide", false, true),
		LEATHER("GUI/Leather", false, true),
		ORE("GUI/Ore", true, true),
		INGOT("GUI/Ingot", true, true),
		LOG("GUI/Log", true, true),
		PLANK("GUI/Plank", true, true),
		BONE("GUI/Bone", true, true),
		CLAW("GUI/Claw", true, false),
		FANG("GUI/Fang", true, false),
		SPINE("GUI/Spine", true, false),
		SCALE("GUI/Scale", true, true),
		FEATHER("GUI/Feather", false, true),
		SHELL("GUI/Shell", true, true),
		VIAL("GUI/Vial", true, false),
		SAC("GUI/Sac", true, false),
		POWDER("GUI/Powder", true, true),
		CRYSTAL("GUI/Crystal", true, true),
		GEM("GUI/Gem", true, true);

		public final Sprite icon;
		public final boolean suitableForWeapon;
		public final boolean suitableForArmour;

		private MaterialType(String path, boolean suitableForWeapon, boolean suitableForArmour)
		{
			icon = AssetManager.loadSprite(path);
			this.suitableForWeapon = suitableForWeapon;
			this.suitableForArmour = suitableForArmour;
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
	public String dropChanceEqn;
	public EnumMap<Tier1Element, Integer> elementalStats = Tier1Element.getElementBlock();

	//----------------------------------------------------------------------
	public boolean shouldDrop()
	{
		if (dropChanceEqn == null) { return true; }

		ExpressionBuilder expB = EquationHelper.createEquationBuilder(dropChanceEqn);

		Expression exp = EquationHelper.tryBuild(expB);
		if (exp == null)
		{
			return false;
		}

		double conditionVal = exp.evaluate();

		return conditionVal == 1;
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
		else if (slot != null)
		{
			Item other = inventory.getEquip(slot);
			return createArmourTable(other, entity, skin);
		}

		Table table = new Table();

		table.add(new Label(getName(), skin)).expandX().left();
		table.row();

		Label descLabel = new Label(description, skin);
		descLabel.setWrap(true);
		table.add(descLabel).expand().left().width(com.badlogic.gdx.scenes.scene2d.ui.Value.percentWidth(1, table));
		table.row();

		return table;
	}

	//----------------------------------------------------------------------
	private Table createArmourTable(Item other, GameEntity entity, Skin skin)
	{
		Table table = new Table();

		table.add(new Label(name, skin)).expandX().left();

		table.row();

		Label descLabel = new Label(description, skin);
		descLabel.setWrap(true);
		table.add(descLabel).expand().left().width(com.badlogic.gdx.scenes.scene2d.ui.Value.percentWidth(1, table));
		table.row();

		for (Statistic stat : Statistic.values())
		{
			int oldval = other == null ? 0 : other.getStatistic(entity.getBaseVariableMap(), stat);
			int newval = getStatistic(entity.getBaseVariableMap(), stat);

			if (oldval != 0 || newval != 0)
			{
				String statText = Statistic.formatString(stat.toString()) + ": " + newval;

				if (newval != oldval)
				{
					int diff = newval - oldval;

					if (diff > 0)
					{
						statText += "   [GREEN]+"+diff;
					}
					else
					{
						statText += "   [RED]"+diff;
					}
				}

				table.add(new Label(statText, skin)).expandX().left();
				table.row();
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
			Label label = new Label(weaponType.toString().toLowerCase(), skin);
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
			int oldval = other == null ? 0 : other.getStatistic(entity.getBaseVariableMap(), el.Attack);
			int newval = getStatistic(entity.getBaseVariableMap(), el.Attack);

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

		table.add(new Label(damText, skin)).expandX().left().padBottom(20);
		table.row();

		for (Statistic stat : Statistic.values())
		{
			int oldval = other == null ? 0 : other.getStatistic(entity.getBaseVariableMap(), stat);
			int newval = getStatistic(entity.getBaseVariableMap(), stat);

			if (oldval != 0 || newval != 0)
			{
				String statText = Statistic.formatString(stat.toString()) + ": " + newval;

				if (newval != oldval)
				{
					int diff = newval - oldval;

					if (diff > 0)
					{
						statText += "   [GREEN]+"+diff;
					}
					else
					{
						statText += "   [RED]"+diff;
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
			return name + " " + materialType.toString().toLowerCase();
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
