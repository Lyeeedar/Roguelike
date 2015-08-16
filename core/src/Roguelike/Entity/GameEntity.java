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
import Roguelike.Global.Passability;
import Roguelike.Global.Statistic;
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
import Roguelike.Screens.GameScreen;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.SpriteEffect;
import Roguelike.StatusEffect.StatusEffect;
import Roguelike.Tiles.GameTile;
import Roguelike.Tiles.Point;
import Roguelike.UI.MessageStack.Line;
import Roguelike.UI.MessageStack.Message;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class GameEntity extends Entity
{
	//####################################################################//
	//region Constructor

	private GameEntity() {}

	//endregion Constructor
	//####################################################################//
	//region Public Methods
	
	//----------------------------------------------------------------------
	public Array<Passability> getTravelType()
	{
		return travelType;
	}
	
	//----------------------------------------------------------------------
	@Override
	public void applyDamage(int dam, Entity damager)
	{
		super.applyDamage(dam, damager);
		
		AI.setData("EnemyPos", Pools.obtain(Point.class).set(damager.tile.x, damager.tile.y));
	}

	//----------------------------------------------------------------------
	public void attack(Entity other, Direction dir)
	{
		Global.calculateDamage(this, other, getVariableMap(), true);
	}

	//----------------------------------------------------------------------
	@Override
	public void update(float cost)
	{
		if (inventory.isVariableMapDirty)
		{
			isVariableMapDirty = true;
			inventory.isVariableMapDirty = false;
		}
		
		actionDelayAccumulator += cost;

		for (ActiveAbility a : slottedActiveAbilities)
		{
			a.cooldownAccumulator -= cost;
			if (a.cooldownAccumulator < 0) { a.cooldownAccumulator = 0; }
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
	@Override
	public Array<GameEventHandler> getAllHandlers()
	{
		Array<GameEventHandler> handlers = new Array<GameEventHandler>();

		for (PassiveAbility pa : slottedPassiveAbilities)
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
			Item i = inventory.getEquip(slot);
			if (i != null)
			{
				handlers.add(i);
			}
		}

		return handlers;
	}

	//----------------------------------------------------------------------
	public boolean isAllies(GameEntity other)
	{
		for (String faction : factions)
		{
			if (other.factions.contains(faction)) { return true; }
		}

		return false;
	}

	// ----------------------------------------------------------------------
	public boolean isAllies(HashSet<String> other)
	{
		if (other == null) { return false; }
		
		for (String faction : factions)
		{
			if (other.contains(faction)) { return true; }
		}

		return false;
	}

	//----------------------------------------------------------------------
	public static GameEntity load(String name)
	{
		GameEntity e = new GameEntity();
		e.fileName = name;

		e.internalLoad(name);

		e.HP = e.getStatistic(Statistic.MAXHP);

		return e;
	}

	//----------------------------------------------------------------------
	protected void internalLoad(String entity)
	{
		XmlReader xml = new XmlReader();
		Element xmlElement = null;

		try
		{
			xmlElement = xml.parse(Gdx.files.internal("Entities/"+entity+".xml"));
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
		
		super.baseInternalLoad(xmlElement);

		defaultHitEffect = xmlElement.getChildByName("HitEffect") != null ? AssetManager.loadSprite(xmlElement.getChildByName("HitEffect")) : defaultHitEffect;
		AI = xmlElement.getChildByName("AI") != null ? BehaviourTree.load(Gdx.files.internal("AI/" + xmlElement.get("AI") + ".xml")) : AI;
		canSwap = xmlElement.getBoolean("CanSwap", false);
		canMove = xmlElement.getBoolean("CanMove", true);
		essence = xmlElement.getInt("Essence", 20);

		Element factionElement = xmlElement.getChildByName("Factions");
		if (factionElement != null)
		{
			for (Element faction : factionElement.getChildrenByName("Faction"))
			{
				factions.add(faction.getText());
			}
		}

		Element activeAbilityElement = xmlElement.getChildByName("ActiveAbilities");
		if (activeAbilityElement != null)
		{
			for (int i = 0; i < activeAbilityElement.getChildCount() && i < Global.NUM_ABILITY_SLOTS; i++)
			{
				Element abEl = activeAbilityElement.getChild(i);
				
				ActiveAbility ab = null;
				
				if (abEl.getChildCount() > 0)
				{
					ab = ActiveAbility.load(abEl);
				}
				else
				{
					ab = ActiveAbility.load(abEl.getText());
				}

				ab.caster = this;
				slottedActiveAbilities.add(ab);
			}
		}

		Element passiveAbilityElement = xmlElement.getChildByName("PassiveAbilities");
		if (passiveAbilityElement != null)
		{
			for (int i = 0; i < passiveAbilityElement.getChildCount() && i < Global.NUM_ABILITY_SLOTS; i++)
			{
				Element abEl = passiveAbilityElement.getChild(i);
				
				PassiveAbility ab = null;
				
				if (abEl.getChildCount() > 0)
				{
					ab = PassiveAbility.load(abEl);
				}
				else
				{
					ab = PassiveAbility.load(abEl.getText());
				}

				slottedPassiveAbilities.add(ab);
			}
		}
	}

	//----------------------------------------------------------------------
	public int getStatistic(Statistic stat)
	{
		int val = statistics.get(stat) + inventory.getStatistic(Statistic.emptyMap, stat);

		HashMap<String, Integer> variableMap = getBaseVariableMap();

		variableMap.put(stat.toString().toLowerCase(), val);
		
		for (PassiveAbility passive : slottedPassiveAbilities)
		{
			if (passive != null)
			{
				val += passive.getStatistic(variableMap, stat);
			}
		}

		for (StatusEffect se : statusEffects)
		{
			val += se.getStatistic(variableMap, stat);
		}
		
		return val;
	}

	//----------------------------------------------------------------------
	public float getActionDelay()
	{
		int speed = getStatistic(Statistic.SPEED);
		int weight = getStatistic(Statistic.WEIGHT);

		return 1;//(float) weight / (float) speed;
	}

	//----------------------------------------------------------------------
	public void getLight(Array<Light> output)
	{
		if (light != null) { output.add(light); }

		for (EquipmentSlot slot : EquipmentSlot.values())
		{
			Item i = inventory.getEquip(slot);

			if (i != null && i.light != null)
			{
				output.add(i.light);
			}
		}
	}

	//endregion Public Methods
	//####################################################################//
	//region Data
	
	
	
	public String fileName;

	//----------------------------------------------------------------------
	public Array<PassiveAbility> slottedPassiveAbilities = new Array<PassiveAbility>();
	public Array<ActiveAbility> slottedActiveAbilities = new Array<ActiveAbility>();

	//----------------------------------------------------------------------
	public Sprite defaultHitEffect = AssetManager.loadSprite("strike/strike", 0.1f);

	//----------------------------------------------------------------------
	public Array<AbstractTask> tasks = new Array<AbstractTask>();
	public float actionDelayAccumulator;
	public boolean canSwap;
	public boolean canMove;

	//----------------------------------------------------------------------
	public HashSet<String> factions = new HashSet<String>();		
	public BehaviourTree AI;
	
	//endregion Data
	//####################################################################//
}
