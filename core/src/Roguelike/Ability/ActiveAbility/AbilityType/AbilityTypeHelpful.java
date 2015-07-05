package Roguelike.Ability.ActiveAbility.AbilityType;

import com.badlogic.gdx.utils.XmlReader.Element;

public class AbilityTypeHelpful extends AbstractAbilityType
{

	@Override
	public void processElements()
	{
	}

	@Override
	public void parse(Element xml)
	{
	}

	
	@Override
	public AbstractAbilityType copy()
	{
		return new AbilityTypeHelpful();
	}

}
