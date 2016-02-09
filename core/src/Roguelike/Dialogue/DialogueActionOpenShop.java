package Roguelike.Dialogue;

import MobiDevelop.UI.HorizontalFlowGroup;
import Roguelike.Global;
import Roguelike.Items.Item;
import Roguelike.Screens.GameScreen;
import Roguelike.UI.*;
import Roguelike.UI.Tooltip;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.XmlReader;

/**
 * Created by Philip on 07-Feb-16.
 */
public class DialogueActionOpenShop extends AbstractDialogueAction
{
	boolean complete = false;

	@Override
	public DialogueManager.ReturnType process()
	{
		if (complete)
		{
			complete = false;
			return DialogueManager.ReturnType.ADVANCE;
		}

		Table table = new Table();
		fillTable( table );

		GameScreen.Instance.displayContextMenu( table, true );

		return DialogueManager.ReturnType.RUNNING;
	}

	private void fillTable(final Table table)
	{
		table.clear();

		final Skin skin = Global.loadSkin();

		Label shop = new Label( "Shop", skin, "title" );
		table.add( shop ).expandX().left();
		table.row();

		table.add( new Seperator( skin ) ).expandX().fillX();
		table.row();

		Table fundsLine = new Table();

		Item money = Global.CurrentLevel.player.inventory.getItem("money");
		SpriteWidget fundsSprite = new SpriteWidget( money.getIcon(), 32, 32 );
		fundsLine.add( fundsSprite );

		Label funds = new Label("Funds: " + money.count, skin);
		fundsLine.add( funds ).expandX().left();

		table.add( fundsLine ).expandX().fillX();
		table.row();

		table.add( new Seperator( skin ) ).expandX().fillX();
		table.row();

		Table group = new Table();
		group.defaults().pad( 10 );

		for (final Item item : Global.CurrentDialogue.inventory.m_items)
		{
			if (item.value > 0)
			{
				final boolean hasTheCash = Global.CurrentLevel.player.inventory.getItemCount( "money" ) >= item.value;

				Table tooltipTable = new Table(  );
				{
					Table itable = item.createTable( skin, Global.CurrentLevel.player );
					tooltipTable.add( itable ).expand().fill();
					tooltipTable.row();

					Label label = new Label( "Costs " + item.value, skin );

					if ( !hasTheCash )
					{
						label.setColor( Color.RED );
					}

					tooltipTable.add( label );
				}

				Tooltip tooltip = new Tooltip( tooltipTable, skin, GameScreen.Instance.stage );
				TooltipListener tooltipListener = new TooltipListener( tooltip );

				Table itemTable = new Table(  );

				SpriteWidget spriteWidget = new SpriteWidget( item.getIcon(), 32, 32 );
				spriteWidget.addListener( tooltipListener );
				itemTable.add( spriteWidget );

				group.add( itemTable ).expandX().left();

				Label name = new Label(item.getName(), skin);
				name.addListener( tooltipListener );
				group.add( name ).expandX().left();

				Label cost = new Label("Purchase for "  + item.value, skin);
				cost.setColor( Color.RED );

				TextButton purchase = new TextButton( "Purchase for " + item.value, skin );
				purchase.addListener( new ClickListener(  )
				{
					public void clicked (InputEvent event, float x, float y)
					{
						Global.CurrentLevel.player.inventory.equip( item );
						Global.CurrentLevel.player.inventory.removeItem( "money", item.value );

						fillTable( table );
					}
				});

				if (hasTheCash)
				{
					group.add( purchase ).expandX().left();
				}
				else
				{
					group.add( cost ).expandX().left();
				}

				group.row();
			}
		}

		ScrollPane scrollPane = new ScrollPane( group, skin );
		table.add( scrollPane ).expand().fill();
		table.row();

		table.add( new Seperator( skin ) ).expandX().fillX();
		table.row();

		TextButton done = new TextButton( "Done", skin );
		done.addListener( new ClickListener(  )
		{
			public void clicked (InputEvent event, float x, float y)
			{
				GameScreen.Instance.lockContextMenu = false;
				GameScreen.Instance.clearContextMenu();

				complete = true;
				Global.CurrentDialogue.dialogue.advance( Global.CurrentDialogue );
			}
		});
		table.add( done );
		table.row();
	}

	@Override
	public void parse( XmlReader.Element xml )
	{

	}
}
