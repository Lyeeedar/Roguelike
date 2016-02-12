package Roguelike.Entity.ActivationAction;

import Roguelike.Entity.Entity;
import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Global;
import Roguelike.Util.EnumBitflag;
import com.badlogic.gdx.utils.XmlReader;

/**
 * Created by Philip on 25-Jan-16.
 */
public class ActivationActionSetPassable extends AbstractActivationAction
{
	public EnumBitflag<Global.Passability> passableBy;

	public ActivationActionSetPassable()
	{

	}

	public ActivationActionSetPassable( EnumBitflag<Global.Passability> passableBy )
	{
		this.passableBy = passableBy;
	}

	@Override
	public void evaluate( EnvironmentEntity owningEntity, Entity activatingEntity, float delta )
	{
		owningEntity.passableBy = passableBy;
	}

	@Override
	public void parse( XmlReader.Element xml )
	{
		passableBy = Global.Passability.parse( xml.get( "Passable", "false" ) );

		if ( xml.get( "Opaque", null ) != null )
		{
			boolean opaque = xml.getBoolean( "Opaque", false );

			if ( opaque )
			{
				passableBy.clearBit( Global.Passability.LIGHT );
			}
			else
			{
				passableBy.setBit( Global.Passability.LIGHT );
			}
		}
	}
}
