package Roguelike.Sprite;

import Roguelike.AssetManager;

import Roguelike.Global;
import Roguelike.Util.EnumBitflag;
import Roguelike.Util.ImageUtils;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

import java.util.HashMap;

// Naming priority: NSEW
public class TilingSprite
{
	public TilingSprite()
	{

	}

	public TilingSprite( Sprite topSprite, Sprite frontSprite )
	{
		sprites.put( "C", topSprite );
		sprites.put( "S", frontSprite );

		hasAllElements = true;
	}

	public TilingSprite ( String name, String texture, String mask )
	{
		Element spriteElement = new Element( "Sprite", null );

		load( name, texture, mask, spriteElement, null );
	}

	public HashMap<String, Sprite> sprites = new HashMap<String, Sprite>(  );

	public String name;
	public String texName;
	public String maskName;
	public Element spriteBase = new Element( "Sprite", null );

	public boolean hasAllElements;

	public Sprite overhangSprite;

	public void parse( Element xml )
	{
		String name = xml.get( "Name" );
		Element overhangElement = xml.getChildByName( "Overhang" );

		Element topElement = xml.getChildByName("Top");
		if (topElement != null)
		{
			Sprite topSprite = AssetManager.loadSprite( topElement );
			Sprite frontSprite = AssetManager.loadSprite( xml.getChildByName( "Front" ) );

			sprites.put( "C", topSprite );
			sprites.put( "S", frontSprite );

			hasAllElements = true;
		}

		Element spriteElement = xml.getChildByName( "Sprite" );
		String texName = spriteElement != null ? spriteElement.get( "Name" ) : null;
		String maskName = xml.get( "Mask", null );

		load(name, texName, maskName, spriteElement, overhangElement);
	}

	public void load( String name, String texName, String maskName, Element spriteElement, Element overhangElement )
	{
		this.name = name;
		this.texName = texName;
		this.maskName = maskName;
		this.spriteBase = spriteElement;

		if ( overhangElement != null )
		{
			overhangSprite = AssetManager.loadSprite( overhangElement );
		}
	}

	public static TilingSprite load( Element xml )
	{
		TilingSprite sprite = new TilingSprite();
		sprite.parse( xml );
		return sprite;
	}

	private static TextureRegion getMaskedSprite( String baseName, String maskBaseName, Array<String> masks )
	{
		// If this is the center then just return the original texture
		if ( masks.size == 0)
		{
			return AssetManager.loadTextureRegion( "Sprites/" + baseName + ".png" );
		}

		// Build the mask suffix
		String mask = "";
		for ( String m : masks)
		{
			mask += "_" + m;
		}

		String maskedName = baseName + mask;

		TextureRegion tex = AssetManager.loadTextureRegion( "Sprites/" + maskedName + ".png" );

		// We have the texture, so return it
		if (tex != null)
		{
			return tex;
		}

		// If we havent been given a mask, then just return the original texture
		if (maskBaseName == null)
		{
			return AssetManager.loadTextureRegion( "Sprites/" + baseName + ".png" );
		}

		Pixmap base = ImageUtils.textureToPixmap( AssetManager.loadTexture( "Sprites/" + baseName + ".png" ) );
		Pixmap merged = base;
		for (String maskSuffix : masks)
		{
			Texture maskTex = AssetManager.loadTexture( "Sprites/" + maskBaseName + "_" + maskSuffix + ".png" );

			if (maskTex == null)
			{
				continue;
			}

			Pixmap maskedTex = ImageUtils.maskPixmap( merged, ImageUtils.textureToPixmap( maskTex ) );
			if (merged != base) { merged.dispose(); }
			merged = maskedTex;
		}

		return AssetManager.packPixmap( "Sprites/" + maskedName + ".png", merged );
	}

	public Array<String> getMasks( EnumBitflag<Global.Direction> emptyDirections )
	{
		Array<String> masks = new Array<String>();

		if (emptyDirections.contains( Global.Direction.NORTH ))
		{
			if (emptyDirections.contains( Global.Direction.EAST ))
			{
				masks.add("NE");
			}

			if (emptyDirections.contains( Global.Direction.WEST ))
			{
				masks.add("NW");
			}

			if (!emptyDirections.contains( Global.Direction.EAST ) && !emptyDirections.contains( Global.Direction.WEST ))
			{
				masks.add("N");
			}
		}

		if (emptyDirections.contains( Global.Direction.SOUTH ))
		{
			if (emptyDirections.contains( Global.Direction.EAST ))
			{
				masks.add("SE");
			}

			if (emptyDirections.contains( Global.Direction.WEST ))
			{
				masks.add("SW");
			}

			if (!emptyDirections.contains( Global.Direction.EAST ) && !emptyDirections.contains( Global.Direction.WEST ))
			{
				masks.add("S");
			}
		}

		if (emptyDirections.contains( Global.Direction.EAST ))
		{
			if (!emptyDirections.contains( Global.Direction.NORTH ) && !emptyDirections.contains( Global.Direction.SOUTH ))
			{
				masks.add("E");
			}
		}

		if (emptyDirections.contains( Global.Direction.WEST ))
		{
			if (!emptyDirections.contains( Global.Direction.NORTH ) && !emptyDirections.contains( Global.Direction.SOUTH ))
			{
				masks.add("W");
			}
		}

		if (emptyDirections.contains( Global.Direction.NORTHEAST ) && !emptyDirections.contains( Global.Direction.NORTH ) && !emptyDirections.contains( Global.Direction.EAST ))
		{
			masks.add("DNE");
		}

		if (emptyDirections.contains( Global.Direction.NORTHWEST ) && !emptyDirections.contains( Global.Direction.NORTH ) && !emptyDirections.contains( Global.Direction.WEST ))
		{
			masks.add("DNW");
		}

		if (emptyDirections.contains( Global.Direction.SOUTHEAST ) && !emptyDirections.contains( Global.Direction.SOUTH ) && !emptyDirections.contains( Global.Direction.EAST ))
		{
			masks.add("DSE");
		}

		if (emptyDirections.contains( Global.Direction.SOUTHWEST ) && !emptyDirections.contains( Global.Direction.SOUTH ) && !emptyDirections.contains( Global.Direction.WEST ))
		{
			masks.add("DSW");
		}

		return masks;
	}

	public Sprite getSprite( EnumBitflag<Global.Direction> emptyDirections )
	{
		if (hasAllElements)
		{
			if (emptyDirections.contains( Global.Direction.SOUTH ))
			{
				return sprites.get( "S" );
			}
			else
			{
				return sprites.get( "C" );
			}
		}
		else
		{
			Array<String> masks = getMasks( emptyDirections );

			String mask = "";
			for ( String m : masks)
			{
				mask += "_" + m;
			}

			if (sprites.containsKey( mask ))
			{
				return sprites.get( mask );
			}
			else if (texName != null)
			{
				TextureRegion region = getMaskedSprite( texName, maskName, masks );
				Sprite sprite = AssetManager.loadSprite( spriteBase, region );

				sprites.put( mask, sprite );

				return sprite;
			}
			else
			{
				return sprites.get( "C" );
			}
		}
	}
}
