package Roguelike.Ability.ActiveAbility.MovementType;

import com.badlogic.gdx.utils.XmlReader.Element;

public class MovementTypeBolt extends AbstractMovementType
{
	private int speed;
	private int pierce;

	@Override
	public void parse(Element xml)
	{
		super.parse(xml);
		
		speed = xml.getInt("Speed", 1);
		pierce = xml.getInt("Pierce", 0);
	}
}
