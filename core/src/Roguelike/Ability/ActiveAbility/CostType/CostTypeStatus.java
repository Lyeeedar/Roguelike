package Roguelike.Ability.ActiveAbility.CostType;

import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Global;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Entity.Entity.StatusEffectStack;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

public class CostTypeStatus extends AbstractCostType
{
	private String status;
	private String stacksEqn;
	private String[] reliesOn;

	@Override
	public void parse( Element xml )
	{
		reliesOn = xml.getAttribute( "ReliesOn", "" ).split( "," );
		status = xml.getText().toLowerCase();
		stacksEqn = xml.getAttribute( "Stacks", "1" ).toLowerCase();
	}

	@Override
	public boolean isCostAvailable( ActiveAbility aa )
	{
		HashMap<String, Integer> variableMap = aa.getVariableMap();

		for ( String name : reliesOn )
		{
			if ( !variableMap.containsKey( name.toLowerCase() ) )
			{
				variableMap.put( name.toLowerCase(), 0 );
			}
		}

		Array<StatusEffectStack> stacks = aa.getCaster().stackStatusEffects();

		int count = 0;

		if ( Global.isNumber( stacksEqn ) )
		{
			count = Integer.parseInt( stacksEqn );
		}
		else
		{
			ExpressionBuilder expB = EquationHelper.createEquationBuilder( stacksEqn );
			EquationHelper.setVariableNames( expB, variableMap, "" );

			Expression exp = EquationHelper.tryBuild( expB );
			if ( exp == null ) { return false; }

			EquationHelper.setVariableValues( exp, variableMap, "" );

			count = (int) exp.evaluate();
		}

		StatusEffectStack stack = null;
		for ( StatusEffectStack s : stacks )
		{
			if ( s.effect.name.equals( status ) )
			{
				stack = s;
				break;
			}
		}

		if ( stack == null || stack.count < count ) { return false; }

		return true;
	}

	@Override
	public void spendCost( ActiveAbility aa )
	{
		HashMap<String, Integer> variableMap = aa.getVariableMap();

		for ( String name : reliesOn )
		{
			if ( !variableMap.containsKey( name.toLowerCase() ) )
			{
				variableMap.put( name.toLowerCase(), 0 );
			}
		}

		int count = 0;

		if ( Global.isNumber( stacksEqn ) )
		{
			count = Integer.parseInt( stacksEqn );
		}
		else
		{
			ExpressionBuilder expB = EquationHelper.createEquationBuilder( stacksEqn );
			EquationHelper.setVariableNames( expB, variableMap, "" );

			Expression exp = EquationHelper.tryBuild( expB );
			if ( exp == null ) { return; }

			EquationHelper.setVariableValues( exp, variableMap, "" );

			count = (int) exp.evaluate();
		}

		for ( int i = 0; i < count; i++ )
		{
			aa.getCaster().removeStatusEffect( status );
		}
	}

	@Override
	public AbstractCostType copy()
	{
		CostTypeStatus consume = new CostTypeStatus();
		consume.status = status;
		consume.stacksEqn = stacksEqn;
		consume.reliesOn = reliesOn;

		return consume;
	}

	@Override
	public String toString( ActiveAbility aa )
	{
		HashMap<String, Integer> variableMap = aa.getVariableMap();

		for ( String name : reliesOn )
		{
			if ( !variableMap.containsKey( name.toLowerCase() ) )
			{
				variableMap.put( name.toLowerCase(), 0 );
			}
		}

		Array<StatusEffectStack> stacks = aa.getCaster().stackStatusEffects();

		ExpressionBuilder expB = EquationHelper.createEquationBuilder( stacksEqn );
		EquationHelper.setVariableNames( expB, variableMap, "" );

		Expression exp = EquationHelper.tryBuild( expB );
		if ( exp == null ) { return "[RED]ERROR: Parsing equation '" + stacksEqn + "'"; }

		EquationHelper.setVariableValues( exp, variableMap, "" );

		int count = (int) exp.evaluate();

		StatusEffectStack stack = null;
		for ( StatusEffectStack s : stacks )
		{
			if ( s.effect.name.equals( status ) )
			{
				stack = s;
				break;
			}
		}

		String line = "";

		if ( stack == null || stack.count < count )
		{
			line += "[RED]";
		}
		else
		{
			line += "[GREEN]";
		}

		line += "Costs " + count + " " + status;

		return line;
	}
}
