package Roguelike.GameEvent.Damage;

import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.GameEvent.IGameObject;
import Roguelike.StatusEffect.StatusEffect;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

public class StatusEvent extends AbstractOnDamageEvent
{
	public String condition;
	public Element attackerStatus;
	public Element defenderStatus;
	public String stacksEqn;

	private String[] reliesOn;

	@Override
	public boolean handle( DamageObject obj, IGameObject parent )
	{
		if ( condition != null )
		{
			ExpressionBuilder expB = EquationHelper.createEquationBuilder( condition );
			obj.writeVariableNames( expB, reliesOn );

			Expression exp = EquationHelper.tryBuild( expB );
			if ( exp == null ) { return false; }

			obj.writeVariableValues( exp, reliesOn );

			double conditionVal = exp.evaluate();

			if ( conditionVal == 0 ) { return false; }
		}

		int stacks = 1;

		if ( stacksEqn != null )
		{
			ExpressionBuilder expB = EquationHelper.createEquationBuilder( stacksEqn );
			obj.writeVariableNames( expB, reliesOn );

			Expression exp = EquationHelper.tryBuild( expB );
			if ( exp != null )
			{
				obj.writeVariableValues( exp, reliesOn );

				stacks = (int) Math.ceil( exp.evaluate() );
			}
		}

		if ( attackerStatus != null )
		{
			for ( int i = 0; i < stacks; i++ )
			{
				obj.attacker.addStatusEffect( StatusEffect.load( attackerStatus, parent ) );
			}
		}

		if ( defenderStatus != null )
		{
			for ( int i = 0; i < stacks; i++ )
			{
				obj.defender.addStatusEffect( StatusEffect.load( defenderStatus, parent ) );
			}
		}

		return true;
	}

	@Override
	public void parse( Element xml )
	{
		reliesOn = xml.getAttribute( "ReliesOn", "" ).split( "," );
		condition = xml.getAttribute( "Condition", null );
		if ( condition != null )
		{
			condition = condition.toLowerCase();
		}
		attackerStatus = xml.getChildByName( "Attacker" );
		defenderStatus = xml.getChildByName( "Defender" );
		stacksEqn = xml.get( "Stacks", null );
		if ( stacksEqn != null )
		{
			stacksEqn = stacksEqn.toLowerCase();
		}
	}

	@Override
	public Array<String> toString( HashMap<String, Integer> variableMap, IGameObject parent )
	{
		Array<String> lines = new Array<String>();

		lines.add( "Spawns a status:" );

		if ( attackerStatus != null )
		{
			StatusEffect status = StatusEffect.load( attackerStatus, parent );
			lines.addAll( status.toString( variableMap ) );
		}
		else
		{
			StatusEffect status = StatusEffect.load( defenderStatus, parent );
			lines.addAll( status.toString( variableMap ) );
		}

		return lines;
	}

}
