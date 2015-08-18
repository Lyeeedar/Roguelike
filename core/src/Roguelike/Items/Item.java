package Roguelike.Items;

import java.io.IOException;
import java.util.EnumMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.AssetManager;
import Roguelike.Global.Statistic;
import Roguelike.Global.Tier1Element;
import Roguelike.Entity.Entity;
import Roguelike.Entity.GameEntity;
import Roguelike.GameEvent.GameEventHandler;
import Roguelike.Lights.Light;
import Roguelike.Sound.SoundInstance;
import Roguelike.Sprite.Sprite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

public class Item extends GameEventHandler
{
	/*
	 * IDEAS:
	 *
	 * Unlock extra power after condition (absorb x essence, kill x enemy)
	 */

	// ----------------------------------------------------------------------
	public enum EquipmentSlot
	{
		// Armour
		HEAD,
		BODY,
		LEGS,

		// Jewelry
		EARRING,
		NECKLACE,
		RING,

		// Other
		LANTERN,

		// Weapons
		MAINWEAPON,
		OFFWEAPON
	}

	// ----------------------------------------------------------------------
	public enum ItemCategory
	{
		ARMOUR, WEAPON, JEWELRY, TREASURE, MATERIAL, MISC,

		ALL
	}

	// ----------------------------------------------------------------------
	public Item()
	{

	}

	public String name = "";
	public String description = "";
	public Sprite icon;
	public Sprite hitEffect;

	public Array<EquipmentSlot> slots = new Array<EquipmentSlot>();
	public ItemCategory category;
	public String type;

	public int count;
	public Light light;
	public boolean canDrop = true;
	public String dropChanceEqn;
	public EnumMap<Tier1Element, Integer> elementalStats = Tier1Element.getElementBlock();

	// ----------------------------------------------------------------------
	public int getRange( Entity entity )
	{
		int range = getStatistic( entity.getBaseVariableMap(), Statistic.RANGE );
		if ( range == 0 )
		{
			if ( type.equals( "spear" ) )
			{
				range = 2;
			}
			else if ( type.equals( "bow" ) || type.equals( "wand" ) )
			{
				range = 4;
			}
			else
			{
				range = 1;
			}
		}

		return range;
	}

	// ----------------------------------------------------------------------
	public boolean shouldDrop()
	{
		if ( dropChanceEqn == null ) { return true; }

		ExpressionBuilder expB = EquationHelper.createEquationBuilder( dropChanceEqn );

		Expression exp = EquationHelper.tryBuild( expB );
		if ( exp == null ) { return false; }

		double conditionVal = exp.evaluate();

		return conditionVal == 1;
	}

	// ----------------------------------------------------------------------
	public Table createTable( Skin skin, GameEntity entity )
	{
		Inventory inventory = entity.getInventory();

		if ( slots.contains( EquipmentSlot.MAINWEAPON, true ) )
		{
			Item other = inventory.getEquip( EquipmentSlot.MAINWEAPON );
			return createWeaponTable( other, entity, skin );
		}
		else if ( slots.size > 0 )
		{
			Item other = inventory.getEquip( slots.get( 0 ) );
			return createArmourTable( other, entity, skin );
		}

		Table table = new Table();

		table.add( new Label( getName(), skin ) ).expandX().left();
		table.row();

		Label descLabel = new Label( description, skin );
		descLabel.setWrap( true );
		table.add( descLabel ).expand().left().width( com.badlogic.gdx.scenes.scene2d.ui.Value.percentWidth( 1, table ) );
		table.row();

		return table;
	}

