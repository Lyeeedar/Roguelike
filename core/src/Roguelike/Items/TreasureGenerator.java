package Roguelike.Items;

import Roguelike.Ability.AbilityTree;
import Roguelike.Sprite.Sprite;
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
			if ( type.equals( "currency" ) )
			{
				items.addAll( TreasureGenerator.generateCurrency( quality, MathUtils.random ) );
			}
			else if ( type.equals( "ability" ) )
			{
				items.addAll( TreasureGenerator.generateAbility( quality, MathUtils.random ) );
			}
			else if ( type.equals( "armour" ) )
			{
				items.addAll( TreasureGenerator.generateArmour( quality, MathUtils.random ) );
			}
			else if ( type.equals( "weapon" ) )
			{
				items.addAll( TreasureGenerator.generateWeapon( quality, MathUtils.random ) );
			}
			else if ( type.equals( "item" ) )
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
			else if ( type.equals( "random" ) )
			{
				items.addAll( TreasureGenerator.generateRandom( quality, MathUtils.random ) );
			}
		}

		return items;
	}

	public static Array<Item> generateRandom( int quality, Random ran )
	{
		Array<Item> items = new Array<Item>(  );

		// Chances,
		// Currency 3
		// Weapon 2
		// Armour 2
		// Ability 1

		int[] chances = {
			3, // currency
			3, // armour
			3, // weapons
			1 // abilities
		};

		int count = 0;
		for (int i : chances)
		{
			count += chances[i];
		}

		int chance = ran.nextInt( count );

		if ( chance < chances[0] )
		{
			items.addAll( TreasureGenerator.generateCurrency( quality, ran ) );
			return items;
		}
		chance -= chances[0];

		if ( chance < chances[1] )
		{
			items.addAll( TreasureGenerator.generateArmour( quality, ran ) );
			return items;
		}
		chance -= chances[1];

		if ( chance < chances[2] )
		{
			items.addAll( TreasureGenerator.generateWeapon( quality, ran ) );
			return items;
		}
		chance -= chances[2];

		if ( chance < chances[3] )
		{
			items.addAll( TreasureGenerator.generateAbility( quality, ran ) );
			return items;
		}

		return items;
	}

	public static Array<Item> generateCurrency( int quality, Random ran )
	{
		Array<Item> items = new Array<Item>(  );

		int val = ran.nextInt(100) * quality;

		int i = treasureTable.items.size - 1;

		while ( i >= 0 )
		{
			TreasureTable.TreasureItem treasure = treasureTable.items.get( i );

			if ( val >= treasure.value )
			{
				val -= treasure.value;

				Item item = Item.load( "Treasure/" + treasure.fileName );
				items.add(item);
			}
			else
			{
				i--;
			}
		}

		return items;
	}

	public static Array<Item> generateAbility( int quality, Random ran )
	{
		Array<Item> items = new Array<Item>(  );

		int maxQuality = Math.min( quality, abilityList.qualityData.size );
		int chosenQuality = ran.nextInt( maxQuality );

		int numChoices = abilityList.qualityData.get( chosenQuality ).size;
		int choice = ran.nextInt( numChoices );
		String chosen = abilityList.qualityData.get( chosenQuality ).get( choice );

		AbilityTree tree = new AbilityTree( chosen );

		Item item = new Item();
		item.ability = tree;

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
		Item materialItem = getMaterial( materialType, quality, ran );

		Item item = Recipe.createRecipe( recipe.recipeName, materialItem );

		int numModifiers = ran.nextInt( Math.max( 1, quality / 2 ) );
		int maxModifierQuality = Math.min( quality, modifierList.qualityData.size );

		while (numModifiers > 0)
		{
			int chosenQuality = ran.nextInt( maxModifierQuality );

			int numChoices = modifierList.qualityData.get( chosenQuality ).size;
			int choice = ran.nextInt( numChoices );
			String modifier = modifierList.qualityData.get( chosenQuality ).get( choice );

			Recipe.applyModifer( item, modifier, Math.max( 1, quality - chosenQuality), ran.nextBoolean() );

			numModifiers--;
		}

		return item;
	}

	public static Item getMaterial( String materialType, int quality, Random ran )
	{
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

		Item materialItem = null;
		if ( Gdx.files.internal( "Items/Material/"+material+".xml" ).exists() )
		{
			materialItem = Item.load( "Material/" + material );
		}
		else
		{
			materialItem = new Item();
			materialItem.name = material;
			materialItem.quality = materialQuality;
		}

		return materialItem;
	}

	public static final TreasureTable treasureTable = new TreasureTable( "Items/Treasure/TreasureTable.xml" );
	private static final QualityMap abilityList = new QualityMap( "Abilities/AbilityList.xml" );
	private static final QualityMap modifierList = new QualityMap( "Items/Modifiers/ModifierList.xml" );
	private static final HashMap<String, QualityMap> materialLists = new HashMap<String, QualityMap>(  );
	private static final RecipeList recipeList = new RecipeList( "Items/Recipes/Recipes.xml" );

	public static class TreasureTable
	{
		public static class TreasureItem
		{
			public final String fileName;
			public final String itemName;
			public final int value;

			public TreasureItem( String name, int value )
			{
				this.fileName = name;
				this.value = value;

				Item item = Item.load( "Treasure/" + fileName );
				itemName = item.name;
			}
		}

		public Array<TreasureItem> items = new Array<TreasureItem>(  );

		public TreasureTable( String path )
		{
			XmlReader reader = new XmlReader();
			XmlReader.Element xml = null;

			try
			{
				xml = reader.parse( Gdx.files.internal( path ) );
			}
			catch ( IOException e )
			{
				e.printStackTrace();
			}

			for ( int i = 0; i < xml.getChildCount(); i++ )
			{
				XmlReader.Element itemElement = xml.getChild( i );

				items.add( new TreasureItem( itemElement.getName(), Integer.parseInt( itemElement.getText() ) ) );
			}
		}

		public int getValueForCurrency( Item item )
		{
			int value = -1;

			for ( TreasureItem ti : items )
			{
				if ( ti.itemName.equals( item.name ) )
				{
					value = ti.value;
					break;
				}
			}

			return value;
		}

		public Sprite getCurrencySprite( Array<Item> items )
		{
			Sprite sprite = null;
			int bestValue = 0;

			for ( Item item : items )
			{
				int value = getValueForCurrency( item );

				if ( value == -1 )
				{
					return null;
				}

				if ( value > bestValue )
				{
					bestValue = value;
					sprite = item.getIcon();
				}
			}

			return sprite;
		}
	}

	private static class RecipeList
	{
		public Array<RecipeData> armourRecipes = new Array<RecipeData>(  );
		public Array<RecipeData> weaponRecipes = new Array<RecipeData>(  );

		public RecipeList( String path )
		{
			XmlReader reader = new XmlReader();
			XmlReader.Element xml = null;

			try
			{
				xml = reader.parse( Gdx.files.internal( path ) );
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
