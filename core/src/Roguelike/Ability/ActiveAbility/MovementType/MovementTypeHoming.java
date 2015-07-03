package Roguelike.Ability.ActiveAbility.MovementType;

import com.badlogic.gdx.utils.XmlReader.Element;

public class MovementTypeHoming extends AbstractMovementType
{
	private int speed;
	private int homingRate;

	@Override
	public void parse(Element xml)
	{
		super.parse(xml);
		
		speed = xml.getInt("Speed", 1);
		homingRate = xml.getInt("HomingRate", 1);
	}

}
