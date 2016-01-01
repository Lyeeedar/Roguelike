package Roguelike.Ability.ActiveAbility.EffectType;

import com.badlogic.gdx.utils.Array;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Global;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Fields.Field;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

public class EffectTypeField extends AbstractEffectType
{
	public String condition;
	public String fieldName;
	public String stacksEqn;

	private String[] reliesOn;

	@Override
	public void parse( Element xml )
	{
		reliesOn = xml.getAttribute( "ReliesOn", "" ).split( "," );
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
	public void update( ActiveAbility aa, float time, GameTile tile )
	{
		if ( condition != null )
		{
			ExpressionBuilder expB = EquationHelper.createEquationBuilder( condition );
			EquationHelper.setVariableNames( expB, aa.getVariableMap(), "" );

			Expression exp = EquationHelper.tryBuild( expB );
			if ( exp == null ) { return; }

			EquationHelper.setVariableValues( exp, aa.getVariableMap(), "" );

			double conditionVal = exp.evaluate();

			if ( conditionVal == 0 ) { return; }
		}

		int stacks = 1;

		if ( stacksEqn != null )
		{
			if ( Global.isNumber( stacksEqn ) )
			{
				stacks = Integer.parseInt( stacksEqn );
			}
			else
			{
				ExpressionBuilder expB = EquationHelper.createEquationBuilder( stacksEqn );
				EquationHelper.setVariableNames( expB, aa.getVariableMap(), "" );

				Expression exp = EquationHelper.tryBuild( expB );
				if ( exp != null )
				{
					EquationHelper.setVariableValues( exp, aa.getVariableMap(), "" );

					stacks = (int) Math.ceil( exp.evaluate() );
				}
			}
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
