package Roguelike.Save;

import java.util.HashMap;

import Roguelike.Fields.Field;

public class SaveField extends SaveableObject<Field>
{
	public String fileName;
	public HashMap<String, Object> data;
	public int stacks;
	public int[] pos;
	
	@Override
	public void store(Field obj)
	{
		fileName = obj.fileName;
		data = (HashMap<String, Object>)obj.data.clone();
		stacks = obj.stacks;
		
		pos = new int[]{obj.tile.x, obj.tile.y};
	}

	@Override
	public Field create()
	{
		Field field = Field.load(fileName);
		field.stacks = stacks;
		field.data = (HashMap<String, Object>)data.clone();
		
		return field;
	}

}
