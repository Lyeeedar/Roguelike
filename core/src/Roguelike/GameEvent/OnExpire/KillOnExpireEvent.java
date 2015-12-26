package Roguelike.GameEvent.OnExpire;

import Roguelike.Entity.Entity;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;

import java.util.HashMap;

/**
 * Created by Philip on 26/12/2015.
 */
public class KillOnExpireEvent extends AbstractOnExpireEvent
{
	@Override
	public boolean handle( Entity entity )
	{
		entity.HP = 0;

		return true;
	}

	@Override
	public void parse( XmlReader.Element xml )
	{
	}

	@Override
	public Array<String> toString( HashMap<String, Integer> variableMap )
	{
		Array<String> lines = new Array<String>(  );

		lines.add("Kill this entity");

		return lines;
	}
}
