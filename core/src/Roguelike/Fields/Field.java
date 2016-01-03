package Roguelike.Fields;

import java.io.IOException;
import java.util.HashMap;

import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.Global.Passability;
import Roguelike.Entity.Entity;
import Roguelike.Fields.DurationStyle.AbstractDurationStyle;
import Roguelike.Fields.FieldInteractionTypes.AbstractFieldInteractionType;
import Roguelike.Fields.OnDeathEffect.AbstractOnDeathEffect;
import Roguelike.Fields.OnTurnEffect.AbstractOnTurnEffect;
import Roguelike.Fields.SpreadStyle.AbstractSpreadStyle;
import Roguelike.GameEvent.IGameObject;
import Roguelike.Lights.Light;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.TilingSprite;
import Roguelike.Tiles.GameTile;
import Roguelike.Util.EnumBitflag;
import Roguelike.Util.FastEnumMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class Field implements IGameObject
{
	/**
	 * match based on tags, not name (hot, wet, explosive, poisonous, gas)
	 * interation: propogate, spawn, die, none on urn, damage, heal, status,
	 * spawn duration, permanent, time, single modify passability: allow,
	 * restrict
	 *
	 * water spead fast flow fires turns it to steam single stacks disappear
	 * after time add swim passability
	 *
	 * steam blocks vision dissipates after short time
	 *
	 * smoke same as steam
	 *
	 * fire spreads to neighbouring entities does damage disppears after nbot
	 * doing damage for some turns when dies naturally turn into smoke
	 *
	 * electricity disappears after a few turns discharges into entity, high dam
	 * annihaltes with water, discharging into every connected water tile
	 *
	 * ice eveny movement gets doubled fire melts it, turning it into water
	 *
	 * lava flow slow spawns fire around it does high dam
	 *
	 * others poison bog/cobweb thuorns/razorcrystals explosive gas
	 */
	public enum FieldLayer
	{
		GROUND, AIR
	}

	public String fileName;
	public String fieldName;

	public String[] tags = new String[0];

	public GameTile tile;
	public Light light;

	public Sprite sprite;
	public TilingSprite tilingSprite;
	public FieldLayer layer = FieldLayer.GROUND;
	public int stacks = 1;

	public AbstractDurationStyle durationStyle;
	public AbstractSpreadStyle spreadStyle;
	public Array<AbstractOnTurnEffect> onTurnEffects = new Array<AbstractOnTurnEffect>();
	public HashMap<String, AbstractFieldInteractionType> fieldInteractions = new HashMap<String, AbstractFieldInteractionType>();
	public Array<AbstractOnDeathEffect> onDeathEffects = new Array<AbstractOnDeathEffect>();

	public HashMap<String, Object> data = new HashMap<String, Object>();

	public EnumBitflag<Passability> allowPassability = new EnumBitflag<Passability>();
	public EnumBitflag<Passability> restrictPassability = new EnumBitflag<Passability>();

	public void update( float cost )
	{
		if ( tile == null ) { return; } // we are dead

		float onTurnAccumulator = (Float) getData( "OnTurnAccumulator", 0.0f );
		onTurnAccumulator += cost;

		while ( onTurnAccumulator > 1 )
		{
			onTurnAccumulator -= 1;

			if ( tile.environmentEntity != null )
			{
				processOnTurnEffectsForEntity( tile.environmentEntity, 1 );
			}
		}

		setData( "OnTurnAccumulator", onTurnAccumulator );

		durationStyle.update( cost, this );

		if ( stacks > 0 )
		{
			spreadStyle.update( cost, this );
		}
	}

	public void processOnTurnEffectsForEntity( Entity entity, float cost )
	{
		for ( AbstractOnTurnEffect effect : onTurnEffects )
		{
			effect.process( this, entity, cost );
		}
	}

	public void onNaturalDeath()
	{
		for ( AbstractOnDeathEffect effect : onDeathEffects )
		{
			effect.process( this, tile );
		}
	}

	public void trySpawnInTile( GameTile newTile, int stacks )
	{
		Field newField = copy();
		newField.stacks = stacks;

		FastEnumMap<FieldLayer, Field> fieldStore = new FastEnumMap<FieldLayer, Field>( FieldLayer.class );

		for ( FieldLayer layer : FieldLayer.values() )
		{
			Field tileField = newTile.fields.get( layer );

			if ( tileField != null )
			{
				// if same field, increase stacks
				if ( tileField.fieldName.equals( fieldName ) )
				{
					tileField.stacks++;
					fieldStore.put( tileField.layer, tileField );
				}
				// if different field, interact
				else
				{
					// First check for interaction on self
					Field srcField = newField;
					Field dstField = tileField;
					AbstractFieldInteractionType interaction = getInteraction( srcField.fieldInteractions, dstField );
					if ( interaction == null )
					{
						srcField = tileField;
						dstField = newField;
						interaction = getInteraction( srcField.fieldInteractions, dstField );
					}

					if ( interaction != null )
					{
						if ( !fieldStore.containsKey( layer ) )
						{
							fieldStore.put( layer, null );
						}

						Field field = interaction.process( srcField, dstField );
						fieldStore.put( field.layer, field );
					}
					else
					{
						fieldStore.put( tileField.layer, tileField );
					}
				}
			}
		}

		if ( !fieldStore.containsKey( newField.layer ) )
		{
			Field tileField = newTile.fields.get( newField.layer );
			if ( tileField != null && tileField.stacks > 1 )
			{
				fieldStore.put( newField.layer, tileField );
			}
			else
			{
				fieldStore.put( newField.layer, newField );
			}
		}

		for ( FieldLayer layer : FieldLayer.values() )
		{
			if ( fieldStore.containsKey( layer ) )
			{
				Field field = fieldStore.get( layer );

				if ( field == null )
				{
					newTile.clearField( layer );
				}
				else
				{
					newTile.addField( field );
				}
			}
		}
	}

	private AbstractFieldInteractionType getInteraction( HashMap<String, AbstractFieldInteractionType> interactions, Field field )
	{
		if ( interactions.containsKey( field.fieldName ) ) { return interactions.get( field.fieldName ); }
		for ( String tag : field.tags )
		{
			if ( interactions.containsKey( tag ) ) { return interactions.get( tag ); }
		}
		return null;
	}

	public Object getData( String key, Object fallback )
	{
		if ( data.containsKey( key ) ) { return data.get( key ); }

		return fallback;
	}

	public void setData( String key, Object val )
	{
		data.put( key, val );
	}

	public Field copy()
	{
		Field field = new Field();
		field.fileName = fileName;
		field.fieldName = fieldName;
		field.tags = tags;
		field.sprite = sprite.copy();
		field.tilingSprite = tilingSprite;
		field.layer = layer;
		field.stacks = stacks;
		field.light = light != null ? light.copyNoFlag() : null;

		field.durationStyle = durationStyle;
		field.spreadStyle = spreadStyle;
		field.onTurnEffects = onTurnEffects;
		field.fieldInteractions = fieldInteractions;
		field.onDeathEffects = onDeathEffects;

		field.allowPassability = allowPassability;
		field.restrictPassability = restrictPassability;

		return field;
	}

	// ----------------------------------------------------------------------
	protected void internalLoad( String fileName )
	{
		XmlReader xmlReader = new XmlReader();
		Element xml = null;

		try
		{
			xml = xmlReader.parse( Gdx.files.internal( "Fields/" + fileName + ".xml" ) );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}

		String extendsElement = xml.getAttribute( "Extends", null );
		if ( extendsElement != null )
		{
			String[] split = extendsElement.split( "," );
			for ( String file : split )
			{
				internalLoad( file );
			}
		}

		String tagsElement = xml.get( "Tags", null );
		if ( tagsElement != null )
		{
			Array<String> tagArr = new Array<String>();
			if ( tags != null )
			{
				tagArr.addAll( tags );
			}

			String[] split = tagsElement.toLowerCase().split( "," );
			tagArr.addAll( split );

			tags = tagArr.toArray( String.class );
		}

		fieldName = xml.get( "FieldName", fieldName );

		Element spriteElement = xml.getChildByName( "Sprite" );
		if (spriteElement != null)
		{
			sprite = AssetManager.loadSprite( spriteElement );
		}

		Element tilingSpriteElement = xml.getChildByName( "TilingSprite" );
		if (tilingSpriteElement != null)
		{
			tilingSprite = TilingSprite.load( tilingSpriteElement );
		}

		Element lightElement = xml.getChildByName( "Light" );
		if ( lightElement != null )
		{
			light = Roguelike.Lights.Light.load( lightElement );
		}

		layer = xml.get( "Layer", null ) != null ? FieldLayer.valueOf( xml.get( "Layer" ).toUpperCase() ) : layer;

		Element durationElement = xml.getChildByName( "Duration" );
		if ( durationElement != null )
		{
			durationStyle = AbstractDurationStyle.load( durationElement.getChild( 0 ) );
		}

		Element spreadElement = xml.getChildByName( "Spread" );
		if ( spreadElement != null )
		{
			spreadStyle = AbstractSpreadStyle.load( spreadElement.getChild( 0 ) );
		}

		Element onTurnElement = xml.getChildByName( "OnTurn" );
		if ( onTurnElement != null )
		{
			for ( int i = 0; i < onTurnElement.getChildCount(); i++ )
			{
				Element effectElement = onTurnElement.getChild( i );
				AbstractOnTurnEffect effect = AbstractOnTurnEffect.load( effectElement );
				onTurnEffects.add( effect );
			}
		}

		Element onDeathElement = xml.getChildByName( "OnDeath" );
		if ( onDeathElement != null )
		{
			for ( int i = 0; i < onDeathElement.getChildCount(); i++ )
			{
				Element deathElement = onDeathElement.getChild( i );
				AbstractOnDeathEffect death = AbstractOnDeathEffect.load( deathElement );
				onDeathEffects.add( death );
			}
		}

		Element interactionsElement = xml.getChildByName( "Interactions" );
		if ( interactionsElement != null )
		{
			for ( int i = 0; i < interactionsElement.getChildCount(); i++ )
			{
				Element interactionEl = interactionsElement.getChild( i );

				String key = interactionEl.getName();
				Element content = interactionEl.getChild( 0 );

				fieldInteractions.put( key.toLowerCase(), AbstractFieldInteractionType.load( content ) );
			}
		}

		String allowString = xml.get( "Allow", null );
		if ( allowString != null )
		{
			EnumBitflag<Passability> pass = Passability.parse( allowString );
			allowPassability.setAll( pass );
		}

		String restrictString = xml.get( "Restrict", null );
		if ( restrictString != null )
		{
			EnumBitflag<Passability> pass = Passability.parse( restrictString );
			restrictPassability.setAll( pass );
		}
	}

	// ----------------------------------------------------------------------
	public static Field load( String name )
	{
		Field f = new Field();
		f.fileName = name;

		f.internalLoad( name );

		return f;
	}

	@Override
	public String getName()
	{
		return fieldName;
	}

	@Override
	public String getDescription()
	{
		return "";
	}

	@Override
	public Sprite getIcon()
	{
		if (sprite != null)
		{
			return sprite;
		}

		return tilingSprite.getSprite( new EnumBitflag<Global.Direction>(  ) );
	}
}
