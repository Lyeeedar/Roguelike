package Roguelike.Fields.FieldInteractionTypes;

import Roguelike.Fields.Field;

import com.badlogic.gdx.utils.XmlReader.Element;

public class KillOtherFieldInteractionType extends AbstractFieldInteractionType
{

	@Override
	public Field process(Field src, Field dst)
	{
		return src;
	}

	@Override
	public void parse(Element xml)
	{
	}

}