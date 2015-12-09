package Roguelike.UI;

import Roguelike.AssetManager;
import Roguelike.Global.Statistic;
import Roguelike.Entity.Entity;
import Roguelike.Entity.Entity.StatusEffectStack;
import Roguelike.Entity.GameEntity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;

public class EntityStatusRenderer
{
	private static Texture heartFull = AssetManager.loadTexture( "Sprites/Oryx/Custom/ui/heart_red_full.png" );
	private static Texture heartEmpty = AssetManager.loadTexture( "Sprites/Oryx/Custom/ui/heart_empty.png" );

	public static void draw( Entity entity, Batch batch, int x, int y, int width, int height, float heightScale )
	{
		BitmapFont font = AssetManager.loadFont( "Sprites/GUI/stan0755.ttf", 8 );

		float val = (float) entity.HP / (float) ( entity.getVariable( Statistic.CONSTITUTION ) * 10 );

		if ( val < 1 )
		{
			int dstHeightPortion = (int) ( heartFull.getHeight() * val );
			int srcHeightPortion = (int) ( ( height / 3 ) * val );

			batch.draw( heartFull, x, y, width / 3, srcHeightPortion, 0, heartFull.getHeight() - dstHeightPortion, heartFull.getWidth(), dstHeightPortion, false, false );
			batch.draw( heartEmpty, x, y, width / 3, height / 3 );
		}

		Array<StatusEffectStack> stacks = entity.stackStatusEffects();

		int statusTileSize = Math.min( width / 3, 32 );
		int sx = x;
		int sy = y + height - statusTileSize;

		for ( StatusEffectStack stack : stacks )
		{
			stack.effect.icon.render( batch, sx, sy, statusTileSize, statusTileSize );
			font.draw( batch, "" + stack.count, sx, sy );

			sx += statusTileSize;

			if ( sx >= x + width )
			{
				sx = x;
				sy += statusTileSize;
			}
		}
	}

	public static void drawHpBar( float val, Batch batch, int x, int y, int width, int height, float heightScale )
	{
		TextureRegion white = AssetManager.loadTextureRegion( "Sprites/white.png" );

		float barheight = height * heightScale;

		batch.setColor( Color.LIGHT_GRAY );
		batch.draw( white, x - 2, y + height - barheight - 2, width + 4, barheight + 4 );

		batch.setColor( Color.DARK_GRAY );
		batch.draw( white, x - 1, y + height - barheight - 1, width + 2, barheight + 2 );

		batch.setColor( new Color( Color.RED ).lerp( Color.GREEN, val ) );
		batch.draw( white, x, y + height - barheight, width * val, barheight );
		batch.setColor( Color.WHITE );
	}

	public static Table getMouseOverTable( GameEntity entity, int x, int y, int width, int height, float heightScale, int mousex, int mousey, Skin skin )
	{
		if ( mousex >= x && mousex <= x + width / 3 && mousey >= y && mousey <= y + height / 3 )
		{
			int hp = entity.HP;
			int maxhp = entity.getVariable( Statistic.CONSTITUTION ) * 10;

			if ( hp < maxhp )
			{
				Table table = new Table();
				table.add( new Label( "HP: " + hp + " / " + maxhp, skin ) );
				return table;
			}
		}

		Array<StatusEffectStack> stacks = entity.stackStatusEffects();

		int statusTileSize = Math.min( width / 3, 32 );
		int sx = x;
		int sy = y + height - statusTileSize;

		for ( StatusEffectStack stack : stacks )
		{
			if ( mousex >= sx && mousex <= sx + statusTileSize && mousey >= sy && mousey <= sy + statusTileSize ) { return stack.effect.createTable( skin, entity ); }

			sx += statusTileSize;

			if ( sx >= x + width )
			{
				sx = x;
				sy += statusTileSize;
			}
		}

		return null;
	}

}
