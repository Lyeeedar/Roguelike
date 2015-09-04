package Roguelike.Items;

import java.io.IOException;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Global.Tier1Element;
import Roguelike.GameEvent.Constant.ConstantEvent;
import Roguelike.Items.Item.ItemCategory;
import Roguelike.Util.FastEnumMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Functions.RandomFunction;
import exp4j.Helpers.EquationHelper;
import exp4j.Operators.BooleanOperators;

public class Recipe
{
	public String name;

	public RecipeSlot[] slots;

	private Element itemTemplate;

	public Item generate( Item[] materials )
	{
		Item output = Item.load( itemTemplate );
		output.constantEvent = new ConstantEvent();

		for ( int i = 0; i < materials.length; i++ )
		{
			Item material = materials[i];
			RecipeSlot slot = slots[i];

			if ( material != null && slot != null )
			{
				slot.process( material, output );
			}
		}

		output.icon = output.getIcon().copy();
		output.icon.colour = new Color( Color.WHITE );

		Item mainMat = null;
		int max = 0;
		FastEnumMap<Tier1Element, Integer> totalElements = Tier1Element.getElementBlock();

		for ( Item mat : materials )
		{
			int total = 0;
			for ( Tier1Element el : Tier1Element.values() )
			{
				total += mat.elementalStats.get( el );
				totalElements.put( el, totalElements.get( el ) + mat.elementalStats.get( el ) );
			}

			if ( total > max )
			{
				max = total;
				mainMat = mat;
			}
		}

		for ( Tier1Element el : Tier1Element.values() )
		{
			int val = totalElements.get( el );

			Color col = new Color( Color.WHITE ).lerp( el.Colour, val / 1000.0f );

			output.icon.colour.mul( col );
		}

		output.name = mainMat.name + " " + name;

		return output;
	}

	protected void internalLoad( String path )
	{
		XmlReader xml = new XmlReader();
		Element xmlElement = null;

		try
		{
			xmlElement = xml.parse( Gdx.files.internal( "Recipes/" + path + ".xml" ) );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}

		name = xmlElement.get( "Name" );
		itemTemplate = xmlElement.getChildByName( "ItemTemplate" );

		Array<RecipeSlot> slotsArr = new Array<RecipeSlot>();

		Element slotsElement = xmlElement.getChildByName( "Slots" );
		for ( int i = 0; i < slotsElement.getChildCount(); i++ )
		{
			Element el = slotsElement.getChild( i );

			RecipeSlot slot = null;

			if ( el.getName().toLowerCase().equals( "damagedefense" ) )
			{
				slot = new DamageDefenseSlot();
			}
			else if ( el.getName().toLowerCase().equals( "piercehardiness" ) )
			{
				slot = new PierceHardinessSlot();
			}

			slot.parse( el );

			slotsArr.add( slot );
		}

		slots = slotsArr.toArray( RecipeSlot.class );
	}

	public static Recipe load( String path )
	{
		Recipe recipe = new Recipe();

		recipe.internalLoad( path );

		return recipe;
	}

	public static abstract class RecipeSlot
	{
		public FastEnumMap<Tier1Element, String> equations;

		public RecipeSlot()
		{
			equations = new FastEnumMap<Tier1Element, String>( Tier1Element.class );

			for ( Tier1Element el : Tier1Element.values() )
			{
				equations.put( el, "Value" );
			}
		}

		public abstract void process( Item material, Item targetItem );

		public void parse( Element xml )
		{
			for ( int i = 0; i < xml.getChildCount(); i++ )
			{
				Element xmlElement = xml.getChild( i );

				String name = xmlElement.getName().toUpperCase();

				if ( name.equals( "SCALE" ) )
				{
					for ( Tier1Element el : Tier1Element.values() )
					{
						equations.put( el, "Value*" + xmlElement.getText() );
					}
				}
				else
				{
					Tier1Element el = Tier1Element.valueOf( name );
					equations.put( el, xmlElement.getText() );
				}
			}
		}

		protected FastEnumMap<Tier1Element, Integer> process( FastEnumMap<Tier1Element, Integer> params )
		{
			FastEnumMap<Tier1Element, Integer> values = Tier1Element.getElementBlock();

			for ( Tier1Element el : Tier1Element.values() )
			{
				String eqn = equations.get( el );
				int val = params.get( el );

				ExpressionBuilder expB = new ExpressionBuilder( eqn );
				BooleanOperators.applyOperators( expB );
				expB.function( new RandomFunction() );
				expB.variable( "Value" );

				Expression exp = EquationHelper.tryBuild( expB );
				if ( exp != null )
				{
					exp.setVariable( "Value", val );
					val = (int) exp.evaluate();
				}

				values.put( el, val );
			}

			return values;
		}
	}

	public static class DamageDefenseSlot extends RecipeSlot
	{
		@Override
		public void process( Item material, Item targetItem )
		{
			FastEnumMap<Tier1Element, Integer> values = material.elementalStats;
			values = super.process( values );

			for ( Tier1Element el : Tier1Element.values() )
			{
				if ( targetItem.category == ItemCategory.WEAPON )
				{
					targetItem.getStatisticsObject().put( el.Attack, values.get( el ).toString() );
				}
				else
				{
					targetItem.getStatisticsObject().put( el.Defense, values.get( el ).toString() );
				}
			}
		}
	}

	public static class PierceHardinessSlot extends RecipeSlot
	{
		@Override
		public void process( Item material, Item targetItem )
		{
			FastEnumMap<Tier1Element, Integer> values = material.elementalStats;
			values = super.process( values );

			for ( Tier1Element el : Tier1Element.values() )
			{
				if ( targetItem.category == ItemCategory.WEAPON )
				{
					targetItem.getStatisticsObject().put( el.Pierce, values.get( el ).toString() );
				}
				else
				{
					targetItem.getStatisticsObject().put( el.Hardiness, "50" );
				}
			}
		}
	}

	// ---------- DEBUG STUFF
	public static Item generateItemForMaterial( Item mat )
	{
		Recipe recipe = Recipe.getRandomRecipe( true, true );

		int numMats = recipe.slots.length;
		Item[] materials = new Item[numMats];

		for ( int i = 0; i < numMats; i++ )
		{
			materials[i] = mat;
		}

		return recipe.generate( materials );
	}

	private static final Recipe[] WeaponRecipes = { Recipe.load( "Sword" ), Recipe.load( "Axe" ), Recipe.load( "Spear" ), Recipe.load( "Bow" ), Recipe.load( "Wand" ), };
	private static final Recipe[] ArmourRecipes = { Recipe.load( "Helm" ), Recipe.load( "Cuirass" ), Recipe.load( "Greaves" ) };

	public static Recipe getRandomRecipe( boolean weapon, boolean armour )
	{
		int max = 0;
		if ( weapon )
		{
			max += WeaponRecipes.length;
		}
		if ( armour )
		{
			max += ArmourRecipes.length;
		}

		int val = MathUtils.random( max - 1 );

		if ( weapon )
		{
			if ( val < WeaponRecipes.length )
			{
				return WeaponRecipes[val];
			}
			else
			{
				val -= WeaponRecipes.length;
			}
		}

		if ( armour ) { return ArmourRecipes[val]; }

		return null;
	}

}
