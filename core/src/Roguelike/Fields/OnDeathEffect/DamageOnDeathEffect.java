package Roguelike.Fields.OnDeathEffect;

import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Global;
import Roguelike.Global.Statistic;
import Roguelike.Entity.Entity;
import Roguelike.Fields.Field;
import Roguelike.Tiles.GameTile;
import Roguelike.Util.FastEnumMap;

import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

public class DamageOnDeathEffect extends AbstractOnDeathEffect
{
	private String condition;
	private String eqn;
	private String[] reliesOn;

	@Override
	public void process( Field field, GameTile tile )
	{
		if ( tile == null ) { return; }

		if ( tile.entity != null )
		{
			doDamage( tile.entity, field );
		}

		if ( tile.environmentEntity != null && tile.environmentEntity.canTakeDamage )
		{
			doDamage( tile.environmentEntity, field );
		}
	}

	private void doDamage( Entity entity, Field field )
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
			ExpressionBuilder expB = EquationHelper.createEquationBuilder( condition );
			EquationHelper.setVariableNames( expB, variableMap, "" );

			Expression exp = EquationHelper.tryBuild( expB );
			if ( exp == null ) { return; }

			EquationHelper.setVariableValues( exp, variableMap, "" );

			double conditionVal = exp.evaluate();

			if ( conditionVal == 0 ) { return; }
		}

		int raw = 0;

		if (Global.isNumber( eqn ))
		{
			raw = Integer.parseInt( eqn );
		}
		else
		{
			ExpressionBuilder expB = EquationHelper.createEquationBuilder( eqn );
			EquationHelper.setVariableNames( expB, variableMap, "" );

			Expression exp = EquationHelper.tryBuild( expB );
			if ( exp != null )
			{
				EquationHelper.setVariableValues( exp, variableMap, "" );
				raw = (int) exp.evaluate();
			}
		}

		Global.calculateDamage( entity, entity, raw, entity.getVariable( Statistic.DEFENSE ), true );
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

}
