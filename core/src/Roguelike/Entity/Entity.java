package Roguelike.Entity;

import java.util.EnumMap;
import java.util.HashMap;

import Roguelike.AssetManager;
import Roguelike.Global.Passability;
import Roguelike.Global.Statistic;
import Roguelike.GameEvent.GameEventHandler;
import Roguelike.Items.Inventory;
import Roguelike.Items.Item;
import Roguelike.Items.Item.EquipmentSlot;
import Roguelike.Lights.Light;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.SpriteEffect;
import Roguelike.StatusEffect.StatusEffect;
import Roguelike.Tiles.GameTile;
import Roguelike.Util.EnumBitflag;
import Roguelike.Util.FastEnumMap;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public abstract class Entity
{
	// ----------------------------------------------------------------------
	public abstract void update( float delta );

	// ----------------------------------------------------------------------
	public abstract int getStatistic( Statistic stat );

	// ----------------------------------------------------------------------
	protected abstract void internalLoad( String file );

	// ----------------------------------------------------------------------
	public abstract Array<GameEventHandler> getAllHandlers();

	// ----------------------------------------------------------------------
	protected void baseInternalLoad( Element xml )
	{
		name = xml.get( "Name", name );
		sprite = xml.getChildByName( "Sprite" ) != null ? AssetManager.loadSprite( xml.getChildByName( "Sprite" ) ) : sprite;

		Element lightElement = xml.getChildByName( "Light" );
		if ( lightElement != null )
		{
			light = Roguelike.Lights.Light.load( lightElement );
		}

		Element statElement = xml.getChildByName( "Statistics" );
		if ( statElement != null )
		{
			Statistic.load( statElement, statistics );
			HP = getStatistic( Statistic.MAXHP );

			statistics.put( Statistic.WALK, 1 );
			statistics.put( Statistic.ENTITY, 1 );
		}

		Element inventoryElement = xml.getChildByName( "Inventory" );
		if ( inventoryElement != null )
		{
			inventory.load( inventoryElement );
		}

		UID = getClass().getSimpleName() + " " + name + ": ID " + hashCode();
	}

	// ----------------------------------------------------------------------
	public int getVariable( Statistic stat )
	{
		return variableMap.get( stat.toString().toLowerCase() );
	}

	// ----------------------------------------------------------------------
	private EnumMap<Statistic, Integer> getStatistics()
	{
		EnumMap<Statistic, Integer> newMap = new EnumMap<Statistic, Integer>( Statistic.class );

		for ( Statistic stat : Statistic.values() )
		{
			newMap.put( stat, getStatistic( stat ) );
		}

		return newMap;
	}

	// ----------------------------------------------------------------------
	public HashMap<String, Integer> getVariableMap()
	{
		recalculateMaps();
		return variableMap;
	}

	// ----------------------------------------------------------------------
	public HashMap<String, Integer> getBaseVariableMap()
	{
		recalculateMaps();
		return baseVariableMap;
	}

	// ----------------------------------------------------------------------
	public void recalculateMaps()
	{
		if ( isVariableMapDirty )
		{
			isVariableMapDirty = false;
			calculateBaseVariableMap();
			calculateVariableMap();
			travelType = Passability.variableMapToTravelType( variableMap );
		}
	}

	// ----------------------------------------------------------------------
	protected void calculateVariableMap()
	{
		variableMap.clear();

		variableMap.put( "hp", HP );

		for ( EquipmentSlot slot : EquipmentSlot.values() )
		{
			Item equipped = inventory.getEquip( slot );
			if ( equipped != null && equipped.type != null )
			{
				variableMap.put( equipped.type, 1 );
			}
		}

		for ( StatusEffectStack s : stacks )
		{
			variableMap.put( s.effect.name.toLowerCase(), s.count );
		}

		for ( Statistic s : Statistic.values() )
		{
			variableMap.put( s.toString().toLowerCase(), getStatistic( s ) );
		}
	}

	// ----------------------------------------------------------------------
	protected void calculateBaseVariableMap()
	{
		baseVariableMap.clear();

		for ( Statistic s : Statistic.values() )
		{
			baseVariableMap.put( s.toString().toLowerCase(), statistics.get( s ) + inventory.getStatistic( Statistic.emptyMap, s ) );
		}

		baseVariableMap.put( "hp", HP );

		for ( EquipmentSlot slot : EquipmentSlot.values() )
		{
			Item equipped = inventory.getEquip( slot );
			if ( equipped != null && equipped.type != null )
			{
				baseVariableMap.put( equipped.type, 1 );
			}
		}

		for ( StatusEffectStack s : stacks )
		{
			baseVariableMap.put( s.effect.name.toLowerCase(), s.count );
		}
	}

	// ----------------------------------------------------------------------
	public Inventory getInventory()
	{
		return inventory;
	}

	// ----------------------------------------------------------------------
	public void applyDamage( int dam, Entity damager )
	{
		if ( !canTakeDamage || HP == 0 ) { return; }

		HP = Math.max( HP - dam, 0 );

		if ( HP == 0 )
		{
			for ( GameEventHandler handler : getAllHandlers() )
			{
				handler.onDeath( this, damager );
			}
		}

		damageAccumulator += dam;
		hasDamage = true;

		if ( dam != 0 )
		{
			isVariableMapDirty = true;
		}
	}

	// ----------------------------------------------------------------------
	public void applyHealing( int heal )
	{
		if ( !canTakeDamage ) { return; }

		int appliedHeal = Math.min( heal, getStatistic( Statistic.MAXHP ) - HP );
		HP += appliedHeal;

		healingAccumulator += appliedHeal;

		if ( heal != 0 )
		{
			isVariableMapDirty = true;
		}
	}

	// ----------------------------------------------------------------------
	public Array<StatusEffectStack> stackStatusEffects()
	{
		Array<StatusEffectStack> stacks = new Array<StatusEffectStack>();

		for ( StatusEffect se : statusEffects )
		{
			boolean found = false;
			for ( StatusEffectStack stack : stacks )
			{
				if ( stack.effect.name.equals( se.name ) )
				{
					stack.count++;
					found = true;
					break;
				}
			}

			if ( !found )
			{
				StatusEffectStack stack = new StatusEffectStack();
				stack.count = 1;
				stack.effect = se;

				stacks.add( stack );
			}
		}

		return stacks;
	}

	// ----------------------------------------------------------------------
	public void addStatusEffect( StatusEffect se )
	{
		if ( !canTakeDamage ) { return; }

		statusEffects.add( se );

		isVariableMapDirty = true;
	}

	// ----------------------------------------------------------------------
	public void removeStatusEffect( StatusEffect se )
	{
		statusEffects.removeValue( se, true );

		isVariableMapDirty = true;
	}

	// ----------------------------------------------------------------------
	public void removeStatusEffect( String se )
	{
		for ( int i = 0; i < statusEffects.size; i++ )
		{
			if ( statusEffects.get( i ).name.equals( se ) )
			{
				statusEffects.removeIndex( i );
				isVariableMapDirty = true;

				break;
			}
		}
	}

	// ----------------------------------------------------------------------
	public boolean isVariableMapDirty = true;

	// ----------------------------------------------------------------------
	protected HashMap<String, Integer> baseVariableMap = new HashMap<String, Integer>();
	protected HashMap<String, Integer> variableMap = new HashMap<String, Integer>();
	protected EnumBitflag<Passability> travelType = new EnumBitflag<Passability>();

	// ----------------------------------------------------------------------
	public int damageAccumulator = 0;
	public int healingAccumulator = 0;
	public boolean hasDamage = false;

	// ----------------------------------------------------------------------
	public FastEnumMap<Statistic, Integer> statistics = Statistic.getStatisticsBlock();
	public Array<StatusEffect> statusEffects = new Array<StatusEffect>( false, 16 );
	public Array<StatusEffectStack> stacks = new Array<StatusEffectStack>();
	public Inventory inventory = new Inventory();

	// ----------------------------------------------------------------------
	public String name;

	// ----------------------------------------------------------------------
	public String popup;
	public float popupDuration = 0;
	public float popupFade = 0;

	// ----------------------------------------------------------------------
	public Sprite sprite;
	public Light light;
	public Array<SpriteEffect> spriteEffects = new Array<SpriteEffect>( false, 16 );

	// ----------------------------------------------------------------------
	public GameTile tile;

	// ----------------------------------------------------------------------
	public int HP = 1;
	public int essence = 0;

	// ----------------------------------------------------------------------
	public boolean canTakeDamage = true;

	public String UID;

	// ----------------------------------------------------------------------
	public static class StatusEffectStack
	{
		public StatusEffect effect;
		public int count;
	}

}
