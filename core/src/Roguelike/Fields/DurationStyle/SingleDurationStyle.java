package Roguelike.Fields.DurationStyle;

import Roguelike.Fields.Field;

import com.badlogic.gdx.utils.XmlReader.Element;

public class SingleDurationStyle extends AbstractDurationStyle
{
	@Override
	public void update(float delta, Field field)
	{
		boolean collide = false;
		if (field.tile.entity != null && field.tile.entity.HP > 0)
		{
			collide = true;
		}
		
		if (field.tile.environmentEntity != null && field.tile.environmentEntity.canTakeDamage && field.tile.environmentEntity.HP > 0)
		{
			collide = true;
		}
		
		if (collide)
		{
			field.stacks = 0;
			field.onNaturalDeath();
		}
	}

	@Override
	public void parse(Element xml)
	{
	}

}
