package Roguelike.GameEvent.OnHit;

import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Entity.Entity;
import Roguelike.Entity.GameEntity;
import Roguelike.Global;
import Roguelike.Tiles.GameTile;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import exp4j.Helpers.EquationHelper;

import java.util.HashMap;

/**
 * Created by Philip on 26-Feb-16.
 */
public class AbilityOnHitEvent extends AbstractOnHitEvent
{
	private String condition;
	private String[] reliesOn;
	private ActiveAbility ability;

	@Override
	public boolean handle( GameEntity entity, GameTile tile )
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

		aa.source = tile;

		aa.lockTarget( tile );

		boolean finished = aa.update();

		if ( !finished )
		{
			tile.level.addActiveAbility( aa );
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
		Array<String> lines = new Array<String>();

		lines.add( "Ability" );

		return lines;
	}
}
