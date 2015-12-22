package Roguelike.Items;

import Roguelike.Ability.AbilityTree;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;

import java.io.IOException;
import java.util.Random;

/**
 * Created by Philip on 22-Dec-15.
 */
public class TreasureGenerator
{
	public static Array<Item> generateLoot( int quality, String typeBlock, Random ran )
	{
		Array<Item> items = new Array<Item>(  );
		String[] types = typeBlock.toLowerCase().split( "," );

		for (String type : types)
		{
			int choice = MathUtils.random.nextInt( 2 );

			if ( type.equals( "currency" ) || ( type.equals( "random" ) && choice == 0 ) )
			{
				items.addAll( TreasureGenerator.generateCurrency( quality, MathUtils.random ) );
			}
			else if ( type.equals( "ability" ) || ( type.equals( "random" ) && choice == 1 ) )
			{
				items.addAll( TreasureGenerator.generateAbility( quality, MathUtils.random ) );
			}
		}

		return items;
	}

	public static Array<Item> generateCurrency( int quality, Random ran )
	{
		Array<Item> items = new Array<Item>(  );

		int val = ran.nextInt(100) * quality;

		while (val >= 100)
		{
			Item item = Item.load( "Treasure/GoldCoin" );
			items.add(item);
			val -= 100;
		}

		while (val >= 10)
		{
			Item item = Item.load( "Treasure/SilverCoin" );
			items.add(item);
			val -= 10;
		}

		while (val >= 1)
		{
			Item item = Item.load( "Treasure/CopperCoin" );
			items.add(item);
			val -= 1;
		}

		return items;
	}

	public static Array<Item> generateAbility( int quality, Random ran )
	{
		Array<Item> items = new Array<Item>(  );

		XmlReader reader = new XmlReader();
		XmlReader.Element xml = null;

		try
		{
			xml = reader.parse( Gdx.files.internal( "Abilities/AbilityList.xml" ) );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}

		Item item = null;

		while (item == null)
		{
			XmlReader.Element xmlData = xml.getChildByName( "Quality" + quality );
			if (xmlData != null)
			{
				int numChoices = xmlData.getChildCount();
				int choice = ran.nextInt( numChoices );
				XmlReader.Element chosenXml = xmlData.getChild( choice );

				AbilityTree tree = new AbilityTree( chosenXml.getName() );

				item = new Item();
				item.ability = tree;
			}
			else
			{
				quality--;
			}
		}

		items.add(item);

		return items;
	}
}
