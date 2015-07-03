package Roguelike.Ability.ActiveAbility.EffectType;

import java.util.EnumMap;

import com.badlogic.gdx.utils.XmlReader.Element;

import Roguelike.Global.Tier1Element;

public class EffectTypeAttune extends AbstractEffectType
{
	public EnumMap<Tier1Element, Integer> elementMap = Tier1Element.getElementMap();
	
	public void parse(Element xml)
	{
		super.parse(xml);
		
		for (int i = 0; i < xml.getChildCount(); i++)
		{
			Element child = xml.getChild(i);
			Tier1Element element = Tier1Element.valueOf(child.getName().toUpperCase());
			
			elementMap.put(element, Integer.parseInt(child.getText()));
		}
	}
}
