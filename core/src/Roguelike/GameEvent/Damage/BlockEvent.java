package Roguelike.GameEvent.Damage;

import Roguelike.Entity.Entity;
import Roguelike.GameEvent.IGameObject;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import exp4j.Helpers.EquationHelper;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.HashMap;

/**
 * Created by Philip on 21-Dec-15.
 */
public final class BlockEvent extends AbstractOnDamageEvent
{
	private String condition;
	private String[] reliesOn;

	@Override
	public boolean handle( Entity entity, DamageObject obj, IGameObject parent )
	{
		HashMap<String, Integer> variableMap = entity.getVariableMap();
		for ( String name : reliesOn )
		{
			if ( !variableMap.containsKey( name.toLowerCase() ) )
			{
				variableMap.put( name.toLowerCase(), 0 );
			}
		}
		variableMap.put( "damage", obj.damage );

		if ( condition != null )
		{
			int conditionVal = EquationHelper.evaluate( condition, variableMap );
			if ( conditionVal == 0 ) { return false; }
		}

		obj.damage = 0;

		return true;
	}

	@Override
	public void parse( XmlReader.Element xml )
	{
		reliesOn = xml.getAttribute( "ReliesOn", "" ).split( "," );
		condition = xml.getAttribute( "Condition", null );
		if ( condition != null )
		{
			condition = condition.toLowerCase();
		}
	}

	@Override
	public Array<String> toString( HashMap<String, Integer> variableMap, IGameObject parent )
	{
		Array<String> lines = new Array<String>();

		String line = "Blocks damage";
		lines.add( line );

		return lines;
	}
}
