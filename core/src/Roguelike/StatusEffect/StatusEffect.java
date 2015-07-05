package Roguelike.StatusEffect;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

import Roguelike.AssetManager;
import Roguelike.Entity.Entity;
import Roguelike.Sprite.Sprite;
import Roguelike.StatusEffect.OnTurn.AbstractOnTurnEffect;

public class StatusEffect
{
	public String name;
	private String description;
	
	public Sprite icon;
	public Sprite continualEffect;
	public float duration;
	public Entity attachedTo;
	
	private Array<AbstractOnTurnEffect> onTurnEffects = new Array<AbstractOnTurnEffect>();
	
	public Table createTable(Skin skin)
	{
		Table table = new Table();
		
		table.add(new Label(name, skin)).expandX().left();
		table.row();
		
		Label descLabel = new Label(description, skin);
		descLabel.setWrap(true);
		table.add(descLabel).expand().left().width(com.badlogic.gdx.scenes.scene2d.ui.Value.percentWidth(1, table));
		table.row();
		
		return table;
	}
	
	public void onTurn(float cost)
	{
		duration -= cost;
		
		for (AbstractOnTurnEffect effect : onTurnEffects)
		{
			effect.evaluate(this, cost);
		}
	}
	
	private void internalLoad(String name)
	{
		XmlReader xml = new XmlReader();
		Element xmlElement = null;
		
		try
		{
			xmlElement = xml.parse(Gdx.files.internal("StatusEffects/"+name+".xml"));
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
		
		this.name = xmlElement.get("Name", this.name);
		description = xmlElement.get("Description", description);
		
		icon = xmlElement.getChildByName("Icon") != null ? AssetManager.loadSprite(xmlElement.getChildByName("Icon")) : icon;
		continualEffect = xmlElement.getChildByName("ContinualEffect") != null ? AssetManager.loadSprite(xmlElement.getChildByName("ContinualEffect")) : continualEffect;
		duration = xmlElement.getFloat("Duration", duration);
		
		Element onTurnEffectsElement = xmlElement.getChildByName("OnTurnEffects");
		if (onTurnEffectsElement != null)
		{
			for (int i = 0; i < onTurnEffectsElement.getChildCount(); i++)
			{
				Element onTurnEffectElement = onTurnEffectsElement.getChild(i);
				onTurnEffects.add(AbstractOnTurnEffect.load(onTurnEffectElement));
			}
		}
	}
	
	public static StatusEffect load(String name)
	{
		StatusEffect se = new StatusEffect();
		
		se.internalLoad(name);
		
		return se;
	}
}

/*
IDEAS

DOT
HOT
STAT BUFF
STAT DEBUFF
BLOCK ON HIT
DRAIN
DELAYED EXPLOSION
TELEPORT ON HIT
SLEEP
INSANITY
TURN DELAY
HASTE
STOP

*/