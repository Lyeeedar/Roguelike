package Roguelike.Ability;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Ability.PassiveAbility.PassiveAbility;
import Roguelike.Entity.Entity;
import Roguelike.Global;
import Roguelike.Screens.GameScreen;
import Roguelike.UI.Seperator;
import Roguelike.UI.Tooltip;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.XmlReader;

import java.io.IOException;

/**
 * Created by Philip on 15-Dec-15.
 */
public class AbilityTree
{
	public String treePath;

	public AbilityStage root;
	public AbilityStage current;

	public AbilityTree( IAbility ability )
	{
		root = new AbilityStage( this, ability );
		current = root;
	}

	public AbilityTree( String treePath )
	{
		this.treePath = treePath;

		XmlReader xmlReader = new XmlReader();
		XmlReader.Element treeElement = null;

		try
		{
			treeElement = xmlReader.parse( Gdx.files.internal( "Abilities/" + treePath + "/AbilityTree.xml" ) );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}

		root = new AbilityStage( this, treeElement );
		current = root;
	}

	public static class AbilityStage
	{
		public AbilityTree tree;

		public IAbility current;

		public int level = 1;
		public int expToNextLevel = 100;
		public int exp;

		public boolean needsLevelAnim;

		public AbilityStage branch1;
		public AbilityStage branch2;

		public AbilityStage( AbilityTree tree, IAbility ability )
		{
			this.tree = tree;
			this.current = ability;
			ability.setTree(this);
		}

		public AbilityStage( AbilityTree tree, XmlReader.Element xml )
		{
			this.tree = tree;
			parse( xml );
		}

		public void gainExp(int _exp)
		{
			if (level == 10) { return; }

			exp += _exp;

			while (exp >= expToNextLevel && level < 10)
			{
				level++;
				exp -= expToNextLevel;
				expToNextLevel *= 1.3f;
				needsLevelAnim = true;
			}
		}

		public void mutate( Skin skin, Entity entity, Stage stage )
		{
			Table table = new Table();

			branch1.current.setCaster(entity);
			branch2.current.setCaster(entity);

			table.add( branch1.current.createTable( skin, entity ) ).expand().fillX().top();
			table.add( new Seperator( skin, true ) ).expandY().fillY().pad( 10 );
			table.add( branch2.current.createTable( skin, entity ) ).expand().fillX().top();
			table.row();

			TextButton branch1Button = new TextButton( ""+branch1.current.getName(), skin);
			branch1Button.addListener( new InputListener()
			{

				@Override
				public boolean touchDown( InputEvent event, float x, float y, int pointer, int button )
				{
					return true;
				}

				@Override
				public void touchUp( InputEvent event, float x, float y, int pointer, int button )
				{
					GameScreen.Instance.lockContextMenu = false;
					GameScreen.Instance.clearContextMenu();
					tree.current = branch1;
				}
			} );

			TextButton branch2Button = new TextButton( ""+branch2.current.getName(), skin );
			branch2Button.addListener( new InputListener()
			{

				@Override
				public boolean touchDown( InputEvent event, float x, float y, int pointer, int button )
				{
					return true;
				}

				@Override
				public void touchUp( InputEvent event, float x, float y, int pointer, int button )
				{
					GameScreen.Instance.lockContextMenu = false;
					GameScreen.Instance.clearContextMenu();
					tree.current = branch2;
				}
			} );

			table.add( branch1Button ).expand().center();
			table.add( new Table() );
			table.add( branch2Button ).expand().center();

			table.pack();

			GameScreen.Instance.displayContextMenu( table, true );
		}

		public void parse( XmlReader.Element xml )
		{
			String abilityPath = tree.treePath + "/" + xml.getName();

			XmlReader xmlReader = new XmlReader();
			XmlReader.Element abilityElement = null;

			try
			{
				abilityElement = xmlReader.parse( Gdx.files.internal( "Abilities/" + abilityPath + ".xml" ) );
			}
			catch ( IOException e )
			{
				e.printStackTrace();
			}

			if (abilityElement.getName().equalsIgnoreCase("Active"))
			{
				current = ActiveAbility.load( abilityElement );
			}
			else if (abilityElement.getName().equalsIgnoreCase("Passive"))
			{
				current = PassiveAbility.load( abilityElement );
			}
			else
			{
				throw new RuntimeException("Unknown ability type: "+abilityElement.getName());
			}

			current.setTree( this );

			expToNextLevel = xml.getIntAttribute("BaseExp", expToNextLevel);

			if (xml.getChildCount() > 0)
			{
				branch1 = new AbilityStage(tree, xml.getChild(0));
				branch2 = new AbilityStage(tree, xml.getChild(1));
			}
		}
	}
}
