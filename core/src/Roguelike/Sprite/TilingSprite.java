package Roguelike.Sprite;

import Roguelike.AssetManager;

import Roguelike.Global;
import Roguelike.Util.EnumBitflag;
import Roguelike.Util.ImageUtils;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.XmlReader.Element;

// Naming priority: NSEW
public class TilingSprite
{
	private static final Global.Direction[][] PossibleDirections =
	{
		{ Global.Direction.NORTH, Global.Direction.SOUTH },
		{ Global.Direction.EAST, Global.Direction.WEST },
		{ Global.Direction.NORTH, Global.Direction.EAST } ,
		{ Global.Direction.NORTH, Global.Direction.WEST },
		{ Global.Direction.SOUTH, Global.Direction.EAST },
		{ Global.Direction.SOUTH, Global.Direction.WEST },
		{ Global.Direction.NORTH },
		{ Global.Direction.SOUTH },
		{ Global.Direction.EAST },
		{ Global.Direction.WEST },
		{ Global.Direction.CENTER }
	};

	public TilingSprite()
	{

	}

	public TilingSprite( Sprite topSprite, Sprite frontSprite )
	{
		for (int i = 0; i < sprites.length; i++)
		{
			sprites[i] = topSprite;
		}

		sprites[7] = frontSprite;
	}

	public TilingSprite ( String name, String texture, String mask )
	{
		Element spriteElement = new Element( "Sprite", null );

		load( name, texture, mask, spriteElement, null );
	}

	public Sprite[] sprites = new Sprite[PossibleDirections.length];

	public String name;

	public Sprite overhangSprite;

	public void parse( Element xml )
	{
		String name = xml.get( "Name" );
		Element spriteElement = xml.getChildByName( "Sprite" );
		String texName = spriteElement.get( "Name" );
		String maskName = xml.get( "Mask", null );
		Element overhangElement = xml.getChildByName( "Overhang" );

		load(name, texName, maskName, spriteElement, overhangElement);
	}

	public void load( String name, String texName, String maskName, Element spriteElement, Element overhangElement )
	{
		this.name = name;

		for ( int i = 0; i < PossibleDirections.length; i++ )
		{
			Global.Direction[] dirs = PossibleDirections[i];

			TextureRegion texture = getMaskedSprite( texName, maskName, dirs );

			Sprite sprite = AssetManager.loadSprite( spriteElement, texture );
			sprites[i] = sprite;
		}

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

	private static TextureRegion getMaskedSprite( String baseName, String maskBaseName, Global.Direction[] directions )
	{
		// If this is the center or all no mask, then just return the original texture
		if ( directions[0] == Global.Direction.CENTER)
		{
			return AssetManager.loadTextureRegion( "Sprites/" + baseName + ".png" );
		}

		String mask = "";
		for ( Global.Direction dir : directions)
		{
			mask += dir.toString().substring( 0, 1 );
		}

		String maskedName = baseName + "_" + mask;

		TextureRegion tex = AssetManager.loadTextureRegion( "Sprites/" + maskedName + ".png" );

		// We have the texture, so return it
		if (tex != null)
		{
			return tex;
		}

		if (maskBaseName == null)
		{
			return AssetManager.loadTextureRegion( "Sprites/" + baseName + ".png" );
		}

		// We dont have the texture, so generate it
		Texture maskTex = AssetManager.loadTexture( "Sprites/" + maskBaseName + "_" + mask + ".png" );

		Texture baseTex = AssetManager.loadTexture( "Sprites/" + baseName + ".png" );

		Pixmap merged = ImageUtils.maskPixmap( baseTex, maskTex );

		return AssetManager.packPixmap( "Sprites/" + maskedName + ".png", merged );
	}

	public Sprite getSprite( EnumBitflag<Global.Direction> emptyDirections )
	{
		for ( int i = 0; i < PossibleDirections.length; i++ )
		{
			Global.Direction[] dirs = PossibleDirections[i];
			boolean valid = true;
			for ( Global.Direction dir : dirs)
			{
				if (!emptyDirections.contains( dir ))
				{
					valid = false;
					break;
				}
			}

			if (valid)
			{
				Sprite sprite = sprites[i];

				if (sprite == null)
				{
					break;
				}
				else
				{
					return sprite;
				}
			}
		}

		return sprites[ PossibleDirections.length-1 ];
	}
}
