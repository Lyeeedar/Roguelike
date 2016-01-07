package Roguelike.Tiles;

import Roguelike.AssetManager;
import Roguelike.Global.Passability;
import Roguelike.Lights.AmbientShadow;
import Roguelike.Lights.Light;
import Roguelike.Sprite.TilingSprite;
import Roguelike.Sprite.Sprite;
import Roguelike.Util.EnumBitflag;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public class TileData
{
	public Array<SpriteGroup> spriteGroups = new Array<SpriteGroup>( );

	public AmbientShadow shadow;

	public EnumBitflag<Passability> passableBy;

	public String description;

	public boolean canFeature = true;
	public boolean canSpawn = true;

	private TileData()
	{

	}

	public TileData( EnumBitflag<Passability> passableBy, Sprite... sprites )
	{
		SpriteGroup group = new SpriteGroup( sprites, null, null );

		this.passableBy = passableBy;
	}

	public static TileData parse( Element xml )
	{
		TileData data = new TileData();

		// Load single group
		{
			Array<Sprite> sprites = new Array<Sprite>();
			for ( Element spriteElement : xml.getChildrenByName( "Sprite" ) )
			{
				sprites.add( AssetManager.loadSprite( spriteElement ) );
			}

			TilingSprite tilingSprite = null;
			Element raisedSpriteElement = xml.getChildByName( "TilingSprite" );
			if ( raisedSpriteElement != null )
			{
				tilingSprite = TilingSprite.load( raisedSpriteElement );
			}

			Light light = null;
			Element lightElement = xml.getChildByName( "Light" );
			if ( lightElement != null )
			{
				light = Roguelike.Lights.Light.load( lightElement );
			}

			if (sprites.size > 0 || tilingSprite != null)
			{
				SpriteGroup group = new SpriteGroup( sprites, tilingSprite, light );
				data.spriteGroups.add( group );
			}

		}

		// Load groups
		for (int i = 0; i < xml.getChildCount(); i++)
		{
			Element el = xml.getChild( i );

			if (el.getName().toLowerCase().startsWith( "group" ))
			{
				Array<Sprite> sprites = new Array<Sprite>();
				for ( Element spriteElement : el.getChildrenByName( "Sprite" ) )
				{
					sprites.add( AssetManager.loadSprite( spriteElement ) );
				}

				TilingSprite tilingSprite = null;
				Element raisedSpriteElement = el.getChildByName( "TilingSprite" );
				if ( raisedSpriteElement != null )
				{
					tilingSprite = TilingSprite.load( raisedSpriteElement );
				}

				Light light = null;
				Element lightElement = el.getChildByName( "Light" );
				if ( lightElement != null )
				{
					light = Roguelike.Lights.Light.load( lightElement );
				}

				SpriteGroup group = new SpriteGroup( sprites, tilingSprite, light );
				group.chance = el.getFloatAttribute( "Chance", -1 );

				data.spriteGroups.add( group );
			}
		}

		// Fix chances
		float totalChance = 1;
		int numWithoutChance = 0;
		for (SpriteGroup group : data.spriteGroups)
		{
			if (group.chance < 0)
			{
				numWithoutChance++;
			}
			else
			{
				totalChance -= group.chance;
			}
		}
		totalChance /= numWithoutChance;

		for (SpriteGroup group : data.spriteGroups)
		{
			if ( group.chance < 0 )
			{
				group.chance = totalChance;
			}
		}

		if ( data.spriteGroups.size == 0 )
		{
			throw new RuntimeException( "Failed to find any sprite groups!" );
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

	public static class SpriteGroup
	{
		public Array<Sprite> sprites;
		public TilingSprite tilingSprite;
		public Light light;
		public float chance = -1;

		public SpriteGroup(Array<Sprite> sprites, TilingSprite tilingSprite, Light light)
		{
			this.sprites = sprites;
			this.tilingSprite = tilingSprite;
			this.light = light;
		}

		public SpriteGroup(Sprite[] sprites, TilingSprite tilingSprite, Light light)
		{
			this.sprites = new Array<Sprite>( sprites );
			this.tilingSprite = tilingSprite;
			this.light = light;
		}
	}
}
