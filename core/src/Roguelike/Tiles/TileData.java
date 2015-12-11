package Roguelike.Tiles;

import Roguelike.AssetManager;
import Roguelike.Global.Passability;
import Roguelike.Lights.AmbientShadow;
import Roguelike.Sprite.TilingSprite;
import Roguelike.Sprite.Sprite;
import Roguelike.Util.EnumBitflag;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public class TileData
{
	public Sprite[] sprites;
	public AmbientShadow shadow;
	public TilingSprite tilingSprite;

	public EnumBitflag<Passability> passableBy;

	public String description;

	public boolean canFeature = true;
	public boolean canSpawn = true;

	private TileData()
	{

	}

	public TileData( EnumBitflag<Passability> passableBy, Sprite... sprites )
	{
		this.sprites = sprites;
		this.passableBy = passableBy;
	}

	public static TileData parse( Element xml )
	{
		TileData data = new TileData();

		Array<Sprite> sprites = new Array<Sprite>();
		for ( Element spriteElement : xml.getChildrenByName( "Sprite" ) )
		{
			sprites.add( AssetManager.loadSprite( spriteElement ) );
		}
		data.sprites = sprites.toArray( Sprite.class );

		Element raisedSpriteElement = xml.getChildByName( "TilingSprite" );
		if ( raisedSpriteElement != null )
		{
			data.tilingSprite = TilingSprite.load( raisedSpriteElement );
		}

		Element ambientShadowElement = xml.getChildByName( "AmbientShadow" );
		if ( ambientShadowElement != null )
		{
			data.shadow = AmbientShadow.load( ambientShadowElement );
		}

		data.passableBy = Passability.parse( xml.get( "Passable", "false" ) );

		if ( xml.get( "Opaque", null ) != null )
		{
			boolean opaque = xml.getBoolean( "Opaque", false );

			if ( opaque )
			{
				data.passableBy.clearBit( Passability.LIGHT );
			}
			else
			{
				data.passableBy.setBit( Passability.LIGHT );
			}
		}

		data.canFeature = xml.getBoolean( "CanFeature", true );
		data.canSpawn = xml.getBoolean( "CanSpawn", true );

		return data;
	}
}
