package Roguelike.GameEvent.OnTask;

import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Global;
import Roguelike.Global.Statistic;
import Roguelike.Entity.Entity;
import Roguelike.Entity.Tasks.AbstractTask;
import Roguelike.GameEvent.IGameObject;
import Roguelike.Util.FastEnumMap;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

public final class DamageTaskEvent extends AbstractOnTaskEvent
{
	private String condition;
	private String eqn;
	private String[] reliesOn;

	@Override
	public boolean handle( Entity entity, AbstractTask task, IGameObject parent )
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

		int raw = EquationHelper.evaluate( eqn, variableMap );

		Global.calculateDamage( entity, entity, raw, entity.getVariable( Statistic.DEFENSE ), 0, false );

		return true;
	}

	@Override
	public void parse( Element xml )
	{
		condition = xml.getAttribute( "Condition", null );
		if ( condition != null )
		{
			condition = condition.toLowerCase();
		}

		reliesOn = xml.getAttribute( "ReliesOn", "" ).split( "," );

		eqn = xml.getText();
	}

	@Override
	public Array<String> toString( HashMap<String, Integer> variableMap, String eventType, IGameObject parent )
	{
		Array<String> lines = new Array<String>();

		lines.add( "Take " + eqn + " damage every turn" );

		return lines;
	}

}
