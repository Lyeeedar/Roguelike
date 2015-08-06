package Roguelike;

import java.util.HashMap;

import Roguelike.Sound.SoundInstance;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.Sprite.AnimationMode;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public class AssetManager
{
	private static HashMap<String, Sound> loadedSounds = new HashMap<String, Sound>();
	public static Sound loadSound(String path)
	{
		if (loadedSounds.containsKey(path))
		{
			return loadedSounds.get(path);
		}
		
		FileHandle file = Gdx.files.internal("Sound/"+path+".mp3");
		if (!file.exists())
		{
			file = Gdx.files.internal("Sound/"+path+".ogg");
			
			if (!file.exists())
			{
				loadedSounds.put(path, null);
				return null;
			}
		}
		
		Sound sound = Gdx.audio.newSound(file);
		
		loadedSounds.put(path, sound);
		
		return sound;
	}
	
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
			loadedTextures.put(path, null);
			return null;
		}
		
		Texture tex = new Texture(file, true);
		
		tex.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.MipMapLinearLinear);
		
		loadedTextures.put(path, tex);
		
		return tex;
	}
	
	public static Sprite loadSprite(String name)
	{
		return loadSprite(name, 0.5f, new int[]{0, 0}, new int[]{0, 0}, Color.WHITE, AnimationMode.TEXTURE, null);
	}
	
	public static Sprite loadSprite(String name, int size)
	{
		return loadSprite(name, 0.5f, new int[]{size, size}, new int[]{0, 0}, Color.WHITE, AnimationMode.TEXTURE, null);
	}
	
	public static Sprite loadSprite(String name, float updateTime)
	{
		return loadSprite(name, updateTime, new int[]{0, 0}, new int[]{0, 0}, Color.WHITE, AnimationMode.TEXTURE, null);
	}
	
	public static Sprite loadSprite(String name, float updateTime, int[] tileSize, int[] tileIndex, Color colour, AnimationMode mode, SoundInstance sound)
	{
		Array<Texture> textures = new Array<Texture>();
		
		int i = 0;
		while (true)
		{
			Texture tex = loadTexture("Sprites/"+name+"_"+i+".png");
			
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
		
		if (updateTime <= 0)
		{
			if (mode == AnimationMode.SINE)
			{
				updateTime = 4;
			}
			else
			{
				updateTime = 0.5f;
			}
		}
		
		Sprite sprite = new Sprite(name, updateTime, textures, tileSize, tileIndex, colour, mode, sound);
		
		return sprite;
	}

	public static Sprite loadSprite(Element xml)
	{
		Element colourElement = xml.getChildByName("Colour");
		Color colour = Color.WHITE;
		if (colourElement != null)
		{
			colour = new Color();
			colour.a = 1;
			
			String rgb = colourElement.get("RGB", null);
			if (rgb != null)
			{
				String[] cols = rgb.split(",");
				colour.r = Float.parseFloat(cols[0]) / 255.0f;
				colour.g = Float.parseFloat(cols[1]) / 255.0f;
				colour.b = Float.parseFloat(cols[2]) / 255.0f;
			}
			
			colour.r = colourElement.getFloat("Red", colour.r);
			colour.g = colourElement.getFloat("Green", colour.g);
			colour.b = colourElement.getFloat("Blue", colour.b);
			colour.a = colourElement.getFloat("Alpha", colour.a);
		}
		
		Element soundElement = xml.getChildByName("Sound");
		SoundInstance sound = null;
		if (soundElement != null)
		{
			sound = SoundInstance.load(soundElement);
		}
		
		return loadSprite(
				xml.get("Name"),
				xml.getFloat("UpdateRate", 0),
				new int[]{xml.getInt("Width", xml.getInt("Size", 0)), xml.getInt("Height", xml.getInt("Size", 0))},
				new int[]{xml.getInt("IndexX", 0), xml.getInt("IndexY", 0)},
				colour,
				AnimationMode.valueOf(xml.get("AnimationMode", "Texture").toUpperCase()),
				sound
				);
	}

}
