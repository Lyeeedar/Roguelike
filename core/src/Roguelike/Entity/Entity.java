package Roguelike.Entity;

import java.util.EnumMap;
import java.util.HashMap;

import Roguelike.AssetManager;
import Roguelike.Global.Passability;
import Roguelike.Global.Statistic;
import Roguelike.GameEvent.GameEventHandler;
import Roguelike.Items.Item.EquipmentSlot;
import Roguelike.Lights.Light;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.SpriteEffect;
import Roguelike.StatusEffect.StatusEffect;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public abstract class Entity
{
	//----------------------------------------------------------------------
	public Array<Passability> getTravelType()
	{
		return Passability.statsToTravelType(getStatistics());
	}
	
	//----------------------------------------------------------------------
	public abstract void update(float delta);
	
	//----------------------------------------------------------------------
	public abstract int getStatistic(Statistic stat);
	
	//----------------------------------------------------------------------
	protected abstract void internalLoad(String file);
	
	//----------------------------------------------------------------------
	public abstract Array<GameEventHandler> getAllHandlers();
	
	//----------------------------------------------------------------------
	protected void baseInternalLoad(Element xml)
	{
		name = xml.get("Name", name);
		sprite = xml.getChildByName("Sprite") != null ? AssetManager.loadSprite(xml.getChildByName("Sprite")) : sprite;

		Element lightElement = xml.getChildByName("Light");
		if (lightElement != null)
		{
			light = Roguelike.Lights.Light.load(lightElement);
		}

		Element statElement = xml.getChildByName("Statistics");
		if (statElement != null)
		{
			Statistic.load(statElement, statistics);
			HP = getStatistic(Statistic.MAXHP);
			
			statistics.put(Statistic.WALK, 1);
		}
		
		Element inventoryElement = xml.getChildByName("Inventory");
		if (inventoryElement != null)
		{
			inventory.load(inventoryElement);
		}
	}

	//----------------------------------------------------------------------
	public EnumMap<Statistic, Integer> getStatistics()
	{
		EnumMap<Statistic, Integer> newMap = new EnumMap<Statistic, Integer>(Statistic.class);

		for (Statistic stat : Statistic.values())
		{
			newMap.put(stat, getStatistic(stat));
		}

		return newMap;
	}
	
	//----------------------------------------------------------------------
	public HashMap<String, Integer> getVariableMap()
	{
		HashMap<String, Integer> variableMap = new HashMap<String, Integer>();

		for (Statistic s : Statistic.values())
		{
			variableMap.put(s.toString().toLowerCase(), getStatistic(s));
		}

		variableMap.put("hp", HP);
		
		if (inventory.getEquip(EquipmentSlot.MAINWEAPON) != null)
		{
			variableMap.put(inventory.getEquip(EquipmentSlot.MAINWEAPON).weaponType.toString().toLowerCase(), 1);
		}

		for (StatusEffectStack s : stacks)
		{
			variableMap.put(s.effect.name.toLowerCase(), s.count);
		}

		return variableMap;
	}
	
	//----------------------------------------------------------------------
	public HashMap<String, Integer> getBaseVariableMap()
	{
		HashMap<String, Integer> variableMap = new HashMap<String, Integer>();

		for (Statistic s : Statistic.values())
		{
			variableMap.put(s.toString().toLowerCase(), statistics.get(s));
		}

		variableMap.put("hp", HP);
		
		if (inventory.getEquip(EquipmentSlot.MAINWEAPON) != null)
		{
			variableMap.put(inventory.getEquip(EquipmentSlot.MAINWEAPON).weaponType.toString().toLowerCase(), 1);
		}

		for (StatusEffectStack s : stacks)
		{
			variableMap.put(s.effect.name.toLowerCase(), s.count);
		}

		return variableMap;
	}

	//----------------------------------------------------------------------
	public Inventory getInventory()
	{
		return inventory;
	}
	
	//----------------------------------------------------------------------
	public void applyDamage(int dam, Entity damager)
	{
		if (!canTakeDamage) { return; }
		
		HP = Math.max(HP-dam, 0);

		if (HP == 0)
		{
			damager.essence += essence;
			essence = 0;
		}

		damageAccumulator += dam;
	}

	//----------------------------------------------------------------------
	public void applyHealing(int heal)
	{
		if (!canTakeDamage) { return; }
		
		int appliedHeal = Math.min(heal, getStatistic(Statistic.MAXHP) - HP);
		HP += appliedHeal;

		healingAccumulator += appliedHeal;
	}

	//----------------------------------------------------------------------
	public Array<StatusEffectStack> stackStatusEffects()
	{
		Array<StatusEffectStack> stacks = new Array<StatusEffectStack>();

		for (StatusEffect se : statusEffects)
		{
			boolean found = false;
			for (StatusEffectStack stack : stacks)
			{
				if (stack.effect.name.equals(se.name))
				{
					stack.count++;
					found = true;
					break;
				}
			}

			if (!found)
			{
				StatusEffectStack stack = new StatusEffectStack();
				stack.count = 1;
				stack.effect = se;

				stacks.add(stack);
			}
		}

		return stacks;
	}	

	//----------------------------------------------------------------------
	public void addStatusEffect(StatusEffect se)
	{
		if (!canTakeDamage) { return; }
		
		se.attachedTo = this;
		statusEffects.add(se);
	}
	
	//----------------------------------------------------------------------
	public void removeStatusEffect(StatusEffect se)
	{
		se.attachedTo = null;
		statusEffects.removeValue(se, true);
	}
	
	//----------------------------------------------------------------------
	public void removeStatusEffect(String se)
	{
		for (int i = 0; i < statusEffects.size; i++)
		{
			if (statusEffects.get(i).name.equals(se))
			{
				statusEffects.removeIndex(i);
				break;
			}
		}
	}

	//----------------------------------------------------------------------
	public int damageAccumulator = 0;
	public int healingAccumulator = 0;

	//----------------------------------------------------------------------
	public EnumMap<Statistic, Integer> statistics = Statistic.getStatisticsBlock();
	public Array<StatusEffect> statusEffects = new Array<StatusEffect>(false, 16);
	public Array<StatusEffectStack> stacks = new Array<StatusEffectStack>();	
	public Inventory inventory = new Inventory();

	//----------------------------------------------------------------------
	public String name;
	
	//----------------------------------------------------------------------
	public Actor popup;

	//----------------------------------------------------------------------
	public Sprite sprite;
	public Light light;
	public Array<SpriteEffect> spriteEffects = new Array<SpriteEffect>(false, 16);

	//----------------------------------------------------------------------
	public GameTile tile;

	//----------------------------------------------------------------------
	public int HP = 1;
	public int essence = 0;
	
	//----------------------------------------------------------------------
	public boolean canTakeDamage = true;

	//----------------------------------------------------------------------
	public static class StatusEffectStack
	{
		public StatusEffect effect;
		public int count;
	}

}
