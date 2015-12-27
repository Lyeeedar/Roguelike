package Roguelike.UI;

import Roguelike.AssetManager;
import Roguelike.Global.Statistic;
import Roguelike.Entity.Entity;
import Roguelike.Entity.Entity.StatusEffectStack;
import Roguelike.Entity.GameEntity;

import Roguelike.StatusEffect.StatusEffect;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;

public class EntityStatusRenderer
{
	public static void draw( Entity entity, Batch batch, int x, int y, int width, int height, float heightScale, Color colour )
	{
		BitmapFont font = AssetManager.loadFont( "Sprites/GUI/stan0755.ttf", 8 );

		float val = (float) entity.HP / (float) ( entity.getVariable( Statistic.CONSTITUTION ) * 10 );
		float extraVal = (float) entity.extraUIHP / (float) ( entity.getVariable( Statistic.CONSTITUTION ) * 10 );

		float barheight = height * heightScale;

		int by = y-height+(int)barheight+8;

		drawHpBar( val, extraVal, batch, x, by, width, height, heightScale, colour );

		int statusTileSize = Math.min( width / 3, 32 );
		int sx = x;
		int sy = y + (int)barheight + 16;

		for ( StatusEffect status : entity.statusEffects )
		{
			status.icon.render( batch, sx, sy, statusTileSize, statusTileSize );
			font.draw( batch, "" + status.duration, sx, sy );

			sx += statusTileSize;

			if ( sx >= x + width )
			{
				sx = x;
				sy += statusTileSize;
			}
		}
	}

	public static void drawHpBar( float val, float extraVal, Batch batch, int x, int y, int width, int height, float heightScale, Color colour )
	{
		TextureRegion white = AssetManager.loadTextureRegion( "Sprites/white.png" );

		float barheight = height * heightScale;

		batch.setColor( Color.LIGHT_GRAY );
		batch.draw( white, x - 2, y + height - barheight - 2, width + 4, barheight + 4 );

		batch.setColor( Color.DARK_GRAY );
		batch.draw( white, x - 1, y + height - barheight - 1, width + 2, barheight + 2 );

		batch.setColor( Color.LIGHT_GRAY );
		batch.draw( white, x, y + height - barheight, width * (val + extraVal), barheight );

		batch.setColor( colour );
		batch.draw( white, x, y + height - barheight, width * val, barheight );
		batch.setColor( Color.WHITE );
	}

	public static Table getMouseOverTable( GameEntity entity, int x, int y, int width, int height, float heightScale, int mousex, int mousey, Skin skin )
	{
		float barheight = height * heightScale;

		int statusTileSize = Math.min( width / 3, 32 );
		int sx = x;
		int sy = y + (int)barheight + 16;

		for ( StatusEffect status : entity.statusEffects )
		{
			if ( mousex >= sx && mousex <= sx + statusTileSize && mousey >= sy && mousey <= sy + statusTileSize )
			{
				Table table = status.createTable( skin, entity );
				table.setWidth( 200 );
				return table;
			}

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
