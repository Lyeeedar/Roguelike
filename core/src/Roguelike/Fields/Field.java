package Roguelike.Fields;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;

import Roguelike.AssetManager;
import Roguelike.Global.Passability;
import Roguelike.Fields.DurationStyle.AbstractDurationStyle;
import Roguelike.Fields.OnTurnEffect.AbstractOnTurnEffect;
import Roguelike.Fields.SpreadStyle.AbstractSpreadStyle;
import Roguelike.Lights.Light;
import Roguelike.Sprite.Sprite;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class Field
{
 /**
  * match based on tags, not name (hot, wet, explosive, poisonous, gas)
  * interation: propogate, spawn, die, none
  * on urn, damage, heal, status, spawn
  * duration, permanent, time, single
  * modify passability: allow, restrict
  * 
  * water
  * spead fast flow
  * fires turns it to steam
  * single stacks disappear after time
  * add swim passability
  * 
  * steam
  * blocks vision
  * dissipates after short time
  * 
  * smoke
  * same as steam
  * 
  * fire
  * spreads to neighbouring entities
  * does damage
  * disppears after nbot doing damage for some turns
  * when dies naturally turn into smoke
  * 
  * electricity
  * disappears after a few turns
  * discharges into entity, high dam
  * annihaltes with water, discharging into every connected water tile
  * 
  * ice
  * eveny movement gets doubled
  * fire melts it, turning it into water
  * 
  * lava
  * flow slow
  * spawns fire around it
  * does high dam
  * 
  * others
  * poison
  * bog/cobweb
  * thuorns/razorcrystals
  * explosive gas
  */
	public String fileName;
	public String fieldName;
	
	public GameTile tile;
	public Light light;
	
	public Sprite sprite;
	public boolean drawAbove = false;
	public int stacks = 1;
	
	public AbstractDurationStyle durationStyle;
	public AbstractSpreadStyle spreadStyle;
	public Array<AbstractOnTurnEffect> onTurnEffects = new Array<AbstractOnTurnEffect>();
	//public HashMap<String, AbstractInteractionType> fieldInteractions;
	
	public HashMap<String, Object> data = new HashMap<String, Object>();
	
	public EnumSet<Passability> allowPassability = EnumSet.noneOf(Passability.class);
	public EnumSet<Passability> restrictPassability = EnumSet.noneOf(Passability.class);
	
	public void update(float cost)
	{
		for (AbstractOnTurnEffect effect : onTurnEffects)
		{
			effect.update(cost, this);
		}
		
		durationStyle.update(cost, this);
		
		if (stacks > 0)
		{
			spreadStyle.update(cost, this);
		}
	}
	
	public void onNaturalDeath()
	{
		//onDeathEffect.process(this);
	}
	
	public void trySpawnInTile(int x, int y)
	{
		GameTile newTile = tile.level.getGameTile(x, y);
				
		if (newTile.field != null)
		{
			// if same field, increase stacks
			if (newTile.field.fieldName.equals(fieldName))
			{
				newTile.field.stacks++;
			}
			// if different field, interact
			else
			{
				// do interaction somehow				
			}
		}
		else
		{
			Field newField = copy();
			newField.stacks = 1;
			
			newTile.addField(newField);
		}	
	}
	
	public Object getData(String key, Object fallback)
	{
		if (data.containsKey(key))
		{
			return data.get(key);
		}
		
		return fallback;
	}
	
	public void setData(String key, Object val)
	{
		data.put(key, val);
	}
	
	public Field copy()
	{
		Field field = new Field();
		field.fileName = fileName;
		field.fieldName = fieldName;
		field.sprite = sprite.copy();
		field.drawAbove = drawAbove;
		field.stacks = stacks;
		field.light = light != null ? light.copy() : null;
		
		field.durationStyle = durationStyle;
		field.spreadStyle = spreadStyle;
		field.onTurnEffects = onTurnEffects;
		//field.fieldInteractions = fieldInteractions;
		
		field.allowPassability = allowPassability;
		field.restrictPassability = restrictPassability;
		
		return field;
	}
	
	//----------------------------------------------------------------------
	protected void internalLoad(String fileName)
	{
		XmlReader xmlReader = new XmlReader();
		Element xml = null;

		try
		{
			xml = xmlReader.parse(Gdx.files.internal("Fields/"+fileName+".xml"));
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}

		String extendsElement = xml.getAttribute("Extends", null);
		if (extendsElement != null)
		{
			internalLoad(extendsElement);
		}
		
		fieldName = xml.get("FieldName", fieldName);
		
		sprite = xml.getChildByName("Sprite") != null ? AssetManager.loadSprite(xml.getChildByName("Sprite")) : sprite;

		Element lightElement = xml.getChildByName("Light");
		if (lightElement != null)
		{
			light = Roguelike.Lights.Light.load(lightElement);
		}
		
		drawAbove = xml.getBoolean("DrawAbove", drawAbove);
		
		Element durationElement = xml.getChildByName("Duration");
		if (durationElement != null)
		{
			durationStyle = AbstractDurationStyle.load(durationElement.getChild(0));
		}
		
		Element spreadElement = xml.getChildByName("Spread");
		if (spreadElement != null)
		{
			spreadStyle = AbstractSpreadStyle.load(spreadElement.getChild(0));
		}
		
		Element onTurnElement = xml.getChildByName("OnTurn");
		if (onTurnElement != null)
		{
			for (int i = 0; i < onTurnElement.getChildCount(); i++)
			{
				Element effectElement = onTurnElement.getChild(i);
				AbstractOnTurnEffect effect = AbstractOnTurnEffect.load(effectElement);
				onTurnEffects.add(effect);
			}
		}
		
		String allowString = xml.get("AllowPassability", null);
		if (allowString != null)
		{
			EnumSet<Passability> pass = Passability.parse(allowString);
			allowPassability.addAll(pass);
		}
		
		String restrictString = xml.get("RestrictPassability", null);
		if (restrictString != null)
		{
			EnumSet<Passability> pass = Passability.parse(restrictString);
			restrictPassability.addAll(pass);
		}
	}

	//----------------------------------------------------------------------
	public static Field load(String name)
	{
		Field f = new Field();
		f.fileName = name;

		f.internalLoad(name);

		return f;
	}
}