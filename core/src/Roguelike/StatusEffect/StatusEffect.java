package Roguelike.StatusEffect;

import java.io.IOException;
import java.util.HashMap;

import Roguelike.AssetManager;
import Roguelike.Entity.Entity;
import Roguelike.GameEvent.GameEventHandler;
import Roguelike.GameEvent.IGameObject;
import Roguelike.Global;
import Roguelike.Sprite.Sprite;

import Roguelike.UI.Seperator;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public final class StatusEffect extends GameEventHandler
{
	public enum DurationType
	{
		TURN,
		PROCESSED,
		PERMANENT
	}

	public String name;
	private String description;

	public boolean stackable = true;

	public Sprite icon;
	public int duration = 1;
	public DurationType durationType = DurationType.TURN;

	@Override
	public void onTurn( Entity entity, float cost )
	{
		super.onTurn( entity, cost );

		if ( durationType == DurationType.TURN )
		{
			duration--;
		}
	}

	@Override
	public void processed()
	{
		if ( durationType == DurationType.PROCESSED )
		{
			duration--;
		}
	}

	public Table createTable( Skin skin, Entity entity )
	{
		Table table = new Table();

		table.add( new Label( name, skin ) ).expandX().left();
		table.row();

		Label descLabel = new Label( description, skin );
		descLabel.setWrap( true );
		table.add( descLabel ).expand().left().width( com.badlogic.gdx.scenes.scene2d.ui.Value.percentWidth( 1, table ) );
		table.row();

		table.add( new Label( "", skin ) );
		table.row();

		Array<String> lines = toString( entity.getVariableMap(), false );
		for (String line : lines)
		{
			if (line.equals( "---" ))
			{
				table.add( new Seperator( skin, false ) ).expandX().fillX();
			}
			else
			{
				Label lineLabel = new Label( line, skin );
				lineLabel.setWrap( true );
				table.add( lineLabel ).expandX().left().width( com.badlogic.gdx.scenes.scene2d.ui.Value.percentWidth( 1, table ) );
				table.row();
			}

			table.row();
		}

		return table;
	}

	private void internalLoad( String name )
	{
		XmlReader xml = new XmlReader();
		Element xmlElement = null;

		try
		{
			xmlElement = xml.parse( Gdx.files.internal( "StatusEffects/" + name + ".xml" ) );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}

		internalLoad( xmlElement );
	}

	private void internalLoad( Element xmlElement )
	{
		String extendsElement = xmlElement.getAttribute( "Extends", null );
		if ( extendsElement != null )
		{
			internalLoad( extendsElement );
		}

		this.name = xmlElement.get( "Name", this.name );
		description = xmlElement.get( "Description", description );

		icon = xmlElement.getChildByName( "Icon" ) != null ? AssetManager.loadSprite( xmlElement.getChildByName( "Icon" ) ) : icon;
		duration = xmlElement.getInt( "Duration", duration );

		String durationTypeString = xmlElement.get( "DurationType", null );
		if (durationTypeString != null)
		{
			durationType = DurationType.valueOf( durationTypeString.toUpperCase() );
		}
		stackable = xmlElement.getBoolean( "Stackable", stackable );

		Element eventsElement = xmlElement.getChildByName( "Events" );
		if ( eventsElement != null )
		{
			super.parse( eventsElement );
		}
	}

	public static StatusEffect load( String name )
	{
		StatusEffect se = new StatusEffect();

		se.internalLoad( name );

		return se;
	}

	public static StatusEffect load( Element xml, IGameObject parent )
	{
		StatusEffect se = new StatusEffect();

		if ( xml.getText() != null )
		{
			se.internalLoad( xml.getText() );
		}
		else
		{
			se.internalLoad( xml );

			if ( se.name == null )
			{
				se.name = parent != null ? parent.getName() : "Status";
			}

			if ( se.description == null )
			{
				se.description = parent != null ? parent.getDescription() : "";
			}

			if ( se.icon == null )
			{
				se.icon = parent != null ? parent.getIcon().copy() : AssetManager.loadSprite( "white" );
			}
		}

		return se;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	@Override
	public Sprite getIcon()
	{
		return icon;
	}
}

/*
 * IDEAS
 *
 * DOT HOT STAT BUFF STAT DEBUFF BLOCK ON HIT DRAIN DELAYED EXPLOSION TELEPORT
 * ON HIT SLEEP INSANITY TURN DELAY HASTE STOP
 */