package Roguelike.Items;

import Roguelike.GameEvent.Constant.ConstantEvent;
import Roguelike.Global;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.XmlReader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by Philip on 22-Dec-15.
 */
public class Recipe
{
	public static Item createRecipe( String recipe, Item material )
	{
		Item item = Item.load( "Recipes/"+recipe );
		item.quality = material.quality;

		applyQuality( item, material.quality );

		combineItems( item, material );

		item.name = material.name + " " + item.name;

		return item;
	}

	public static void applyModifer( Item item, String modifierName, int quality, boolean isPrefix )
	{
		Item modifier = loadModifier(modifierName, quality);

		combineItems( item, modifier );

		if (isPrefix)
		{
			item.name = modifier.name + " " + item.name;
		}
		else
		{
			item.name += " of the " + item.name;
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
		item1.onExpireEvents.addAll( item2.onExpireEvents );
	}

	public static Item loadModifier( String modifier, int quality )
	{
		Item item = Item.load( "Modifiers/"+modifier );

		applyQuality( item, quality );

		XmlReader reader = new XmlReader();
		XmlReader.Element xml = null;

		try
		{
			xml = reader.parse( Gdx.files.internal( "Items/Modifiers/" + modifier + ".xml" ) );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}

		XmlReader.Element namesElement = xml.getChildByName( "Names" );
		if (quality > namesElement.getChildCount())
		{
			quality = namesElement.getChildCount()-1;
		}

		String name = namesElement.getChild( quality ).getName();

		item.name = name;

		return item;
	}

	public static void applyQuality( Item item, int quality )
	{
		HashMap<String, Integer> variables = new HashMap<String, Integer>(  );
		variables.put( "quality", quality );

		for ( Global.Statistic stat : Global.Statistic.values() )
		{
			if ( item.constantEvent.equations.containsKey( stat ) )
			{
				item.constantEvent.putStatistic( stat, ""+item.constantEvent.getStatistic( variables, stat ) );
			}
		}
	}
}
