package Roguelike.Entity;

import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.RoguelikeGame;
import Roguelike.Global.Direction;
import Roguelike.Global.Statistics;
import Roguelike.Global.Tier1Element;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Ability.PassiveAbility.PassiveAbility;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree;
import Roguelike.Entity.Tasks.AbstractTask;
import Roguelike.Entity.Tasks.TaskWait;
import Roguelike.GameEvent.GameEventHandler;
import Roguelike.GameEvent.Damage.DamageObject;
import Roguelike.Items.Item;
import Roguelike.Items.Item.EquipmentSlot;
import Roguelike.Lights.Light;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.SpriteEffect;
import Roguelike.StatusEffect.StatusEffect;
import Roguelike.Tiles.GameTile;
import Roguelike.UI.MessageStack.Line;
import Roguelike.UI.MessageStack.Message;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class GameEntity
{
	//####################################################################//
	//region Constructor
	
	private GameEntity() {}
		
	//endregion Constructor
	//####################################################################//
	//region Public Methods
	
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
		se.attachedTo = this;
		statusEffects.add(se);
	}
	
	//----------------------------------------------------------------------
	public void updateAccumulators(float cost)
	{
		actionDelayAccumulator += cost;
		
		for (ActiveAbility a : m_slottedActiveAbilities)
		{
			if (a != null)
			{
				boolean gtz = a.cooldownAccumulator > 0;
				a.cooldownAccumulator -= cost;
				
				if (gtz && a.cooldownAccumulator <= 0 && Tile.Level.player == this)
				{
					RoguelikeGame.Instance.addAbilityAvailabilityAction(a.Icon);
				}
			}
		}
		
		for (GameEventHandler h : getAllHandlers())
		{
			h.onTurn(this, cost);
		}
		
		Iterator<StatusEffect> itr = statusEffects.iterator();
		while (itr.hasNext())
		{
			StatusEffect se = itr.next();

			if(se.duration <= 0)
			{
				itr.remove();
			}
		}
		
		stacks = stackStatusEffects();
	}
	
	//----------------------------------------------------------------------
	public Array<GameEventHandler> getAllHandlers()
	{
		Array<GameEventHandler> handlers = new Array<GameEventHandler>();
		
		for (PassiveAbility pa : m_slottedPassiveAbilities)
		{
			if (pa != null)
			{
				handlers.add(pa);
			}
		}
		
		for (StatusEffect se : statusEffects)
		{
			handlers.add(se);
		}
		
		for (EquipmentSlot slot : EquipmentSlot.values())
		{
			Item i = m_inventory.getEquip(slot);
			if (i != null)
			{
				handlers.add(i);
			}
		}
		
		return handlers;
	}
	
	//----------------------------------------------------------------------
	public void attack(GameEntity other, Direction dir)
	{
		Item weapon = getInventory().getEquip(EquipmentSlot.MAINWEAPON);
		Sprite hitEffect = weapon != null ? weapon.HitEffect : m_defaultHitEffect;
		
		Global.calculateDamage(this, other, null, true);
		
		// add hit effects
		SpriteEffect e = new SpriteEffect(hitEffect, dir, null);
		e.Sprite.rotation = dir.GetAngle();
		
		other.SpriteEffects.add(e);
	}
	
	//----------------------------------------------------------------------
	public void applyDamage(int dam, GameEntity damager)
	{
		HP = Math.max(HP-dam, 0);
		
		if (HP == 0)
		{
			damager.Essence += Essence;
			Essence = 0;
		}
		
		damageAccumulator += dam;
	}
	
	//----------------------------------------------------------------------
	public void applyHealing(int heal)
	{
		HP = Math.min(HP+heal, getStatistic(Statistics.MAXHP));
		
	}
	
	//----------------------------------------------------------------------
	public boolean isAllies(GameEntity other)
	{
		for (String faction : m_factions)
		{
			if (other.m_factions.contains(faction)) { return true; }
		}
		
		return false;
	}
	
	// ----------------------------------------------------------------------
	public boolean isAllies(HashSet<String> other)
	{
		for (String faction : m_factions)
		{
			if (other.contains(faction)) { return true; }
		}

		return false;
	}
	
	//----------------------------------------------------------------------
	public static GameEntity load(String name)
	{
		GameEntity e = new GameEntity();
		
		e.internalLoad(name);
		
		e.HP = e.getStatistic(Statistics.MAXHP);
		
		return e;
	}

	//----------------------------------------------------------------------
	private void internalLoad(String name)
	{
		XmlReader xml = new XmlReader();
		Element xmlElement = null;
		
		try
		{
			xmlElement = xml.parse(Gdx.files.internal("Entities/"+name+".xml"));
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
		Sprite = xmlElement.getChildByName("Sprite") != null ? AssetManager.loadSprite(xmlElement.getChildByName("Sprite")) : Sprite;
		m_defaultHitEffect = xmlElement.getChildByName("HitEffect") != null ? AssetManager.loadSprite(xmlElement.getChildByName("HitEffect")) : m_defaultHitEffect;
		AI = xmlElement.getChildByName("AI") != null ? BehaviourTree.load(Gdx.files.internal("AI/" + xmlElement.get("AI") + ".xml")) : AI;
		
		Element factionElement = xmlElement.getChildByName("Factions");
		if (factionElement != null)
		{
			for (Element faction : factionElement.getChildrenByName("Faction"))
			{
				m_factions.add(faction.getText());
			}
		}
		
		Element lightElement = xmlElement.getChildByName("Light");
		if (lightElement != null)
		{
			Light = Roguelike.Lights.Light.load(lightElement);
		}
		
		Element statElement = xmlElement.getChildByName("Statistics");
		if (statElement != null)
		{
			Statistics.load(statElement, m_statistics);
		}
				
		Element activeAbilityElement = xmlElement.getChildByName("ActiveAbilities");
		if (activeAbilityElement != null)
		{
			for (int i = 0; i < activeAbilityElement.getChildCount() && i < Global.NUM_ABILITY_SLOTS; i++)
			{
				Element abEl = activeAbilityElement.getChild(i);
				
				ActiveAbility ab = ActiveAbility.load(abEl.getText());
				m_slottedActiveAbilities[i] = ab;
				ab.caster = this;
			}
		}
		
		Element passiveAbilityElement = xmlElement.getChildByName("PassiveAbilities");
		if (passiveAbilityElement != null)
		{
			for (int i = 0; i < passiveAbilityElement.getChildCount() && i < Global.NUM_ABILITY_SLOTS; i++)
			{
				Element abEl = passiveAbilityElement.getChild(i);
				
				PassiveAbility ab = PassiveAbility.load(abEl.getText());
				m_slottedPassiveAbilities[i] = ab;
			}
		}
		
		Element inventoryElement = xmlElement.getChildByName("Inventory");
		if (inventoryElement != null)
		{
			m_inventory.load(inventoryElement);
		}
	}
	
	//----------------------------------------------------------------------
	public int getStatistic(Statistics stat)
	{
		int val = m_statistics.get(stat);
		
		for (PassiveAbility passive : m_slottedPassiveAbilities)
		{
			if (passive != null)
			{
				val += passive.getStatistic(this, stat);
			}
		}
		
		for (StatusEffect se : statusEffects)
		{
			val += se.getStatistic(this, stat);
		}
		
		val += m_inventory.getStatistic(this, stat);
		
		return val;
	}
	
	//----------------------------------------------------------------------
	public EnumMap<Statistics, Integer> getStatistics()
	{
		EnumMap<Statistics, Integer> newMap = new EnumMap<Statistics, Integer>(Statistics.class);
		
		for (Statistics stat : Statistics.values())
		{
			newMap.put(stat, getStatistic(stat));
		}
		
		return newMap;
	}
	
	//----------------------------------------------------------------------
	public float getActionDelay()
	{
		int speed = getStatistic(Statistics.SPEED);
		int weight = getStatistic(Statistics.WEIGHT);
		
		return 1;//(float) weight / (float) speed;
	}
	
	//----------------------------------------------------------------------
	public Inventory getInventory()
	{
		return m_inventory;
	}
	
	//----------------------------------------------------------------------
	public int getActiveAbilityIndex(ActiveAbility aa)
	{
		for (int i = 0; i < Global.NUM_ABILITY_SLOTS; i++)
		{
			if (m_slottedActiveAbilities[i] == aa)
			{
				return i;
			}
		}
		
		return -1;
	}
	
	//----------------------------------------------------------------------
	public int getPassiveAbilityIndex(PassiveAbility pa)
	{
		for (int i = 0; i < Global.NUM_ABILITY_SLOTS; i++)
		{
			if (m_slottedPassiveAbilities[i] == pa)
			{
				return i;
			}
		}

		return -1;
	}
	
	//----------------------------------------------------------------------
	public ActiveAbility[] getSlottedActiveAbilities()
	{
		return m_slottedActiveAbilities;
	}
	
	//----------------------------------------------------------------------
	public void slotActiveAbility(ActiveAbility aa, int index)
	{
		// if the target index is on cooldown, then cant swap
		if (m_slottedActiveAbilities[index] != null && m_slottedActiveAbilities[index].cooldownAccumulator > 0)
		{
			return;
		}
		
		// check if aa is currently slotted
		int currentIndex = -1;
		for (int i = 0; i < Global.NUM_ABILITY_SLOTS; i++)
		{
			if (m_slottedActiveAbilities[i] == aa)
			{
				currentIndex = i;
				break;
			}
		}
		
		// if is equipped, then swap the abilities without any waits
		if (currentIndex >= 0)
		{
			ActiveAbility temp = m_slottedActiveAbilities[index];
			m_slottedActiveAbilities[index] = aa;
			m_slottedActiveAbilities[currentIndex] = temp;
		}
		else
		{
			m_slottedActiveAbilities[index] = aa;
			
			if (aa != null)
			{
				aa.caster = this;
				
				for (int i = 0; i < 3; i++)
				{
					Tasks.add(new TaskWait());
				}
			}
		}
	}
	
	//----------------------------------------------------------------------
	public PassiveAbility[] getSlottedPassiveAbilities()
	{
		return m_slottedPassiveAbilities;
	}
	
	//----------------------------------------------------------------------
	public void slotPassiveAbility(PassiveAbility pa, int index)
	{
		for (int i = 0; i < Global.NUM_ABILITY_SLOTS; i++)
		{
			if (m_slottedPassiveAbilities[i] == pa)
			{
				m_slottedPassiveAbilities[i] = null;
			}
		}
		
		m_slottedPassiveAbilities[index] = pa;
		
		for (int i = 0; i < 3; i++)
		{
			Tasks.add(new TaskWait());
		}
	}
		
	//----------------------------------------------------------------------
	public HashMap<String, Integer> getVariableMap()
	{
		HashMap<String, Integer> variableMap = new HashMap<String, Integer>();
				
		for (Statistics s : Statistics.values())
		{
			variableMap.put(s.toString().toLowerCase(), getStatistic(s));
		}
		
		variableMap.put("hp", HP);
		
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

		for (Statistics s : Statistics.values())
		{
			variableMap.put(s.toString().toLowerCase(), m_statistics.get(s));
		}
		
		variableMap.put("hp", HP);
		
		for (StatusEffectStack s : stacks)
		{
			variableMap.put(s.effect.name.toLowerCase(), s.count);
		}

		return variableMap;
	}
	
	//endregion Public Methods
	//####################################################################//
	//region Private Methods
		
	//endregion Private Methods
	//####################################################################//
	//region Data
	
	//----------------------------------------------------------------------
	public int damageAccumulator = 0;
	
	//----------------------------------------------------------------------
	private EnumMap<Statistics, Integer> m_statistics = Statistics.getStatisticsBlock();
	private PassiveAbility[] m_slottedPassiveAbilities = new PassiveAbility[Global.NUM_ABILITY_SLOTS];
	private ActiveAbility[] m_slottedActiveAbilities = new ActiveAbility[Global.NUM_ABILITY_SLOTS];
	private Inventory m_inventory = new Inventory();
	public Array<StatusEffect> statusEffects = new Array<StatusEffect>(false, 16);
	private Array<StatusEffectStack> stacks;
	
	//----------------------------------------------------------------------
	public String Name;
	
	//----------------------------------------------------------------------
	public Sprite Sprite;
	public Light Light;
	public Array<SpriteEffect> SpriteEffects = new Array<SpriteEffect>(false, 16);
	private Sprite m_defaultHitEffect = AssetManager.loadSprite("strike/strike", 0.1f);
	
	//----------------------------------------------------------------------
	public GameTile Tile;
	public Array<AbstractTask> Tasks = new Array<AbstractTask>();
	public float actionDelayAccumulator;
	public boolean CanSwap;
	
	//----------------------------------------------------------------------
	public int HP;
	public int Essence = 100;
	public HashSet<String> m_factions = new HashSet<String>();		
	public BehaviourTree AI;
	public ActiveAbility Channeling = null;
	
	//----------------------------------------------------------------------
	public static class StatusEffectStack
	{
		public StatusEffect effect;
		public int count;
	}
		
	//endregion Data
	//####################################################################//
}