	// ----------------------------------------------------------------------
	private Table createArmourTable( Item other, GameEntity entity, Skin skin )
	{
		Table table = new Table();

		table.add( new Label( name, skin ) ).expandX().left();

		table.row();

		Label descLabel = new Label( description, skin );
		descLabel.setWrap( true );
		table.add( descLabel ).expand().left().width( com.badlogic.gdx.scenes.scene2d.ui.Value.percentWidth( 1, table ) );
		table.row();

		for ( Statistic stat : Statistic.values() )
		{
			int oldval = other == null ? 0 : other.getStatistic( entity.getBaseVariableMap(), stat );
			int newval = getStatistic( entity.getBaseVariableMap(), stat );

			if ( oldval != 0 || newval != 0 )
			{
				String statText = Statistic.formatString( stat.toString() ) + ": " + newval;

				if ( newval != oldval )
				{
					int diff = newval - oldval;

					if ( diff > 0 )
					{
						statText += "   [GREEN]+" + diff;
					}
					else
					{
						statText += "   [RED]" + diff;
					}
				}

				table.add( new Label( statText, skin ) ).expandX().left();
				table.row();
			}
		}

		return table;
	}

	// ----------------------------------------------------------------------
	private Table createWeaponTable( Item other, GameEntity entity, Skin skin )
	{
		Table table = new Table();

		table.add( new Label( name, skin ) ).expandX().left();

		{
			Label label = new Label( type, skin );
			label.setFontScale( 0.7f );
			table.add( label ).expandX().right();
		}

		table.row();

		Label descLabel = new Label( description, skin );
		descLabel.setWrap( true );
		table.add( descLabel ).expand().left().width( com.badlogic.gdx.scenes.scene2d.ui.Value.percentWidth( 1, table ) );
		table.row();

		int oldDam = 0;
		int newDam = 0;
		for ( Tier1Element el : Tier1Element.values() )
		{
			int oldval = other == null ? 0 : other.getStatistic( entity.getBaseVariableMap(), el.Attack );
			int newval = getStatistic( entity.getBaseVariableMap(), el.Attack );

			oldDam += oldval;
			newDam += newval;
		}

		String damText = "Damage: " + newDam;
		if ( newDam != oldDam )
		{
			int diff = newDam - oldDam;

			if ( diff > 0 )
			{
				damText += "   [GREEN]+" + diff;
			}
			else
			{
				damText += "   [RED]" + diff;
			}
		}

		table.add( new Label( damText, skin ) ).expandX().left().padBottom( 20 );
		table.row();

		for ( Statistic stat : Statistic.values() )
		{
			int oldval = other == null ? 0 : other.getStatistic( entity.getBaseVariableMap(), stat );
			int newval = getStatistic( entity.getBaseVariableMap(), stat );

			if ( oldval != 0 || newval != 0 )
			{
				String statText = Statistic.formatString( stat.toString() ) + ": " + newval;

				if ( newval != oldval )
				{
					int diff = newval - oldval;

					if ( diff > 0 )
					{
						statText += "   [GREEN]+" + diff;
					}
					else
					{
						statText += "   [RED]" + diff;
					}
				}

				table.add( new Label( statText, skin ) ).expandX().left();
				table.row();
			}
		}

		return table;
	}

	// ----------------------------------------------------------------------
	public static Item load( String name )
	{
		Item item = new Item();

		item.internalLoad( name );

		return item;
	}

	// ----------------------------------------------------------------------
	public static Item load( Element xml )
	{
		Item item = new Item();

		item.internalLoad( xml );

		return item;
	}

	// ----------------------------------------------------------------------
	private void internalLoad( String name )
	{
		XmlReader xml = new XmlReader();
		Element xmlElement = null;

		try
		{
			xmlElement = xml.parse( Gdx.files.internal( "Items/" + name + ".xml" ) );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}

		internalLoad( xmlElement );
	}

