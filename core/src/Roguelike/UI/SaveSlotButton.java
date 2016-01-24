package Roguelike.UI;

import Roguelike.Ability.AbilityTree;
import Roguelike.AssetManager;
import Roguelike.Entity.GameEntity;
import Roguelike.Global;
import Roguelike.Items.Item;
import Roguelike.Save.SaveFile;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;

/**
 * Created by Philip on 23-Jan-16.
 */
public class SaveSlotButton extends Widget
{
	private final BitmapFont font;
	private final BitmapFont titleFont;
	private final Skin skin;
	private final SaveFile save;
	private final int slot;
	private final GlyphLayout layout = new GlyphLayout();
	private final TextureRegion white;
	private final NinePatch background;

	private final int edgePad = 6;

	public boolean mouseOver;

	public SaveSlotButton( Skin skin, int saveSlot )
	{
		this.skin = skin;
		this.slot = saveSlot;
		this.font = skin.getFont( "default" );
		this.titleFont = skin.getFont( "title" );
		this.white = AssetManager.loadTextureRegion( "Sprites/white.png" );
		this.background = new NinePatch( AssetManager.loadTextureRegion( "Sprites/GUI/Button.png" ), 12, 12, 12, 12 );

		SaveFile file = null;
		try
		{
			SaveFile save = new SaveFile();
			save.load(slot);
			file = save;
		} catch (Exception e) {}
		this.save = file;

		this.addListener( new InputListener()
		{
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor )
			{
				mouseOver = true;
			}

			public void exit (InputEvent event, float x, float y, int pointer, Actor toActor)
			{
				mouseOver = false;
			}

			@Override
			public boolean touchDown( InputEvent event, float x, float y, int pointer, int button )
			{
				return true;
			}

			@Override
			public void touchUp( InputEvent event, float x, float y, int pointer, int button )
			{
				Global.SaveSlot = slot;

				if (save == null)
				{
					Global.newWorld();
					Global.save();
				}

				Global.load();
			}
		} );
	}

	private void drawUnfilledSlot( Batch batch )
	{
		layout.setText( titleFont, "Empty Slot" );

		float cx = getX() + getWidth() / 2.0f;
		float cy = getY() + getHeight() / 2.0f;

		if (!mouseOver)
		{
			titleFont.setColor( Color.LIGHT_GRAY );
		}
		else
		{
			titleFont.setColor( Color.WHITE );
		}

		titleFont.draw( batch, layout, cx - layout.width / 2, cy + layout.height / 2 );
	}

	private void drawFilledSlot( Batch batch )
	{
		if (!mouseOver)
		{
			batch.setColor( Color.LIGHT_GRAY );
		}
		else
		{
			batch.setColor( Color.WHITE );
		}

		if (save.isDead)
		{
			layout.setText( titleFont, "Dead" );

			float cx = getX() + getWidth() / 2.0f;
			float cy = getY() + getHeight() / 2.0f;

			if (!mouseOver)
			{
				titleFont.setColor( Color.LIGHT_GRAY );
			}
			else
			{
				titleFont.setColor( Color.WHITE );
			}

			titleFont.draw( batch, layout, cx - layout.width / 2, cy + layout.height / 2 );
		}
		else
		{
			float width = getWidth() - edgePad * 2;
			float height = getHeight() - edgePad * 2;

			GameEntity player = save.levelManager.current.currentLevel.getPlayer();
			TextureRegion playerSprite = player.sprite.getCurrentTexture();

			batch.draw( playerSprite, getX()+edgePad, getY()+edgePad, height, height );

			float hh = height / 2 - edgePad / 2;

			float x = getX()+edgePad + height+edgePad;
			for ( int i = 0; i < Global.NUM_ABILITY_SLOTS; i++ )
			{
				AbilityTree tree = null;
				if ( i < player.slottedAbilities.size )
				{
					tree = player.slottedAbilities.get( i );
				}

				if (tree != null)
				{
					TextureRegion region = tree.current.current.getIcon().getCurrentTexture();
					batch.draw( region, x, getY()+edgePad+hh+edgePad/2, hh, hh );
				}

				x += hh + edgePad/2;
			}

			x = getX()+edgePad + height+edgePad;
			for ( Item.EquipmentSlot slot : Item.EquipmentSlot.values() )
			{
				Item item = player.getInventory().getEquip( slot );

				if (item != null)
				{
					TextureRegion region = item.getIcon().getCurrentTexture();
					batch.draw( region, x, getY()+edgePad, hh, hh );
				}

				x += hh + edgePad/2;
			}
		}

		batch.setColor( Color.WHITE );
	}

	@Override
	public void draw( Batch batch, float parentAlpha )
	{
		background.draw( batch, getX(), getY(), getWidth(), getHeight() );

		if (save == null)
		{
			drawUnfilledSlot( batch );
		}
		else
		{
			drawFilledSlot( batch );
		}
	}
}