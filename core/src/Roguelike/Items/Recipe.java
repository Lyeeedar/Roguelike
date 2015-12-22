package Roguelike.Items;

import Roguelike.Global;

import java.util.HashMap;

/**
 * Created by Philip on 22-Dec-15.
 */
public class Recipe
{
	public static Item createRecipe( String recipe, Item material )
	{
		Item item = Item.load( "Recipes/"+recipe );

		HashMap<String, Integer> variables = new HashMap<String, Integer>(  );
		variables.put( "quality", material.quality );

		for ( Global.Statistic stat : Global.Statistic.values() )
		{
			if ( item.constantEvent.equations.containsKey( stat ) )
			{
				item.constantEvent.putStatistic( stat, ""+item.constantEvent.getStatistic( variables, stat ) );
			}
		}

		item.name = material.name + " " + item.name;

		return item;
	}

	public static void applyModifer( Item item, String modifierName, boolean isPrefix )
	{
		Item modifier = Item.load( "Modifiers/"+modifierName );

		if ( modifier.constantEvent != null )
		{
			for ( Global.Statistic stat : Global.Statistic.values() )
			{
				if ( modifier.constantEvent.equations.containsKey( stat ) )
				{
					int itemVal = item.constantEvent.getStatistic( Global.Statistic.emptyMap, stat );
					int modifierVal = modifier.constantEvent.getStatistic( Global.Statistic.emptyMap, stat );

					int newVal = itemVal + modifierVal;

					item.constantEvent.putStatistic( stat, ""+newVal );
				}
			}
		}

		item.onTurnEvents.addAll( modifier.onTurnEvents );
		item.onDealDamageEvents.addAll( modifier.onDealDamageEvents );
		item.onReceiveDamageEvents.addAll( modifier.onReceiveDamageEvents );
		item.onTaskEvents.addAll( modifier.onTaskEvents );
		item.onMoveEvents.addAll( modifier.onMoveEvents );
		item.onAttackEvents.addAll( modifier.onAttackEvents );
		item.onWaitEvents.addAll( modifier.onWaitEvents );
		item.onUseAbilityEvents.addAll( modifier.onUseAbilityEvents );
		item.onDeathEvents.addAll( modifier.onDeathEvents );

		if (isPrefix)
		{
			item.name = modifier.name + " " + item.name;
		}
		else
		{
			item.name += " of " + item.name;
		}
	}
}