	// ----------------------------------------------------------------------
	private void internalLoad( Element xmlElement )
	{
		String extendsElement = xmlElement.getAttribute( "Extends", null );
		if ( extendsElement != null )
		{
			internalLoad( extendsElement );
		}

		name = xmlElement.get( "Name", name );
		description = xmlElement.get( "Description", description );

		Element iconElement = xmlElement.getChildByName( "Icon" );
		if ( iconElement != null )
		{
			icon = AssetManager.loadSprite( iconElement );
		}

		Element hitElement = xmlElement.getChildByName( "HitEffect" );
		if ( hitElement != null )
		{
			hitEffect = AssetManager.loadSprite( hitElement );
		}

		Element eventsElement = xmlElement.getChildByName( "Events" );
		if ( eventsElement != null )
		{
			super.parse( eventsElement );
		}

		Element elementElement = xmlElement.getChildByName( "Elements" );
		if ( elementElement != null )
		{
			Tier1Element.load( elementElement, elementalStats );
		}

		Element lightElement = xmlElement.getChildByName( "Light" );
		if ( lightElement != null )
		{
			light = Light.load( lightElement );
		}

		String slotsElement = xmlElement.get( "Slot" );
		if ( slotsElement != null )
		{
			String[] split = slotsElement.split( "," );
			for ( String s : split )
			{
				slots.add( EquipmentSlot.valueOf( s.toUpperCase() ) );
			}
		}
		category = xmlElement.get( "Category", null ) != null ? ItemCategory.valueOf( xmlElement.get( "Category" ).toUpperCase() ) : category;
		type = xmlElement.get( "Type", null ) != null ? xmlElement.get( "Type" ).toLowerCase() : type;
	}

	// ----------------------------------------------------------------------
	@Override
	public String getName()
	{
		if ( category == ItemCategory.MATERIAL ) { return name + " " + type; }

		return name;
	}

	// ----------------------------------------------------------------------
	@Override
	public String getDescription()
	{
		return description;
	}

	// ----------------------------------------------------------------------
	public EquipmentSlot getMainSlot()
	{
		return slots.size > 0 ? slots.get( 0 ) : null;
	}

	// ----------------------------------------------------------------------
	@Override
	public Sprite getIcon()
	{
		if ( icon != null ) { return icon; }

		EquipmentSlot slot = getMainSlot();

		if ( slot == EquipmentSlot.MAINWEAPON )
		{
			if ( type.equals( "sword" ) )
			{
				return AssetManager.loadSprite( "GUI/Sword" );
			}
			else if ( type.equals( "spear" ) )
			{
				return AssetManager.loadSprite( "GUI/Spear" );
			}
			else if ( type.equals( "axe" ) )
			{
				return AssetManager.loadSprite( "GUI/Axe" );
			}
			else if ( type.equals( "bow" ) )
			{
				return AssetManager.loadSprite( "GUI/Bow" );
			}
			else if ( type.equals( "wand" ) ) { return AssetManager.loadSprite( "GUI/Wand" ); }
		}
		else if ( slot == EquipmentSlot.OFFWEAPON )
		{
			if ( type.equals( "shield" ) )
			{
				return AssetManager.loadSprite( "GUI/Shield" );
			}
			else if ( type.equals( "torch" ) ) { return AssetManager.loadSprite( "GUI/Torch" ); }
		}
		else if ( slot == EquipmentSlot.HEAD )
		{
			return AssetManager.loadSprite( "GUI/Head" );
		}
		else if ( slot == EquipmentSlot.BODY )
		{
			return AssetManager.loadSprite( "GUI/Body" );
		}
		else if ( slot == EquipmentSlot.LEGS )
		{
			return AssetManager.loadSprite( "GUI/Legs" );
		}
		else if ( category == ItemCategory.MATERIAL )
		{
			if ( type.equals( "fabric" ) )
			{
				return AssetManager.loadSprite( "GUI/Fabric" );
			}
			else if ( type.equals( "hide" ) )
			{
				return AssetManager.loadSprite( "GUI/Hide" );
			}
			else if ( type.equals( "leather" ) )
			{
				return AssetManager.loadSprite( "GUI/Leather" );
			}
			else if ( type.equals( "ore" ) )
			{
				return AssetManager.loadSprite( "GUI/Ore" );
			}
			else if ( type.equals( "ingot" ) )
			{
				return AssetManager.loadSprite( "GUI/Ingot" );
			}
			else if ( type.equals( "log" ) )
			{
				return AssetManager.loadSprite( "GUI/Log" );
			}
			else if ( type.equals( "plank" ) )
			{
				return AssetManager.loadSprite( "GUI/Plank" );
			}
			else if ( type.equals( "bone" ) )
			{
				return AssetManager.loadSprite( "GUI/Bone" );
			}
			else if ( type.equals( "claw" ) )
			{
				return AssetManager.loadSprite( "GUI/Claw" );
			}
			else if ( type.equals( "fang" ) )
			{
				return AssetManager.loadSprite( "GUI/Fang" );
			}
			else if ( type.equals( "spine" ) )
			{
				return AssetManager.loadSprite( "GUI/Spine" );
			}
			else if ( type.equals( "scale" ) )
			{
				return AssetManager.loadSprite( "GUI/Scale" );
			}
			else if ( type.equals( "feather" ) )
			{
				return AssetManager.loadSprite( "GUI/Feather" );
			}
			else if ( type.equals( "shell" ) )
			{
				return AssetManager.loadSprite( "GUI/Shell" );
			}
			else if ( type.equals( "vial" ) )
			{
				return AssetManager.loadSprite( "GUI/Vial" );
			}
			else if ( type.equals( "sac" ) )
			{
				return AssetManager.loadSprite( "GUI/Sac" );
			}
			else if ( type.equals( "powder" ) )
			{
				return AssetManager.loadSprite( "GUI/Powder" );
			}
			else if ( type.equals( "crystal" ) )
			{
				return AssetManager.loadSprite( "GUI/Crystal" );
			}
			else if ( type.equals( "gem" ) ) { return AssetManager.loadSprite( "GUI/Gem" ); }
		}

		return AssetManager.loadSprite( "white" );
	}

