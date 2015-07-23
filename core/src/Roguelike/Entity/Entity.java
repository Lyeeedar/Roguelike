package Roguelike.Entity;

import java.util.EnumMap;
import java.util.HashMap;

import Roguelike.AssetManager;
import Roguelike.GameEvent.GameEventHandler;
import Roguelike.Global.Statistics;
import Roguelike.Lights.Light;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.SpriteEffect;
import Roguelike.StatusEffect.StatusEffect;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public abstract class Entity
{
	//----------------------------------------------------------------------
	public abstract void update(float delta);
	
	//----------------------------------------------------------------------
	public abstract int getStatistic(Statistics stat);
	
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
			Statistics.load(statElement, statistics);
			HP = getStatistic(Statistics.MAXHP);
		}
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
			variableMap.put(s.toString().toLowerCase(), statistics.get(s));
		}

		variableMap.put("hp", HP);

		for (StatusEffectStack s : stacks)
		{
			variableMap.put(s.effect.name.toLowerCase(), s.count);
		}

		return variableMap;
	}

	//----------------------------------------------------------------------
	public void applyDamage(int dam, Entity damager)
	{
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
		HP = Math.min(HP+heal, getStatistic(Statistics.MAXHP));

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
		se.attachedTo = this;
		statusEffects.add(se);
	}

	//----------------------------------------------------------------------
	public int damageAccumulator = 0;

	//----------------------------------------------------------------------
	public EnumMap<Statistics, Integer> statistics = Statistics.getStatisticsBlock();
	public Array<StatusEffect> statusEffects = new Array<StatusEffect>(false, 16);
	public Array<StatusEffectStack> stacks = new Array<StatusEffectStack>();

	//----------------------------------------------------------------------
	public String name;

	//----------------------------------------------------------------------
	public Sprite sprite;
	public Light light;
	public Array<SpriteEffect> spriteEffects = new Array<SpriteEffect>(false, 16);

	//----------------------------------------------------------------------
	public GameTile tile;

	//----------------------------------------------------------------------
	public int HP = 1;
	public int essence = 100;

	//----------------------------------------------------------------------
	public static class StatusEffectStack
	{
		public StatusEffect effect;
		public int count;
	}

}
