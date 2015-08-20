package Roguelike.Ability;

import java.io.IOException;

import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Ability.PassiveAbility.PassiveAbility;
import Roguelike.Screens.GameScreen;
import Roguelike.Sprite.Sprite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class AbilityPool
{
	public final Array<AbilityLine> abilityLines = new Array<AbilityLine>( false, 16 );

	public boolean isVariableMapDirty = true;
	public PassiveAbility[] slottedPassiveAbilities = new PassiveAbility[Global.NUM_ABILITY_SLOTS];
	public ActiveAbility[] slottedActiveAbilities = new ActiveAbility[Global.NUM_ABILITY_SLOTS];

	// ----------------------------------------------------------------------
	public void update( float cost )
	{
		for ( ActiveAbility a : slottedActiveAbilities )
		{
			if ( a != null )
			{
				boolean gtz = a.cooldownAccumulator > 0;
				a.cooldownAccumulator -= cost;

				if ( gtz && a.cooldownAccumulator <= 0 )
				{
					GameScreen.Instance.addAbilityAvailabilityAction( a.Icon );
				}
			}
		}
	}

	// ----------------------------------------------------------------------
	public void clearActiveAbility( ActiveAbility aa )
	{
		if ( aa.cooldownAccumulator > 0 ) { return; }

		int index = getActiveAbilityIndex( aa );
		if ( index >= 0 )
		{
			slottedActiveAbilities[index] = null;
		}

		isVariableMapDirty = true;
	}

	// ----------------------------------------------------------------------
	public void clearPassiveAbility( PassiveAbility aa )
	{
		int index = getPassiveAbilityIndex( aa );
		if ( index >= 0 )
		{
			slottedPassiveAbilities[index] = null;
		}

		isVariableMapDirty = true;
	}

	// ----------------------------------------------------------------------
	public void slotActiveAbility( ActiveAbility aa, int index )
	{
		// if the target index is on cooldown, then cant swap
		if ( slottedActiveAbilities[index] != null && slottedActiveAbilities[index].cooldownAccumulator > 0 ) { return; }

		// check if aa is currently slotted
		int currentIndex = -1;
		for ( int i = 0; i < Global.NUM_ABILITY_SLOTS; i++ )
		{
			if ( slottedActiveAbilities[i] == aa )
			{
				currentIndex = i;
				break;
			}
		}

		// if is equipped, then swap the abilities without any waits
		if ( currentIndex >= 0 )
		{
			ActiveAbility temp = slottedActiveAbilities[index];
			slottedActiveAbilities[index] = aa;
			slottedActiveAbilities[currentIndex] = temp;
		}
		else
		{
			slottedActiveAbilities[index] = aa;
		}

		isVariableMapDirty = true;
	}

	// ----------------------------------------------------------------------
	public void slotPassiveAbility( PassiveAbility pa, int index )
	{
		for ( int i = 0; i < Global.NUM_ABILITY_SLOTS; i++ )
		{
			if ( slottedPassiveAbilities[i] == pa )
			{
				slottedPassiveAbilities[i] = null;
			}
		}

		slottedPassiveAbilities[index] = pa;

		isVariableMapDirty = true;
	}

	// ----------------------------------------------------------------------
	public int getActiveAbilityIndex( ActiveAbility aa )
	{
		for ( int i = 0; i < slottedActiveAbilities.length; i++ )
		{
			if ( slottedActiveAbilities[i] == aa ) { return i; }
		}

		return -1;
	}

	// ----------------------------------------------------------------------
	public int getPassiveAbilityIndex( PassiveAbility aa )
	{
		for ( int i = 0; i < slottedPassiveAbilities.length; i++ )
		{
			if ( slottedPassiveAbilities[i] == aa ) { return i; }
		}

		return -1;
	}

	// ----------------------------------------------------------------------
	public AbilityPool()
	{

	}

	// ----------------------------------------------------------------------
	public static AbilityPool createAbilityPool( String linesCSV )
	{
		AbilityPool pool = new AbilityPool();

		String[] lines = linesCSV.split( "," );
		for ( String line : lines )
		{
			line = line.trim();
			pool.loadAbilityLine( line + "/" + line );
		}

		return pool;
	}

	// ----------------------------------------------------------------------
	public void loadAbilityLine( String name )
	{
		abilityLines.add( AbilityLine.load( name ) );
	}

	// ----------------------------------------------------------------------
	public void addAbilityLine( AbilityLine line )
	{
		abilityLines.add( line );
	}

	// ----------------------------------------------------------------------
	public static class AbilityLine
	{
		public String fileName;
		public String name;
		public String description;
		public Sprite icon;

		public static AbilityLine load( String file )
		{
			XmlReader xml = new XmlReader();
			Element xmlElement = null;

			try
			{
				xmlElement = xml.parse( Gdx.files.internal( "Abilities/Lines/" + file + ".xml" ) );
			}
			catch ( IOException e )
			{
				e.printStackTrace();
			}

			AbilityLine newLine = new AbilityLine();
			newLine.fileName = file;

			newLine.name = xmlElement.get( "Name" );
			newLine.description = xmlElement.get( "Description" );

			newLine.icon = AssetManager.loadSprite( xmlElement.getChildByName( "Icon" ) );

			Element tiersElement = xmlElement.getChildByName( "Tiers" );
			for ( Element tierElement : tiersElement.getChildrenByName( "Tier" ) )
			{
				Ability[] tier = new Ability[5];

				for ( int i = 0; i < 5; i++ )
				{
					Element abilityElement = tierElement.getChild( i );

					Ability ab = new Ability();

					if ( abilityElement.getName().equals( "ActiveAbility" ) )
					{
						ab.ability = ActiveAbility.load( abilityElement.get( "Name" ) );
					}
					else
					{
						ab.ability = PassiveAbility.load( abilityElement.get( "Name" ) );
					}

					ab.cost = abilityElement.getInt( "Cost" );

					tier[i] = ab;
				}

				newLine.abilityTiers.add( tier );
			}

			return newLine;
		}

		public static Sprite getSprite( String file )
		{
			XmlReader xml = new XmlReader();
			Element xmlElement = null;

			try
			{
				xmlElement = xml.parse( Gdx.files.internal( "Abilities/Lines/" + file + ".xml" ) );
			}
			catch ( IOException e )
			{
				e.printStackTrace();
			}

			return AssetManager.loadSprite( xmlElement.getChildByName( "Icon" ) );
		}

		public final Array<Ability[]> abilityTiers = new Array<Ability[]>();
	}

	// ----------------------------------------------------------------------
	public static class Ability
	{
		public IAbility ability;
		public int cost;
		public boolean unlocked;
	}
}
