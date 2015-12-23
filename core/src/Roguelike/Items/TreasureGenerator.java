package Roguelike.Items;

import Roguelike.Ability.AbilityTree;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;

import java.io.IOException;
import java.util.HashMap;
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
			int choice = MathUtils.random.nextInt( 5 );

			if ( type.equals( "currency" ) || ( type.equals( "random" ) && choice == 0 ) )
			{
				items.addAll( TreasureGenerator.generateCurrency( quality, MathUtils.random ) );
			}
			else if ( type.equals( "ability" ) || ( type.equals( "random" ) && choice == 1 ) )
			{
				items.addAll( TreasureGenerator.generateAbility( quality, MathUtils.random ) );
			}
			else if ( type.equals( "armour" ) || ( type.equals( "random" ) && choice == 2 ) )
			{
				items.addAll( TreasureGenerator.generateArmour( quality, MathUtils.random ) );
			}
			else if ( type.equals( "weapon" ) || ( type.equals( "random" ) && choice == 3 ) )
			{
				items.addAll( TreasureGenerator.generateWeapon( quality, MathUtils.random ) );
			}
			else if ( type.equals( "item" ) || ( type.equals( "random" ) && choice == 4 ) )
			{
				if ( ran.nextBoolean() )
				{
					items.addAll( TreasureGenerator.generateArmour( quality, MathUtils.random ) );
				}
				else
				{
					items.addAll( TreasureGenerator.generateWeapon( quality, MathUtils.random ) );
				}
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
		quality = quality - 1;

		Array<Item> items = new Array<Item>(  );

		Item item = null;

		while (item == null)
		{
			if (quality < abilityList.qualityData.size)
			{
				int numChoices = abilityList.qualityData.get( quality ).size;
				int choice = ran.nextInt( numChoices );
				String chosen = abilityList.qualityData.get( quality ).get( choice );

				AbilityTree tree = new AbilityTree( chosen );

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

	public static Array<Item> generateArmour( int quality, Random ran )
	{
		Array<Item> items = new Array<Item>(  );

		RecipeData recipe = recipeList.armourRecipes.get( ran.nextInt( recipeList.armourRecipes.size ) );

		items.add( itemFromRecipe( recipe, quality, ran ) );

		return items;
	}

	public static Array<Item> generateWeapon( int quality, Random ran )
	{
		Array<Item> items = new Array<Item>(  );

		RecipeData recipe = recipeList.weaponRecipes.get( ran.nextInt( recipeList.weaponRecipes.size ) );

		items.add( itemFromRecipe( recipe, quality, ran ) );

		return items;
	}

	public static Item itemFromRecipe( RecipeData recipe, int quality, Random ran )
	{
		String materialType = recipe.acceptedMaterials[ ran.nextInt( recipe.acceptedMaterials.length ) ];

		QualityMap materialMap = materialLists.get( materialType );
		if (materialMap == null)
		{
			materialMap = new QualityMap( "Items/Material/" + materialType + ".xml" );
			materialLists.put( materialType, materialMap );
		}

		int materialQuality = Math.min( quality, materialMap.qualityData.size );

		String material = null;
		{
			int numChoices = materialMap.qualityData.get( materialQuality - 1 ).size;
			int choice = ran.nextInt( numChoices );
			material = materialMap.qualityData.get( materialQuality - 1 ).get( choice );
		}

		Item item = Recipe.createRecipe( recipe.recipeName, Item.load( "Material/" + material ) );

		int numModifiers = ran.nextInt( Math.max( 1, quality / 2 ) );
		int maxModifierQuality = Math.min( quality, modifierList.qualityData.size );

		while (numModifiers > 0)
		{
			int chosenQuality = ran.nextInt( maxModifierQuality );

			int numChoices = modifierList.qualityData.get( chosenQuality ).size;
			int choice = ran.nextInt( numChoices );
			String modifier = modifierList.qualityData.get( chosenQuality ).get( choice );

			Recipe.applyModifer( item, modifier, quality, ran.nextBoolean() );

			numModifiers--;
		}

		return item;
	}



	private static final QualityMap abilityList = new QualityMap( "Abilities/AbilityList.xml" );
	private static final QualityMap modifierList = new QualityMap( "Items/Modifiers/ModifierList.xml" );
	private static final HashMap<String, QualityMap> materialLists = new HashMap<String, QualityMap>(  );
	private static final RecipeList recipeList = new RecipeList();

	private static class RecipeList
	{
		public Array<RecipeData> armourRecipes = new Array<RecipeData>(  );
		public Array<RecipeData> weaponRecipes = new Array<RecipeData>(  );

		public RecipeList()
		{
			XmlReader reader = new XmlReader();
			XmlReader.Element xml = null;

			try
			{
				xml = reader.parse( Gdx.files.internal( "Items/Recipes/Recipes.xml" ) );
			}
			catch ( IOException e )
			{
				e.printStackTrace();
			}

			XmlReader.Element armoursElement = xml.getChildByName( "Armours" );
			for (int i = 0; i < armoursElement.getChildCount(); i++)
			{
				XmlReader.Element armourElement = armoursElement.getChild( i );

				armourRecipes.add( new RecipeData( armourElement.getName(), armourElement.getText().split( "," ) ) );
			}

			XmlReader.Element weaponsElement = xml.getChildByName( "Weapons" );
			for (int i = 0; i < weaponsElement.getChildCount(); i++)
			{
				XmlReader.Element weaponElement = weaponsElement.getChild( i );

				weaponRecipes.add( new RecipeData( weaponElement.getName(), weaponElement.getText().split( "," ) ) );
			}
		}
	}

	private static class RecipeData
	{
		public String recipeName;
		public String[] acceptedMaterials;

		public RecipeData( String recipeName, String[] acceptedMaterials )
		{
			this.recipeName = recipeName;
			this.acceptedMaterials = acceptedMaterials;
		}
	}

	private static class QualityMap
	{
		public Array<Array<String>> qualityData = new Array<Array<String>>(  );

		public QualityMap( String file )
		{
			XmlReader reader = new XmlReader();
			XmlReader.Element xml = null;

			try
			{
				xml = reader.parse( Gdx.files.internal( file ) );
			}
			catch ( IOException e )
			{
				e.printStackTrace();
			}

			for (int i = 0; i < xml.getChildCount(); i++)
			{
				XmlReader.Element qualityElement = xml.getChild( i );

				Array<String> qualityLevel = new Array<String>(  );

				for (int ii = 0; ii < qualityElement.getChildCount(); ii++)
				{
					qualityLevel.add( qualityElement.getChild( ii ).getName() );
				}

				qualityData.add( qualityLevel );
			}
		}
	}
}
