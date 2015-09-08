package Roguelike.Save;

import java.util.HashMap;

import Roguelike.Fields.Field;
import Roguelike.Tiles.Point;

public final class SaveField extends SaveableObject<Field>
{
	public String fileName;
	public HashMap<String, Object> data;
	public int stacks;
	public Point pos = new Point();

	@Override
	public void store( Field obj )
	{
		fileName = obj.fileName;
		data = (HashMap<String, Object>) obj.data.clone();
		stacks = obj.stacks;

		pos.set( obj.tile.x, obj.tile.y );
	}

	@Override
	public Field create()
	{
		Field field = Field.load( fileName );
		field.stacks = stacks;
		field.data = (HashMap<String, Object>) data.clone();

		return field;
	}

}
