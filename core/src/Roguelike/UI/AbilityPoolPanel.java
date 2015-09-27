package Roguelike.UI;

import Roguelike.AssetManager;
import Roguelike.Global;
import Roguelike.Ability.AbilityPool.Ability;
import Roguelike.Ability.AbilityPool.AbilityLine;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Ability.PassiveAbility.PassiveAbility;
import Roguelike.Screens.GameScreen;
import Roguelike.Sprite.Sprite;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;

public class AbilityPoolPanel extends Table
{
	private final TextureRegion white;

	private final Sprite locked;

	private final Sprite tileBackground;
	private final Sprite tileBorder;

	private final Sprite buttonBorder;

	private final Sprite buttonUp;

	private final BitmapFont font;
	private final GlyphLayout layout = new GlyphLayout();

	private int selectedAbilityLine = 0;

	private AbilityLineList abilityLine;
	private AbilityList abilityList;

	public AbilityPoolPanel( Skin skin, Stage stage )
	{
		font = AssetManager.loadFont( "Sprites/GUI/stan0755.ttf", 8 );

		this.white = AssetManager.loadTextureRegion( "Sprites/white.png" );
		this.locked = AssetManager.loadSprite( "GUI/locked" );

		this.tileBackground = AssetManager.loadSprite( "GUI/TileBackground" );
		this.tileBorder = AssetManager.loadSprite( "GUI/TileBorder" );

		this.buttonUp = AssetManager.loadSprite( "GUI/Button" );
		this.buttonBorder = AssetManager.loadSprite( "GUI/ButtonBorder" );

		abilityLine = new AbilityLineList( skin, stage, buttonUp, tileBorder, 32 );
		abilityList = new AbilityList( skin, stage, tileBackground, tileBorder, 32 );

		add( abilityLine ).expandY().fillY();
		add( abilityList ).expand().fill();
	}

	public class AbilityLineList extends TilePanel
	{

		public AbilityLineList( Skin skin, Stage stage, Sprite tileBackground, Sprite tileBorder, int tileSize )
		{
			super( skin, stage, tileBackground, tileBorder, 1, 1, tileSize, true );
			padding = 5;
		}

		@Override
		public void populateTileData()
		{
			tileData.clear();

			for ( AbilityLine line : Global.abilityPool.abilityLines )
			{
				tileData.add( line );
			}

			dataHeight = tileData.size;
		}

		@Override
		public Sprite getSpriteForData( Object data )
		{
			return ( (AbilityLine) data ).icon;
		}

		@Override
		public void handleDataClicked( Object data, InputEvent event, float x, float y )
		{
			selectedAbilityLine = Global.abilityPool.abilityLines.indexOf( (AbilityLine) data, true );
		}

		@Override
		public Table getToolTipForData( Object data )
		{
			Table table = new Table();
			table.add( new Label( ( (AbilityLine) data ).name, skin, "title" ) ).width( Value.percentWidth( 1, table ) );

			return table;
		}

		@Override
		public Color getColourForData( Object data )
		{
			AbilityLine line = (AbilityLine) data;
			int index = Global.abilityPool.abilityLines.indexOf( line, true );

			return selectedAbilityLine == index ? Color.GREEN : null;
		}

		@Override
		public void onDrawItemBackground( Object data, Batch batch, int x, int y, int width, int height )
		{
		}

		@Override
		public void onDrawItem( Object data, Batch batch, int x, int y, int width, int height )
		{
		}

		@Override
		public void onDrawItemForeground( Object data, Batch batch, int x, int y, int width, int height )
		{
			buttonBorder.render( batch, x, y, width, height );
		}

	}

	public class AbilityList extends TilePanel
	{

		public AbilityList( Skin skin, Stage stage, Sprite tileBackground, Sprite tileBorder, int tileSize )
		{
			super( skin, stage, tileBackground, tileBorder, 5, 1, tileSize, true );
			padding = 5;
		}

		@Override
		public void populateTileData()
		{
			tileData.clear();

			AbilityLine selected = Global.abilityPool.abilityLines.get( selectedAbilityLine );

			for ( Ability[] tier : selected.abilityTiers )
			{
				for ( Ability a : tier )
				{
					tileData.add( a );
				}
			}

			dataHeight = tileData.size / 5;
		}

		@Override
		public Sprite getSpriteForData( Object data )
		{
			return ( (Ability) data ).ability.getIcon();
		}

