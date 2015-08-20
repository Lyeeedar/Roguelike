package Roguelike.Tiles;

import java.util.EnumSet;

import Roguelike.AssetManager;
import Roguelike.Global.Passability;
import Roguelike.Lights.AmbientShadow;
import Roguelike.Lights.Light;
import Roguelike.Sprite.Sprite;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public class TileData
{
	public Sprite[] sprites;
	public Light light;
	public AmbientShadow shadow;

	public EnumSet<Passability> passableBy;

	public String description;

	public boolean canFeature = true;
	public boolean canSpawn = true;

	private TileData()
	{

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

		if ( xml.getChildByName( "Light" ) != null )
		{
			data.light = Light.load( xml.getChildByName( "Light" ) );
		}

		if ( xml.getChildByName( "AmbientShadow" ) != null )
		{
			data.shadow = AmbientShadow.load( xml.getChildByName( "AmbientShadow" ) );
		}

		data.passableBy = Passability.parse( xml.get( "Passable", "false" ) );

		if ( xml.get( "Opaque", null ) != null )
		{
			boolean opaque = xml.getBoolean( "Opaque", false );

			if ( opaque )
			{
				data.passableBy.remove( Passability.LIGHT );
			}
			else
			{
				data.passableBy.add( Passability.LIGHT );
			}
		}

		data.canFeature = xml.getBoolean( "CanFeature", true );
		data.canSpawn = xml.getBoolean( "CanSpawn", true );

		return data;
	}
}
