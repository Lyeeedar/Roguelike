package Roguelike.Ability.PassiveAbility;

import java.io.IOException;
import java.util.EnumMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

import Roguelike.AssetManager;
import Roguelike.Ability.IAbility;
import Roguelike.GameEvent.GameEventHandler;
import Roguelike.Global.Statistics;
import Roguelike.Global.Tier1Element;
import Roguelike.Sprite.Sprite;

public class PassiveAbility extends GameEventHandler implements IAbility
{
	public String Name;
	public String Description;
	public Sprite Icon;

	//----------------------------------------------------------------------
	public Sprite getIcon()
	{
		return Icon;
	}
	
	//----------------------------------------------------------------------
	public Table createTable(Skin skin)
	{
		Table table = new Table();

		table.add(new Label(Name, skin)).expandX().left();
		table.row();

		Label descLabel = new Label(Description, skin);
		descLabel.setWrap(true);
		table.add(descLabel).expand().left().width(com.badlogic.gdx.scenes.scene2d.ui.Value.percentWidth(1, table));
		table.row();

		return table;
	}

	//----------------------------------------------------------------------
	private void internalLoad(String name)
	{
		XmlReader xml = new XmlReader();
		Element xmlElement = null;
		
		try
		{
			xmlElement = xml.parse(Gdx.files.internal("Abilities/Passive/"+name+".xml"));
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
		Icon = xmlElement.getChildByName("Icon") != null ? AssetManager.loadSprite(xmlElement.getChildByName("Icon")) : Icon;
		
		Element eventsElement = xmlElement.getChildByName("Events");
		if (eventsElement != null)
		{
			super.parse(eventsElement);
		}
	}
	
	//----------------------------------------------------------------------
	public static PassiveAbility load(String name)
	{
		PassiveAbility ab = new PassiveAbility();
		
		ab.internalLoad(name);
		
		return ab;
	}
	
}
