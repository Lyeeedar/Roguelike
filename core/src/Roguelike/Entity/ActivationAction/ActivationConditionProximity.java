package Roguelike.Entity.ActivationAction;

import Roguelike.Entity.Entity;
import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Global;
import Roguelike.Tiles.Point;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.XmlReader;

/**
 * Created by Philip on 25-Jan-16.
 */
public class ActivationConditionProximity extends AbstractActivationCondition
{
	public int dist;
	public boolean playerOnly;

	@Override
	public boolean evaluate( EnvironmentEntity owningEntity, Entity activatingEntity, float delta )
	{
		if (playerOnly)
		{
			if (activatingEntity != Global.CurrentLevel.player)
			{
				return false;
			}
		}

		Point eePos = Global.PointPool.obtain().set( owningEntity );
		Point pPos = Global.PointPool.obtain().set( activatingEntity );

		boolean closeEnough = Global.TaxiDist( eePos, pPos ) <= dist;

		Global.PointPool.free( eePos );
		Global.PointPool.free( pPos );

		return closeEnough;
	}

	@Override
	public void parse( XmlReader.Element xml )
	{
		dist = Integer.parseInt( xml.getText() );
		playerOnly = xml.getBooleanAttribute( "PlayerOnly", false );
	}
}
