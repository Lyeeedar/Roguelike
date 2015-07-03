package Roguelike.Ability.ActiveAbility.EffectType;

import com.badlogic.gdx.utils.XmlReader.Element;

public class EffectTypeHeal extends AbstractEffectType
{
	private int power;
	
	@Override
	public void parse(Element xml)
	{
		super.parse(xml);
		
		power = xml.getInt("Power");
	}
}
