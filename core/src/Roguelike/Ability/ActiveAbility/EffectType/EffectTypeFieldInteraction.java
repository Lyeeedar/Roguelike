package Roguelike.Ability.ActiveAbility.EffectType;

import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Entity.GameEntity;
import Roguelike.Fields.FieldInteractionTypes.AbstractFieldInteractionType;
import Roguelike.Util.FastEnumMap;
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

public class EffectTypeFieldInteraction extends AbstractEffectType
{
	public String condition;
	public String[] tags;
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
		tags = xml.getText().toLowerCase().split( "," );
		stacksEqn = xml.getAttribute( "Stacks", null );
		if ( stacksEqn != null )
		{
			stacksEqn = stacksEqn.toLowerCase();
		}
	}

	@Override
	public void update( ActiveAbility aa, float time, GameTile tile, GameEntity entity, EnvironmentEntity envEntity )
	{
		if ( !tile.hasFields )
		{
			return;
		}

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
			Field test = new Field();
			test.tags = tags;
			test.stacks = stacks;

			FastEnumMap<Field.FieldLayer, Field> fieldStore = new FastEnumMap<Field.FieldLayer, Field>( Field.FieldLayer.class );

			for ( Field.FieldLayer layer : Field.FieldLayer.values() )
			{
				Field tileField = tile.fields.get( layer );

				if ( tileField != null )
				{
					// First check for interaction on self
					Field srcField = tileField;
					Field dstField = test;
					AbstractFieldInteractionType interaction = Field.getInteraction( srcField.fieldInteractions, dstField );

					if ( interaction != null )
					{
						if ( !fieldStore.containsKey( layer ) )
						{
							fieldStore.put( layer, null );
						}

						tile.fields.put( layer, null );

						Field field = interaction.process( srcField, dstField );

						if ( field != null )
						{
							fieldStore.put( field.layer, field );
						}
					}
					else
					{
						fieldStore.put( tileField.layer, tileField );
					}
				}
			}

			for ( Field.FieldLayer layer : Field.FieldLayer.values() )
			{
				if ( fieldStore.containsKey( layer ) )
				{
					Field field = fieldStore.get( layer );

					if ( field == null )
					{
						tile.clearField( layer );
					}
					else
					{
						tile.addField( field );
					}
				}
			}
		}
	}

	@Override
	public AbstractEffectType copy()
	{
		EffectTypeFieldInteraction e = new EffectTypeFieldInteraction();
		e.condition = condition;
		e.tags = tags;
		e.stacksEqn = stacksEqn;
		e.reliesOn = reliesOn;
		return e;
	}

	@Override
	public Array<String> toString( ActiveAbility aa )
	{
		return new Array<String>(  );
	}
}
