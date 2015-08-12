package Roguelike.Fields.FieldInteractionTypes;

import Roguelike.Fields.Field;

import com.badlogic.gdx.utils.XmlReader.Element;

public class KillThisFieldInteractionType extends AbstractFieldInteractionType
{

	@Override
	public Field process(Field src, Field dst)
	{
		return dst;
	}

	@Override
	public void parse(Element xml)
	{
	}

}
