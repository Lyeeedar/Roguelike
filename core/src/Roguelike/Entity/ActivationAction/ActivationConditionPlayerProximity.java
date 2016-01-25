package Roguelike.Entity.ActivationAction;

import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Global;
import Roguelike.Tiles.Point;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.XmlReader;

/**
 * Created by Philip on 25-Jan-16.
 */
public class ActivationConditionPlayerProximity extends AbstractActivationCondition
{
	public int dist;

	@Override
	public boolean evaluate( EnvironmentEntity entity, float delta )
	{
		Point eePos = Global.PointPool.obtain().set( entity );
		Point pPos = Global.PointPool.obtain().set( Global.CurrentLevel.player );

		boolean closeEnough = Global.TaxiDist( eePos, pPos ) <= dist;

		Global.PointPool.free( eePos );
		Global.PointPool.free( pPos );

		return closeEnough;
	}

	@Override
	public void parse( XmlReader.Element xml )
	{
		dist = Integer.parseInt( xml.getText() );
	}
}
