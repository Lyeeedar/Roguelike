package Roguelike.Util;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import Roguelike.DungeonGeneration.Symbol;
import Roguelike.Sprite.Sprite;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ImageUtils
{
	public static void writeSymbolGridToFile(Symbol[][] grid, String filename, int tileSize)
	{
		// Get size of image
		int height = grid[0].length * tileSize;
		int width = grid.length * tileSize;

		// Create resources
		Pixmap pixmap = new Pixmap(width, height, Format.RGBA4444);

		// do render
		for (int x = 0; x < grid.length; x++)
		{
			for (int y = 0; y < grid[0].length; y++)
			{
				Symbol s = grid[x][y];

				if (s.character == '#')
				{
					pixmap.setColor(Color.MAROON);
				}
				else if (s.character == '.')
				{
					pixmap.setColor(Color.GREEN);
				}
				else
				{
					pixmap.setColor(Color.BLUE);
				}
				pixmap.fillRectangle(x*tileSize, y*tileSize, tileSize, tileSize);

//				if (s.hasEnvironmentEntity())
//				{
//					Pixmap src = TextureToPixmap(s.getEnvironmentEntity("").sprite.getCurrentTexture());
//					pixmap.drawPixmap(src,
//							0, 0, src.getWidth(), src.getHeight(),
//							x*tileSize, y*tileSize, tileSize, tileSize);
//					src.dispose();
//				}
//
//				if (s.hasGameEntity())
//				{
//					Pixmap src = TextureToPixmap(s.getGameEntity().sprite.getCurrentTexture());
//					pixmap.drawPixmap(src,
//							0, 0, src.getWidth(), src.getHeight(),
//							x*tileSize, y*tileSize, tileSize, tileSize);
//					src.dispose();
//				}
			}
		}

		// Save texture
		try
		{
			BufferedImage bi = pixmapToImage(pixmap);
			File outputfile = new File(filename);
			ImageIO.write(bi, "png", outputfile);

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		// dispose of resources
		pixmap.dispose();
	}

	public static BufferedImage pixmapToImage(Pixmap pm)
	{
		BufferedImage image = new BufferedImage(pm.getWidth(), pm.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < pm.getWidth(); x++)
		{
			for (int y = 0; y < pm.getHeight(); y++)
			{
				Color c = new Color();
				Color.rgba8888ToColor(c, pm.getPixel(x, y));

				java.awt.Color cc = new java.awt.Color(c.r, c.g, c.b, c.a);

				image.setRGB(x, y, cc.getRGB());
			}
		}

		return image;
	}

	public static Pixmap textureToPixmap(Texture texture)
	{
		if (!texture.getTextureData().isPrepared())
		{
			texture.getTextureData().prepare();
		}

		Pixmap pixmap = texture.getTextureData().consumePixmap();

		return pixmap;
	}

	public static Pixmap maskPixmap( Texture image, Texture mask )
	{
		return maskPixmap( textureToPixmap( image ), textureToPixmap( mask ) );
	}

	public static Pixmap maskPixmap( Pixmap image, Pixmap mask )
	{
		Pixmap pixmap = new Pixmap( image.getWidth(), image.getHeight(), Format.RGBA8888 );

		pixmap.setColor( 0, 0, 0, 0 );
		pixmap.fill();

		pixmap.drawPixmap( mask, 0, 0, mask.getWidth(), mask.getHeight(), 0, 0, pixmap.getWidth(), pixmap.getHeight() );

		Color cb = new Color();
		Color ca = new Color();

		for (int x = 0; x < pixmap.getWidth(); x++)
		{
			for (int y = 0; y < pixmap.getHeight(); y++)
			{
				Color.rgba8888ToColor(ca, image.getPixel(x, y));
				Color.rgba8888ToColor(cb, pixmap.getPixel(x, y));

				ca.mul( cb );

				pixmap.drawPixel(x, y, Color.rgba8888(ca));
			}
		}

		return pixmap;
	}
}
