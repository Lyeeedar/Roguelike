package Roguelike.GameEvent.OnExpire;

import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Entity.Entity;
import Roguelike.Global;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import exp4j.Helpers.EquationHelper;

import java.util.HashMap;

/**
 * Created by Philip on 15-Jan-16.
 */
public class AbilityOnExpireEvent extends AbstractOnExpireEvent
{
	private String condition;
	private String[] reliesOn;
	private ActiveAbility ability;

	@Override
	public boolean handle( Entity entity )
	{
		HashMap<String, Integer> variableMap = entity.getVariableMap();
		for ( String name : reliesOn )
		{
			if ( !variableMap.containsKey( name.toLowerCase() ) )
			{
				variableMap.put( name.toLowerCase(), 0 );
			}
		}

		if ( condition != null )
		{
			int conditionVal = EquationHelper.evaluate( condition, variableMap );
			if ( conditionVal == 0 ) { return false; }
		}

		ActiveAbility aa = ability.copy();

		aa.setCaster( entity );
		aa.setVariableMap( Global.Statistic.emptyMap );

		aa.source = entity.tile[0][0];

		aa.lockTarget( entity.tile[0][0] );

		boolean finished = aa.update();

		if ( !finished )
		{
			entity.tile[0][0].level.addActiveAbility( aa );
		}

		return true;
	}

	@Override
	public void parse( XmlReader.Element xml )
	{
		condition = xml.getAttribute( "Condition", null );
		if ( condition != null )
		{
			condition = condition.toLowerCase();
		}
		reliesOn = xml.getAttribute( "ReliesOn", "" ).split( "," );

		ability = ActiveAbility.load( xml );
	}

	@Override
	public Array<String> toString( HashMap<String, Integer> variableMap )
	{
		Array<String> lines = new Array<String>(  );

		lines.add("EXPLODE");

		return lines;
	}
}
