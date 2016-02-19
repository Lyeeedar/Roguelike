package Roguelike.Entity.ActivationAction;

import Roguelike.Entity.Entity;
import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Fields.Field;
import Roguelike.Global;
import Roguelike.Tiles.GameTile;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import exp4j.Helpers.EquationHelper;

/**
 * Created by Philip on 25-Jan-16.
 */
public class ActivationActionSpawnField extends AbstractActivationAction
{
	public String fieldName;
	public String stacks;
	public boolean around;

	@Override
	public void evaluate( EnvironmentEntity owningEntity, Entity activatingEntity, float delta )
	{
		Field field = Field.load(fieldName);

		int stacks = EquationHelper.evaluate( this.stacks );

		if (!around)
		{
			field.trySpawnInTile( owningEntity.tile[0][0], stacks );
		}
		else
		{
			Array<Global.Direction> dirs = new Array<Global.Direction>(  );
			for ( Global.Direction dir : Global.Direction.values() )
			{
				if ( dir != Global.Direction.CENTER )
				{
					dirs.add( dir );
				}
			}

			while (dirs.size > 0)
			{
				Global.Direction dir = dirs.removeIndex( MathUtils.random( dirs.size - 1 ) );
				GameTile tile = owningEntity.tile[0][0].level.getGameTile( owningEntity.tile[0][0].x + dir.getX(), owningEntity.tile[0][0].y + dir.getY() );
				if (tile != null)
				{
					field.trySpawnInTile( tile, stacks );
					break;
				}
			}
		}
	}

	@Override
	public void parse( XmlReader.Element xml )
	{
		fieldName = xml.getText();
		stacks = xml.getAttribute( "Stacks", "1" );
		around = xml.getBooleanAttribute( "Around", false );
	}
}
