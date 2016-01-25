package Roguelike.Entity.ActivationAction;

import Roguelike.AssetManager;
import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.TilingSprite;
import com.badlogic.gdx.utils.XmlReader;

/**
 * Created by Philip on 25-Jan-16.
 */
public class ActivationActionSetSprite extends AbstractActivationAction
{
	public Sprite sprite;
	public TilingSprite tilingSprite;

	public ActivationActionSetSprite()
	{

	}

	public ActivationActionSetSprite( Sprite sprite, TilingSprite tilingSprite )
	{
		this.sprite = sprite;
		this.tilingSprite = tilingSprite;
	}

	@Override
	public void evaluate( EnvironmentEntity entity, float delta )
	{
		entity.sprite = sprite;
		entity.tilingSprite = tilingSprite;
	}

	@Override
	public void parse( XmlReader.Element xml )
	{
		XmlReader.Element spriteElement = xml.getChildByName( "Sprite" );
		if ( spriteElement != null )
		{
			sprite = AssetManager.loadSprite( xml.getChildByName( "Sprite" ) );
		}

		XmlReader.Element raisedSpriteElement = xml.getChildByName( "TilingSprite" );
		if ( raisedSpriteElement != null )
		{
			tilingSprite = TilingSprite.load( raisedSpriteElement );
		}
	}
}
