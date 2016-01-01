package Roguelike.Ability.PassiveAbility;

import java.io.IOException;
import java.util.HashMap;

import Roguelike.Ability.AbilityTree;
import Roguelike.AssetManager;
import Roguelike.Ability.IAbility;
import Roguelike.Entity.Entity;
import Roguelike.GameEvent.GameEventHandler;
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

public class PassiveAbility extends GameEventHandler implements IAbility
{
	public String Name;
	public String Description;
	public Sprite Icon;

	public AbilityTree.AbilityStage tree;

	// ----------------------------------------------------------------------
	@Override
	public void setCaster(Entity entity)
	{}

	// ----------------------------------------------------------------------
	@Override
	public void setTree(AbilityTree.AbilityStage tree )
	{
		this.tree = tree;
	}

	// ----------------------------------------------------------------------
	@Override
	protected void appendExtraVariables(HashMap<String, Integer> variableMap )
	{
		if (tree != null)
		{
			variableMap.put("level", tree.level);
		}
		else
		{
			variableMap.put("level", 1);
		}

		for (Object[] data : extraData)
		{
			variableMap.put( (String)data[0], (Integer)data[1] );
		}
	}

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

		Table header = new Table();

		header.add( new Label( Name, skin, "title" ) ).expandX().left();

		{
			Label label = new Label( "Passive", skin );
			label.setFontScale( 0.7f );
			header.add( label ).expandX().right();
		}
		table.add(header).expandX().fillX().left();

		table.row();

		String level = "Level: " + tree.level;

		if (tree.level == 10)
		{
			if (tree.branch1 != null)
			{
				level += " ( Mutate )";
			}
			else
			{
				level += " ( Max )";
			}
		}
		else
		{
			float per = (float) tree.exp / (float) tree.expToNextLevel;
			per *= 100;
			level += " ( " + (int)per + "% )";
		}

		table.add(new Label(level, skin)).left();
		table.row();

		Label descLabel = new Label( Description, skin );
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

	// ----------------------------------------------------------------------
	@Override
	public String getName()
	{
		return Name;
	}

	// ----------------------------------------------------------------------
	@Override
	public String getDescription()
	{
		return Description;
	}

	// ----------------------------------------------------------------------
	@Override
	public void onTurn()
	{
	}

	// ----------------------------------------------------------------------
	@Override
	public void setCooldown( int val )
	{
	}

	// ----------------------------------------------------------------------
	@Override
	public int getCooldown()
	{
		return 0;
	}

}
