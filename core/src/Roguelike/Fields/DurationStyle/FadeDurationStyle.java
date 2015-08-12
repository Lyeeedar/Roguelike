package Roguelike.Fields.DurationStyle;

import Roguelike.Fields.Field;

import com.badlogic.gdx.utils.XmlReader.Element;

public class FadeDurationStyle extends AbstractDurationStyle
{
	private float fadeRate;
	private boolean resetOnStack;
	private boolean resetOnEntity;

	@Override
	public void update(float delta, Field field)
	{
		float updateAccumulator = (Float)field.getData("DurationAccumulator", 0.0f);
		
		updateAccumulator += delta;
		
		if (resetOnStack)
		{
			if (field.stacks > 1) { updateAccumulator = 0; }
		}
		
		if (resetOnEntity)
		{
			if (field.tile.entity != null)
			{
				updateAccumulator = 0;
			}
			else if (field.tile.environmentEntity != null && field.tile.environmentEntity.canTakeDamage)
			{
				updateAccumulator = 0;
			}
		}
		
		while (updateAccumulator >= fadeRate && field.stacks > 0)
		{
			updateAccumulator -= fadeRate;
			field.stacks--;
			
			if (field.stacks == 0)
			{
				field.onNaturalDeath();
			}
		}
		
		field.setData("DurationAccumulator", updateAccumulator);
	}

	@Override
	public void parse(Element xml)
	{
		fadeRate = xml.getFloat("Rate", 5);
		resetOnStack = xml.getBoolean("ResetOnStacks", false);
		resetOnEntity = xml.getBoolean("ResetOnEntity", false);
	}

}
