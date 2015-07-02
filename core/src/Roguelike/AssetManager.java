package Roguelike;

import java.io.IOException;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

import Roguelike.Items.Item;
import Roguelike.Sprite.Sprite;

public class AssetManager
{
	private static HashMap<String, Texture> loadedTextures = new HashMap<String, Texture>();
	public static Texture loadTexture(String path)
	{
		if (loadedTextures.containsKey(path))
		{
			return loadedTextures.get(path);
		}
		
		FileHandle file = Gdx.files.internal(path);
		if (!file.exists())
		{
			return null;
		}
		
		Texture tex = new Texture(file);
		loadedTextures.put(path, tex);
		
		return tex;
	}
	
	public static Sprite loadSprite(String name)
	{
		return loadSprite(name, 0.5f, new int[]{0, 0}, new int[]{0, 0});
	}
	
	public static Sprite loadSprite(String name, float updateTime, int[] tileSize, int[] tileIndex)
	{
		Array<Texture> textures = new Array<Texture>();
		
		int i = 0;
		while (true)
		{
			Texture tex = loadTexture("Sprites/"+name+i+".png");
			
			if (tex == null)
			{
				break;
			}
			else
			{
				textures.add(tex);
			}
			
			i++;
		}
		
		if (textures.size == 0)
		{
			Texture tex = loadTexture("Sprites/"+name+".png");
			
			if (tex != null)
			{
				textures.add(tex);
			}
		}
		
		if (textures.size == 0)
		{
			throw new RuntimeException("Cant find any textures for " + name + "!");
		}
		
		if (tileSize[0] == 0)
		{
			tileSize[0] = textures.get(0).getWidth();
		}
		
		if (tileSize[1] == 0)
		{
			tileSize[1] = textures.get(0).getHeight();
		}
		
		Sprite sprite = new Sprite(updateTime, textures, tileSize, tileIndex);
		
		return sprite;
	}

	public static Sprite loadSprite(Element xml)
	{
		return loadSprite(
				xml.get("Name"),
				xml.getFloat("UpdateRate", 0.5f),
				new int[]{xml.getInt("Width", xml.getInt("Size", 0)), xml.getInt("Height", xml.getInt("Size", 0))},
				new int[]{xml.getInt("IndexX", 0), xml.getInt("IndexY", 0)});
	}

}
