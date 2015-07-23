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

public class GameEntity extends Entity
{
	//####################################################################//
	//region Constructor

	private GameEntity() {}

	//endregion Constructor
	//####################################################################//
	//region Public Methods

	//----------------------------------------------------------------------
	public void attack(Entity other, Direction dir)
	{
		Global.calculateDamage(this, other, null, true);
	}

	//----------------------------------------------------------------------
	@Override
	public void update(float cost)
	{
		actionDelayAccumulator += cost;

		for (ActiveAbility a : slottedActiveAbilities)
		{
			if (a != null)
			{
				boolean gtz = a.cooldownAccumulator > 0;
				a.cooldownAccumulator -= cost;

				if (gtz && a.cooldownAccumulator <= 0 && tile.level.player == this)
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

		e.internalLoad(name);

		e.HP = e.getStatistic(Statistics.MAXHP);

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

				ActiveAbility ab = ActiveAbility.load(abEl.getText());
				slottedActiveAbilities[i] = ab;
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
				slottedPassiveAbilities[i] = ab;
			}
		}

		Element inventoryElement = xmlElement.getChildByName("Inventory");
		if (inventoryElement != null)
		{
			inventory.load(inventoryElement);
		}
	}

	//----------------------------------------------------------------------
	public int getStatistic(Statistics stat)
	{
		int val = statistics.get(stat);

		for (PassiveAbility passive : slottedPassiveAbilities)
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

		val += inventory.getStatistic(this, stat);

		return val;
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
		return inventory;
	}

	//----------------------------------------------------------------------
	public int getActiveAbilityIndex(ActiveAbility aa)
	{
		for (int i = 0; i < Global.NUM_ABILITY_SLOTS; i++)
		{
			if (slottedActiveAbilities[i] == aa)
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
			if (slottedPassiveAbilities[i] == pa)
			{
				return i;
			}
		}

		return -1;
	}

	//----------------------------------------------------------------------
	public ActiveAbility[] getSlottedActiveAbilities()
	{
		return slottedActiveAbilities;
	}

	//----------------------------------------------------------------------
	public void slotActiveAbility(ActiveAbility aa, int index)
	{
		// if the target index is on cooldown, then cant swap
		if (slottedActiveAbilities[index] != null && slottedActiveAbilities[index].cooldownAccumulator > 0)
		{
			return;
		}

		// check if aa is currently slotted
		int currentIndex = -1;
		for (int i = 0; i < Global.NUM_ABILITY_SLOTS; i++)
		{
			if (slottedActiveAbilities[i] == aa)
			{
				currentIndex = i;
				break;
			}
		}

		// if is equipped, then swap the abilities without any waits
		if (currentIndex >= 0)
		{
			ActiveAbility temp = slottedActiveAbilities[index];
			slottedActiveAbilities[index] = aa;
			slottedActiveAbilities[currentIndex] = temp;
		}
		else
		{
			slottedActiveAbilities[index] = aa;

			if (aa != null)
			{
				aa.caster = this;

				for (int i = 0; i < 3; i++)
				{
					tasks.add(new TaskWait());
				}
			}
		}
	}

	//----------------------------------------------------------------------
	public PassiveAbility[] getSlottedPassiveAbilities()
	{
		return slottedPassiveAbilities;
	}

	//----------------------------------------------------------------------
	public void slotPassiveAbility(PassiveAbility pa, int index)
	{
		for (int i = 0; i < Global.NUM_ABILITY_SLOTS; i++)
		{
			if (slottedPassiveAbilities[i] == pa)
			{
				slottedPassiveAbilities[i] = null;
			}
		}

		slottedPassiveAbilities[index] = pa;

		for (int i = 0; i < 3; i++)
		{
			tasks.add(new TaskWait());
		}
	}

	//----------------------------------------------------------------------
	public Array<Light> getLight()
	{
		Array<Light> lights = new Array<Light>();

		if (light != null) { lights.add(light); }

		for (EquipmentSlot slot : EquipmentSlot.values())
		{
			Item i = inventory.getEquip(slot);

			if (i != null && i.light != null)
			{
				lights.add(i.light);
			}
		}

		return lights;
	}

	//endregion Public Methods
	//####################################################################//
	//region Data

	//----------------------------------------------------------------------
	private PassiveAbility[] slottedPassiveAbilities = new PassiveAbility[Global.NUM_ABILITY_SLOTS];
	private ActiveAbility[] slottedActiveAbilities = new ActiveAbility[Global.NUM_ABILITY_SLOTS];
	private Inventory inventory = new Inventory();

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
