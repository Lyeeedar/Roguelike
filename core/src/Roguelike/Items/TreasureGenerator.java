package Roguelike.Items;

import Roguelike.Ability.AbilityTree;
import Roguelike.Sprite.Sprite;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
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
				items.addAll( TreasureGenerator.generateCurrency( quality, ran ) );
			}
			else if ( type.startsWith( "ability" ) )
			{
				String[] abilityParts = type.split( "[\\(\\)]" );
				String[] tags = new String[]{};

				if (abilityParts.length > 1)
				{
					tags = new String[]{ abilityParts[1] };
				}

				items.addAll( TreasureGenerator.generateAbility( quality, ran, tags ) );
			}
			else if ( type.equals( "armour" ) )
			{
				items.addAll( TreasureGenerator.generateArmour( quality, ran ) );
			}
			else if ( type.equals( "weapon" ) )
			{
				items.addAll( TreasureGenerator.generateWeapon( quality, ran ) );
			}
			else if ( type.equals( "item" ) )
			{
				if ( ran.nextBoolean() )
				{
					items.addAll( TreasureGenerator.generateArmour( quality, ran ) );
				}
				else
				{
					items.addAll( TreasureGenerator.generateWeapon( quality, ran ) );
				}
			}
			else if ( type.equals( "random" ) )
			{
				items.addAll( TreasureGenerator.generateRandom( quality, ran ) );
			}
			else if ( type.startsWith( "item(" ) )
			{
				String[] parts = type.split( "[\\(\\)]" );

				items.addAll( TreasureGenerator.generateItemFromMaterial( parts[1], quality, ran ) );
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
			0, // currency
			3, // armour
			3, // weapons
			2 // abilities
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
			items.addAll( TreasureGenerator.generateAbility( quality, ran, new String[]{} ) );
			return items;
		}

		return items;
	}

	public static Array<Item> generateCurrency( int quality, Random ran )
	{
		Array<Item> items = new Array<Item>(  );

		int val = ran.nextInt(100) * quality;

		Item money = Item.load( "Treasure/Money" );
		money.count = val;

		items.add(money);

		return items;
	}

	public static Array<Item> generateAbility( int quality, Random ran, String[] tags )
	{
		Array<Item> items = new Array<Item>(  );

		Array<QualityData> validList = new Array<QualityData>(  );

		for (int i = 0; i < quality && i < abilityList.qualityData.size; i++)
		{
			for (QualityData qd : abilityList.qualityData.get( i ))
			{
				boolean valid = true;
				for (String tag : tags)
				{
					if (!qd.tags.contains( tag ))
					{
						valid = false;
						break;
					}
				}

				if (valid)
				{
					validList.add( qd );
				}
			}
		}

		String chosen = validList.get( ran.nextInt( validList.size ) ).name;

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

	public static Array<Item> generateItemFromMaterial( String materialType, int quality, Random ran )
	{
		Array<RecipeData> validRecipes = new Array<RecipeData>(  );

		for (RecipeData recipe : recipeList.weaponRecipes)
		{
			if (recipe.acceptsMaterial( materialType ))
			{
				validRecipes.add( recipe );
			}
		}

		for (RecipeData recipe : recipeList.armourRecipes)
		{
			if (recipe.acceptsMaterial( materialType ))
			{
				validRecipes.add( recipe );
			}
		}

		Array<Item> items = new Array<Item>(  );

		if (validRecipes.size == 0)
		{
			return items;
		}

		RecipeData chosen = validRecipes.get( ran.nextInt( validRecipes.size ) );
		items.add( itemFromRecipe( chosen, quality, ran ) );

		return items;
	}

	public static Item itemFromRecipe( RecipeData recipe, int quality, Random ran )
	{
		String materialType = recipe.acceptedMaterials[ ran.nextInt( recipe.acceptedMaterials.length ) ];
		Item materialItem = getMaterial( materialType, quality, ran );

		Item item = Recipe.createRecipe( recipe.recipeName, materialItem );

		item.getIcon().colour.mul( materialItem.getIcon().colour );

		int numModifiers = ran.nextInt( Math.max( 1, quality / 2 ) );
		int maxModifierQuality = Math.min( quality, modifierList.qualityData.size );

		while (numModifiers > 0)
		{
			int chosenQuality = ran.nextInt( maxModifierQuality );

			int numChoices = modifierList.qualityData.get( chosenQuality ).size;
			int choice = ran.nextInt( numChoices );
			String modifier = modifierList.qualityData.get( chosenQuality ).get( choice ).name;

			Recipe.applyModifer( item, modifier, Math.max( 1, quality - chosenQuality), ran.nextBoolean() );

			numModifiers--;
		}

		return item;
	}

	public static Item getMaterial( String materialType, int quality, Random ran )
	{
		if (!materialLists.containsKey( materialType ))
		{
			if (Gdx.files.internal( "Items/Material/" + materialType + ".xml" ).exists())
			{
				QualityMap materialMap = new QualityMap( "Items/Material/" + materialType + ".xml" );
				materialLists.put( materialType, materialMap );
			}
			else
			{
				materialLists.put( materialType, null );
			}
		}

		QualityMap materialMap = materialLists.get( materialType );
		if (materialMap == null)
		{
			return null;
		}

		int materialQuality = Math.min( quality, materialMap.qualityData.size );

		String material = null;
		String colour = null;
		{
			int numChoices = materialMap.qualityData.get( materialQuality - 1 ).size;
			int choice = ran.nextInt( numChoices );
			QualityData qdata = materialMap.qualityData.get( materialQuality - 1 ).get( choice );
			material = qdata.name;
			colour = qdata.colour;
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

		if ( colour != null )
		{
			Color col = new Color();

			String[] cols = colour.split( "," );
			col.r = Float.parseFloat( cols[0] ) / 255.0f;
			col.g = Float.parseFloat( cols[1] ) / 255.0f;
			col.b = Float.parseFloat( cols[2] ) / 255.0f;
			col.a = 1;

			materialItem.getIcon().colour = col;
		}

		return materialItem;
	}

	private static final QualityMap abilityList = new QualityMap( "Abilities/AbilityList.xml" );
	private static final QualityMap modifierList = new QualityMap( "Items/Modifiers/ModifierList.xml" );
	private static final HashMap<String, QualityMap> materialLists = new HashMap<String, QualityMap>(  );
	private static final RecipeList recipeList = new RecipeList( "Items/Recipes/Recipes.xml" );

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

		public boolean acceptsMaterial( String material )
		{
			for (int i = 0; i < acceptedMaterials.length; i++)
			{
				if (acceptedMaterials[i].equalsIgnoreCase( material ))
				{
					return true;
				}
			}

			return false;
		}
	}

	private static class QualityMap
	{
		public Array<Array<QualityData>> qualityData = new Array<Array<QualityData>>(  );

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

				Array<QualityData> qualityLevel = new Array<QualityData>(  );

				for (int ii = 0; ii < qualityElement.getChildCount(); ii++)
				{
					XmlReader.Element qEl = qualityElement.getChild( ii );
					String name = qEl.getName();
					String colour = qEl.getAttribute( "RGB", null );
					String tagString = qEl.getText();
					if (tagString == null) { tagString = ""; }
					String[] tags = tagString.toLowerCase().split( "," );
					qualityLevel.add( new QualityData( name, colour, tags ) );
				}

				qualityData.add( qualityLevel );
			}
		}
	}

	private static class QualityData
	{
		public String name;
		public String colour;
		public ObjectSet<String> tags = new ObjectSet<String>(  );

		public QualityData( String name, String colour, String[] tags )
		{
			this.name = name;
			this.colour = colour;
			this.tags.addAll( tags );
		}
	}
}
