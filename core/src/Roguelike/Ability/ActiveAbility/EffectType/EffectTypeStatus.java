package Roguelike.Ability.ActiveAbility.EffectType;

import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Entity.Entity;
import Roguelike.GameEvent.Damage.DamageObject;
import Roguelike.GameEvent.Damage.StatusEvent;
import Roguelike.Global;
import Roguelike.StatusEffect.StatusEffect;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;
import exp4j.Helpers.EquationHelper;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.HashMap;

public class EffectTypeStatus extends AbstractEffectType
{
	public String condition;
	public Element statusData;
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
		statusData = xml;

		stacksEqn = xml.getAttribute( "Stacks", null );
		if ( stacksEqn != null )
		{
			stacksEqn = stacksEqn.toLowerCase();
		}
	}

	@Override
	public void update( ActiveAbility aa, float time, GameTile tile )
	{
		if ( tile.entity != null )
		{
			handle( aa, tile.entity );
		}

		if ( tile.environmentEntity != null )
		{
			handle( aa, tile.environmentEntity );
		}
	}

	public void handle( ActiveAbility aa, Entity target )
	{
		HashMap<String, Integer> variableMap = aa.getVariableMap();
		for ( String name : reliesOn )
		{
			if ( !variableMap.containsKey( name ) )
			{
				variableMap.put(name, 0);
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

		for ( int i = 0; i < stacks; i++ )
		{
			StatusEffect status = StatusEffect.load( statusData, aa );
			status.extraData.add( new Object[]{ "level", aa.tree.level } );
			aa.getCaster().addStatusEffect( status );
		}
	}

	@Override
	public AbstractEffectType copy()
	{
		EffectTypeStatus e = new EffectTypeStatus();
		e.condition = condition;
		e.statusData = statusData;
		e.stacksEqn = stacksEqn;
		e.reliesOn = reliesOn;
		return e;
	}

	@Override
	public Array<String> toString( ActiveAbility aa )
	{
		Array<String> lines = new Array<String>();

		lines.add( "Spawns a status:" );

		StatusEffect status = StatusEffect.load( statusData, aa );
		lines.addAll( status.toString( aa.getVariableMap(), false ) );

		return lines;
	}
}
