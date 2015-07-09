package Roguelike.Entity;

import java.io.IOException;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.Global.Direction;
import Roguelike.Global.Statistics;
import Roguelike.Global.Tier1Element;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Ability.PassiveAbility.PassiveAbility;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree;
import Roguelike.Entity.Tasks.AbstractTask;
import Roguelike.GameEvent.GameEventHandler;
import Roguelike.GameEvent.Damage.DamageObject;
import Roguelike.Items.Item;
import Roguelike.Items.Item.EquipmentSlot;
import Roguelike.Lights.Light;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.SpriteEffect;
import Roguelike.StatusEffect.StatusEffect;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class Entity
{
	//####################################################################//
	//region Constructor
	
	private Entity() {}
		
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
				a.cooldownAccumulator -= cost;
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
	}
	
	//----------------------------------------------------------------------
	private Array<GameEventHandler> getAllHandlers()
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
	public void attack(Entity other, Direction dir)
	{
		Item weapon = getInventory().getEquip(EquipmentSlot.MAINWEAPON);
		Sprite hitEffect = weapon != null ? weapon.HitEffect : m_defaultHitEffect;
		
		int damage = Global.calculateDamage(getAttunements(), other.getAttunements(), weapon != null ? weapon.getAttunements() : Tier1Element.getElementMap());
		DamageObject damObj = new DamageObject(this, other, damage);
		
		for (GameEventHandler handler : getAllHandlers())
		{
			handler.onDealDamage(damObj);
		}
		
		for (GameEventHandler handler : other.getAllHandlers())
		{
			handler.onReceiveDamage(damObj);
		}
		
		other.applyDamage(damObj.damage);
		
		// add hit effects
		SpriteEffect e = new SpriteEffect(hitEffect, dir, null);
		e.Sprite.rotation = dir.GetAngle();
		
		other.SpriteEffects.add(e);
	}
	
	//----------------------------------------------------------------------
	public void applyDamage(int dam)
	{
		HP = Math.max(HP-dam, 0);
	}
	
	//----------------------------------------------------------------------
	public void applyHealing(int heal)
	{
		HP = Math.min(HP+heal, getStatistic(Statistics.MAXHP));
		
	}
	
	//----------------------------------------------------------------------
	public boolean isAllies(Entity other)
	{
		for (String faction : m_factions)
		{
			if (other.m_factions.contains(faction)) { return true; }
		}
		
		return false;
	}
	
	//----------------------------------------------------------------------
	public static Entity load(String name)
	{
		Entity e = new Entity();
		
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
		
		Element attunementElement = xmlElement.getChildByName("Attunement");
		if (attunementElement != null)
		{
			Tier1Element.load(attunementElement, m_attunement);
		}
				
		Element activeAbilityElement = xmlElement.getChildByName("ActiveAbilities");
		if (activeAbilityElement != null)
		{
			for (int i = 0; i < activeAbilityElement.getChildCount() && i < Global.NUM_ABILITY_SLOTS; i++)
			{
				Element abEl = activeAbilityElement.getChild(i);
				
				ActiveAbility ab = ActiveAbility.load(abEl.getText());
				m_slottedActiveAbilities[i] = ab;
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
	
	// Stats
	public int getStatistic(Statistics stat)
	{
		int val = m_statistics.get(stat);
		
		for (PassiveAbility passive : m_slottedPassiveAbilities)
		{
			if (passive != null)
			{
				val += passive.getStatistic(stat);
			}
		}
		
		for (StatusEffect se : statusEffects)
		{
			val += se.getStatistic(stat);
		}
		
		val += m_inventory.getStatistic(stat);
		
		return val;
	}
	
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
	public int getAttunement(Tier1Element el)
	{
		int val = m_attunement.get(el);
		val += m_inventory.getAttunement(el);
		
		for (StatusEffect se : statusEffects)
		{
			val += se.getAttunement(el);
		}
		
		for (PassiveAbility passive : m_slottedPassiveAbilities)
		{
			if (passive != null)
			{
				val += passive.getAttunement(el);
			}
		}
		
		return val;
	}
	
	//----------------------------------------------------------------------
	public EnumMap<Tier1Element, Integer> getAttunements()
	{
		EnumMap<Tier1Element, Integer> map = new EnumMap<Tier1Element, Integer>(Tier1Element.class);

		for (Tier1Element el : Tier1Element.values())
		{
			int val = getAttunement(el);
			map.put(el, val);
		}

		return map;
	}
	
	public float getActionDelay()
	{
		int speed = getStatistic(Statistics.SPEED);
		int weight = getStatistic(Statistics.WEIGHT);
		
		return 1;//(float) weight / (float) speed;
	}
	
	public Inventory getInventory()
	{
		return m_inventory;
	}
	
	public ActiveAbility[] getSlottedActiveAbilities()
	{
		return m_slottedActiveAbilities;
	}
	
	public void slotActiveAbility(ActiveAbility aa, int index)
	{
		for (int i = 0; i < Global.NUM_ABILITY_SLOTS; i++)
		{
			if (m_slottedActiveAbilities[i] == aa)
			{
				m_slottedActiveAbilities[i] = null;
			}
		}
		
		if (m_slottedActiveAbilities[index] != null && m_slottedActiveAbilities[index].cooldownAccumulator > 0)
		{
			return;
		}
		
		m_slottedActiveAbilities[index] = aa;
	}
	
	public PassiveAbility[] getSlottedPassiveAbilities()
	{
		return m_slottedPassiveAbilities;
	}
	
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
	}
	
	public void fillExpressionBuilderWithValues(ExpressionBuilder expB, String prefix)
	{
		Array<StatusEffectStack> stacks = stackStatusEffects();
		
		for (Tier1Element el : Tier1Element.values())
		{
			expB.variable(prefix+el.toString());
		}
		
		for (Statistics s : Statistics.values())
		{
			expB.variable(prefix+s.toString());
		}
		
		expB.variable(prefix+"HP");
		
		for (StatusEffectStack s : stacks)
		{
			expB.variable(s.effect.name.toUpperCase());
		}			
	}
	
	public void fillExpressionWithValues(Expression exp, String prefix)
	{
		Array<StatusEffectStack> stacks = stackStatusEffects();
		
		for (Tier1Element el : Tier1Element.values())
		{
			exp.setVariable(prefix+el.toString(), getAttunement(el));
		}
		
		for (Statistics s : Statistics.values())
		{
			exp.setVariable(prefix+s.toString(), getStatistic(s));
		}
		
		exp.setVariable(prefix+"HP", HP);
		
		for (StatusEffectStack s : stacks)
		{
			exp.setVariable(prefix+s.effect.name.toUpperCase(), s.count);
		}		
	}
	
	//endregion Public Methods
	//####################################################################//
	//region Private Methods
		
	//endregion Private Methods
	//####################################################################//
	//region Data
	

	//----------------------------------------------------------------------
	private EnumMap<Statistics, Integer> m_statistics = Statistics.getStatisticsBlock();
	private EnumMap<Tier1Element, Integer> m_attunement = Tier1Element.getElementMap();
	private PassiveAbility[] m_slottedPassiveAbilities = new PassiveAbility[Global.NUM_ABILITY_SLOTS];
	private ActiveAbility[] m_slottedActiveAbilities = new ActiveAbility[Global.NUM_ABILITY_SLOTS];
	private Inventory m_inventory = new Inventory();
	public Array<StatusEffect> statusEffects = new Array<StatusEffect>(false, 16);
	
	//----------------------------------------------------------------------
	public String Name;
	
	//----------------------------------------------------------------------
	public Sprite Sprite;
	public Light Light;
	public Array<SpriteEffect> SpriteEffects = new Array<SpriteEffect>(false, 16);
	private Sprite m_defaultHitEffect;
	
	//----------------------------------------------------------------------
	public GameTile Tile;
	public Array<AbstractTask> Tasks = new Array<AbstractTask>();
	public float actionDelayAccumulator;
	public boolean CanSwap;
	
	//----------------------------------------------------------------------
	public int HP;	
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
