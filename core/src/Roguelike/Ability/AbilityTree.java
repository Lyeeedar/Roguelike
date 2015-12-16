package Roguelike.Ability;

import Roguelike.Entity.Entity;
import Roguelike.Global;
import Roguelike.Screens.GameScreen;
import Roguelike.UI.Tooltip;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.XmlReader;

/**
 * Created by Philip on 15-Dec-15.
 */
public class AbilityTree
{
	public AbilityStage root;
	public AbilityStage current;

	public AbilityTree()
	{}

	public AbilityTree(IAbility ability)
	{
		root = new AbilityStage( ability );
		root.tree = this;

		current = root;

		ability.setTree(root);
	}

	public void parse(XmlReader.Element xml )
	{}


	public static AbilityTree load( XmlReader.Element xml )
	{
		AbilityTree tree = new AbilityTree(  );

		tree.parse( xml );

		return tree;
	}

	public static class AbilityStage
	{
		public AbilityTree tree;

		public IAbility current;

		public int level = 1;
		public int expToNextLevel = 10;
		public int exp;

		public boolean needsLevelAnim;

		public AbilityStage branch1;
		public AbilityStage branch2;

		public AbilityStage()
		{}

		public AbilityStage(IAbility ability)
		{
			current = ability;
		}

		public void gainExp(int _exp)
		{
			if (level == 10) { return; }

			exp += _exp;

			while (exp >= expToNextLevel && level < 10)
			{
				level++;
				exp -= expToNextLevel;
				//expToNextLevel += level * 50;
				needsLevelAnim = true;
			}
		}

		public void mutate( Skin skin, Entity entity, Stage stage )
		{
			Table table = new Table();

			table.add( branch1.current.createTable( skin, entity ) );
			table.add( branch2.current.createTable( skin, entity ) );
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

			table.add( branch1Button );
			table.add( branch2Button );

			table.pack();

			GameScreen.Instance.contextMenu = new Tooltip( table, skin, stage );
			GameScreen.Instance.contextMenu.show( Global.Resolution[ 0 ] / 2 - GameScreen.Instance.contextMenu.getWidth() / 2, Global.Resolution[ 1 ] / 2 - GameScreen.Instance.contextMenu.getHeight() );
			GameScreen.Instance.lockContextMenu = true;
		}
	}
}