	// ----------------------------------------------------------------------
	public Sprite getWeaponHitEffect()
	{
		if ( hitEffect != null ) { return hitEffect; }

		if ( type.equals( "sword" ) )
		{
			return AssetManager.loadSprite( "slash/slash", 0.1f );
		}
		else if ( type.equals( "spear" ) )
		{
			return AssetManager.loadSprite( "thrust/thrust", 0.1f );
		}
		else if ( type.equals( "axe" ) )
		{
			return AssetManager.loadSprite( "slash/slash", 0.1f );
		}
		else if ( type.equals( "bow" ) )
		{
			return AssetManager.loadSprite( "arrow", 0.1f );
		}
		else if ( type.equals( "wand" ) ) { return AssetManager.loadSprite( "bolt", 0.1f ); }

		return AssetManager.loadSprite( "strike/strike", 0.1f );
	}

	// ----------------------------------------------------------------------
	public SoundInstance getWeaponSound()
	{
		if ( hitEffect != null && hitEffect.sound != null ) { return hitEffect.sound; }

		if ( type.equals( "sword" ) )
		{
			return new SoundInstance( AssetManager.loadSound( "knife_stab" ) );
		}
		else if ( type.equals( "spear" ) )
		{
			return new SoundInstance( AssetManager.loadSound( "knife_stab" ) );
		}
		else if ( type.equals( "axe" ) )
		{
			return new SoundInstance( AssetManager.loadSound( "knife_stab" ) );
		}
		else if ( type.equals( "bow" ) )
		{
			return new SoundInstance( AssetManager.loadSound( "arrow_approaching_and_hitting_target" ) );
		}
		else if ( type.equals( "wand" ) ) { return new SoundInstance( AssetManager.loadSound( "arrow_approaching_and_hitting_target" ) ); }

		return new SoundInstance( AssetManager.loadSound( "knife_stab" ) );
	}
}
