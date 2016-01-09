package Roguelike.Ability.ActiveAbility.EffectType;

import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Entity.GameEntity;
import com.badlogic.gdx.utils.Array;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Global;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Fields.Field;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

import java.util.HashMap;

public class EffectTypeField extends AbstractEffectType
{
	public String condition;
	public String fieldName;
	public String stacksEqn;

	private String[] reliesOn;

	@Override
	public void parse( Element xml )
	{
		reliesOn = xml.getAttribute( "ReliesOn", "" ).toLowerCase().split( "," );
		condition = xml.getAttribute( "Condition", null );
		if ( condition != null )
		{
			condition = condition.toLowerCase();
		}
		fieldName = xml.getText();
		stacksEqn = xml.getAttribute( "Stacks", null );
		if ( stacksEqn != null )
		{
			stacksEqn = stacksEqn.toLowerCase();
		}
	}

	@Override
	public void update( ActiveAbility aa, float time, GameTile tile, GameEntity entity, EnvironmentEntity envEntity )
	{
		HashMap<String, Integer> variableMap = aa.getVariableMap();

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
			if ( conditionVal == 0 ) { return; }
		}

		int stacks = 1;

		if ( stacksEqn != null )
		{
			stacks = EquationHelper.evaluate( stacksEqn, variableMap );
		}

		if ( stacks > 0 )
		{
			Field field = Field.load( fieldName );
			field.trySpawnInTile( tile, stacks );
		}
	}

	@Override
	public AbstractEffectType copy()
	{
		EffectTypeField e = new EffectTypeField();
		e.condition = condition;
		e.fieldName = fieldName;
		e.stacksEqn = stacksEqn;
		e.reliesOn = reliesOn;
		return e;
	}

	@Override
	public Array<String> toString( ActiveAbility aa )
	{
		Array<String> lines = new Array<String>(  );
		lines.add( "Has a chance of creating " + fieldName );
		return lines;
	}
}
