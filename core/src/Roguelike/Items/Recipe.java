package Roguelike.Items;

import Roguelike.GameEvent.Constant.ConstantEvent;
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

		combineItems( item, material );

		item.name = material.name + " " + item.name;

		return item;
	}

	public static void applyModifer( Item item, String modifierName, boolean isPrefix )
	{
		Item modifier = Item.load( "Modifiers/"+modifierName );

		combineItems( item, modifier );

		if (isPrefix)
		{
			item.name = modifier.name + " " + item.name;
		}
		else
		{
			item.name += " of " + item.name;
		}
	}

	public static void combineItems( Item item1, Item item2 )
	{
		if ( item2.constantEvent != null )
		{
			if (item1.constantEvent == null)
			{
				item1.constantEvent = new ConstantEvent();
			}

			for ( Global.Statistic stat : Global.Statistic.values() )
			{
				if ( item2.constantEvent.equations.containsKey( stat ) )
				{
					int itemVal = item1.constantEvent.getStatistic( Global.Statistic.emptyMap, stat );
					int modifierVal = item2.constantEvent.getStatistic( Global.Statistic.emptyMap, stat );

					int newVal = itemVal + modifierVal;

					item1.constantEvent.putStatistic( stat, ""+newVal );
				}
			}
		}

		item1.onTurnEvents.addAll( item2.onTurnEvents );
		item1.onDealDamageEvents.addAll( item2.onDealDamageEvents );
		item1.onReceiveDamageEvents.addAll( item2.onReceiveDamageEvents );
		item1.onTaskEvents.addAll( item2.onTaskEvents );
		item1.onMoveEvents.addAll( item2.onMoveEvents );
		item1.onAttackEvents.addAll( item2.onAttackEvents );
		item1.onWaitEvents.addAll( item2.onWaitEvents );
		item1.onUseAbilityEvents.addAll( item2.onUseAbilityEvents );
		item1.onDeathEvents.addAll( item2.onDeathEvents );
	}
}
