package Roguelike.Fields.OnTurnEffect;

import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Global;
import Roguelike.Global.Statistic;
import Roguelike.Entity.Entity;
import Roguelike.Fields.Field;
import Roguelike.Util.FastEnumMap;

import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

public class DamageOnTurnEffect extends AbstractOnTurnEffect
{
	private String condition;
	private String eqn;
	private String[] reliesOn;

	private void doDamage( Entity entity, Field field, float cost )
	{
		HashMap<String, Integer> variableMap = entity.getVariableMap();
		for ( String name : reliesOn )
		{
			if ( !variableMap.containsKey( name.toLowerCase() ) )
			{
				variableMap.put( name.toLowerCase(), 0 );
			}
		}
		variableMap.put( "stacks", field.stacks );

		if ( condition != null )
		{
			int conditionVal = EquationHelper.evaluate( condition, variableMap );
			if ( conditionVal == 0 ) { return; }
		}

		int raw = Math.round(EquationHelper.evaluate( eqn, variableMap ) * cost);

		Global.calculateDamage( entity, entity, raw, 0, false );
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
	public void process( Field field, Entity entity, float cost )
	{
		doDamage( entity, field, cost );
	}

}
