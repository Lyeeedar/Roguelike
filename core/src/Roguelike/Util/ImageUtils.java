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
				
				for (Sprite sprite : s.getTileData().sprites)
				{
					Pixmap src = TextureToPixmap(sprite.getCurrentTexture());
					pixmap.drawPixmap(src, 
							0, 0, src.getWidth(), src.getHeight(),
							x*tileSize, y*tileSize, tileSize, tileSize);
					src.dispose();
				}
				
				if (s.hasEnvironmentEntity())
				{
					Pixmap src = TextureToPixmap(s.getEnvironmentEntity("").sprite.getCurrentTexture());
					pixmap.drawPixmap(src, 
							0, 0, src.getWidth(), src.getHeight(),
							x*tileSize, y*tileSize, tileSize, tileSize);
					src.dispose();
				}
				
				if (s.hasGameEntity())
				{
					Pixmap src = TextureToPixmap(s.getGameEntity().sprite.getCurrentTexture());
					pixmap.drawPixmap(src, 
							0, 0, src.getWidth(), src.getHeight(),
							x*tileSize, y*tileSize, tileSize, tileSize);
					src.dispose();
				}
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
	
	public static Pixmap TextureToPixmap(TextureRegion texture)
	{
		try 
		{
			texture.getTexture().getTextureData().prepare();
		}
		catch (Exception e)
		{
			
		}
		return texture.getTexture().getTextureData().consumePixmap();
	}
	
	public static Texture PixmapToTexture(Pixmap pixmap)
	{
		return new Texture(pixmap);
	}
}