		@Override
		public void handleDataClicked( Object data, InputEvent event, float x, float y )
		{
			final Ability a = (Ability) data;

			Table table = new Table();
			if ( !a.unlocked )
			{
				if ( Global.CurrentLevel.player.essence >= a.cost )
				{
					TextButton button = new TextButton( "Unlock for " + a.cost + " essence?\nYou have " + Global.CurrentLevel.player.essence + " essence.", skin );
					table.add( button ).width( Value.percentWidth( 1, table ) ).pad( 2 );

					table.addListener( new InputListener()
					{
						@Override
						public boolean touchDown( InputEvent event, float x, float y, int pointer, int button )
						{
							return true;
						}

						@Override
						public void touchUp( InputEvent event, float x, float y, int pointer, int button )
						{
							Global.CurrentLevel.player.essence -= a.cost;

							a.unlocked = true;
							GameScreen.Instance.clearContextMenu();
						}
					} );
				}
				else
				{
					table.add( new Label( "Not enough essence " + Global.CurrentLevel.player.essence + " / " + a.cost + ".\nGo kill more things", skin ) ).width( Value.percentWidth( 1, table ) ).pad( 2 );

					table.addListener( new InputListener()
					{
						@Override
						public boolean touchDown( InputEvent event, float x, float y, int pointer, int button )
						{
							return true;
						}

						@Override
						public void touchUp( InputEvent event, float x, float y, int pointer, int button )
						{
							GameScreen.Instance.clearContextMenu();
						}
					} );
				}
			}
			else
			{
				if ( a.ability instanceof ActiveAbility )
				{
					for ( int ii = 0; ii < Global.NUM_ABILITY_SLOTS; ii++ )
					{
						ActiveAbility equipped = Global.abilityPool.slottedActiveAbilities[ii];
						final int index = ii;

						Table row = new Table();

						String text = ( ii + 1 ) + ".";
						if ( equipped != null )
						{
							text += "  " + equipped.getName();
						}

						TextButton button = new TextButton( text, skin );
						row.add( button ).expand().fill();

						row.addListener( new InputListener()
						{
							@Override
							public boolean touchDown( InputEvent event, float x, float y, int pointer, int button )
							{
								return true;
							}

							@Override
							public void touchUp( InputEvent event, float x, float y, int pointer, int button )
							{
								Global.abilityPool.slotActiveAbility( (ActiveAbility) a.ability, index );
								GameScreen.Instance.clearContextMenu();
							}
						} );

						table.add( row ).width( Value.percentWidth( 1, table ) ).pad( 2 ).padLeft( 5 );
						table.row();
					}
				}
				else if ( a.ability instanceof PassiveAbility )
				{
					for ( int ii = 0; ii < Global.NUM_ABILITY_SLOTS; ii++ )
					{
						PassiveAbility equipped = Global.abilityPool.slottedPassiveAbilities[ii];
						final int index = ii;

						Table row = new Table();

						String text = ( ii + 1 ) + ".";
						if ( equipped != null )
						{
							text += "  " + equipped.getName();
						}

						TextButton button = new TextButton( text, skin );
						row.add( button ).expand().fill();

						row.addListener( new InputListener()
						{
							@Override
							public boolean touchDown( InputEvent event, float x, float y, int pointer, int button )
							{
								return true;
							}

							@Override
							public void touchUp( InputEvent event, float x, float y, int pointer, int button )
							{
								Global.abilityPool.slotPassiveAbility( (PassiveAbility) a.ability, index );
								GameScreen.Instance.clearContextMenu();
							}
						} );

						table.add( row ).width( Value.percentWidth( 1, table ) ).pad( 2 ).padLeft( 5 );
						table.row();
					}
				}
			}

			table.pack();

			Tooltip tooltip = new Tooltip( table, skin, stage );
			tooltip.show( event, x - tooltip.getWidth() / 2, y - tooltip.getHeight() / 2 );

			GameScreen.Instance.contextMenu = tooltip;
		}

		@Override
		public Table getToolTipForData( Object data )
		{
			Ability a = (Ability) data;

			if ( a.ability instanceof ActiveAbility )
			{
				( (ActiveAbility) a.ability ).caster = Global.CurrentLevel.player;
				( (ActiveAbility) a.ability ).variableMap = Global.CurrentLevel.player.getVariableMap();
			}

			Table table = a.ability.createTable( skin, Global.CurrentLevel.player );

			if ( !a.unlocked )
			{
				table.row();
				table.add( new Label( "Cost: " + a.cost, skin ) );
			}

			return table;
		}

		@Override
		public Color getColourForData( Object data )
		{
			return null;
		}

		@Override
		public void onDrawItemBackground( Object data, Batch batch, int x, int y, int width, int height )
		{
		}

		@Override
		public void onDrawItem( Object data, Batch batch, int x, int y, int width, int height )
		{
		}

		@Override
		public void onDrawItemForeground( Object data, Batch batch, int x, int y, int width, int height )
		{
			Ability a = (Ability) data;

			if ( !a.unlocked )
			{
				batch.setColor( 0.5f, 0.5f, 0.5f, 0.5f );
				batch.draw( white, x, y, width, height );

				batch.setColor( Color.WHITE );
				locked.render( batch, x, y, width, height );
			}
			else
			{
				int slotIndex = -1;
				if ( a.ability instanceof ActiveAbility )
				{
					slotIndex = Global.abilityPool.getActiveAbilityIndex( (ActiveAbility) a.ability );
				}
				else
				{
					slotIndex = Global.abilityPool.getPassiveAbilityIndex( (PassiveAbility) a.ability );
				}

				if ( slotIndex >= 0 )
				{
					slotIndex++;
					layout.setText( font, "" + slotIndex );

					font.draw( batch, "" + slotIndex, x + width / 2 - layout.width / 2, y + height / 2 - layout.height / 2 );
				}
			}
		}

	}
}
