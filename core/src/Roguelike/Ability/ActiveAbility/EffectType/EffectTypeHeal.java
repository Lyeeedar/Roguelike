package Roguelike.Ability.ActiveAbility.EffectType;

import java.util.HashMap;

import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Entity.GameEntity;
import com.badlogic.gdx.utils.Array;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Global;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Entity.Entity;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

public class EffectTypeHeal extends AbstractEffectType
{
	private String equation;
	private String[] reliesOn;

	@Override
	public void parse( Element xml )
	{
		reliesOn = xml.getAttribute( "ReliesOn", "" ).split( "," );
		equation = xml.getText().toLowerCase();
	}

	@Override
	public void update( ActiveAbility aa, float time, GameTile tile, GameEntity entity, EnvironmentEntity envEntity )
	{
		if ( entity != null )
		{
			applyToEntity( entity, aa );
		}

		if ( envEntity != null )
		{
			applyToEntity( envEntity, aa );
		}
	}

	private void applyToEntity( Entity e, ActiveAbility aa )
	{
		int raw = getHealing( aa );
		e.applyHealing( raw );
	}

	private int getHealing( ActiveAbility aa )
	{
		HashMap<String, Integer> variableMap = aa.getVariableMap();

		for ( String name : reliesOn )
		{
			if ( !variableMap.containsKey( name.toLowerCase() ) )
			{
				variableMap.put( name.toLowerCase(), 0 );
			}
		}

		return EquationHelper.evaluate( equation, variableMap );
	}

	@Override
	public AbstractEffectType copy()
	{
		EffectTypeHeal heal = new EffectTypeHeal();
		heal.equation = equation;
		heal.reliesOn = reliesOn;

		return heal;
	}

	@Override
	public Array<String> toString( ActiveAbility aa )
	{
		Array<String> lines = new Array<String>(  );
		lines.add( "Heals " + getHealing( aa ) + " health" );
		return lines;
	}
}
