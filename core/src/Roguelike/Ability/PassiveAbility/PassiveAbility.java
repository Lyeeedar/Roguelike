package Roguelike.Ability.PassiveAbility;

import java.io.IOException;

import Roguelike.AssetManager;
import Roguelike.Ability.IAbility;
import Roguelike.Entity.Entity;
import Roguelike.GameEvent.GameEventHandler;
import Roguelike.Sprite.Sprite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class PassiveAbility extends GameEventHandler implements IAbility
{
	public String Name;
	public String Description;
	public Sprite Icon;

	// ----------------------------------------------------------------------
	@Override
	public Sprite getIcon()
	{
		return Icon;
	}

	// ----------------------------------------------------------------------
	@Override
	public Table createTable( Skin skin, Entity entity )
	{
		Table table = new Table();

		table.add( new Label( Name, skin, "title" ) ).expandX().left();
		table.row();

		Label descLabel = new Label( Description, skin );
		descLabel.setWrap( true );
		table.add( descLabel ).expand().left().width( com.badlogic.gdx.scenes.scene2d.ui.Value.percentWidth( 1, table ) );
		table.row();

		table.add( new Label( "", skin ) );
		table.row();

		Label statLabel = new Label( String.join( "\n", toString( entity.getBaseVariableMap() ) ), skin );
		statLabel.setWrap( true );
		table.add( statLabel ).expand().left().width( com.badlogic.gdx.scenes.scene2d.ui.Value.percentWidth( 1, table ) );

		return table;
	}

	// ----------------------------------------------------------------------
	private void internalLoad( String name )
	{
		XmlReader xml = new XmlReader();
		Element xmlElement = null;

		try
		{
			xmlElement = xml.parse( Gdx.files.internal( "Abilities/Lines/" + name + ".xml" ) );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}

		internalLoad( xmlElement );
	}

	// ----------------------------------------------------------------------
	private void internalLoad( Element xml )
	{
		String extendsElement = xml.getAttribute( "Extends", null );
		if ( extendsElement != null )
		{
			internalLoad( extendsElement );
		}

		Name = xml.get( "Name", Name );
		Icon = xml.getChildByName( "Icon" ) != null ? AssetManager.loadSprite( xml.getChildByName( "Icon" ) ) : Icon;
		Description = xml.get( "Description", Description );

		Element eventsElement = xml.getChildByName( "Events" );
		if ( eventsElement != null )
		{
			super.parse( eventsElement );
		}
	}

	// ----------------------------------------------------------------------
	public static PassiveAbility load( String name )
	{
		PassiveAbility ab = new PassiveAbility();

		ab.internalLoad( name );

		return ab;
	}

	// ----------------------------------------------------------------------
	public static PassiveAbility load( Element xml )
	{
		PassiveAbility ab = new PassiveAbility();

		ab.internalLoad( xml );

		return ab;
	}

	@Override
	public String getName()
	{
		return Name;
	}

	@Override
	public String getDescription()
	{
		return Description;
	}

}
