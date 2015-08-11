package Roguelike.Fields;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;

import Roguelike.AssetManager;
import Roguelike.Global.Passability;
import Roguelike.Fields.SpreadStyle.AbstractSpreadStyle;
import Roguelike.Lights.Light;
import Roguelike.Sprite.Sprite;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class Field
{
 /**
  * Spread style: non, flow, adjacent, wander, line
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
	
	public AbstractSpreadStyle spreadStyle;
	//public Array<AbstractOnTurnEffect> onTurnEffects;
	//public HashMap<String, AbstractInteractionType> fieldInteractions;
	//public AbstractDurationType durationType;
	
	public HashMap<String, Object> data = new HashMap<String, Object>();
	
	public EnumSet<Passability> allowPassability = EnumSet.noneOf(Passability.class);
	public EnumSet<Passability> restrictPassability = EnumSet.noneOf(Passability.class);
	
	public void update(float cost)
	{
		spreadStyle.update(cost, this);
//		for (AbstractOnTurnEffect effect : onTurnEffects)
//		{
//			effect.update(cost, this);
//		}
	}
	
	public void onDeath()
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
				boolean thisSurvived = false;
				//if (fieldInteractions.containsKey(newTile.field.fieldName))
				//{
				//	thisSurvived = fieldInteractions.interact(this, newTile.field);
				//}
			}
		}
		else
		{
			Field newField = copy();
			newField.stacks = 1;
			
			newTile.field = newField;
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
		field.fieldName = fieldName;
		field.sprite = sprite;
		field.drawAbove = drawAbove;
		field.stacks = stacks;
		field.light = light != null ? light.copy() : null;
		
		field.spreadStyle = spreadStyle;
		//field.onTurnEffects = onTurnEffects;
		//field.fieldInteractions = fieldInteractions;
		//filed.durationType = durationType;
		
		field.allowPassability = allowPassability;
		field.restrictPassability = restrictPassability;
		
		return field;
	}
	
	//----------------------------------------------------------------------
	protected void internalLoad(String fieldName)
	{
		XmlReader xmlReader = new XmlReader();
		Element xml = null;

		try
		{
			xml = xmlReader.parse(Gdx.files.internal("Fields/"+fieldName+".xml"));
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
		
		Element spreadElement = xml.getChildByName("SpreadStyle");
		if (spreadElement != null)
		{
			spreadStyle = AbstractSpreadStyle.load(spreadElement);
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
