package Roguelike.Entity.ActivationAction;

import Roguelike.Entity.Entity;
import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Fields.Field;
import com.badlogic.gdx.utils.XmlReader;

/**
 * Created by Philip on 25-Jan-16.
 */
public class ActivationActionSpawnField extends AbstractActivationAction
{
	public String fieldName;
	public int stacks;

	@Override
	public void evaluate( EnvironmentEntity owningEntity, Entity activatingEntity, float delta )
	{
		Field field = Field.load(fieldName);

		field.trySpawnInTile( owningEntity.tile[0][0], stacks );
	}

	@Override
	public void parse( XmlReader.Element xml )
	{
		fieldName = xml.getText();
		stacks = xml.getIntAttribute( "Stacks", 1 );
	}
}
